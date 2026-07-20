package com.fittrack.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void initialize() {
        try (
                Connection connection = DatabaseConnection.connect();
                Statement statement = connection.createStatement()
        ) {
            createUsersTable(statement);
            createUserProfilesTable(statement);
            createWeightLogsTable(statement);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to initialize database.",
                    e
            );
        }
    }

    private static void createUsersTable(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private static void createUserProfilesTable(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS user_profiles (
                user_id INTEGER PRIMARY KEY,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                date_of_birth TEXT NOT NULL,
                gender TEXT NOT NULL,
                height REAL NOT NULL,
                activity_level TEXT NOT NULL,
                goal_type TEXT NOT NULL,
                goal_weight REAL,
                weekly_goal REAL,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);
    }

    private static void createWeightLogsTable(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS weight_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                logged_date TEXT NOT NULL,
                weight REAL NOT NULL,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT,
                UNIQUE (user_id, logged_date),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """);
    }

}
