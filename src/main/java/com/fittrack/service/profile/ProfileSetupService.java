package com.fittrack.service.profile;

import com.fittrack.dao.UserProfileDAO;
import com.fittrack.dao.WeightLogDAO;
import com.fittrack.database.DatabaseConnection;
import com.fittrack.model.profile.UserProfile;
import com.fittrack.model.measurement.WeightLog;

import java.sql.Connection;
import java.sql.SQLException;

public class ProfileSetupService {
    private final UserProfileDAO userProfileDAO;
    private final WeightLogDAO weightLogDAO;

    public ProfileSetupService() {
        this.userProfileDAO = new UserProfileDAO();
        this.weightLogDAO = new WeightLogDAO();
    }

    public void completeProfile(UserProfile userProfile, WeightLog weightLog) {
        try (Connection connection = DatabaseConnection.connect()) {

            connection.setAutoCommit(false);

            try {
                userProfileDAO.insert(connection, userProfile);
                weightLogDAO.insert(connection, weightLog);

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to complete profile setup.",
                    exception
            );
        }
    }

    public boolean isProfileSetupComplete(int userId)  {
        try {
            return userProfileDAO.existsByUserId(userId);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to check profile setup status.",
                    exception
            );
        }
    }
}
