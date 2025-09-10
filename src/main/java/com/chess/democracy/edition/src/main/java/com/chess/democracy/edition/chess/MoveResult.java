package com.chess.democracy.edition.chess;

public class MoveResult {
    private final boolean success;
    private String errorMessage;
    private final boolean animateKing;

    public MoveResult(boolean success) {
        this.success = success;
        this.animateKing = false;
    }

    public MoveResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.animateKing = false;
    }

    public MoveResult(boolean success, String errorMessage, boolean animateKing) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.animateKing = animateKing;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean shouldAnimateKing() {
        return animateKing;
    }


}
