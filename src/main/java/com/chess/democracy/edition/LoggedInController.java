package com.chess.democracy.edition;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class LoggedInController implements SceneChanger {
    @Override
    public void changeScene(ActionEvent event, String fxmlFile, String title) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(fxmlFile));

        if (fxmlFile.equals("Login.fxml")) {
            try {
                root = loader.load();
                LoginController controller = loader.getController();
            } catch (IOException e) {
                System.out.println("IOException");
            }
        } else {
            try {
                root = loader.load();
                DashboardController controller = loader.getController();
            } catch (IOException e) {
                System.out.println("IOException");
            }
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root, 1700, 1000));
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.show();
    }

    public void logout(ActionEvent event) {

        changeScene(event, "Login.fxml", "Login");
        // Should set the DBUtils.currentLoggedInUser to null
    }
}
