package com.fittrack.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private DatabaseConnection() {
        /* This utility class should not be instantiated */
    }

    private static final String URL = "jdbc:sqlite:fittrack.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}