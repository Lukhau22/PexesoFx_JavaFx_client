package game.pexesofx;

import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Set;

import static java.lang.Thread.sleep;


public class ClientSelect {

    public static final int PING_INTERVAL_MS = 2000;
    private String loginName;
    private Thread networkThread;
    private Main main;
    private ImageController imageController;
    private SocketChannel client;

    public void setMain(Main main) {
        this.main = main;
    }

    public void setImageController(ImageController imageController) {
        this.imageController = imageController;
    }

    public void sendMove(int row, int col) {
        if(state == State.ON_MOVE1 || state == State.ON_MOVE2 ){
            if(pexeso.exposeMat[row][col] == -1){
                String clientMSG= String.format("MOVE|%d|%d|\n", row, col);
                ByteBuffer toSendByteBuffer = encodeString(clientMSG);
                try {
                    client.write(toSendByteBuffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);// vyresit ze se nepodarilo zapsat
                }
                System.out.println("Send: "+ clientMSG);
                if(state == State.ON_MOVE1){
                    state = State.MOVE1_EVALUATION;
                }
                else if(state == State.ON_MOVE2){
                    state = State.MOVE2_EVALUATION;
                }
            }
        }
    }

    public void sendRejoin() {
        String clientMSG= "JOIN|\n";
        ByteBuffer toSendByteBuffer = encodeString(clientMSG);
        try {
            client.write(toSendByteBuffer);
        } catch (IOException e) {
            throw new RuntimeException(e);// vyresit ze se nepodarilo zapsat
        }
    }




    public enum State{

        LOGIN,
        AFTER_LOGIN,
        JOIN,

        JOIN_EVALUATION,
        ON_MOVE1,
        MOVE1_EVALUATION,
        ON_MOVE2,
        MOVE2_EVALUATION,

        WAITING,

        ERROR, GAME_END

    }

    private State state;

    public void setState(State state) {
        this.state = state;
    }

    private Game pexeso = new Game(0,0,4,4);

    public void connectAndLogin(String loginName) {
        this.loginName = loginName;
        state = State.LOGIN;
        networkThread = new Thread(this::connectionThread);
        networkThread.setDaemon(true);
        networkThread.start();
    }

