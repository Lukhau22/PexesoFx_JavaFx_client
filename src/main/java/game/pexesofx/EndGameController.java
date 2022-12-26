package game.pexesofx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class EndGameController {

    @FXML
    private Button EndBotton;

    @FXML
    private Label EvaluationLable;

    @FXML
    private Label GameOverLabel;

    @FXML
    private Button ReplayButton;

    @FXML
    private ImageView smile;

    @FXML
    private Label yourScore;

    @FXML
    private Label OpponentScore;

    private Main main;

    private ClientSelect client;

    public void setMain(Main main) {
        this.main = main;
    }

    public void setClient(ClientSelect client) {
        this.client = client;
    }

    @FXML
    public void handleReplayButton(/*MouseEvent event*/) {
        client.sendRejoin();
        client.setState(ClientSelect.State.JOIN_EVALUATION);
        main.switchToWaitScreen();
        //opravit stavy
        //poslat zpravu na server
    }

    @FXML
    public void handleEndBotton(/*MouseEvent event*/) {
        client.sendExit();
        System.exit(0);
    }

    public void setYourScore(String yourScore) {
        this.yourScore.setText(yourScore);
    }

    public void setOpponentScore(String opponentScore) {
        OpponentScore.setText(opponentScore);
    }

    public void setEvaluationLable(String result) {
        EvaluationLable.setText(result);
    }

    public void setColorRedEvaluationLable() {
        EvaluationLable.setTextFill(Color.RED);
    }

    public void setColorOrangeEvaluationLable() {
        EvaluationLable.setTextFill(Color.ORANGE);
    }

    public void setColorGreenEvaluationLable() {
        EvaluationLable.setTextFill(Color.GREEN);
    }

    public void setSmile(String pictureName) {
        Image image = new Image(getClass().getResourceAsStream(pictureName));
        this.smile.setImage(image);
    }
}