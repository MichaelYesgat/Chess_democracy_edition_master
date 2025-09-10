package com.chess.democracy.edition.chess;

public class Rook extends ChessPiece {

    public Rook(PieceColor color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public boolean isValidMove(int endRow, int endCol, ChessGameLogic game) {
        if (isSamePosition(endRow, endCol)) {
            return false; // Can't move to the same square
        }

        if (this.getRow() != endRow && this.getCol() != endCol) {
            return false;
        }
        return game.isPathClear(this.getRow(), this.getCol(), endRow, endCol);
    }

    @Override
    public boolean canAttackSquare(int endRow, int endCol, ChessGameLogic game) {
        if (this.getRow() != endRow && this.getCol() != endCol) {
            return false;
        }
        return game.isPathClear(this.getRow(), this.getCol(), endRow, endCol);
    }
}
