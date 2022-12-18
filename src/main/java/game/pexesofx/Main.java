package game.pexesofx;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main extends Application {
    public String player_user_name;

    public String serverAddress = "127.0.0.1";

    ImageController images;
    private Stage primaryStage;
    public ClientSelect client;

    public String loginErrorLabel = "";



    public static void main(String[] args) {launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Java fx client start");
        this.primaryStage = primaryStage;
        client = new ClientSelect();
        client.setMain(Main.this);
        switchToLogin();
    }

    public void switchToLogin() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("loginWindow.fxml"));
        Parent root = null;
        try {
            root = (Parent)loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LoginWindowConntroller controller = loader.getController();
        controller.setErrorLabel(loginErrorLabel);
        controller.setMain(this);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
//        primaryStage.setTitle("Pexeso Client Welcome");
//        GridPane grid = new GridPane();
//        grid.setAlignment(Pos.CENTER);
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setPadding(new Insets(25, 25, 25, 25));
//
//        Text scenetitle = new Text("Welcome");
//        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
//        grid.add(scenetitle, 0, 0, 2, 1);
//
//        Label userName = new Label("User Name:");
//        grid.add(userName, 0, 1);
//
//        TextField userTextField = new TextField();
//        grid.add(userTextField, 1, 1);
//
//        Button btn = new Button("Sign in");
//        HBox hbBtn = new HBox(10);
//        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
//        hbBtn.getChildren().add(btn);
//        grid.add(hbBtn, 1, 4);
//
//        final Text actiontarget = new Text();
//        grid.add(actiontarget, 1, 6);
//
//        btn.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent e) {
//                player_user_name = userTextField.getText();
//                if(player_user_name.equals("Undif")){
//                    switchToLogin();
//                }
//                else {
//                    actiontarget.setFill(Color.FIREBRICK);
//                    actiontarget.setText("Sign in button pressed " + player_user_name); //get text obsahuje zadane username
//                    client = new ClientSelect();
//                    client.setMain(Main.this);
//                    client.connectAndLogin(player_user_name);
//
//                }
//            }
//        });
//        Scene scene = new Scene(grid, 300, 275);
//        primaryStage.setScene(scene);
//        primaryStage.show();
    }


    public void switchToEndGame() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("endGame.fxml"));
        Parent root = null;
        try {
            root = (Parent)loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        EndGameController controller = loader.getController();
        controller.setMain(this);
        controller.setClient(client);
        controller.setYourScore("Your score: "+ images.getMyScore());
        controller.setOpponentScore("Opponent score: " + images.getOpponentScore());
        int myScoreNum = Integer.valueOf(images.getMyScore());
        int opponentScoreNum = Integer.valueOf(images.getOpponentScore());
        if(myScoreNum == opponentScoreNum){
            controller.setEvaluationLable("DRAW");
            controller.setColorOrangeEvaluationLable();
            controller.setSmile("drawSmile.jpg");
        } else if (myScoreNum > opponentScoreNum) {
            controller.setEvaluationLable("WIN");
            controller.setColorGreenEvaluationLable();
            controller.setSmile("winSmile.jpg");
        }else{
            controller.setEvaluationLable("LOSS");
            controller.setColorRedEvaluationLable();
            controller.setSmile("lossSmile.jpg");
        }

        primaryStage.setScene(new Scene(root));
    }

    public void switchToWaitScreen() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("waitScreen.fxml"));
        Parent root = null;
        try {
            root = (Parent)loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        WaitScreenController controller = loader.getController();
        controller.setMain(this);
        primaryStage.setScene(new Scene(root));
    }


    public void switchToGameImages() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameImages.fxml"));
        Parent root = null;
        try {
            root = (Parent)loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImageController controller = loader.getController();
        controller.initialize();
        controller.setMain(this);
        controller.setPlayer1Name(player_user_name);
        images = controller;

        client.setImageController(controller);
        controller.setClient(client);
        primaryStage.setScene(new Scene(root));

    }

    public void loginError(IOException e) {

    }

    public void loginOk() {
        switchToWaitScreen();
    }



}
