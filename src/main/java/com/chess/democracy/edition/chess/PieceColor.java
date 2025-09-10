package com.chess.democracy.edition.chess;

public enum PieceColor {
    WHITE("w"), BLACK("b");

    private final String abbreviation;

    PieceColor(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
