package com.chess.democracy.edition.chess;

import java.util.ArrayList;
import java.util.List;

public class ChessGameLogic implements Cloneable {

    // Game state
    private boolean isWhiteTurn = true;
    private boolean gameOver = false;
    private boolean isCheckmate = false;
    private boolean isStalemate = false;
    private PieceColor winner = null;

    // Board representation
    private ChessPiece[][] board;

    // Lists to keep track of captured pieces
    private List<ChessPiece> capturedPiecesWhite = new ArrayList<>();
    private List<ChessPiece> capturedPiecesBlack = new ArrayList<>();

    // Move details for UI updates
    private MoveDetails lastMoveDetails;

    // Constructor
    public ChessGameLogic() {
        setupBoard();
    }

    // Board setup methods
    private void setupBoard() {
        board = new ChessPiece[8][8];

        // Add pawns
        for (int col = 0; col < 8; col++) {
            board[6][col] = new Pawn(PieceColor.WHITE, 6, col);
            board[1][col] = new Pawn(PieceColor.BLACK, 1, col);
        }

        // Add other pieces
        addBackRow(PieceColor.WHITE, 7);
        addBackRow(PieceColor.BLACK, 0);
    }

    private void addBackRow(PieceColor color, int row) {
        ChessPiece[] backRow = {
                new Rook(color, row, 0),
                new Knight(color, row, 1),
                new Bishop(color, row, 2),
                new Queen(color, row, 3),
                new King(color, row, 4),
                new Bishop(color, row, 5),
                new Knight(color, row, 6),
                new Rook(color, row, 7)
        };
        System.arraycopy(backRow, 0, board[row], 0, 8);
    }

    // Getters
    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public ChessPiece[][] getBoard() {
        return board;
    }

    public boolean isCheckmate() {
        return isCheckmate;
    }

    public boolean isStalemate() {
        return isStalemate;
    }

    public PieceColor getWinner() {
        return winner;
    }

    public List<ChessPiece> getCapturedPiecesWhite() {
        return capturedPiecesWhite;
    }

    public List<ChessPiece> getCapturedPiecesBlack() {
        return capturedPiecesBlack;
    }

    public MoveDetails getLastMoveDetails() {
        return lastMoveDetails;
    }

    // Methods for moving pieces and updating the game state
    public MoveResult movePiece(int startRow, int startCol, int endRow, int endCol) {
        ChessPiece movingPiece = board[startRow][startCol];

        if (movingPiece == null) {
            return new MoveResult(false, "No piece at the selected starting position.");
        }

        if (!isCorrectTurn(movingPiece)) {
            return new MoveResult(false, "It's not your turn.");
        }

        String validationError = validateMove(movingPiece, endRow, endCol);
        if (validationError != null) {
            boolean animateKing = false;
            if ("Invalid move: You cannot leave your king in check!".equals(validationError)) {
                animateKing = true;
            }
            return new MoveResult(false, validationError, animateKing);
        }

        lastMoveDetails = new MoveDetails();
        lastMoveDetails.setMovingPiece(movingPiece);

        // Handle castling
        if (movingPiece instanceof King && Math.abs(endCol - startCol) == 2) {
            // Perform castling
            performCastling((King) movingPiece, endRow, endCol);
            movingPiece.setHasMoved(true);
            lastMoveDetails.setCastlingOccurred(true); // Optionally track castling
        } else {
            handleCapture(movingPiece, endRow, endCol);

            board[endRow][endCol] = movingPiece;
            board[startRow][startCol] = null;
            movingPiece.setPosition(endRow, endCol);

            if (movingPiece instanceof Pawn) {
                Pawn pawn = (Pawn) movingPiece;
                int rowDiff = endRow - startRow;
                pawn.setMovedTwoSquaresLastTurn(Math.abs(rowDiff) == 2);

                if ((pawn.getColor() == PieceColor.WHITE && endRow == 0) ||
                        (pawn.getColor() == PieceColor.BLACK && endRow == 7)) {

                    lastMoveDetails.setPawnPromoted(true);
                    lastMoveDetails.setPromotionPosition(endRow, endCol);
                    return new MoveResult(true, "Pawn promotion pending.");
                }
            }
        }

        movingPiece.setHasMoved(true);

        switchTurn();

        resetEnPassantFlags(movingPiece.getColor());

        return new MoveResult(true);
    }

