package com.fittrack.util;

import com.fittrack.api.common.ApiException;

public class ApiExceptionUtils {

    private ApiExceptionUtils() {}

    public static boolean isConflict(Throwable exception) {
        return exception instanceof ApiException apiException
                && Integer.valueOf(409).equals(apiException.getStatusCode());
    }
}