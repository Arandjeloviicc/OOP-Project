package com.fittrack.backend.repository.profile;

import com.fittrack.backend.dto.profile.editor.PersonalInfoUpdateRequest;
import com.fittrack.backend.entity.profile.ActivityLevel;
import com.fittrack.backend.entity.profile.Gender;
import com.fittrack.backend.entity.profile.WeightGoal;
import com.fittrack.backend.repository.profile.projection.PersonalInfoData;
import com.fittrack.backend.repository.profile.projection.ProfileData;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProfileJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<ProfileData> findByUserId(Integer userId) {
        // users (u) + user_profiles (up) - osnovni i licni podaci korisnika
        // nutrition_goals (ng) - uzima se samo aktivan cilj korisnika (end_date IS NULL)
        // current_weight (LATERAL JOIN) - najnoviji unet unos tezine korisnika
        // start_weight - pocetna tezina progress ciklusa, cuva se na aktivnom nutrition goal-u
        // LEFT JOIN za current_weight omogucava da profil ostane dostupan i ako nema weight loga

            String sql = """
                SELECT
                    u.username,
                    u.email,
    
                    up.first_name,
                    up.last_name,
                    up.date_of_birth,
                    up.gender,
                    up.height,
    
                    current_weight.weight AS current_weight,
                    ng.progress_start_weight AS start_weight,
    
                    ng.goal_type,
                    ng.goal_weight,
                    ng.weekly_goal,
                    ng.activity_level,
    
                    ng.target_calories,
                    ng.target_carbs,
                    ng.target_fat,
                    ng.target_protein

                FROM users u

                JOIN user_profiles up
                    ON up.user_id = u.id
    
                JOIN nutrition_goals ng
                    ON ng.user_id = u.id
                   AND ng.end_date IS NULL
    
                LEFT JOIN LATERAL (
                    SELECT wl.weight
                    FROM weight_logs wl
                    WHERE wl.user_id = u.id
                    ORDER BY wl.logged_at DESC, wl.id DESC
                    LIMIT 1
                ) current_weight ON TRUE
    
                WHERE u.id = ?
                """;

        List<ProfileData> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new ProfileData(
                        resultSet.getString("username"),
                        resultSet.getString("email"),

                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getObject(
                                "date_of_birth",
                                LocalDate.class
                        ),
                        Gender.valueOf(resultSet.getString("gender")),
                        resultSet.getDouble("height"),

                        resultSet.getObject("current_weight", Double.class),
                        resultSet.getObject("start_weight", Double.class),

                        WeightGoal.valueOf(
                                resultSet.getString("goal_type")
                        ),
                        resultSet.getObject("goal_weight", Double.class),
                        resultSet.getObject("weekly_goal", Double.class),
                        ActivityLevel.valueOf(
                                resultSet.getString("activity_level")
                        ),

                        resultSet.getInt("target_calories"),
                        resultSet.getDouble("target_carbs"),
                        resultSet.getDouble("target_fat"),
                        resultSet.getDouble("target_protein")
                ),
                userId
        );

        return results.stream().findFirst();
    }

    public Optional<PersonalInfoData> findPersonalInfoByUserId(Integer userId) {
        String sql = """
            SELECT
                first_name,
                last_name,
                date_of_birth,
                gender,
                height
            FROM user_profiles
            WHERE user_id = ?
            """;

        List<PersonalInfoData> results = jdbcTemplate.query(
                sql,
                (resultSet, _) -> new PersonalInfoData(
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getObject(
                                "date_of_birth",
                                LocalDate.class
                        ),
                        Gender.valueOf(
                                resultSet.getString("gender")
                        ),
                        resultSet.getDouble("height")
                ),
                userId
        );

        return results.stream().findFirst();
    }

    public int updatePersonalInfo(Integer userId, PersonalInfoUpdateRequest request) {
        String sql = """
            UPDATE user_profiles
            SET
                first_name = ?,
                last_name = ?,
                date_of_birth = ?,
                gender = ?,
                height = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE user_id = ?
            """;

        return jdbcTemplate.update(
                sql,
                request.firstName().trim(),
                request.lastName().trim(),
                request.dateOfBirth(),
                request.gender().name(),
                request.height(),
                userId
        );
    }
}