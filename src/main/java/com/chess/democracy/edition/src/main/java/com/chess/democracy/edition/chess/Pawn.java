package com.chess.democracy.edition.chess;

public class Pawn extends ChessPiece {
    private boolean movedTwoSquaresLastTurn;

    public Pawn(PieceColor color, int row, int col) {
        super(color, row, col);
        this.movedTwoSquaresLastTurn = false;
    }

    public boolean hasMovedTwoSquaresLastTurn() {
        return movedTwoSquaresLastTurn;
    }

    public void setMovedTwoSquaresLastTurn(boolean movedTwoSquaresLastTurn) {
        this.movedTwoSquaresLastTurn = movedTwoSquaresLastTurn;
    }

    @Override
    public boolean isValidMove(int endRow, int endCol, ChessGameLogic game) {
        if (isSamePosition(endRow, endCol)) {
            return false; // Can't move to the same square
        }

        int direction = this.getColor() == PieceColor.WHITE ? -1 : 1;
        int startRow = this.getRow();
        int startCol = this.getCol();
        int rowDiff = endRow - startRow;
        int colDiff = endCol - startCol;
        ChessPiece targetPiece = game.getBoard()[endRow][endCol];

        // Standard move forward
        if (colDiff == 0) {
            if (targetPiece != null) return false;
            if (rowDiff == direction) return true;
            if (!this.hasMoved() &&
                    rowDiff == 2 * direction &&
                    game.getBoard()[startRow + direction][startCol] == null &&
                    game.getBoard()[endRow][endCol] == null) {
                return true;
            }
        }
        // Capture move
        else if (Math.abs(colDiff) == 1 && rowDiff == direction) {
            // Normal capture
            if (targetPiece != null && targetPiece.getColor() != this.getColor()) {
                return true;
            }
            // En Passant capture
            ChessPiece adjacentPawn = game.getBoard()[startRow][endCol];
            if (adjacentPawn instanceof Pawn &&
                    adjacentPawn.getColor() != this.getColor() &&
                    ((Pawn) adjacentPawn).hasMovedTwoSquaresLastTurn() &&
                    targetPiece == null) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnPassantMove(int endRow, int endCol, ChessGameLogic game) {
        int direction = this.getColor() == PieceColor.WHITE ? -1 : 1;
        int startRow = this.getRow();
        int startCol = this.getCol();
        int rowDiff = endRow - startRow;
        int colDiff = endCol - startCol;

        if (Math.abs(colDiff) == 1 && rowDiff == direction) {
            ChessPiece targetPiece = game.getBoard()[endRow][endCol];
            if (targetPiece == null) {
                ChessPiece adjacentPawn = game.getBoard()[startRow][endCol];
                if (adjacentPawn instanceof Pawn &&
                        adjacentPawn.getColor() != this.getColor() &&
                        ((Pawn) adjacentPawn).hasMovedTwoSquaresLastTurn()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canAttackSquare(int endRow, int endCol, ChessGameLogic game) {
        int direction = this.getColor() == PieceColor.WHITE ? -1 : 1;
        int rowDiff = endRow - this.getRow();
        int colDiff = endCol - this.getCol();
        return rowDiff == direction && Math.abs(colDiff) == 1;
    }

    @Override
    public Pawn clone() throws CloneNotSupportedException {
        Pawn clonedPawn = (Pawn) super.clone();
        clonedPawn.movedTwoSquaresLastTurn = this.movedTwoSquaresLastTurn;
        return clonedPawn;
    }
}
