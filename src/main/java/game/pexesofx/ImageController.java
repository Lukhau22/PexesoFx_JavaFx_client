package game.pexesofx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ImageController {

    @FXML
    private Label player1;

    @FXML
    private Label player2;


    @FXML
    private ImageView image10;

    @FXML
    private ImageView image11;

    @FXML
    private ImageView image12;

    @FXML
    private ImageView image13;

    @FXML
    private ImageView image20;

    @FXML
    private ImageView image21;

    @FXML
    private ImageView image22;

    @FXML
    private ImageView image23;

    @FXML
    private ImageView image30;

    @FXML
    private ImageView image31;

    @FXML
    private ImageView image32;

    @FXML
    private ImageView image33;

    @FXML
    private ImageView image00;

    @FXML
    private ImageView image01;

    @FXML
    private ImageView image02;

    @FXML
    private ImageView image03;

    @FXML
    public Label whoPlay;

    @FXML
    private Label myScore;

    @FXML
    private Label OpponentScore;

    @FXML
    public Label opponentDisconn;

    @FXML
    public Label OppConnect;


    private Main main;

    private ImageView [][] imageBox = new ImageView[4][4];
    private ClientSelect client;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1,
                    r -> {
                        Thread t = Executors.defaultThreadFactory().newThread(r);
                        t.setDaemon(true);
                        return t;
                    });

    private HashSet<Runnable> delayRefresh = new HashSet<>(); //set runnablu

    @FXML
    public void handleUnhidePicture00(/*MouseEvent event*/) {
//        Image image = new Image(getClass().getResourceAsStream("kosky.png"));
//        image00.setImage(image);
        client.sendMove(0,0);

    }
    @FXML
    public void handleUnhidePicture01(/*MouseEvent event*/) {
        client.sendMove(0,1);
    }

    @FXML
    public void handleUnhidePicture02(/*MouseEvent event*/) {
        client.sendMove(0,2);
    }

    @FXML
    public void handleUnhidePicture03(/*MouseEvent event*/) {
        client.sendMove(0,3);
    }

    @FXML
    public void handleUnhidePicture10(/*MouseEvent event*/) {
        client.sendMove(1,0);
    }

    @FXML
    public void handleUnhidePicture11(/*MouseEvent event*/) {
        client.sendMove(1,1);
    }

    @FXML
    public void handleUnhidePicture12(/*MouseEvent event*/) {
        client.sendMove(1,2);
    }

    @FXML
    public void handleUnhidePicture13(/*MouseEvent event*/) {
        client.sendMove(1,3);
    }

    @FXML
    public void handleUnhidePicture20(/*MouseEvent event*/) {
        client.sendMove(2,0);
    }

    @FXML
    public void handleUnhidePicture21(/*MouseEvent event*/) {
        client.sendMove(2,1);
    }

    @FXML
    public void handleUnhidePicture22(/*MouseEvent event*/) {
        client.sendMove(2,2);
    }

    @FXML
    public void handleUnhidePicture23(/*MouseEvent event*/) {
        client.sendMove(2,3);
    }

    @FXML
    public void handleUnhidePicture30(/*MouseEvent event*/) {
        client.sendMove(3,0);
    }

    @FXML
    public void handleUnhidePicture31(/*MouseEvent event*/) {
        client.sendMove(3,1);
    }

    @FXML
    public void handleUnhidePicture32(/*MouseEvent event*/) {
        client.sendMove(3,2);

    }

    @FXML
    public void handleUnhidePicture33(/*MouseEvent event*/) {
        client.sendMove(3,3);
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public void setPlayer1Name(String player_user_name) {
        player1.setText(player_user_name);
    }

    public void setPlayer2Name(String opponent_user_name) {
        player2.setText(opponent_user_name);
    }

    public void setMyScore(String myScore) {
        this.myScore.setText(myScore);
    }

    public void setOpponentScore(String opponentScore) {
        this.OpponentScore.setText(opponentScore);
    }

    public String getMyScore() {
        return myScore.getText();
    }

    public String getOpponentScore() {
        return OpponentScore.getText();
    }

    public void updateFieldsGame(Game pexeso, ClientSelect.State state) {

        if(state == ClientSelect.State.MOVE1_EVALUATION){

        }


    }

    public void initialize() {

        imageBox[0][0] = image00;
        imageBox[0][1] = image01;
        imageBox[0][2] = image02;
        imageBox[0][3] = image03;

        imageBox[1][0] = image10;
        imageBox[1][1] = image11;
        imageBox[1][2] = image12;
        imageBox[1][3] = image13;

        imageBox[2][0] = image20;
        imageBox[2][1] = image21;
        imageBox[2][2] = image22;
        imageBox[2][3] = image23;

        imageBox[3][0] = image30;
        imageBox[3][1] = image31;
        imageBox[3][2] = image32;
        imageBox[3][3] = image33;
    }


    public void changePictures(int row, int col, int value) {
        if(value>=0){
            for (Runnable delayed : delayRefresh) {
                delayed.run();
            }
            delayRefresh.clear();
            setPicture(row, col, value);
            System.out.println("UNHIDE picture value: " + value);
        }else{

            Runnable delayed = new Runnable() {

                @Override
                public void run() {
                    if(delayRefresh.contains(this)) {
                        setPicture(row, col, value);
                    }
                }
            };
            delayRefresh.add(delayed);
            scheduler.schedule(() -> Platform.runLater(delayed), 2, SECONDS);
        }

    }

    private void setPicture(int row, int col, int value) {
        String picture = selectPicture(value);
        Image image = new Image(getClass().getResourceAsStream(picture));
        imageBox[row][col].setImage(image);

    }

    public String selectPicture(int value) {
        String picture;
        switch(value) {
            case 0:
                picture = "Alfa.jpg";
                break;
            case 1:
                picture = "BMW.jpg";
                break;
            case 2:
                picture = "Bugatti.jpg";
                break;
            case 3:
                picture = "Dodge.jpg";
                break;
            case 4:
                picture = "Ferrari.jpg";
                break;
            case 5:
                picture = "Honda.jpg";
                break;
            case 6:
                picture = "Jaguar.jpg";
                break;
            case 7:
                picture = "Lamborghini.jpg";
                break;
            default:
                picture = "backside.jpg";
        }
        return picture;
    }


    public void setClient(ClientSelect client) {
        this.client = client;
    }
}




