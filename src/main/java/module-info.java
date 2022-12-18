module game.pexesofx {
    requires javafx.controls;
    requires javafx.fxml;


    opens game.pexesofx to javafx.fxml;
    exports game.pexesofx;
}