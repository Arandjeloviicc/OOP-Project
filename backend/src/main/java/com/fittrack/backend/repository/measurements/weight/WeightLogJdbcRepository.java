package com.fittrack.backend.repository.measurements.weight;

import com.fittrack.backend.dto.measurements.weight.WeightHistoryResponse;
import com.fittrack.backend.dto.measurements.weight.WeightLogRequest;
import com.fittrack.backend.dto.measurements.weight.WeightLogResponse;
import com.fittrack.backend.entity.profile.WeightGoal;
import com.fittrack.backend.repository.measurements.weight.projection.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

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

    public CreateWeightLogResult create(Integer userId, WeightLogRequest request) {
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate loggedDate = request.loggedAt()
                .atZone(zoneId)
                .toLocalDate();

        Timestamp dayStart = Timestamp.from(
                loggedDate.atStartOfDay(zoneId).toInstant()
        );

        Timestamp nextDayStart = Timestamp.from(
                loggedDate.plusDays(1)
                        .atStartOfDay(zoneId)
                        .toInstant()
        );

        // target_user - proverava da korisnik postoji; prazan ako ne postoji, sto se dalje koristi i za status
        //               i posredno za CROSS JOIN u insert-u (nema reda -> INSERT nista ne unosi)
        // date_conflict - EXISTS provera da li korisnik VEC ima unos u (dayStart, nextDayStart) rasponu
        // inserted - INSERT se izvrsava SAMO ako korisnik postoji (CROSS JOIN target_user - prazan target_user
        //            znaci 0 redova iz CROSS JOIN-a) i ako nema konflikta za taj dan (WHERE NOT dc.exists);
        //            ako bilo koji uslov ne prodje, inserted ostaje prazan bez ijedne bacene greske
        // status - redosled provere je bitan: prvo USER_NOT_FOUND (target_user prazan), tek onda
        //          DATE_CONFLICT (date_conflict.exists), inace CREATED - ako je user nevalidan ne
        //          zeli se da se prijavi DATE_CONFLICT kao razlog neuspeha
        // is_latest - racunanje da lije merenje najskorije
        // FROM (SELECT 1) base LEFT JOIN inserted i ON TRUE
        //       - garantuje tacno jedan red u finalnom rezultatu cak i kad je inserted prazan (queryForObject
        //       bi inace bacio EmptyResultDataAccessException)

        String sql = """
            WITH target_user AS (
                SELECT id
                FROM users
                WHERE id = ?
            ),
            date_conflict AS (
                SELECT EXISTS (
                    SELECT 1
                    FROM weight_logs
                    WHERE user_id = ?
                      AND logged_at >= ?
                      AND logged_at < ?
                ) AS exists
            ),
            inserted AS (
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
                FROM target_user u
                CROSS JOIN date_conflict dc
                WHERE NOT dc.exists
                RETURNING
                    id,
                    user_id,
                    weight,
                    logged_at
            )
            SELECT
                CASE
                    WHEN NOT EXISTS (
                        SELECT 1
                        FROM target_user
                    ) THEN 'USER_NOT_FOUND'
                    WHEN (
                        SELECT exists
                        FROM date_conflict
                    ) THEN 'DATE_CONFLICT'
                    ELSE 'CREATED'
                END AS status,
                i.id,
                i.weight,
                i.logged_at,
                CASE
                    WHEN i.id IS NULL THEN FALSE
                    ELSE NOT EXISTS (
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
                    )
                END AS is_latest
            FROM (SELECT 1) base
            LEFT JOIN inserted i ON TRUE
            """;

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, _) -> {
                    CreateWeightLogStatus status =
                            CreateWeightLogStatus.valueOf(
                                    resultSet.getString("status")
                            );

                    WeightLogResponse weightLog = null;

                    if (status == CreateWeightLogStatus.CREATED) {
                        weightLog = new WeightLogResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("weight"),
                                resultSet.getTimestamp("logged_at").toInstant()
                        );
                    }

                    return new CreateWeightLogResult(
                            status,
                            weightLog,
                            resultSet.getBoolean("is_latest")
                    );
                },
                userId,
                userId,
                dayStart,
                nextDayStart,
                request.weight(),
                Timestamp.from(request.loggedAt())
        );
    }

    public UpdateWeightLogResult update(Integer weightLogId, Integer userId, WeightLogRequest request) {
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate loggedDate = request.loggedAt()
                .atZone(zoneId)
                .toLocalDate();

        Timestamp dayStart = Timestamp.from(
                loggedDate.atStartOfDay(zoneId).toInstant()
        );

        Timestamp nextDayStart = Timestamp.from(
                loggedDate.plusDays(1)
                        .atStartOfDay(zoneId)
                        .toInstant()
        );

        // target - proverava da unos postoji i pripada korisniku (id + user_id), i istovremeno racuna
        //          was_latest poredjenjem sa trenutno najnovijim unosom pre izmene
        // date_conflict - proverava da li korisnik vec ima drugi unos (w.id <> ?) u (dayStart, nextDayStart)
        //                 rasponu novog datuma; mora da se iskljuci id tog merenja da ne bi pravio problem sam sebi
        // updated - UPDATE se izvrsava samo ako target ima red i nema konflikta (WHERE ... AND NOT dc.exists);
        //           ako bilo koji uslov ne prodje, updated ostaje prazan bez greske
        // status - NOT_FOUND ima prioritet nad DATE_CONFLICT, prvo se proverava
        //          da li unos uopste postoji/pripada korisniku, tek onda da li novi datum konfliktuje
        // was_latest - COALESCE(..., FALSE) jer target moze imati red (NOT_FOUND se ne desava) ali updated
        //              ostati prazan (DATE_CONFLICT slucaj) - tada u.was_latest ne postoji pa bi bez COALESCE
        //              ostao NULL umesto FALSE
        // is_latest - racuna se NAKON izmene (novi weight/logged_at), za razliku od was_latest koji je
        //             racunat pre, ova dva zajedno hvataju i slucaj kad se datum promeni tako da unos
        //             vise nije najnoviji, i slucaj kad postane najnoviji iako to pre nije bio
        // FROM (SELECT 1) base LEFT JOIN updated u ON TRUE
        //           - garantuje tacno jedan red u rezultatu cak i kad je updated prazan (NOT_FOUND ili
        //             DATE_CONFLICT), da queryForObject ne baci gresku na 0 redova

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
            date_conflict AS (
                SELECT EXISTS (
                    SELECT 1
                    FROM weight_logs w
                    WHERE w.user_id = ?
                      AND w.id <> ?
                      AND w.logged_at >= ?
                      AND w.logged_at < ?
                ) AS exists
            ),
            updated AS (
                UPDATE weight_logs w
                SET
                    weight = ?,
                    logged_at = ?,
                    updated_at = CURRENT_TIMESTAMP
                FROM target t
                CROSS JOIN date_conflict dc
                WHERE w.id = t.id
                  AND NOT dc.exists
                RETURNING
                    w.id,
                    w.user_id,
                    w.weight,
                    w.logged_at,
                    t.was_latest
            )
            SELECT
                CASE
                    WHEN NOT EXISTS (
                        SELECT 1
                        FROM target
                    ) THEN 'NOT_FOUND'
                    WHEN (
                        SELECT exists
                        FROM date_conflict
                    ) THEN 'DATE_CONFLICT'
                    ELSE 'UPDATED'
                END AS status,
                u.id,
                u.weight,
                u.logged_at,
                COALESCE(u.was_latest, FALSE) AS was_latest,
                CASE
                    WHEN u.id IS NULL THEN FALSE
                    ELSE NOT EXISTS (
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
                    )
                END AS is_latest
            FROM (SELECT 1) base
            LEFT JOIN updated u ON TRUE
            """;

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, _) -> {
                    UpdateWeightLogStatus status =
                            UpdateWeightLogStatus.valueOf(
                                    resultSet.getString("status")
                            );

                    WeightLogResponse weightLog = null;

                    if (status == UpdateWeightLogStatus.UPDATED) {
                        weightLog = new WeightLogResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("weight"),
                                resultSet.getTimestamp("logged_at").toInstant()
                        );
                    }

                    return new UpdateWeightLogResult(
                            status,
                            weightLog,
                            resultSet.getBoolean("was_latest"),
                            resultSet.getBoolean("is_latest")
                    );
                },
                userId,
                weightLogId,
                userId,

                userId,
                weightLogId,
                dayStart,
                nextDayStart,

                request.weight(),
                Timestamp.from(request.loggedAt())
        );
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