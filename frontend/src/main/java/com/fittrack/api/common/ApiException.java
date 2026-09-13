package com.fittrack.api.common;

public class ApiException extends RuntimeException {

    private final Integer statusCode;
    private final String responseBody;

    public ApiException(String message, Throwable cause) {
        super(message, cause);

        this.statusCode = null;
        this.responseBody = null;
    }

    public ApiException(int statusCode, String responseBody) {
        super("Request failed with status: " + statusCode);

        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
