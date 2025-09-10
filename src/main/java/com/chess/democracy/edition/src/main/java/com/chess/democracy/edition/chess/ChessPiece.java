package com.chess.democracy.edition.chess;

public abstract class ChessPiece implements Cloneable {
    private final PieceColor color;
    private int row;
    private int col;
    private boolean hasMoved;
    private boolean captured;

    public ChessPiece(PieceColor color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.hasMoved = false;
        this.captured = false;
    }

    public PieceColor getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean isCaptured() {
        return captured;
    }

    public boolean isSamePosition(int endRow, int endCol) {
        return this.getRow() == endRow && this.getCol() == endCol;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    // Abstract methods for movement and attack
    public abstract boolean isValidMove(int endRow, int endCol, ChessGameLogic game);

    public abstract boolean canAttackSquare(int endRow, int endCol, ChessGameLogic game);

    @Override
    public ChessPiece clone() throws CloneNotSupportedException {
        return (ChessPiece) super.clone();
    }
}
