package com.fittrack.backend.repository.user.projection;

public interface LoginData {
    Integer getId();

    String getUsername();

    String getEmail();

    String getPasswordHash();

    Boolean getProfileSetupComplete();
}
