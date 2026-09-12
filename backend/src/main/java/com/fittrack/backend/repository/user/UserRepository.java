package com.fittrack.backend.repository.user;

import com.fittrack.backend.repository.user.projection.LoginData;
import com.fittrack.backend.repository.user.projection.RegistrationAvailability;
import org.jspecify.annotations.NonNull;
import com.fittrack.backend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<@NonNull User, @NonNull Integer> {

    @Query(value = """
    SELECT
        u.id AS id,
        u.username AS username,
        u.email AS email,
        u.password_hash AS "passwordHash",
        EXISTS (
            SELECT 1
            FROM user_profiles up
            WHERE up.user_id = u.id
        ) AS "profileSetupComplete"
    FROM users u
    WHERE u.email = :email
    """, nativeQuery = true
    )
    Optional<LoginData> findLoginDataByEmail(
            @Param("email") String email
    );

    @Query(value = """
    SELECT
        EXISTS (
            SELECT 1
            FROM users
            WHERE username = :username
        ) AS "usernameTaken",
        EXISTS (
            SELECT 1
            FROM users
            WHERE email = :email
        ) AS "emailTaken"
    """, nativeQuery = true
    )
    RegistrationAvailability checkRegistrationAvailability(
            @Param("username") String username,
            @Param("email") String email
    );
}