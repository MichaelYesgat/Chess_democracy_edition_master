package com.chess.democracy.edition.chess;

public class King extends ChessPiece {

    public King(PieceColor color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public boolean isValidMove(int endRow, int endCol, ChessGameLogic game) {
        if (isSamePosition(endRow, endCol)) {
            return false; // Can't move to the same square
        }

        int rowDiff = endRow - this.getRow();
        int colDiff = endCol - this.getCol();

        // Standard king move (one square in any direction)
        if (Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1) {
            // Ensure the destination is not under attack
            if (!game.isSquareUnderAttack(endRow, endCol, this.getColor())) {
                return true;
            } else {
                return false;
            }
        }

        // Castling move
        if (!this.hasMoved() && rowDiff == 0 && Math.abs(colDiff) == 2) {
            return canCastle(endRow, endCol, game);
        }

        return false;
    }

    private boolean canCastle(int endRow, int endCol, ChessGameLogic game) {
        int direction = (endCol - this.getCol()) > 0 ? 1 : -1;
        int rookCol = direction > 0 ? 7 : 0;

        // Check if king is in check
        if (game.isKingInCheck(this.getColor())) {
            return false;
        }

        ChessPiece rook = game.getBoard()[this.getRow()][rookCol];

        // Check if the rook exists, is the correct color, and hasn't moved
        if (!(rook instanceof Rook) || rook.hasMoved() || rook.getColor() != this.getColor()) {
            return false;
        }

        // Check that all squares between king and rook are empty
        for (int col = this.getCol() + direction; col != rookCol; col += direction) {
            if (game.getBoard()[this.getRow()][col] != null) {
                return false;
            }
        }

        // Check that the squares the king passes over are not under attack
        for (int col = this.getCol(); col != endCol + direction; col += direction) {
            if (game.isSquareUnderAttack(this.getRow(), col, this.getColor())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canAttackSquare(int endRow, int endCol, ChessGameLogic game) {
        int rowDiff = Math.abs(endRow - this.getRow());
        int colDiff = Math.abs(endCol - this.getCol());
        return rowDiff <= 1 && colDiff <= 1;
    }
}
