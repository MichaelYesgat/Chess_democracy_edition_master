package com.chess.democracy.edition;

import com.chess.democracy.edition.networking.GameServer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ServerGUIController {

    @FXML
    private TextField ipAddressField;

    @FXML
    private TextField portField;

    @FXML
    private Button startServerButton;

    @FXML
    private Button stopServerButton;

    @FXML
    private TextArea logArea;

    @FXML
    private VBox serverControls;

    private GameServer gameServer;

    public void initialize() {
        // Set default values
        ipAddressField.setText("localhost");
        portField.setText("1024");
        stopServerButton.setDisable(true);

        // Attempt to start the server with default settings automatically
        startServerWithDefaultSettings();
    }

    @FXML
    private void startServer(ActionEvent event) {
        startServerWithSettings();
    }

    @FXML
    private void stopServer(ActionEvent event) {
        if (GameServer.getInstance() != null) {
            GameServer.getInstance().stopServer();
            startServerButton.setDisable(false);
            stopServerButton.setDisable(true);
            ipAddressField.setDisable(false);
            portField.setDisable(false);
            logArea.appendText("Server stopped.\n");
        } else {
            logArea.appendText("Server is not running.\n");
        }
    }

    private void startServerWithSettings() {
        String ipAddress = ipAddressField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert("Invalid Port", "Please enter a valid port number.");
            return;
        }

        try {
            gameServer = GameServer.getInstance(ipAddress, port);
            startServerButton.setDisable(true);
            stopServerButton.setDisable(false);
            ipAddressField.setDisable(true);
            portField.setDisable(true);
            logArea.appendText("Server started on " + ipAddress + ":" + port + "\n");
        } catch (IOException e) {
            showAlert("Server Error", "Failed to start server: " + e.getMessage());
        }
    }

    /**
     * Starts the server automatically with default settings.
     * This method ensures the server starts only once.
     */
    public void startServerWithDefaultSettings() {
        Platform.runLater(() -> {
            if (GameServer.getInstance() == null) {
                startServerWithSettings();
            } else {
                logArea.appendText("Server is already running on " + GameServer.getInstance().getIPAddress() + ":" + GameServer.getInstance().getPort() + "\n");
                startServerButton.setDisable(true);
                stopServerButton.setDisable(false);
                ipAddressField.setDisable(true);
                portField.setDisable(true);
            }
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
