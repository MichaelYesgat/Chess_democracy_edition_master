package com.chess.democracy.edition.chess;

public class MoveDetails {

    private ChessPiece capturedPiece;
    private boolean enPassantOccurred;
    private boolean pawnPromoted;
    private int promotionRow;
    private int promotionCol;
    private boolean castlingOccurred;

    public void setMovingPiece(ChessPiece movingPiece) {}

    public boolean isCastlingOccurred() {
        return castlingOccurred;
    }

    public void setCastlingOccurred(boolean castlingOccurred) {
        this.castlingOccurred = castlingOccurred;
    }

    public ChessPiece getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(ChessPiece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    public boolean isEnPassantOccurred() {
        return enPassantOccurred;
    }

    public void setEnPassantOccurred(boolean enPassantOccurred) {
        this.enPassantOccurred = enPassantOccurred;
    }

    public boolean isPawnPromoted() {
        return pawnPromoted;
    }

    public void setPawnPromoted(boolean pawnPromoted) {
        this.pawnPromoted = pawnPromoted;
    }

    public int getPromotionRow() {
        return promotionRow;
    }

    public int getPromotionCol() {
        return promotionCol;
    }

    public void setPromotionPosition(int row, int col) {
        this.promotionRow = row;
        this.promotionCol = col;
    }
}
