package com.fittrack.dao;

import com.fittrack.database.DatabaseConnection;
import com.fittrack.model.profile.UserProfile;

import java.sql.*;

public class UserProfileDAO {

    // Inserts profile data for a User
    public void insert(Connection connection, UserProfile profile) throws SQLException {
        String sql = """
            INSERT INTO user_profiles (
                user_id,
                first_name,
                last_name,
                date_of_birth,
                gender,
                height,
                activity_level,
                goal_type,
                goal_weight,
                weekly_goal
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, profile.userId());
            statement.setString(2, profile.firstName());
            statement.setString(3, profile.lastName());
            statement.setString(4, profile.dateOfBirth().toString());
            statement.setString(5, profile.gender().getCode());
            statement.setDouble(6, profile.height());
            statement.setString(7, profile.activityLevel().getCode());
            statement.setString(8, profile.goalType().getCode());
            if (profile.goalWeight() == null) {
                statement.setNull(9, Types.REAL);
            } else {
                statement.setDouble(9, profile.goalWeight());
            }

            if (profile.weeklyGoal() == null) {
                statement.setNull(10, Types.REAL);
            } else {
                statement.setDouble(10, profile.weeklyGoal());
            }

            statement.executeUpdate();
        }
    }

    // Checks if user with given user_id exists in user_profiles
    public boolean existsByUserId(int userId) throws SQLException {
        String sql = """
                SELECT 1
                FROM user_profiles
                WHERE user_id = ?
                LIMIT 1
            """;

        try (
                Connection connection = DatabaseConnection.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