    private void performCastling(King king, int endRow, int endCol) {
        int startRow = king.getRow();
        int startCol = king.getCol();
        int direction = endCol - startCol > 0 ? 1 : -1;

        // Move the king
        board[endRow][endCol] = king;
        board[startRow][startCol] = null;
        king.setPosition(endRow, endCol);

        // Move the rook
        int rookStartCol = direction > 0 ? 7 : 0;
        int rookEndCol = endCol - direction;

        ChessPiece rook = board[startRow][rookStartCol];
        board[startRow][rookEndCol] = rook;
        board[startRow][rookStartCol] = null;
        rook.setPosition(startRow, rookEndCol);
        rook.setHasMoved(true);
    }


    private void resetEnPassantFlags(PieceColor color) {
        PieceColor oppositeColor = (color == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        for (ChessPiece[] row : board) {
            for (ChessPiece piece : row) {
                if (piece instanceof Pawn && piece.getColor() == oppositeColor) {
                    ((Pawn) piece).setMovedTwoSquaresLastTurn(false);
                }
            }
        }
    }

    public void switchTurn() {
        isWhiteTurn = !isWhiteTurn;

        // After switching, currentPlayerColor is the player whose turn it now is
        PieceColor currentPlayerColor = isWhiteTurn ? PieceColor.WHITE : PieceColor.BLACK;

        boolean hasLegalMoves = playerHasLegalMoves(currentPlayerColor);
        boolean inCheck = isKingInCheck(currentPlayerColor);

        if (!hasLegalMoves) {
            gameOver = true;
            if (inCheck) {
                // Checkmate
                isCheckmate = true;
                winner = (currentPlayerColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            } else {
                // Stalemate
                isStalemate = true;
            }
        }
    }

    public boolean isCorrectTurn(ChessPiece piece) {
        return isWhiteTurn == (piece.getColor() == PieceColor.WHITE);
    }

    public String validateMove(ChessPiece movingPiece, int endRow, int endCol) {
        if (!movingPiece.isValidMove(endRow, endCol, this)) {
            return "Invalid move: This move is not permitted for the selected piece.";
        }

        if (wouldLeaveKingInCheck(movingPiece, endRow, endCol)) {
            return "Invalid move: You cannot leave your king in check!";
        }

        // Check for capturing own piece (not applicable for castling as king moves to empty square)
        ChessPiece targetPiece = board[endRow][endCol];
        if (targetPiece != null && movingPiece.getColor() == targetPiece.getColor()) {
            return "Invalid move: Cannot capture your own piece.";
        }

        return null;
    }


    private boolean wouldLeaveKingInCheck(ChessPiece movingPiece, int endRow, int endCol) {
        int startRow = movingPiece.getRow();
        int startCol = movingPiece.getCol();
        ChessPiece targetPiece = board[endRow][endCol];

        boolean hasMoved = movingPiece.hasMoved();
        boolean targetCaptured = targetPiece != null && targetPiece.isCaptured();

        boolean movedTwoSquaresLastTurn = false;
        if (movingPiece instanceof Pawn) {
            movedTwoSquaresLastTurn = ((Pawn) movingPiece).hasMovedTwoSquaresLastTurn();
        }

        board[endRow][endCol] = movingPiece;
        board[startRow][startCol] = null;
        movingPiece.setPosition(endRow, endCol);
        movingPiece.setHasMoved(true);
        if (movingPiece instanceof Pawn) {
            ((Pawn) movingPiece).setMovedTwoSquaresLastTurn(false);
        }
        if (targetPiece != null) {
            targetPiece.setCaptured(true);
        }

        boolean kingInCheck = isKingInCheck(movingPiece.getColor());

        movingPiece.setPosition(startRow, startCol);
        movingPiece.setHasMoved(hasMoved);
        if (movingPiece instanceof Pawn) {
            ((Pawn) movingPiece).setMovedTwoSquaresLastTurn(movedTwoSquaresLastTurn);
        }
        board[startRow][startCol] = movingPiece;
        board[endRow][endCol] = targetPiece;
        if (targetPiece != null) {
            targetPiece.setCaptured(targetCaptured);
        }

        return kingInCheck;
    }

    public boolean isKingInCheck(PieceColor color) {
        ChessPiece king = findKing(color);
        if (king == null) return false;

        return isSquareUnderAttack(king.getRow(), king.getCol(), color);
    }

    public ChessPiece findKing(PieceColor color) {
        for (ChessPiece[] row : board) {
            for (ChessPiece piece : row) {
                if (piece instanceof King && piece.getColor() == color) {
                    return piece;
                }
            }
        }
        return null;
    }

    public boolean isSquareUnderAttack(int row, int col, PieceColor defendingColor) {
        PieceColor attackingColor = (defendingColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        for (ChessPiece[] boardRow : board) {
            for (ChessPiece piece : boardRow) {
                if (piece != null && piece.getColor() == attackingColor) {
                    if (piece.canAttackSquare(row, col, this)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean playerHasLegalMoves(PieceColor playerColor) {
        for (ChessPiece[] boardRow : board) {
            for (ChessPiece piece : boardRow) {
                if (piece != null && piece.getColor() == playerColor) {
                    for (int destRow = 0; destRow < 8; destRow++) {
                        for (int destCol = 0; destCol < 8; destCol++) {
                            ChessPiece targetPiece = board[destRow][destCol];
                            if (targetPiece != null && targetPiece.getColor() == playerColor) {
                                continue; // Can't capture your own piece
                            }
                            if (piece.isValidMove(destRow, destCol, this)) {
                                if (!wouldLeaveKingInCheck(piece, destRow, destCol)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    // Capture handling including En Passant
    private void handleCapture(ChessPiece movingPiece, int endRow, int endCol) {
        ChessPiece targetPiece = board[endRow][endCol];
        lastMoveDetails.setCapturedPiece(null);
        lastMoveDetails.setEnPassantOccurred(false);

        if (movingPiece instanceof Pawn) {
            Pawn pawn = (Pawn) movingPiece;
            if (pawn.isEnPassantMove(endRow, endCol, this)) {
                int capturedPawnRow = movingPiece.getColor() == PieceColor.WHITE ? endRow + 1 : endRow - 1;
                ChessPiece capturedPawn = board[capturedPawnRow][endCol];
                board[capturedPawnRow][endCol] = null;
                capturedPawn.setCaptured(true);
                lastMoveDetails.setCapturedPiece(capturedPawn);
                lastMoveDetails.setEnPassantOccurred(true);

                if (capturedPawn.getColor() == PieceColor.WHITE) {
                    capturedPiecesBlack.add(capturedPawn);
                } else {
                    capturedPiecesWhite.add(capturedPawn);
                }
            }
        }

        if (targetPiece != null && targetPiece.getColor() != movingPiece.getColor()) {
            targetPiece.setCaptured(true);
            lastMoveDetails.setCapturedPiece(targetPiece);

            if (targetPiece.getColor() == PieceColor.WHITE) {
                capturedPiecesBlack.add(targetPiece);
            } else {
                capturedPiecesWhite.add(targetPiece);
            }
        }
    }

    // Clone method for deep copying
    @Override
    public ChessGameLogic clone() throws CloneNotSupportedException {
        ChessGameLogic clonedGame = (ChessGameLogic) super.clone();
        clonedGame.board = new ChessPiece[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (this.board[row][col] != null) {
                    clonedGame.board[row][col] = this.board[row][col].clone();
                }
            }
        }
        clonedGame.capturedPiecesWhite = new ArrayList<>(this.capturedPiecesWhite);
        clonedGame.capturedPiecesBlack = new ArrayList<>(this.capturedPiecesBlack);
        return clonedGame;
    }

    public boolean isPathClear(int startRow, int startCol, int endRow, int endCol) {
        int rowDirection = Integer.compare(endRow, startRow);
        int colDirection = Integer.compare(endCol, startCol);

        int currentRow = startRow + rowDirection;
        int currentCol = startCol + colDirection;

        while (currentRow != endRow || currentCol != endCol) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowDirection;
            currentCol += colDirection;
        }
        return true;
    }
}
