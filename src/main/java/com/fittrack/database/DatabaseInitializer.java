package com.fittrack.database;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void initialize() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (
                Connection connection = DatabaseConnection.connect();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(createUsersTable);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to initialize database.",
                    e
            );
        }
    }
}
