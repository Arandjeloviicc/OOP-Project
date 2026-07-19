package com.fittrack.database;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    private static final Path DATABASE_PATH =
            resolveApplicationDirectory().resolve("fittrack.db");

    private static final String URL =
            "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath();

    private DatabaseConnection() {
    }

    public static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            connection.close();
            throw e;
        }

        return connection;
    }

    private static Path resolveApplicationDirectory() {
        try {
            Path codeLocation = Path.of(
                    DatabaseConnection.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );

            // When the app is run from JAR file
            if (Files.isRegularFile(codeLocation)) {
                return codeLocation.getParent();
            }

            // When teh app is run from IntelliJ or Maven
            return Path.of(System.getProperty("user.dir"))
                    .toAbsolutePath()
                    .normalize();

        } catch (URISyntaxException e) {
            throw new IllegalStateException(
                    "Could not determine application directory.",
                    e
            );
        }
    }

    public static Path getDatabasePath() {
        return DATABASE_PATH;
    }
}