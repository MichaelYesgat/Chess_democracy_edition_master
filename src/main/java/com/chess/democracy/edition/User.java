// User.java
package com.chess.democracy.edition;

/**
 * Represents a user in the system.
 * Contains user ID, email, and name.
 */
public class User {

    private int userID;
    private String email;
    private String name;

    public User(int userID, String email, String name) {
        this.userID = userID;
        this.email = email;
        this.name = name;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserID(int ID) {
        this.userID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }
}
