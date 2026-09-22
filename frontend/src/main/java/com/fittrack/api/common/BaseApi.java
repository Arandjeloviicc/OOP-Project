package com.fittrack.api.common;

public abstract class BaseApi {

    // Host
    protected static final String BASE_URL = "http://localhost:8080/api";

    // Api Client
    protected final ApiClient apiClient;

    // Host Helpers
    protected static final String AUTH_URL = BASE_URL + "/auth";
    protected static final String PROFILE_URL = BASE_URL + "/profile";
    protected static final String NUTRITION_URL = BASE_URL + "/nutrition";
    protected static final String MEASUREMENTS_URL = BASE_URL + "/measurements";

    protected BaseApi() {
        apiClient = ApiClient.getInstance();
    }
}
