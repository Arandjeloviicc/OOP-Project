package com.fittrack.backend.repository.measurements.weight;

import com.fittrack.backend.dto.measurements.weight.WeightHistoryResponse;
import com.fittrack.backend.dto.measurements.weight.WeightLogRequest;
import com.fittrack.backend.dto.measurements.weight.WeightLogResponse;
import com.fittrack.backend.entity.profile.WeightGoal;
import com.fittrack.backend.repository.measurements.weight.projection.CreateWeightLogResult;
import com.fittrack.backend.repository.measurements.weight.projection.DeleteWeightLogResult;
import com.fittrack.backend.repository.measurements.weight.projection.DeleteWeightLogStatus;
import com.fittrack.backend.repository.measurements.weight.projection.UpdateWeightLogResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WeightLogJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public WeightHistoryResponse findHistoryByUserId(Integer userId) {
        // ng - za svaki red iz weight_logs izvlaci goal_type aktivnog cilja (end_date IS NULL)
        //      istog korisnika; LATERAL dozvoljava da WHERE referencira wl.user_id
        // LEFT JOIN - garantuje da weight_logs redovi ostanu i ako korisnik nema aktivan cilj
        //             (goal_type tada NULL umesto da se svi redovi izgube)

        String sql = """
            SELECT
                wl.id,
                wl.weight,
                wl.logged_at,
                ng.goal_type
            FROM weight_logs wl
            LEFT JOIN LATERAL (
                SELECT goal_type
                FROM nutrition_goals
                WHERE user_id = wl.user_id
                  AND end_date IS NULL
                LIMIT 1
            ) ng ON TRUE
            WHERE wl.user_id = ?
            ORDER BY wl.logged_at DESC, wl.id DESC
            """;

        return jdbcTemplate.query(
                sql,
                resultSet -> {
                    List<WeightLogResponse> logs = new ArrayList<>();
                    WeightGoal goalType = null;

                    while (resultSet.next()) {
                        logs.add(new WeightLogResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("weight"),
                                resultSet.getTimestamp("logged_at").toInstant()
                        ));

                        if (goalType == null) {
                            String goalTypeValue = resultSet.getString("goal_type");

                            if (goalTypeValue != null) {
                                goalType = WeightGoal.valueOf(goalTypeValue);
                            }
                        }
                    }

                    return new WeightHistoryResponse(logs, goalType);
                },
                userId
        );
    }

    public Optional<CreateWeightLogResult> create(Integer userId, WeightLogRequest request) {
        // inserted - proverava da korisnik postoji pre unosa;
        //            ako ne postoji, SELECT vraca 0 redova i INSERT ne unosi nista
        // is_latest - NOT EXISTS trazi bilo koji drugi red istog korisnika koji je "noviji" od upravo
        //             unetog (veci logged_at, ili isti logged_at sa vecim id kao tie-breaker); ako takav
        //             red ne postoji, upravo uneti unos je najnoviji

        String sql = """
            WITH inserted AS (
                INSERT INTO weight_logs (
                    user_id,
                    weight,
                    logged_at,
                    created_at
                )
                SELECT
                    u.id,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP
                FROM users u
                WHERE u.id = ?
                RETURNING
                    id,
                    user_id,
                    weight,
                    logged_at
            )
            SELECT
                i.id,
                i.weight,
                i.logged_at,
                NOT EXISTS (
                    SELECT 1
                    FROM weight_logs w
                    WHERE w.user_id = i.user_id
                      AND w.id <> i.id
                      AND (
                          w.logged_at > i.logged_at
                          OR (
                              w.logged_at = i.logged_at
                              AND w.id > i.id
                          )
                      )
                ) AS is_latest
            FROM inserted i
            """;

        List<CreateWeightLogResult> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new CreateWeightLogResult(
                        new WeightLogResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("weight"),
                                resultSet.getTimestamp("logged_at").toInstant()
                        ),
                        resultSet.getBoolean("is_latest")
                ),
                request.weight(),
                Timestamp.from(request.loggedAt()),
                userId
        );

        return results.stream().findFirst();
    }

    public Optional<UpdateWeightLogResult> update(Integer weightLogId, Integer userId, WeightLogRequest request) {
        // target - proverava da traženi unos postoji i da pripada korisniku (id + user_id zajedno),
        //          a was_latest podupitom utvrđuje da li je baš taj unos trenutno najnoviji unos
        //          korisnika (poredi se sa MAX logged_at, id kao tie-breaker za isti timestamp) -
        //          identičan obrazac kao BodyMeasurementJdbcRepository.update()
        // updated - ako target ima red, UPDATE menja weight i logged_at tog reda,
        //           ažurira updated_at i RETURNING-om vraća novo stanje plus was_latest
        // SELECT - prosleđuje kolone iz updated; prazan updated -> 0 redova -> Java strana kroz
        //          results.stream().findFirst() dobija Optional.empty()

        String sql = """
            WITH target AS (
                SELECT
                    id,
                    id = (
                        SELECT id
                        FROM weight_logs
                        WHERE user_id = ?
                        ORDER BY logged_at DESC, id DESC
                        LIMIT 1
                    ) AS was_latest
                FROM weight_logs
                WHERE id = ?
                  AND user_id = ?
            ),
            updated AS (
                UPDATE weight_logs w
                SET
                    weight = ?,
                    logged_at = ?,
                    updated_at = CURRENT_TIMESTAMP
                FROM target t
                WHERE w.id = t.id
                RETURNING
                    w.id,
                    w.user_id,
                    w.weight,
                    w.logged_at,
                    t.was_latest
            )
            SELECT
                u.id,
                u.weight,
                u.logged_at,
                u.was_latest,
                NOT EXISTS (
                    SELECT 1
                    FROM weight_logs w
                    WHERE w.user_id = u.user_id
                      AND w.id <> u.id
                      AND (
                          w.logged_at > u.logged_at
                          OR (
                              w.logged_at = u.logged_at
                              AND w.id > u.id
                          )
                      )
                ) AS is_latest
            FROM updated u
            """;

        List<UpdateWeightLogResult> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new UpdateWeightLogResult(
                        new WeightLogResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("weight"),
                                resultSet.getTimestamp("logged_at").toInstant()
                        ),
                        resultSet.getBoolean("was_latest"),
                        resultSet.getBoolean("is_latest")
                ),
                userId,
                weightLogId,
                userId,
                request.weight(),
                Timestamp.from(request.loggedAt())
        );

        return results.stream().findFirst();
    }

    public DeleteWeightLogResult delete(Integer weightLogId, Integer userId) {
        // user_logs - uzima SVE unose korisnika i, preko ROW_NUMBER funkcija, svakom redu dodaje position
        //             (1 = najnoviji, po istom logged_at DESC, id DESC redosledu kao svuda) i total_count
        //             (ukupan broj unosa korisnika - isti broj ponovljen na svakom redu
        // target - filtrira user_logs na traženi id; ako id ne postoji ili nije korisnikov, target je prazan -
        //          to je razlog zašto ne treba posebna "user_id = ?" provera u DELETE-u, već je već ugrađena
        //          u to što id dolazi iz user_logs koji je već filtriran po user_id
        // deleted - DELETE se izvršava samo ako je total_count > 1, tj. samo ako korisnik ima više od jednog
        //           unosa težine; ako je ovo jedini preostali unos, uslov je false, DELETE ne pogađa nijedan
        //           red i deleted ostaje prazan iako target ima red - ovo je namerna zaštita da korisnik ne
        //           ostane bez ijednog unosa težine (recalculateTargets/findRecalculationData zahtevaju
        //           bar jedan weight_logs red da bi izračunali nutrition targets)
        // SELECT - status se izvodi iz kombinacije target/deleted umesto da se grana na tri if-a:
        //          target prazan -> 'NOT_FOUND' (ne postoji ili nije korisnikov)
        //          target ima red i deleted ima red -> 'DELETED' (uspešno obrisano)
        //          target ima red ali deleted prazan -> 'LAST_REMAINING' (postoji, ali je jedini pa nije obrisan)
        //          was_latest se čita direktno iz target-a (position = 1), sa COALESCE(..., FALSE) za slučaj
        //          NOT_FOUND kad target nema red i podupit vrati null

        String sql = """
            WITH user_logs AS (
                SELECT
                    id,
                    ROW_NUMBER() OVER (
                        ORDER BY logged_at DESC, id DESC
                    ) AS position,
                    COUNT(*) OVER () AS total_count
                FROM weight_logs
                WHERE user_id = ?
            ),
            target AS (
                SELECT
                    id,
                    position,
                    total_count
                FROM user_logs
                WHERE id = ?
            ),
            deleted AS (
                DELETE FROM weight_logs w
                USING target t
                WHERE w.id = t.id
                  AND t.total_count > 1
                RETURNING w.id
            )
            SELECT
                CASE
                    WHEN NOT EXISTS (SELECT 1 FROM target) THEN 'NOT_FOUND'
                    WHEN EXISTS (SELECT 1 FROM deleted) THEN 'DELETED'
                    ELSE 'LAST_REMAINING'
                END AS status,
                COALESCE(
                    (SELECT position = 1 FROM target),
                    FALSE
                ) AS was_latest
            """;

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, _) -> new DeleteWeightLogResult(
                        DeleteWeightLogStatus.valueOf(
                                resultSet.getString("status")
                        ),
                        resultSet.getBoolean("was_latest")
                ),
                userId,
                weightLogId
        );
    }
}