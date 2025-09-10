package com.chess.democracy.edition;

import com.chess.democracy.edition.chess.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import java.io.IOException;
import java.util.Objects;

public class ChessboardController extends LoggedInController {

    @FXML
    public ListView outputFieldChat;
    @FXML
    private Label profileLabel;
    @FXML
    private GridPane chessGrid;
    @FXML
    private Label gameMessageLabel;
    @FXML
    private GridPane scoreGridTeamBlack;
    @FXML
    private GridPane scoreGridTeamWhite;
    @FXML
    private Label teamName;
    @FXML
    private Label gameIText;
    @FXML
    private TextField inputFieldChat;
    @FXML
    private Button sendButton;
    @FXML
    private Button voteButton;

    private int gameID;
    private ChessGameLogic chessGame;

    // Chat-related fields
    private Socket chatSocket;
    private PrintWriter chatOut;
    private BufferedReader chatIn;
    private ExecutorService chatExecutor;

    // Server settings
    private String serverAddress = "localhost"; // Default value
    private int serverPort = 1024; // Default value

    // Sound effects
    private AudioClip legalMoveSound;
    private AudioClip illegalMoveSound;
    private AudioClip castlingSound;
    private AudioClip checkmateSound;
    private AudioClip captureSound;
    private AudioClip gameStartSound;

    // Utility method to display game messages
    private ScaleTransition infiniteScaleTransition;
    private Timeline infiniteColorTimeline;

    // Constants
    private static final double TILE_SIZE = 112.5;
    private static final Color LIGHT_TILE_COLOR = Color.WHITE;
    private static final Color DARK_TILE_COLOR = Color.web("#0f8aac");
    private static final Font LABEL_FONT = Font.font("Berlin Sans FB Demi Bold", FontWeight.BOLD, 16.0);

    // Setter for gameID
    public void setGameID(int gameID) {
        this.gameID = gameID;
        gameIText.setText(String.valueOf(gameID));
        initializeGame();
    }

    // Initialization
    public void initialize() {
        profileLabel.setText(DBUtility.CurrentSignedInUser.getName());
        chessGame = new ChessGameLogic();
        setupBoard();
        addChessPieces();
        loadSoundEffects();
        playGameStartSound();

        // Connect to chat server
        connectToChatServer();

        // Start listening for incoming chat messages
        chatExecutor = Executors.newSingleThreadExecutor();
        chatExecutor.execute(this::listenForChatMessages);
    }

    private void loadSoundEffects() {
        legalMoveSound = new AudioClip(getClass().getResource("/com/chess/democracy/edition/sound/legal_move.mp3").toExternalForm());
        illegalMoveSound = new AudioClip(getClass().getResource("/com/chess/democracy/edition/sound/illegal_move.mp3").toExternalForm());
        castlingSound = new AudioClip(getClass().getResource("/com/chess/democracy/edition/sound/castling.mp3").toExternalForm());
        checkmateSound = new AudioClip(getClass().getResource("/com/chess/democracy/edition/sound/checkmate.mp3").toExternalForm());
        captureSound = new AudioClip(getClass().getResource("/com/chess/democracy/edition/sound/capture.mp3").toExternalForm());
        gameStartSound = new AudioClip(getClass().getResource("/com/chess/democracy/edition/sound/game_start.mp3").toExternalForm());
    }

    private void playLegalMoveSound() {
        legalMoveSound.play();
    }

    private void playGameStartSound() {
        gameStartSound.play();
    }

    private void playIllegalMoveSound() {
        illegalMoveSound.play();
    }

    private void playCastlingSound() {
        castlingSound.play();
    }

    private void playCheckmateSound() {
        checkmateSound.play();
    }

    private void playCaptureSound() {
        captureSound.play();
    }

