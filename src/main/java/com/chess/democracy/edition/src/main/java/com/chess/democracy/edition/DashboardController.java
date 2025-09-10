package com.chess.democracy.edition;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class DashboardController extends LoggedInController {
    @FXML
    public Button joinaGameButton;
    @FXML
    public TextField gameIdFieldCreate;
    @FXML
    public Button createGameButton;
    @FXML
    public Button logoutButton;
    @FXML
    public ListView<Game> gameListView;
    @FXML
    private Label profileLabel;

    private static final Font LABEL_FONT = Font.font("Lucida Sans Unicode", FontWeight.NORMAL, 15.0);

    public void initialize() {
        profileLabel.setText(DBUtility.CurrentSignedInUser.getName());

        // Set up the cell factory for the gameListView
        gameListView.setCellFactory(lv -> new ListCell<Game>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item.getGameID() == 0) {
                    setText("No game available.");
                    setFont(LABEL_FONT);
                } else {
                    setText("Game ID: " + item.getGameID() + " | Mode: Black Vs White\n" +
                            "Number of players: " + item.getPlayerCount() + "/30\n");
                    setFont(LABEL_FONT);
                }
            }
        });

        populateGameList();
    }

    private void populateGameList() {
        List<Game> games = DBUtility.getAllGames();
        gameListView.getItems().clear();
        if (games.isEmpty()) {
            gameListView.getItems().add(new Game(0, 0, 0)); // Placeholder game with message
        } else {
            gameListView.getItems().setAll(games);
        }
    }

    public void changeScene(ActionEvent event, String fxmlFile, String title, int gameID) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));

        try {
            root = loader.load();
            if (fxmlFile.equals("Chessboard.fxml")) {
                ChessboardController controller = loader.getController();
                controller.setGameID(gameID);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1700, 1000));
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Method to handle creating a new game
    public void createGame(ActionEvent buttonClick) {
        String gameIdText = gameIdFieldCreate.getText().trim();

        if (gameIdText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a Game ID", "Message!", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int gameID = Integer.parseInt(gameIdText);
            boolean created = DBUtility.createGame(gameID);
            if (created) {
                // Add the player to the game
                DBUtility.addPlayerToGame(gameID, DBUtility.CurrentSignedInUser.getUserID());
                // Proceed to the ChessboardController and pass the gameID
                changeScene(buttonClick, "Chessboard.fxml", "Chessboard", gameID);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to create game. Game ID might already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid Game ID. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to handle joining an existing game
    public void joinGame(ActionEvent buttonClick) {
        Game selectedGame = gameListView.getSelectionModel().getSelectedItem();
        if (selectedGame != null && selectedGame.getGameID() != 0) {
            boolean success = DBUtility.addPlayerToGame(selectedGame.getGameID(), DBUtility.CurrentSignedInUser.getUserID());
            if (success) {
                // Proceed to the ChessboardController and pass the gameID
                changeScene(buttonClick, "Chessboard.fxml", "Chessboard", selectedGame.getGameID());
            } else {
                JOptionPane.showMessageDialog(null, "Failed to join game. You might already be in this game.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a valid game to join.", "Message!", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    public void logout(ActionEvent event) {

        // Set current user to null
        DBUtility.CurrentSignedInUser = null;

        // Navigate back to the Login screen
        changeScene(event, "Login.fxml", "Login");
    }

    public void refreshGameListMore(ActionEvent event) {
        populateGameList();
    }

}