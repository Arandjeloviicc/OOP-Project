package com.fittrack.backend.repository.measurements.body;

import com.fittrack.backend.dto.measurements.body.BodyMeasurementHistoryResponse;
import com.fittrack.backend.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.backend.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.repository.measurements.body.projection.CreateBodyMeasurementResult;
import com.fittrack.backend.repository.measurements.body.projection.DeleteBodyMeasurementResult;
import com.fittrack.backend.repository.measurements.body.projection.LatestBodyMeasurement;
import com.fittrack.backend.repository.measurements.body.projection.UpdateBodyMeasurementResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BodyMeasurementJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<LatestBodyMeasurement> findLatestByUserId(Integer userId) {
        String sql = """
                SELECT neck, waist, hip
                FROM body_measurement_logs
                WHERE user_id = ?
                ORDER BY logged_at DESC, id DESC
                LIMIT 1
                """;

        return jdbcTemplate.query(
                sql,
                (rs, _) -> new LatestBodyMeasurement(
                        rs.getObject("neck", Double.class),
                        rs.getObject("waist", Double.class),
                        rs.getObject("hip", Double.class)
                ),
                userId
        ).stream().findFirst();
    }

    public BodyMeasurementHistoryResponse findHistoryByUserId(Integer userId) {
        // FROM user_profiles - kreće se od profila, ne od merenja, jer gender mora da se dobije
        //                      cak i kad korisnik nema nijedan body_measurement_logs red
        // LEFT JOIN - merenja se kace na profil; ako ih nema, dobija se tacno jedan red
        //             sa svim bml.* kolonama NULL i up.gender popunjenim (umesto 0 redova)

        String sql = """
        SELECT
            bml.id,
            bml.neck,
            bml.waist,
            bml.hip,
            bml.logged_at,
            up.gender
        FROM user_profiles up
        LEFT JOIN body_measurement_logs bml
            ON bml.user_id = up.user_id
        WHERE up.user_id = ?
        ORDER BY bml.logged_at DESC, bml.id DESC
        """;

        return jdbcTemplate.query(
                sql,
                resultSet -> {
                    List<BodyMeasurementResponse> measurements = new ArrayList<>();
                    Gender gender = null;

                    while (resultSet.next()) {
                        if (gender == null) {
                            String genderValue = resultSet.getString("gender");

                            if (genderValue != null) {
                                gender = Gender.valueOf(genderValue);
                            }
                        }

                        Integer id = resultSet.getObject("id", Integer.class);

                        if (id != null) {
                            measurements.add(new BodyMeasurementResponse(
                                    id,
                                    resultSet.getDouble("neck"),
                                    resultSet.getDouble("waist"),
                                    resultSet.getObject("hip", Double.class),
                                    resultSet.getTimestamp("logged_at").toInstant()
                            ));
                        }
                    }

                    return new BodyMeasurementHistoryResponse(measurements, gender);
                },
                userId
        );
    }

    public Optional<CreateBodyMeasurementResult> create(Integer userId, BodyMeasurementRequest request) {
        // inserted  proverava da korisnik postoji pre unosa
        //           ako ne postoji, SELECT vraca 0 redova i INSERT ne unosi nista
        // is_latest - NOT EXISTS trazi bilo koji drugi red istog korisnika koji je "noviji" od upravo
        //             unetog (veci logged_at, ili isti logged_at sa vecim id kao tie-breaker); ako takav
        //             red ne postoji, upravo uneti unos je najnoviji

        String sql = """
            WITH inserted AS (
                INSERT INTO body_measurement_logs (
                    user_id,
                    neck,
                    waist,
                    hip,
                    logged_at,
                    created_at
                )
                SELECT
                    u.id,
                    ?,
                    ?,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP
                FROM users u
                WHERE u.id = ?
                RETURNING
                    id,
                    user_id,
                    neck,
                    waist,
                    hip,
                    logged_at
            )
            SELECT
                i.id,
                i.neck,
                i.waist,
                i.hip,
                i.logged_at,
                NOT EXISTS (
                    SELECT 1
                    FROM body_measurement_logs b
                    WHERE b.user_id = i.user_id
                      AND b.id <> i.id
                      AND (
                          b.logged_at > i.logged_at
                          OR (
                              b.logged_at = i.logged_at
                              AND b.id > i.id
                          )
                      )
                ) AS is_latest
            FROM inserted i
            """;

        List<CreateBodyMeasurementResult> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new CreateBodyMeasurementResult(
                        new BodyMeasurementResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("neck"),
                                resultSet.getDouble("waist"),
                                resultSet.getObject("hip", Double.class),
                                resultSet.getTimestamp("logged_at").toInstant()
                        ),
                        resultSet.getBoolean("is_latest")
                ),
                request.neck(),
                request.waist(),
                request.hip(),
                Timestamp.from(request.loggedAt()),
                userId
        );

        return results.stream().findFirst();
    }

    public Optional<UpdateBodyMeasurementResult> update(Integer measurementId, Integer userId, BodyMeasurementRequest request) {
        // target - proverava da traženi unos postoji i da pripada korisniku (id + user_id zajedno), a was_latest podupitom utvrđuje da li je
        //          baš taj unos trenutno najnoviji (poredi se sa unosom koji ima najveći logged_at, id kao tie-breaker za isti timestamp)
        // updated - ako target ima red, UPDATE menja neck/waist/hip i logged_at tog reda,
        //           ažurira updated_at i RETURNING-om vraća novo stanje plus was_latest;
        //           ako target nema red (measurement ne postoji ili ne pripada korisniku),
        //           UPDATE ne pogađa nijedan red i updated ostaje prazan
        // SELECT - vraća ažurirani measurement, was_latest iz stanja pre UPDATE-a
        //          i računa is_latest nakon promene logged_at po pravilu
        //          ORDER BY logged_at DESC, id DESC; prazan updated znači Optional.empty()

        String sql = """
            WITH target AS (
               SELECT
                   id,
                   id = (
                       SELECT id
                       FROM body_measurement_logs
                       WHERE user_id = ?
                       ORDER BY logged_at DESC, id DESC
                       LIMIT 1
                   ) AS was_latest
               FROM body_measurement_logs
               WHERE id = ?
                 AND user_id = ?
            ),
            updated AS (
                UPDATE body_measurement_logs b
                SET
                    neck = ?,
                    waist = ?,
                    hip = ?,
                    logged_at = ?,
                    updated_at = CURRENT_TIMESTAMP
                FROM target t
                WHERE b.id = t.id
                RETURNING
                    b.id,
                    b.user_id,
                    b.neck,
                    b.waist,
                    b.hip,
                    b.logged_at,
                    t.was_latest
            )
            SELECT
                u.id,
                u.neck,
                u.waist,
                u.hip,
                u.logged_at,
                u.was_latest,
                NOT EXISTS (
                    SELECT 1
                    FROM body_measurement_logs b
                    WHERE b.user_id = u.user_id
                      AND b.id <> u.id
                      AND (
                          b.logged_at > u.logged_at
                          OR (
                              b.logged_at = u.logged_at
                              AND b.id > u.id
                          )
                      )
                ) AS is_latest
            FROM updated u
            """;

        List<UpdateBodyMeasurementResult> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new UpdateBodyMeasurementResult(
                        new BodyMeasurementResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("neck"),
                                resultSet.getDouble("waist"),
                                resultSet.getObject("hip", Double.class),
                                resultSet.getTimestamp("logged_at").toInstant()
                        ),
                        resultSet.getBoolean("was_latest"),
                        resultSet.getBoolean("is_latest")
                ),
                userId,
                measurementId,
                userId,
                request.neck(),
                request.waist(),
                request.hip(),
                Timestamp.from(request.loggedAt())
        );

        return results.stream().findFirst();
    }

    public DeleteBodyMeasurementResult delete(Integer measurementId, Integer userId) {
        // target - ista provera kao u update() - postojanje + vlasništvo (id + user_id), plus was_latest flag da se zna da li se briše baš trenutno najnoviji unos korisnika
        // deleted - ako target ima red, DELETE ... USING target t briše taj tačno jedan red i RETURNING vraća njegov id i was_latest; ako target nema red, DELETE ne pogađa ništa i deleted ostaje prazan (0 redova), bez ijedne greške
        // SELECT - EXISTS(...) pretvara "ima li reda u deleted" u pravi boolean (true/false), umesto da se oslanja na broj vraćenih redova; COALESCE(..., FALSE) hvata slučaj kad deleted nema nijedan red (ništa obrisano) tako da se was_latest ne izvlači iz praznog podupita kao null, nego dobija bezbedan default FALSE - ovo je i razlog zašto ova metoda uvek vraća tačno jedan red (queryForObject umesto stream().findFirst()), za razliku od update() koji legitimno može da vrati "nema rezultata"

        String sql = """
            WITH target AS (
                SELECT
                    id,
                    id = (
                        SELECT id
                        FROM body_measurement_logs
                        WHERE user_id = ?
                        ORDER BY logged_at DESC, id DESC
                        LIMIT 1
                    ) AS was_latest
                FROM body_measurement_logs
                WHERE id = ?
                  AND user_id = ?
            ),
            deleted AS (
                DELETE FROM body_measurement_logs b
                USING target t
                WHERE b.id = t.id
                RETURNING
                    b.id,
                    t.was_latest
            )
            SELECT
                EXISTS (
                    SELECT 1
                    FROM deleted
                ) AS deleted,
                COALESCE(
                    (
                        SELECT was_latest
                        FROM deleted
                    ),
                    FALSE
                ) AS was_latest
            """;

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, _) -> new DeleteBodyMeasurementResult(
                        resultSet.getBoolean("deleted"),
                        resultSet.getBoolean("was_latest")
                ),
                userId,
                measurementId,
                userId
        );
    }
}