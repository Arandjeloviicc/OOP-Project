package com.fittrack.backend.repository.measurement;

import com.fittrack.backend.repository.measurement.projection.LatestBodyMeasurement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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
}