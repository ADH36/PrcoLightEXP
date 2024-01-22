// DifficultyFetcher.java
package com.example.demo;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DifficultyFetcher {
    private static final String API_ENDPOINT = "https://explorer.procyoncoin.org/api/getdifficulty";

    public Task<String> createFetchDifficultyTask() {
        return new Task<>() {
            @Override
            protected String call() {
                return fetchDifficulty();
            }
        };
    }

    private String fetchDifficulty() {
        try {
            URL url = new URL(API_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            connection.setRequestMethod("GET");

            // Get the response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response from the API
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Print the response to the console for debugging
                    System.out.println("Raw Difficulty Response: " + response.toString());

                    return response.toString();
                }
            } else {
                System.out.println("Error fetching difficulty. HTTP response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ""; // Return an empty string if fetching fails
    }
}
