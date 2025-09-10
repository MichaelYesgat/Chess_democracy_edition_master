module com.chess.democracy.edition {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;
    requires java.desktop;


    opens com.chess.democracy.edition to javafx.fxml;
    exports com.chess.democracy.edition;
    exports com.chess.democracy.edition.chess;
    opens com.chess.democracy.edition.chess to javafx.fxml;
}