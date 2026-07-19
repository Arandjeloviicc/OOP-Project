package com.fittrack.dao;

import com.fittrack.database.DatabaseConnection;
import com.fittrack.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {
    // Checks if user with given email exists
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users where email = ? LIMIT 1";

        try (
                Connection connection = DatabaseConnection.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Failed to check whether email exists.",
                    e
            );
        }
    }

    // Checks if user with given username exists
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users where username = ? LIMIT 1";

        try (
                Connection connection = DatabaseConnection.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Failed to check whether username exists.",
                    e
            );
        }
    }

    // Inserts new User into database and returns it
    public User save(String username, String email, String passwordHash) {
        String sql = """
            INSERT INTO users (username, email, password_hash)
            VALUES (?, ?, ?)
            RETURNING id, username, email, password_hash
            """;

        try (
                Connection connection = DatabaseConnection.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);
            statement.setString(2, email);
            statement.setString(3, passwordHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "Database did not return the created user."
                    );
                }

                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash")
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to save user.",
                    e
            );
        }
    }

    // Returns User with given email, or Empty Optional Object if it doesn't exist
    public Optional<User> findByEmail(String email) {
        String sql = """
              SELECT id, username, email, password_hash
              FROM users
              WHERE email = ?
              LIMIT 1
              """;

        try (
                Connection connection = DatabaseConnection.connect();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                // If User with given email doesn't exist
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                User user = new User (
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash")
                );

                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(
                    "Failed to find user by email.",
                    e
            );
        }
    }
}