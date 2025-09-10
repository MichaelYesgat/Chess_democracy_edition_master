package com.chess.democracy.edition;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import java.io.IOException;


public class SignupController implements SceneChanger{

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField1;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField passwordFieldText;

    @FXML
    private TextField passwordFieldText1;

    @FXML
    private TextField nameField;



    @Override
    public void changeScene(ActionEvent event, String fxmlFile, String title) {
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(SignupController.class.getResource(fxmlFile));

        try{
            root = loader.load();
            LoginController controller = loader.getController();

        }
        catch(IOException e){
            e.printStackTrace();
        }
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root, 1700, 1000));
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.show();
    }

    public void showPassword(ActionEvent event) {
        String hiddenPassword = passwordField.getText();
        String hiddenPassword1 = passwordField1.getText();
        String showPassword = passwordFieldText.getText();
        String showPassword1 = passwordFieldText1.getText();

        if (showPasswordCheckBox.isSelected()) {
            passwordFieldText.setText(hiddenPassword);
            passwordFieldText1.setText(hiddenPassword1);
            passwordFieldText.setVisible(true);
            passwordFieldText1.setVisible(true);
            passwordField.setVisible(false);
            passwordField1.setVisible(false);
        } else {
            passwordField.setText(showPassword);
            passwordField1.setText(showPassword1);
            passwordField.setVisible(true);
            passwordField1.setVisible(true);
            passwordFieldText.setVisible(false);
            passwordFieldText1.setVisible(false);
        }
    }

    public void logout(ActionEvent event){
        changeScene(event,"Login.fxml","Login");
        //Should set the DBUtils.currentLoggedInUser to null

    }

    public void signup(ActionEvent buttonClick) {
        String email = emailField.getText().trim();
        String name = nameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = passwordField1.getText();
        String visiblePassword = passwordFieldText.getText();
        String secondVisiblePassword = passwordFieldText1.getText();

        // Input validation
        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter all fields", "Message!", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // Validate for duplicate email
            String duplicate = DBUtility.findDuplicateEmail(email);
            if (duplicate != null) {
                JOptionPane.showMessageDialog(null, "Failed to sign up. Email '" + duplicate + "' is already taken. Please try using a different email.", "Message!", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Validate password length
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(null, "Password must be more than 6 characters", "Message!", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Check if passwords match
            if (!password.equals(confirmPassword) || !visiblePassword.equals(secondVisiblePassword)) {
                JOptionPane.showMessageDialog(null, "Failed to sign up. Passwords do not match.", "Message!", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Check if the password contains special characters
            String specialCharacters = "(){}[]|`¬¦! £$%^&*<>:;#~_-+=,@.?/";
            for (char ch : password.toCharArray()) {
                if (specialCharacters.indexOf(ch) != -1) {
                    JOptionPane.showMessageDialog(null, "Password contains special characters: (){}[]|`¬¦! £$%^&*<>:;#~_-+=,@.?/.", "Message!", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }

            // Check if the email matches the general email pattern
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"; // General email pattern
            if (!email.matches(emailRegex)) {
                JOptionPane.showMessageDialog(null, "Failed to sign up. Please enter a valid email address.", "Message!", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Sign up user
            if (showPasswordCheckBox.isSelected()) {
                DBUtility.signUp(email, name, visiblePassword);
            } else {
                DBUtility.signUp(email, name, password);
            }

            // Clear input fields after successful registration
            JOptionPane.showMessageDialog(null, "Successfully registered your new account", "Message!", JOptionPane.INFORMATION_MESSAGE);
            clearInputFields();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to sign up. Please try again later.", "Message!", JOptionPane.ERROR_MESSAGE);
            // Log the exception for further investigation if needed
            e.printStackTrace();
        }
    }

    // Helper method to clear input fields
    private void clearInputFields() {
        emailField.clear();
        nameField.clear();
        passwordField.clear();
        passwordField1.clear();
        passwordFieldText.clear();
        passwordFieldText1.clear();
    }
}
