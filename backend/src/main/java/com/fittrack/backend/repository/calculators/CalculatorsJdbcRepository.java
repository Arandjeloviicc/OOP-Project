package com.fittrack.backend.repository.calculators;

import com.fittrack.backend.dto.calculators.CalculatorsResponse;
import com.fittrack.backend.domain.profile.ActivityLevel;
import com.fittrack.backend.domain.profile.Gender;
import com.fittrack.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class CalculatorsJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public CalculatorsResponse findDataByUserId(Integer userId) {
        String sql = """
            SELECT
                up.height,
                lw.weight,
                up.date_of_birth,
                up.gender,
                ng.activity_level,
                lbm.neck,
                lbm.waist,
                lbm.hip
            FROM user_profiles up
    
            JOIN nutrition_goals ng
                ON ng.user_id = up.user_id
               AND ng.end_date IS NULL
    
            JOIN LATERAL (
                SELECT wl.weight
                FROM weight_logs wl
                WHERE wl.user_id = up.user_id
                ORDER BY wl.logged_at DESC, wl.id DESC
                LIMIT 1
            ) lw ON TRUE
    
            LEFT JOIN LATERAL (
                SELECT
                    bml.neck,
                    bml.waist,
                    bml.hip
                FROM body_measurement_logs bml
                WHERE bml.user_id = up.user_id
                ORDER BY bml.logged_at DESC, bml.id DESC
                LIMIT 1
            ) lbm ON TRUE
    
            WHERE up.user_id = ?
            """;

        CalculatorsResponse result = DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        sql,
                        (resultSet, _) -> new CalculatorsResponse(
                                resultSet.getDouble("height"),
                                resultSet.getDouble("weight"),
                                resultSet.getObject("date_of_birth", LocalDate.class),
                                Gender.valueOf(resultSet.getString("gender")),
                                ActivityLevel.valueOf(resultSet.getString("activity_level")),
                                resultSet.getObject("neck", Double.class),
                                resultSet.getObject("waist", Double.class),
                                resultSet.getObject("hip", Double.class)
                        ),
                        userId
                )
        );

        if (result == null) {
            throw new ResourceNotFoundException(
                    "Calculator data not found."
            );
        }

        return result;
    }
}
