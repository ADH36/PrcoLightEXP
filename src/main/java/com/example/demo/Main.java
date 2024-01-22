// Main.java
package com.example.demo;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class Main extends Application {
    private ProcyonCoinWallet wallet;
    private WalletManager walletManager = new WalletManager();
    private Label balanceLabel;
    private Label addressLabel;
    private Label difficultyLabel;
    private DifficultyFetcher difficultyFetcher;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ProcyonCoin Light Wallet");

        balanceLabel = new Label("Wallet Balance: 0.0 PROCY");
        addressLabel = new Label("");
        difficultyLabel = new Label("Network Difficulty: Fetching...");

        Button generateWalletButton = new Button("Generate Wallet");
        generateWalletButton.setOnAction(e -> handleGenerateWalletButton());

        Button saveWalletButton = new Button("Save Wallet");
        saveWalletButton.setOnAction(e -> handleSaveWalletButton());

        Button loadWalletButton = new Button("Load Wallet");
        loadWalletButton.setOnAction(e -> handleLoadWalletButton());

        Button sendButton = new Button("Send ProcyonCoin");
        sendButton.setOnAction(e -> handleSendButton());

        VBox buttonsLayout = new VBox(10);
        buttonsLayout.getChildren().addAll(generateWalletButton, saveWalletButton, loadWalletButton, sendButton);

        VBox infoLayout = new VBox(10);
        infoLayout.getChildren().addAll(balanceLabel, addressLabel, difficultyLabel);

        HBox root = new HBox(20);
        root.getChildren().addAll(buttonsLayout, infoLayout);

        primaryStage.setScene(new Scene(new StackPane(root), 600, 250));
        primaryStage.show();

        // Initialize and start the difficulty fetching service
        difficultyFetcher = new DifficultyFetcher();
        difficultyFetcher.setPeriod(javafx.util.Duration.seconds(1));
        difficultyFetcher.start();
    }

    private void handleGenerateWalletButton() {
        TextInputDialog dialog = new TextInputDialog("100.0");
        dialog.setTitle("Generate Wallet");
        dialog.setHeaderText("Enter initial balance for the new wallet:");
        dialog.setContentText("Initial Balance:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(balance -> {
            try {
                double initialBalance = Double.parseDouble(balance);
                wallet = walletManager.createWallet(initialBalance);
                updateUI();
            } catch (NumberFormatException ex) {
                showErrorDialog("Invalid input. Please enter a valid number.");
            }
        });
    }

    private void handleSaveWalletButton() {
        if (wallet != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Wallet");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wallet Files", "*.wallet"));
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                walletManager.saveWallet(wallet, selectedFile.getAbsolutePath());
            }
        } else {
            showErrorDialog("No wallet to save. Please generate or load a wallet first.");
        }
    }

    private void handleLoadWalletButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Wallet");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Wallet Files", "*.wallet"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            wallet = walletManager.loadWallet(selectedFile.getAbsolutePath());
            updateUI();
        }
    }

    private void handleSendButton() {
        if (wallet != null) {
            double transactionAmount = 10.0;
            Transaction transaction = new Transaction(-transactionAmount);
            wallet.addTransaction(transaction);

            updateUI();
        }
    }

    private void updateUI() {
        if (wallet != null) {
            balanceLabel.setText("Wallet Balance: " + wallet.getBalance() + " PROCY");
            addressLabel.setText("Wallet Address: " + wallet.getWalletAddress().getAddress());
        } else {
            balanceLabel.setText("No wallet created or loaded.");
            addressLabel.setText("");
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class DifficultyFetcher extends ScheduledService<Double> {
        private final DifficultyFetcherImpl difficultyFetcher = new DifficultyFetcherImpl();

        @Override
        protected Task<Double> createTask() {
            Task<Double> fetchDifficultyTask = difficultyFetcher.createFetchDifficultyTask();

            fetchDifficultyTask.setOnSucceeded(event -> {
                double difficulty = fetchDifficultyTask.getValue();
                difficultyLabel.setText("Network Difficulty: " + difficulty);
            });

            fetchDifficultyTask.setOnFailed(event -> {
                System.out.println("Failed to fetch difficulty: " + fetchDifficultyTask.getException());
            });

            return fetchDifficultyTask;
        }
    }

    private static class DifficultyFetcherImpl {
        private static final String API_ENDPOINT = "https://explorer.procyoncoin.org/api/getdifficulty";

        public Task<Double> createFetchDifficultyTask() {
            return new Task<>() {
                @Override
                protected Double call() {
                    return fetchDifficulty();
                }
            };
        }

        private double fetchDifficulty() {
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

                        // Parse the response to get the difficulty
                        return Double.parseDouble(response.toString());
                    }
                } else {
                    System.out.println("Error fetching difficulty. HTTP response code: " + responseCode);
                }

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return -1.0; // Return a default value if fetching fails
        }
    }
}
