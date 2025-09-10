package com.chess.democracy.edition.chess;

public class Knight extends ChessPiece {

    public Knight(PieceColor color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public boolean isValidMove(int endRow, int endCol, ChessGameLogic game) {
        if (isSamePosition(endRow, endCol)) {
            return false; // Can't move to the same square
        }

        int rowDiff = Math.abs(endRow - this.getRow());
        int colDiff = Math.abs(endCol - this.getCol());
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    @Override
    public boolean canAttackSquare(int endRow, int endCol, ChessGameLogic game) {
        return isValidMove(endRow, endCol, game);
    }
}