    public void connectionThread(){

        System.out.println("client start!");
        String clientMSG;
        Selector selector = null;
        SelectionKey readKey;
        String toSend;
        CharBuffer charBuffer;
        ByteBuffer toSendByteBuffer;

        try {
            String serverMsg;
            selector = Selector.open();

            client = SocketChannel.open();
            System.out.println("client try to connect on " + main.serverAddress + " address");

            client.connect(new InetSocketAddress(main.serverAddress, 10000));//"127.0.0.1"
            main.loginErrorLabel = "Invalid ip address";

            client.configureBlocking(false);
            System.out.println("Client conected");

            Game.printNumbersMat(pexeso.getMat());
            System.out.println();

            Game.printGameMat(pexeso);
            System.out.println();

            ByteBuffer buffer = ByteBuffer.allocate(256);

            readKey = client.register(selector, SelectionKey.OP_READ);


            if(state == State.LOGIN){
                toSend = "LOGIN|" + loginName + "|\n"; //posilam login

                toSendByteBuffer = encodeString(toSend);
                client.write(toSendByteBuffer);
                state = State.AFTER_LOGIN;
            }

        }
        catch (IOException e) {
            if(state == State.LOGIN) {
                Platform.runLater(() -> {
                    main.loginError(e);
                });
                state = State.ERROR;
                return;
            }
            throw new RuntimeException(e);
        }



        long nextPing = System.currentTimeMillis() + PING_INTERVAL_MS;


        try{
            while (true) {

                long waitMs = nextPing - System.currentTimeMillis();
                if (waitMs <= 0) {
                    toSend = "PING|\n";
                    toSendByteBuffer = encodeString(toSend);
                    client.write(toSendByteBuffer);
                    nextPing = System.currentTimeMillis() + PING_INTERVAL_MS;
                    waitMs = PING_INTERVAL_MS;
                }

                selector.select(waitMs);//selector s timeout
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                if (selectedKeys.contains(readKey)){

                    final String serverMsgs = readMsg(client); // cteni zpravy ze socketu

                    final String[] msgs = serverMsgs.split("\n");

                    for (int a = 0; a < msgs.length; a++) {// resi slouceni vice zprav
                        String serverMsg = msgs[a];

                        String[] msgParam = msgServerParser(serverMsg);
                        if (msgParam[0].equals("PING")) {
                           System.out.println(serverMsg);
                        }
                        else{
                            Platform.runLater(() -> {
                                if (state == State.AFTER_LOGIN || state == State.JOIN_EVALUATION || state == State.MOVE1_EVALUATION || state == State.MOVE2_EVALUATION || state == State.WAITING || state == State.ON_MOVE1 || state == State.GAME_END) {
                                    System.out.println(serverMsg);

                                    if (msgParam[0].equals("MOVE")) {
                                        pexeso.exposeFieldMat(Integer.valueOf(msgParam[2]), Integer.valueOf(msgParam[3]), Integer.valueOf(msgParam[4]));

                                            imageController.changePictures(Integer.valueOf(msgParam[2]), Integer.valueOf(msgParam[3]), Integer.valueOf(msgParam[4]));
                                            imageController.updateFieldsGame(pexeso, state);

                                        if (msgParam.length >= 6){
                                            if ((state == State.MOVE2_EVALUATION || state == State.WAITING) && msgParam[5].equals(("SCORE"))) {
                                                pexeso.setMyScore(pexeso.getMyScore() + 1);
                                                System.out.println("YOU SCORE");
                                                pexeso.matMoveScore();
                                                if(state == State.MOVE2_EVALUATION && msgParam.length==7) {
                                                    imageController.setMyScore(msgParam[6]);
                                                    state = State.WAITING;//on move puvodne

                                                }
                                                else{
                                                    imageController.setOpponentScore(msgParam[6]);
                                                }
                                                Game.printNumbersMat(pexeso.getMat());
                                                Game.printNumbersMat(pexeso.getExposeMat());
                                                Game.printGameMat(pexeso);
                                            } else if ((state == State.MOVE2_EVALUATION || state == State.WAITING) && msgParam[5].equals(("NOT_SCORE"))) {
                                                Game.printGameMat(pexeso);

                                                System.out.println("YOU NOT SCORE");
                                                pexeso.matMoveNotScore(imageController); // zmeni obrazky na unhide
                                                if(state == State.MOVE2_EVALUATION) {
                                                    state = State.WAITING;//on move puvodne
                                                }
                                            }
                                        }
                                    } else if (msgParam[0].equals(("GAME_END"))) {
                                        System.out.println("Game end, your score is " + pexeso.getMyScore());
                                        state = State.GAME_END;
                                        pexeso.mat = Game.inicializationMat(pexeso.mat.length,pexeso.mat[0].length);
                                        pexeso.exposeMat = Game.inicializationExposeMat(pexeso.exposeMat.length,pexeso.exposeMat[0].length);
                                        main.switchToEndGame();
                                        //return;
                                    } else if (msgParam[0].equals(("PLAY"))) {
                                        if(msgParam[1].equals("PLAY")){
                                            state = State.ON_MOVE1;
                                            imageController.whoPlay.setText("PLAY");
                                        }
                                        else{
                                            state = State.WAITING;
                                            imageController.whoPlay.setText("WAIT");
                                        }
                                        //return;
                                    } else if (msgParam[0].equals(("GAME_START"))) {
                                        if(msgParam[1].equals("PLAY")){
                                            state = State.ON_MOVE1;
                                            main.switchToGameImages();
                                            imageController.whoPlay.setText("PLAY...");
                                            imageController.setPlayer2Name(msgParam[2]);
                                        }
                                        else{
                                            state = State.WAITING;
                                            main.switchToGameImages();
                                            imageController.whoPlay.setText("WAIT...");
                                            imageController.setPlayer2Name(msgParam[2]);
                                        }
                                    } else if (msgParam[0].equals(("LOGIN"))) {
                                        if (msgParam[1].equals("OK")) {
                                            main.loginOk();
                                            state = State.JOIN;
                                        }else if(msgParam[1].equals("ERR")){
                                            main.loginErrorLabel = "Login error.";
                                            state = State.LOGIN;
                                            if(msgParam[2].equals("DUPLICITY")){
                                                main.loginErrorLabel = "The filled-in username is already used, select another.";
                                                main.switchToLogin();
                                            }
                                            else{
                                                main.switchToLogin();
                                            }
                                        }
                                        else{
                                            main.switchToLogin();
                                            state = State.LOGIN;
                                        }
                                    }
                                    else if (msgParam[0].equals(("JOIN"))) {
                                        if (msgParam[1].equals("OK")) {
                                            state = State.WAITING;
                                        }
                                        else{
                                            System.err.println("JOIN error");
                                        }

                                    }

                                    Game.printGameMat(pexeso);
                                    if (state == State.JOIN) {
                                       /* System.out.println("Insert JOIN to connect into the game:");
                                        main.loginOk(); //ovlivni Gui
                                        state = State.JOIN; //move1 a move2 psat s insert msg*/

                                        String toSendJoin = "JOIN|\n"; //posilam join
                                        try {
                                            client.write(stringToByteBuffer(toSendJoin));
                                            state = State.JOIN_EVALUATION;
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                    } else if (state == State.JOIN_EVALUATION) {
                                        System.out.println("Enter the position of the shelf to reveal it in the form of row space column:");
                                        state = State.ON_MOVE1;
                                    } else if (state == State.MOVE1_EVALUATION) {
                                        System.out.println("Enter the position of the shelf to reveal it in the form of row space column:");
                                        state = State.ON_MOVE2;
                                    }
                                    else if (state == State.MOVE2_EVALUATION) {
                                        //state = State.SCORE_EVALUATION;
                                    }
                                }
                            });

                        }
                    }

                }
//                if (consoleKey.isReadable()) {
//                    clientMSG = readMsg(consolePipe.source());
//
//                    System.out.println("wakeup");
//                    if (!clientMSG.isEmpty()) {
//                        if (state == State.JOIN || state == State.ON_MOVE1 || state == State.ON_MOVE2) {
//                            if ((state == State.ON_MOVE1 || state == State.ON_MOVE2) && controlMoveMsg(clientMSG)) {
//                                String param[] = clientMSG.split(" ");
//
//                                param[1] = param[1].replace("A", "0");
//                                param[1] = param[1].replace("B", "1");
//                                param[1] = param[1].replace("C", "2");
//                                param[1] = param[1].replace("D", "3");
//                                for (int i = 0; i < param.length; i++) {
//                                    param[i] = "|" + param[i];
//                                }
//
//                                param[0] = "MOVE" + param[0];
//
//                                clientMSG = String.join("", param);
//                                System.out.println(clientMSG);
//
//                                if (state == State.ON_MOVE1) {
//                                    state = State.MOVE1_EVALUATION;
//                                } else if (state == State.ON_MOVE2) {
//                                    state = State.MOVE2_EVALUATION;
//                                }
//                                //posilani---------------------------
//                                clientMSG = clientMSG + "|\n";
//                                toSendByteBuffer = encodeString(clientMSG);
//                                client.write(toSendByteBuffer);
//                            } else if (clientMSG.contains("MOVE") && !controlMoveMsg(clientMSG)) {
//                                System.out.println("INVALID MOVE");
//                                System.out.println("Insert new move:");
//
//                            } else if (clientMSG.contains("JOIN")) {
//                                state = State.JOIN_EVALUATION;
//                                //posilani---------------------------
//                                clientMSG = clientMSG + "|\n";
//                                toSendByteBuffer = encodeString(clientMSG);
//                                client.write(toSendByteBuffer);
//                            }
//                        } else {
//                            System.out.println("You are not on move");
//                        }
//                    }
//                }

            }
        }
        catch (IOException e){
            synchronized (this){
                if (state == State.AFTER_LOGIN) {
                    Platform.runLater(() -> {
                        main.loginError(e);
                    });
                    state = State.ERROR;
                    return;

                } else {

                }
            }
        }

    }
    private static ByteBuffer stringToByteBuffer(String toSendMsg){
        ByteBuffer toSendByteBuffer = encodeString(toSendMsg);
        return toSendByteBuffer;
    }

    private static ByteBuffer encodeString(String toSend) {
        return Charset.forName("utf-8").encode(toSend);
    }

    private static String readMsg(ReadableByteChannel byteChannel) throws IOException {
        CharBuffer charBuffer;
        ByteBuffer buffer = ByteBuffer.allocate(256);
        String serverMsg;
        byteChannel.read(buffer);
        buffer.flip();
        charBuffer = decodeBuffer(buffer);
        serverMsg = charBuffer.toString();
        return serverMsg;
    }

    private static CharBuffer decodeBuffer(ByteBuffer buffer) {
        return Charset.forName("utf-8").decode(buffer);
    }

    public static String [] msgServerParser(String serverMsg) {
        String msgParam[];
        msgParam = serverMsg.split("\\|");
        return msgParam;
    }

}
