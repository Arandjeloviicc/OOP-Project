package com.fittrack.dao;

import com.fittrack.model.measurement.WeightLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WeightLogDAO {

    // Inserts a weight log for a user
    public void insert(Connection connection, WeightLog weightLog) throws SQLException {
        String sql = """
            INSERT INTO weight_logs (
                user_id,
                logged_date,
                weight
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

           statement.setInt(1, weightLog.userId());
           statement.setString(2, weightLog.loggedDate().toString());
           statement.setDouble(3, weightLog.weight());

            statement.executeUpdate();
        }
    }
}
