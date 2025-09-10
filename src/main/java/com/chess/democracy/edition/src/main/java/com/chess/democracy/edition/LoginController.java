package com.chess.democracy.edition;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import java.io.IOException;


public class LoginController implements SceneChanger{


    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordFieldText;

    @FXML
    private CheckBox showPasswordCheckBox;


    @Override
    public void changeScene(ActionEvent event, String fxmlFile, String title) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource(fxmlFile));


        if (fxmlFile.equals("Dashboard.fxml")){
            try {
                root = loader.load();
                DashboardController controller = loader.getController();

            }
            catch(IOException e){
                e.printStackTrace();
            }
        }  else {
            try {
                root = loader.load();
                SignupController controller = loader.getController();

            }
            catch(IOException e){
                System.out.println("IOException");
            }
        }

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root, 1700, 1000));
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.show();
    }

    public void login(ActionEvent buttonClick) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String visiblePassword = passwordFieldText.getText();

        if (!validateInput(email, password)) {
            JOptionPane.showMessageDialog(null, "Please enter both email and password.", "Message!", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            boolean loginSuccessful = false;
            if (showPasswordCheckBox.isSelected()) {
                loginSuccessful = DBUtility.verifyLogin(email, visiblePassword);
            } else {
                loginSuccessful = DBUtility.verifyLogin(email, password);
            }

            if (loginSuccessful) {
                // Change scene if login is successful
                changeScene(buttonClick, "Dashboard.fxml", "Dashboard");
                clearFields();
            } else {
                // Show error message if login fails
                JOptionPane.showMessageDialog(null, "Incorrect Username or Password", "Message!", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while processing your request. Please try again later.", "Error!", JOptionPane.ERROR_MESSAGE);
            // Log the exception for further investigation if needed
            e.printStackTrace();
        }
    }

    private boolean validateInput(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

    private void clearFields() {
        emailField.clear();
        passwordField.clear();
        passwordFieldText.clear();
    }

    public void showPassword(ActionEvent event) {
        String hiddenPassword = passwordField.getText();
        String showPassword = passwordFieldText.getText();

        if (showPasswordCheckBox.isSelected()) {
            passwordFieldText.setText(hiddenPassword);
            passwordFieldText.setVisible(true);
            passwordField.setVisible(false);
        } else {
            passwordField.setText(showPassword);
            passwordField.setVisible(true);
            passwordFieldText.setVisible(false);
        }
    }
    public void signup(ActionEvent buttonClick){
        changeScene(buttonClick, "Signup.fxml", "Signup");
        //Should set DBUtils.currentLoggedInUser to null
    }
}
