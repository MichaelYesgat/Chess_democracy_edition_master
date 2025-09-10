// DBUtility.java
package com.chess.democracy.edition;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for database operations.
 * Handles user authentication, game management, and player assignments.
 */
public class DBUtility {
    public static final String DATABASE_URL = "jdbc:sqlite:database.db";
    public static User CurrentSignedInUser = null;

    /**
     * Signs up a new user by adding their details to the database.
     *
     * @param email    the user's email.
     * @param name     the user's name.
     * @param password the user's password.
     */
    public static void signUp(String email, String name, String password) {
        String sql = "INSERT INTO users (email, name, password) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, name);
            stmt.setString(3, password);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if an email already exists in the database.
     *
     * @param email the email to check.
     * @return the email if it exists, null otherwise.
     */
    public static String findDuplicateEmail(String email) {
        String sql = "SELECT email FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return email;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verifies login credentials by matching email and password.
     *
     * @param email    the user's email.
     * @param password the user's password.
     * @return true if credentials are valid, false otherwise.
     */
    public static boolean verifyLogin(String email, String password) {
        String sql = "SELECT user_id, name FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userID = rs.getInt("user_id");
                    String name = rs.getString("name");
                    CurrentSignedInUser = new User(userID, email, name);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Creates a new game in the database.
     *
     * @param gameID the ID of the game.
     * @return true if the game was created, false otherwise.
     */
    public static boolean createGame(int gameID) {
        if (CurrentSignedInUser == null) {
            return false;
        }
        String sql = "INSERT INTO games (gameID, creator_user_id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            stmt.setInt(2, CurrentSignedInUser.getUserID());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all games from the database.
     *
     * @return a list of Game objects.
     */
    public static List<Game> getAllGames() {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT g.gameID, g.creator_user_id, COUNT(p.user_id) AS player_count " +
                "FROM games g LEFT JOIN game_players p ON g.gameID = p.gameID " +
                "GROUP BY g.gameID, g.creator_user_id";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int gameID = rs.getInt("gameID");
                int creatorUserID = rs.getInt("creator_user_id");
                int playerCount = rs.getInt("player_count");
                Game game = new Game(gameID, creatorUserID, playerCount);
                list.add(game);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Adds a player to a game.
     *
     * @param gameID the ID of the game.
     * @param userID the ID of the user.
     * @return true if the player was added, false otherwise.
     */
    public static boolean addPlayerToGame(int gameID, int userID) {
        if (!doesGameExist(gameID)) {
            System.err.println("Game with ID " + gameID + " does not exist.");
            return false;
        }
        if (isUserInGame(gameID, userID)) {
            return false;
        }

        String team = assignBalancedTeam(gameID);
        String sql = "INSERT INTO game_players (gameID, user_id, team) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            stmt.setInt(2, userID);
            stmt.setString(3, team);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if a game exists in the database.
     *
     * @param gameID the ID of the game.
     * @return true if the game exists, false otherwise.
     */
    private static boolean doesGameExist(int gameID) {
        String sql = "SELECT 1 FROM games WHERE gameID = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Assigns a team to a player to balance the teams.
     *
     * @param gameID the ID of the game.
     * @return "white" or "black" depending on team assignment.
     */
    private static String assignBalancedTeam(int gameID) {
        int whiteCount = getTeamPlayerCount(gameID, "white");
        int blackCount = getTeamPlayerCount(gameID, "black");

        return (whiteCount <= blackCount) ? "white" : "black";
    }

    /**
     * Gets the number of players on a team.
     *
     * @param gameID the ID of the game.
     * @param team   the team name.
     * @return the number of players on the team.
     */
    private static int getTeamPlayerCount(int gameID, String team) {
        String sql = "SELECT COUNT(*) FROM game_players WHERE gameID = ? AND team = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            stmt.setString(2, team);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Checks if a user is already in a game.
     *
     * @param gameID the ID of the game.
     * @param userID the ID of the user.
     * @return true if the user is in the game, false otherwise.
     */
    public static boolean isUserInGame(int gameID, int userID) {
        String sql = "SELECT 1 FROM game_players WHERE gameID = ? AND user_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            stmt.setInt(2, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves the team of a user in a game.
     *
     * @param gameID the ID of the game.
     * @param userID the ID of the user.
     * @return the team name, or null if not found.
     */
    public static String getUserTeam(int gameID, int userID) {
        String sql = "SELECT team FROM game_players WHERE gameID = ? AND user_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            stmt.setInt(2, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("team");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Removes a player from a game.
     *
     * @param gameID the ID of the game.
     * @param userID the ID of the user.
     */
    public static void removePlayerFromGame(int gameID, int userID) {
        String sql = "DELETE FROM game_players WHERE gameID = ? AND user_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            stmt.setInt(2, userID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a user is the creator of a game.
     *
     * @param gameID the ID of the game.
     * @param userID the ID of the user.
     * @return true if the user is the creator, false otherwise.
     */
    public static boolean isUserGameCreator(int gameID, int userID) {
        String sql = "SELECT 1 FROM games WHERE gameID = ? AND creator_user_id = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            stmt.setInt(2, userID);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a game from the database.
     *
     * @param gameID the ID of the game.
     */
    public static void deleteGame(int gameID) {
        String sqlDeletePlayers = "DELETE FROM game_players WHERE gameID = ?";
        String sqlDeleteGame = "DELETE FROM games WHERE gameID = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtPlayers = conn.prepareStatement(sqlDeletePlayers);
                 PreparedStatement stmtGame = conn.prepareStatement(sqlDeleteGame)) {
                stmtPlayers.setInt(1, gameID);
                stmtPlayers.executeUpdate();

                stmtGame.setInt(1, gameID);
                stmtGame.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
