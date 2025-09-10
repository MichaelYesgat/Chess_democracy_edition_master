// MainApplication.java
package com.chess.democracy.edition;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;

/**
 * Main entry point for the JavaFX application.
 * Handles database initialization and launches the UI.
 */
public class MainApplication extends Application {

    private static final String DATABASE_URL = DBUtility.DATABASE_URL;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize the database tables
        createDatabaseTables();

        // Start the ServerGUI in a separate window
        startServerGUI();

        // Load the Login screen
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Scene loginScene = new Scene(loginLoader.load());
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void startServerGUI() {
        Platform.runLater(() -> {
            try {
                FXMLLoader serverGUILoader = new FXMLLoader(getClass().getResource("ServerGUI.fxml"));
                Scene serverGUIScene = new Scene(serverGUILoader.load());
                Stage serverStage = new Stage();
                serverStage.setScene(serverGUIScene);
                serverStage.setTitle("Server GUI");
                serverStage.setResizable(false);
                serverStage.show();

                // Start the server with default settings
                ServerGUIController controller = serverGUILoader.getController();
                controller.startServerWithDefaultSettings();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Creates necessary database tables if they do not exist.
     */
    private static void createDatabaseTables() {
        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "email TEXT, " +
                "password TEXT)";
        executeSQL(createUsersTableSQL);

        String createGamesTableSQL = "CREATE TABLE IF NOT EXISTS games (" +
                "gameID INTEGER PRIMARY KEY, " +
                "creator_user_id INTEGER)";
        executeSQL(createGamesTableSQL);

        String createGamePlayersTableSQL = "CREATE TABLE IF NOT EXISTS game_players (" +
                "gameID INTEGER, " +
                "user_id INTEGER, " +
                "team TEXT, " +
                "PRIMARY KEY (gameID, user_id))";
        executeSQL(createGamePlayersTableSQL);
    }

    /**
     * Executes an SQL statement.
     *
     * @param sql the SQL statement to execute.
     */
    private static void executeSQL(String sql) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