    // Additional initialization after gameID is set
    private void initializeGame() {
        if (gameID == 0) {
            updateGameMessage("Error: Game ID not set." , false);
            return;
        }

        // Get the user's team
        String team = DBUtility.getUserTeam(gameID, DBUtility.CurrentSignedInUser.getUserID());
        teamName.setText("Team " + capitalizeFirstLetter(team));

        // Display waiting message
        updateGameMessage("Waiting for other players...", false);
    }

    // Utility method to capitalize the first letter of the team name
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public void changeScene(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if ("Chessboard.fxml".equals(fxmlFile)) {
                ChessboardController controller = loader.getController();
                controller.setGameID(gameID);
            } else if ("Dashboard.fxml".equals(fxmlFile)) {
                DashboardController controller = loader.getController();
            } else if ("Login.fxml".equals(fxmlFile)) {
                LoginController controller = loader.getController();
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1700, 1000));
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectToChatServer() {
        try {
            chatSocket = new Socket(serverAddress, serverPort);
            chatOut = new PrintWriter(chatSocket.getOutputStream(), true);
            chatIn = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));

            // Send initial info: gameID and userName
            String initInfo = gameID + ":" + DBUtility.CurrentSignedInUser.getName();
            chatOut.println(initInfo);
        } catch (IOException e) {
            System.err.println("Failed to connect to chat server: " + e.getMessage());
            updateGameMessage("Failed to connect to chat server.", false);
        }
    }
    // Optional method to set server details if needed
    public void setServerDetails(String ip, int port) {
        this.serverAddress = ip;
        this.serverPort = port;
    }

    private void listenForChatMessages() {
        String message;
        try {
            while ((message = chatIn.readLine()) != null) {
                String finalMessage = message;
                Platform.runLater(() -> outputFieldChat.getItems().add(finalMessage));
            }
        } catch (IOException e) {
            System.err.println("Error reading from chat server: " + e.getMessage());
        } finally {
            closeChatConnection();
        }
    }

    private void closeChatConnection() {
        try {
            if (chatSocket != null && !chatSocket.isClosed()) {
                chatSocket.close();
            }
            if (chatExecutor != null && !chatExecutor.isShutdown()) {
                chatExecutor.shutdownNow();
            }
        } catch (IOException e) {
            // Ignore
        }
    }




    // Board setup methods
    private void setupBoard() {
        chessGrid.getChildren().clear();
        chessGrid.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.6)));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane cell = createCell(row, col);
                chessGrid.add(cell, col, row);
            }
        }
    }

    private StackPane createCell(int row, int col) {
        Rectangle tileRectangle = new Rectangle(TILE_SIZE, TILE_SIZE);
        tileRectangle.setFill((row + col) % 2 == 0 ? LIGHT_TILE_COLOR : DARK_TILE_COLOR);

        StackPane cell = new StackPane(tileRectangle);
        addGuidingLabels(cell, row, col);

        cell.setOnDragOver(this::handleDragOver);
        cell.setOnDragDropped(this::handleDragDropped);

        return cell;
    }

    private void addGuidingLabels(StackPane cell, int row, int col) {
        String textColor = ((row + col) % 2 == 0) ? "black" : "white";

        if (col == 0) {
            Label rankLabel = createGuidingLabel(String.valueOf(8 - row), textColor, Pos.TOP_LEFT);
            StackPane.setMargin(rankLabel, new Insets(5, 0, 0, 5));
            cell.getChildren().add(rankLabel);
        }

        if (row == 7) {
            Label fileLabel = createGuidingLabel(String.valueOf((char) ('a' + col)), textColor, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(fileLabel, new Insets(0, 5, 5, 0));
            cell.getChildren().add(fileLabel);
        }
    }

    private Label createGuidingLabel(String text, String textColor, Pos position) {
        Label label = new Label(text);
        label.setFont(LABEL_FONT);
        label.setStyle("-fx-text-fill: " + textColor + ";");
        label.setOpacity(0.4);
        StackPane.setAlignment(label, position);
        return label;
    }

    // Methods to add chess pieces to the board
    private void addChessPieces() {
        ChessPiece[][] board = chessGame.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null) {
                    addPiece(piece);
                }
            }
        }
    }

    private void addPiece(ChessPiece chessPiece) {
        String imagePath = getPieceImagePath(chessPiece);
        ImageView pieceImageView = createPieceImageView(new Image(imagePath), chessPiece);

        StackPane cell = getCellAt(chessPiece.getRow(), chessPiece.getCol());
        cell.getChildren().add(pieceImageView);
    }

    private String getPieceImagePath(ChessPiece piece) {
        String typeName = piece.getClass().getSimpleName().toLowerCase();
        String imageFileName = String.format(
                "/com/chess/democracy/edition/images/piece/%s-%s.png",
                typeName,
                piece.getColor().getAbbreviation()
        );
        return Objects.requireNonNull(getClass().getResource(imageFileName)).toExternalForm();
    }

    private ImageView createPieceImageView(Image image, ChessPiece chessPiece) {
        ImageView pieceImageView = new ImageView(image);
        pieceImageView.setFitWidth(TILE_SIZE * 0.8);
        pieceImageView.setPreserveRatio(true);
        pieceImageView.setUserData(chessPiece);

        setupPieceEventHandlers(pieceImageView);

        return pieceImageView;
    }

    private void setupPieceEventHandlers(ImageView pieceImageView) {
        pieceImageView.setOnDragDetected(event -> handleDragDetected(event, pieceImageView));
        pieceImageView.setOnDragDone(DragEvent::consume);

        pieceImageView.setOnMouseEntered(event -> {
            pieceImageView.setFitWidth(TILE_SIZE * 0.96);
            pieceImageView.getScene().setCursor(Cursor.HAND);
        });

        pieceImageView.setOnMouseExited(event -> {
            pieceImageView.setFitWidth(TILE_SIZE * 0.8);
            pieceImageView.getScene().setCursor(Cursor.DEFAULT);
        });
    }

    // Event handling methods
    private void handleDragDetected(MouseEvent event, ImageView pieceImageView) {
        if (chessGame.isGameOver()) {
            updateGameMessage("The game is over." ,false);
            return;
        }
        ChessPiece chessPiece = (ChessPiece) pieceImageView.getUserData();
        if (!chessGame.isCorrectTurn(chessPiece)) {
            updateGameMessage("It's not your team's turn!", false);
            return;
        }

        Dragboard db = pieceImageView.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putImage(pieceImageView.getImage());
        db.setContent(content);

        event.consume();
    }

    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != event.getGestureTarget() && event.getDragboard().hasImage()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        boolean success = false;
        Dragboard db = event.getDragboard();

        if (db.hasImage()) {
            ImageView sourcePieceImageView = (ImageView) event.getGestureSource();
            ChessPiece movingPiece = (ChessPiece) sourcePieceImageView.getUserData();

            Node targetNode = event.getPickResult().getIntersectedNode();
            int[] targetPosition = getTargetPosition(targetNode);

            if (targetPosition != null) {
                int endRow = targetPosition[0];
                int endCol = targetPosition[1];

                int startRow = movingPiece.getRow();
                int startCol = movingPiece.getCol();

                MoveResult moveResult = chessGame.movePiece(startRow, startCol, endRow, endCol);
                if (moveResult.isSuccess()) {
                    movePieceImageView(sourcePieceImageView, startRow, startCol, endRow, endCol);
                    handleCaptures(movingPiece, endRow, endCol);

                    // Play sound based on the move
                    if (chessGame.getLastMoveDetails().isCastlingOccurred()) {
                        playCastlingSound();
                    } else if (chessGame.getLastMoveDetails().getCapturedPiece() != null) {
                        playCaptureSound();
                    } else {
                        playLegalMoveSound();
                    }

                    if (chessGame.getLastMoveDetails().isPawnPromoted()) {
                        promptPawnPromotion(movingPiece.getColor());
                    } else {
                        switchTurn();
                    }

                    success = true;
                } else {
                    updateGameMessage(moveResult.getErrorMessage(), false);
                    playIllegalMoveSound();

                    if (moveResult.shouldAnimateKing()) {
                        ChessPiece king = chessGame.findKing(movingPiece.getColor());
                        animateKingInCheck(king);
                    }
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }


    private void promptPawnPromotion(PieceColor color) {
        chessGrid.setDisable(true);
        updateGameMessage("Pawn promotion! Choose a piece: 1 - Queen, 2 - Rook, 3 - Bishop, 4 - Knight", false);
    }

    private void handlePawnPromotionInput(String input) {
        int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            updateGameMessage("Invalid input! Please enter a number between 1 and 4.", false);
            return;
        }

        if (choice < 1 || choice > 4) {
            updateGameMessage("Invalid choice! Please enter a number between 1 and 4.", false);
            return;
        }

        int row = chessGame.getLastMoveDetails().getPromotionRow();
        int col = chessGame.getLastMoveDetails().getPromotionCol();

        // Get the pawn at the promotion position
        ChessPiece pawn = chessGame.getBoard()[row][col];
        if (pawn == null || !(pawn instanceof Pawn)) {
            updateGameMessage("Error: No pawn to promote at the expected position.", false);
            return;
        }
        PieceColor color = pawn.getColor();

        ChessPiece newPiece;
        switch (choice) {
            case 1:
                newPiece = new Queen(color, row, col);
                break;
            case 2:
                newPiece = new Rook(color, row, col);
                break;
            case 3:
                newPiece = new Bishop(color, row, col);
                break;
            case 4:
                newPiece = new Knight(color, row, col);
                break;
            default:
                return;
        }

        // Replace the pawn with the new piece on the board
        chessGame.getBoard()[row][col] = newPiece;

        // Remove the pawn's ImageView from the UI
        ImageView pieceImageView = getPieceImageViewAt(row, col);
        if (pieceImageView != null) {
            StackPane cell = getCellAt(row, col);
            cell.getChildren().remove(pieceImageView);
        }

        // Add the new piece's ImageView to the UI
        addPiece(newPiece);

        // Reset the promotion flag and re-enable the chessboard
        chessGame.getLastMoveDetails().setPawnPromoted(false);
        chessGrid.setDisable(false);

        // Switch turns now that promotion is complete
        switchTurn();
    }


    private void handleCaptures(ChessPiece movingPiece, int endRow, int endCol) {
        MoveDetails moveDetails = chessGame.getLastMoveDetails();
        if (moveDetails.getCapturedPiece() != null && moveDetails.getCapturedPiece().isCaptured()) {
            ImageView capturedPieceImageView;
            if (moveDetails.isEnPassantOccurred()) {
                int capturedPawnRow = (movingPiece.getColor() == PieceColor.WHITE) ? endRow + 1 : endRow - 1;
                capturedPieceImageView = getPieceImageViewAt(capturedPawnRow, endCol);
                if (capturedPieceImageView != null) {
                    removeCapturedPiece(capturedPieceImageView, moveDetails.getCapturedPiece());
                }
            } else {
                capturedPieceImageView = getPieceImageViewAt(endRow, endCol);
                if (capturedPieceImageView != null) {
                    removeCapturedPiece(capturedPieceImageView, moveDetails.getCapturedPiece());
                }
            }
        }
    }

    private int[] getTargetPosition(Node node) {
        while (node != null && !(node instanceof StackPane)) {
            node = node.getParent();
        }
        if (node != null) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            if (rowIndex != null && colIndex != null) {
                return new int[]{rowIndex, colIndex};
            }
        }
        return null;
    }

    private void movePieceImageView(ImageView pieceImageView, int startRow, int startCol, int endRow, int endCol) {
        StackPane oldCell = getCellAt(startRow, startCol);
        StackPane newCell = getCellAt(endRow, endCol);

        oldCell.getChildren().remove(pieceImageView);
        newCell.getChildren().add(pieceImageView);

        // Update piece's position
        ChessPiece movingPiece = (ChessPiece) pieceImageView.getUserData();
        movingPiece.setPosition(endRow, endCol);

        // Handle castling UI update
        if (movingPiece instanceof King && Math.abs(endCol - startCol) == 2) {
            int direction = endCol - startCol > 0 ? 1 : -1;
            int rookStartCol = direction > 0 ? 7 : 0;
            int rookEndCol = endCol - direction;

            // Get the rook's ImageView
            ImageView rookImageView = getPieceImageViewAt(startRow, rookStartCol);
            if (rookImageView != null) {
                StackPane rookOldCell = getCellAt(startRow, rookStartCol);
                StackPane rookNewCell = getCellAt(startRow, rookEndCol);

                rookOldCell.getChildren().remove(rookImageView);
                rookNewCell.getChildren().add(rookImageView);

                // Update rook's position
                ChessPiece rookPiece = (ChessPiece) rookImageView.getUserData();
                rookPiece.setPosition(startRow, rookEndCol);
            }
        }
    }


    private ImageView getPieceImageViewAt(int row, int col) {
        StackPane cell = getCellAt(row, col);
        if (cell != null) {
            for (Node node : cell.getChildren()) {
                if (node instanceof ImageView) {
                    return (ImageView) node;
                }
            }
        }
        return null;
    }

    private void removeCapturedPiece(ImageView pieceImageView, ChessPiece capturedPiece) {
        StackPane cell = getCellAt(capturedPiece.getRow(), capturedPiece.getCol());
        if (cell != null) {
            cell.getChildren().remove(pieceImageView);
        }

        GridPane scoreGrid = (capturedPiece.getColor() == PieceColor.WHITE) ? scoreGridTeamBlack : scoreGridTeamWhite;
        addCapturedPieceToScoreGrid(capturedPiece, scoreGrid);
    }

    private void addCapturedPieceToScoreGrid(ChessPiece capturedPiece, GridPane scoreGrid) {
        String imagePath = getPieceImagePath(capturedPiece);
        ImageView capturedPieceImageView = new ImageView(new Image(imagePath));
        capturedPieceImageView.setFitWidth(scoreGrid.getPrefWidth() / 8);
        capturedPieceImageView.setPreserveRatio(true);

        int cellIndex = scoreGrid.getChildren().size();
        int col = cellIndex % 8;
        int row = cellIndex / 8;

        scoreGrid.add(capturedPieceImageView, col, row);
        GridPane.setHalignment(capturedPieceImageView, HPos.CENTER);
        GridPane.setValignment(capturedPieceImageView, VPos.CENTER);
    }

    private void updateGameMessage(String message, boolean loopIndefinitely) {
        Platform.runLater(() -> {
            gameMessageLabel.setText(message);

            // Stop any existing infinite animations
            if (infiniteScaleTransition != null && infiniteScaleTransition.getStatus() == Animation.Status.RUNNING) {
                infiniteScaleTransition.stop();
            }
            if (infiniteColorTimeline != null && infiniteColorTimeline.getStatus() == Animation.Status.RUNNING) {
                infiniteColorTimeline.stop();
            }

            // ScaleTransition for the "pop" effect
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.4), gameMessageLabel);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.2);
            scaleTransition.setToY(1.2);
            scaleTransition.setAutoReverse(true);



            if (loopIndefinitely) {
                scaleTransition.setCycleCount(20 * 2); // Multiply by 2 because of auto-reverse
                infiniteScaleTransition = scaleTransition;

                // Looping color transition
                Timeline colorTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(gameMessageLabel.textFillProperty(), Color.GREEN)),
                        new KeyFrame(Duration.seconds(0.4), new KeyValue(gameMessageLabel.textFillProperty(), Color.BLACK)),
                        new KeyFrame(Duration.seconds(0.8), new KeyValue(gameMessageLabel.textFillProperty(), Color.GREEN))
                );
                colorTimeline.setCycleCount(20);
                colorTimeline.play();
                infiniteColorTimeline = colorTimeline;
                chessGrid.setOpacity(0.9);
            } else {
                scaleTransition.setCycleCount(2);

                // Simple color transition
                Timeline colorTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(gameMessageLabel.textFillProperty(), Color.GREEN)),
                        new KeyFrame(Duration.seconds(0.2), new KeyValue(gameMessageLabel.textFillProperty(), Color.BLACK))
                );
                colorTimeline.play();
            }

            // Play the scale animation
            scaleTransition.play();
        });
    }


    private void disableAllPieces() {
        chessGrid.setDisable(true);
    }

    private void animateKingInCheck(ChessPiece king) {
        Platform.runLater(() -> {
            ImageView kingImageView = getPieceImageViewAt(king.getRow(), king.getCol());
            if (kingImageView != null) {
                kingImageView.setTranslateX(0);
                kingImageView.setTranslateY(0);

                TranslateTransition bounce = new TranslateTransition(Duration.seconds(0.25), kingImageView);
                bounce.setByY(-20);
                bounce.setCycleCount(10);
                bounce.setAutoReverse(true);
                bounce.play();
            }
        });
    }


    private void switchTurn() {
        if (chessGame.isGameOver()) {
            if (chessGame.isCheckmate()) {
                String message = "Checkmate! Team " + chessGame.getWinner() + " wins!";
                updateGameMessage(message, true);
                disableAllPieces();
                // Play checkmate sound
                playCheckmateSound();
            } else if (chessGame.isStalemate()) {
                String message = "Stalemate! The game is a draw.";
                updateGameMessage(message, true);
                disableAllPieces();
            }
        } else {
            // Existing code for switching turns
            PieceColor currentPlayerColor = chessGame.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK;
            if (chessGame.isKingInCheck(currentPlayerColor)) {
                updateGameMessage("Your king is in check! Protect your king.", false);
                ChessPiece king = chessGame.findKing(currentPlayerColor);
                animateKingInCheck(king);
            } else {
                updateGameMessage("It's Team " + currentPlayerColor + "'s turn.", false);
            }
        }
    }


    private StackPane getCellAt(int row, int col) {
        for (Node node : chessGrid.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (StackPane) node;
            }
        }
        return null;
    }

    public void quit(ActionEvent event) {
        int userID = DBUtility.CurrentSignedInUser.getUserID();

        // Remove the user from the game
        DBUtility.removePlayerFromGame(gameID, userID);

        // Check if the current user is the creator of the game
        if (DBUtility.isUserGameCreator(gameID, userID)) {
            // Delete the game
            DBUtility.deleteGame(gameID);
        }

        // Navigate back to the Dashboard
        changeScene(event, "Dashboard.fxml", "Dashboard");
    }

    // Action methods for vote
    public void vote(ActionEvent event) {
        // Implement vote functionality here
    }


    /**
    public void send(ActionEvent event) {
        String input = inputFieldChat.getText().trim();
        inputFieldChat.clear();

        if (chessGame.getLastMoveDetails() != null && chessGame.getLastMoveDetails().isPawnPromoted()) {
            handlePawnPromotionInput(input);
        } else {
            String messageContent = inputFieldChat.getText().trim();
            if (!messageContent.isEmpty()) {
                chatOut.println(messageContent);
                inputFieldChat.clear();
            }
        }

        inputFieldChat.clear();
    }
     **/

    // Action methods for send
    public void send(ActionEvent event) {
        String messageContent = inputFieldChat.getText().trim();
        if (!messageContent.isEmpty()) {
            chatOut.println(messageContent);
            inputFieldChat.clear();
        }
    }
}
