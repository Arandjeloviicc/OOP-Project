package com.fittrack.backend.repository.user;

import com.fittrack.backend.repository.user.projection.CreatedUser;
import com.fittrack.backend.repository.user.projection.LoginData;
import com.fittrack.backend.repository.user.projection.RegistrationAvailability;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<LoginData> findLoginDataByEmail(String email) {
        String sql = """
                SELECT
                    u.id,
                    u.username,
                    u.email,
                    u.password_hash,
                    EXISTS (
                        SELECT 1
                        FROM user_profiles up
                        WHERE up.user_id = u.id
                    ) AS profile_setup_complete
                FROM users u
                WHERE u.email = ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, _) -> new LoginData(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getBoolean("profile_setup_complete")
                ),
                email
        ).stream().findFirst();
    }

    public RegistrationAvailability checkRegistrationAvailability(String username, String email) {
        String sql = """
                SELECT
                    EXISTS (
                        SELECT 1
                        FROM users
                        WHERE username = ?
                    ) AS username_taken,
                    EXISTS (
                        SELECT 1
                        FROM users
                        WHERE email = ?
                    ) AS email_taken
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, _) -> new RegistrationAvailability(
                        resultSet.getBoolean("username_taken"),
                        resultSet.getBoolean("email_taken")
                ),
                username,
                email
        );
    }

    public CreatedUser create(String username, String email, String passwordHash) {
        String sql = """
                INSERT INTO users (
                    username,
                    email,
                    password_hash,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                RETURNING
                    id,
                    username,
                    email
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (resultSet, _) -> new CreatedUser(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email")
                ),
                username,
                email,
                passwordHash
        );
    }
}
