package com.fittrack.backend.repository.measurements.body;

import com.fittrack.backend.dto.measurements.body.BodyMeasurementRequest;
import com.fittrack.backend.dto.measurements.body.BodyMeasurementResponse;
import com.fittrack.backend.repository.measurements.body.projection.DeleteBodyMeasurementResult;
import com.fittrack.backend.repository.measurements.body.projection.LatestBodyMeasurement;
import com.fittrack.backend.repository.measurements.body.projection.UpdateBodyMeasurementResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class BodyMeasurementJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public BodyMeasurementJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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

    public List<BodyMeasurementResponse> findAllByUserId(Integer userId) {
        String sql = """
            SELECT
                id,
                neck,
                waist,
                hip,
                logged_at
            FROM body_measurement_logs
            WHERE user_id = ?
            ORDER BY logged_at DESC, id DESC
            """;

        return jdbcTemplate.query(
                sql,
                (resultSet, _) -> new BodyMeasurementResponse(
                        resultSet.getInt("id"),
                        resultSet.getDouble("neck"),
                        resultSet.getDouble("waist"),
                        resultSet.getObject("hip", Double.class),
                        resultSet.getTimestamp("logged_at").toInstant()
                ),
                userId
        );
    }

    public Optional<BodyMeasurementResponse> create(Integer userId, BodyMeasurementRequest request, Instant loggedAt) {
        String sql = """
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
                neck,
                waist,
                hip,
                logged_at
            """;

        List<BodyMeasurementResponse> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new BodyMeasurementResponse(
                        resultSet.getInt("id"),
                        resultSet.getDouble("neck"),
                        resultSet.getDouble("waist"),
                        resultSet.getObject("hip", Double.class),
                        resultSet.getTimestamp("logged_at").toInstant()
                ),
                request.neck(),
                request.waist(),
                request.hip(),
                Timestamp.from(loggedAt),
                userId
        );

        return results.stream().findFirst();
    }

    public Optional<UpdateBodyMeasurementResult> update(Integer measurementId, Integer userId, BodyMeasurementRequest request) {
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
                    updated_at = CURRENT_TIMESTAMP
                FROM target t
                WHERE b.id = t.id
                RETURNING
                    b.id,
                    b.neck,
                    b.waist,
                    b.hip,
                    b.logged_at,
                    t.was_latest
            )
            SELECT
                id,
                neck,
                waist,
                hip,
                logged_at,
                was_latest
            FROM updated
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
                        resultSet.getBoolean("was_latest")
                ),
                userId,
                measurementId,
                userId,
                request.neck(),
                request.waist(),
                request.hip()
        );

        return results.stream().findFirst();
    }

    public DeleteBodyMeasurementResult delete(Integer measurementId, Integer userId) {
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