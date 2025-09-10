// Game.java
package com.chess.democracy.edition;

/**
 * Represents a game in the system.
 * Contains information about the game ID, creator, and player count.
 */
public class Game {
    private final int gameID;
    private final int creatorUserID;
    private final int playerCount;

    public Game(int gameID, int creatorUserID, int playerCount) {
        this.gameID = gameID;
        this.creatorUserID = creatorUserID;
        this.playerCount = playerCount;
    }

    public int getGameID() {
        return gameID;
    }

    public int getCreatorUserID() {
        return creatorUserID;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    @Override
    public String toString() {
        return "Game ID: " + gameID + " | Mode: Black Vs White\n" +
                "Number of players: " + playerCount + "/30\n";
    }
}
