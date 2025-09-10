package com.chess.democracy.edition.chess;

public class Queen extends ChessPiece {

    public Queen(PieceColor color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public boolean isValidMove(int endRow, int endCol, ChessGameLogic game) {
        if (isSamePosition(endRow, endCol)) {
            return false; // Can't move to the same square
        }

        if (Math.abs(this.getRow() - endRow) == Math.abs(this.getCol() - endCol) ||
                this.getRow() == endRow || this.getCol() == endCol) {
            return game.isPathClear(this.getRow(), this.getCol(), endRow, endCol);
        }
        return false;
    }

    @Override
    public boolean canAttackSquare(int endRow, int endCol, ChessGameLogic game) {
        return isValidMove(endRow, endCol, game);
    }
}
