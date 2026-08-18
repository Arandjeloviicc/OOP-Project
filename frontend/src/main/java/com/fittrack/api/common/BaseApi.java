package com.fittrack.api.common;

import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;

public abstract class BaseApi {

    // Host
    protected static final String BASE_URL = "http://localhost:8080/api";

    // Host Helpers
    protected static final String AUTH_URL = BASE_URL + "/auth";
    protected static final String PROFILE_URL = BASE_URL + "/profile";
    protected static final String NUTRITION_URL = BASE_URL + "/nutrition";

    // Helpers
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;

    protected BaseApi() {
        httpClient = HttpClient.newHttpClient();
        objectMapper = JsonMapper.getMapper();
    }
}
