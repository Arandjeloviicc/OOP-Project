package com.fittrack.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:fittrack.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}