package game.pexesofx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class LoginWindowConntroller {
    private Main main;
    @FXML
    private TextField ipAddress;

    @FXML
    private TextField userName;

    @FXML
    private Button loginButton;

    @FXML
    public Label errorLabel;

    @FXML
    private TextField Port;

    public void setErrorLabel(String error) {
        this.errorLabel.setText(error);
    }

    public void setMain(Main main) {
        this.main = main;
    }

    @FXML
    void handleLoginButton(MouseEvent event) {
        main.player_user_name = userName.getText();
        if(!ipAddress.getText().isEmpty()){
            main.serverAddress = ipAddress.getText();
        }
        else{
            System.out.println("Client used default ip address 127.0.0.1");
        }

        if(!Port.getText().isEmpty()){
            main.port =  Integer. valueOf(Port.getText());
        }
        else{
            System.out.println("Client used default port 10000");
        }

        if(main.player_user_name.equals("")){
            System.out.println("Error empty login");
            main.loginErrorLabel = "Username must be filled in.";
            main.switchToLogin();
        }else if (main.player_user_name.equals("Undif")){
            System.out.println("Error Undif login");
            main.loginErrorLabel = "The filled username already exists. Choose another username.";
            main.switchToLogin();
        }
        else {
            main.client.connectAndLogin(main.player_user_name);
        }
    }

}
