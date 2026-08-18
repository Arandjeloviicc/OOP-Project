package com.fittrack.api.common;

import tools.jackson.databind.ObjectMapper;

public class JsonMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonMapper() {}

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
