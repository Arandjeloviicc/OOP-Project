package com.fittrack.backend.repository.measurements.weight;

import com.fittrack.backend.dto.measurements.weight.WeightLogRequest;
import com.fittrack.backend.dto.measurements.weight.WeightLogResponse;
import com.fittrack.backend.repository.measurements.weight.projection.DeleteWeightLogResult;
import com.fittrack.backend.repository.measurements.weight.projection.DeleteWeightLogStatus;
import com.fittrack.backend.repository.measurements.weight.projection.UpdateWeightLogResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class WeightLogJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public WeightLogJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<WeightLogResponse> findAllByUserId(Integer userId) {
        String sql = """
                SELECT
                    id,
                    weight,
                    logged_at
                FROM weight_logs
                WHERE user_id = ?
                ORDER BY logged_at DESC, id DESC
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, _) -> new WeightLogResponse(
                        resultSet.getInt("id"),
                        resultSet.getDouble("weight"),
                        resultSet.getTimestamp("logged_at").toInstant()
                ),
                userId
        );
    }

    public Optional<WeightLogResponse> create(Integer userId, WeightLogRequest request, Instant loggedAt) {
        String sql = """
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
                    weight,
                    logged_at
                """;

        List<WeightLogResponse> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new WeightLogResponse(
                        resultSet.getInt("id"),
                        resultSet.getDouble("weight"),
                        resultSet.getTimestamp("logged_at").toInstant()
                ),
                request.weight(),
                Timestamp.from(loggedAt),
                userId
        );

        return results.stream().findFirst();
    }

    public Optional<UpdateWeightLogResult> update(Integer weightLogId, Integer userId, WeightLogRequest request) {
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
                    updated_at = CURRENT_TIMESTAMP
                FROM target t
                WHERE w.id = t.id
                RETURNING
                    w.id,
                    w.weight,
                    w.logged_at,
                    t.was_latest
            )
            SELECT
                id,
                weight,
                logged_at,
                was_latest
            FROM updated
            """;

        List<UpdateWeightLogResult> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new UpdateWeightLogResult(
                        new WeightLogResponse(
                                resultSet.getInt("id"),
                                resultSet.getDouble("weight"),
                                resultSet.getTimestamp("logged_at").toInstant()
                        ),
                        resultSet.getBoolean("was_latest")
                ),
                userId,
                weightLogId,
                userId,
                request.weight()
        );

        return results.stream().findFirst();
    }

    public DeleteWeightLogResult delete(Integer weightLogId, Integer userId) {
        //

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