package com.chess.democracy.edition;

import javafx.event.ActionEvent;

public interface SceneChanger {
     void changeScene(ActionEvent event, String fxmlFile, String title);
}
