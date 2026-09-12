package com.fittrack.api.profile;

import com.fittrack.api.common.BaseApi;
import com.fittrack.dto.profile.ProfileSetupRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProfileSetupApi extends BaseApi {

    public void completeProfile(ProfileSetupRequest profileSetupRequest) {
        try {
            String url = PROFILE_URL + "/setup";

            String requestBody = objectMapper.writeValueAsString(profileSetupRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Failed to complete profile setup."
                );
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not communicate with the FitTrack server.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Profile setup request was interrupted.",
                    exception
            );
        }
    }
}
