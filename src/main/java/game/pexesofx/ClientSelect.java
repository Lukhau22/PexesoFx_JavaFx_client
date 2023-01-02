package game.pexesofx;

import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;


public class ClientSelect {

    public static final int PING_INTERVAL_MS = 20000;
    private String loginName;
    private Thread networkThread;
    private Main main;
    private ImageController imageController;
    private SocketChannel client;

    private int count_of_empty_msg;

    private LogManager logManager;

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
                    logManager.outcomingMessage(clientMSG, state); //logManager
                } catch (IOException e) {
                    throw new RuntimeException(e);// vyresit ze se nepodarilo zapsat
                }
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
            logManager.outcomingMessage(clientMSG, state); //logManager
        } catch (IOException e) {
            throw new RuntimeException(e);// vyresit ze se nepodarilo zapsat
        }
    }

    public void sendExit() {
        String clientMSG= "EXIT|\n";
        ByteBuffer toSendByteBuffer = encodeString(clientMSG);
        try {
            client.write(toSendByteBuffer);
            logManager.outcomingMessage(clientMSG, state); //logManager
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
        RECONNECT,
        ERROR,
        GAME_END

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
        this.logManager = new LogManager();
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
            System.out.println("client try to connect on address: " + main.serverAddress + " and port: " + main.port);
            client.connect(new InetSocketAddress(main.serverAddress, main.port));//default: "127.0.0.1" "10000"


            client.configureBlocking(false);
            System.out.println("Client conected");

            ByteBuffer buffer = ByteBuffer.allocate(256);

            readKey = client.register(selector, SelectionKey.OP_READ);


            if(state == State.LOGIN){
                toSend = "LOGIN|" + loginName + "|\n"; //posilam login
                toSendByteBuffer = encodeString(toSend);
                client.write(toSendByteBuffer);
                logManager.outcomingMessage(toSend, state); //logManager
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
                    logManager.outcomingMessage(toSend, state); //logManager
                    nextPing = System.currentTimeMillis() + PING_INTERVAL_MS;
                    waitMs = PING_INTERVAL_MS;
                }

                selector.select(waitMs);//selector s timeout
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                if (selectedKeys.contains(readKey)){

                    final String serverMsgs = readMsg(client); // cteni zpravy ze socketu

                    final String[] msgs = serverMsgs.split("\n");//-------------------------INCOMING MSG PARSER--------------------

                    for (int a = 0; a < msgs.length; a++) {// resi slouceni vice zprav
                        String serverMsg = msgs[a];
                        logManager.incomingMessage(serverMsg, state);

                        String[] msgParam = msgServerParser(serverMsg);//------------PIPE PARSER-----------------
                        if (msgParam[0].equals("PING")) {
                           System.out.println(serverMsg);
                        }
                        else{
                            Platform.runLater(() -> {
                                if (state == State.AFTER_LOGIN || state == State.JOIN_EVALUATION || state == State.MOVE1_EVALUATION || state == State.MOVE2_EVALUATION || state == State.WAITING || state == State.ON_MOVE1 ||state == State.ON_MOVE2 || state == State.RECONNECT || state == State.GAME_END) {
                                    System.out.println(serverMsg);

                                    if (msgParam[0].equals("MOVE")) {//-----------------------------------------MOVE--------------------------------------------
                                        pexeso.exposeFieldMat(Integer.valueOf(msgParam[2]), Integer.valueOf(msgParam[3]), Integer.valueOf(msgParam[4]));
                                        imageController.changePictures(Integer.valueOf(msgParam[2]), Integer.valueOf(msgParam[3]), Integer.valueOf(msgParam[4]));

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
                                                //Game.printNumbersMat(pexeso.getMat());
                                                //Game.printNumbersMat(pexeso.getExposeMat());
                                                //Game.printGameMat(pexeso);
                                            } else if ((state == State.MOVE2_EVALUATION || state == State.WAITING) && msgParam[5].equals(("NOT_SCORE"))) {
                                                //Game.printGameMat(pexeso);

                                                System.out.println("YOU NOT SCORE");
                                                pexeso.matMoveNotScore(imageController); // zmeni obrazky na unhide
                                                if(state == State.MOVE2_EVALUATION) {
                                                    state = State.WAITING;//on move puvodne
                                                }
                                            }
                                        }
                                    } else if (msgParam[0].equals(("GAME_END"))) {//---------------------------------------------GAME_END---------------------------------------
                                        System.out.println("Game end, your score is " + pexeso.getMyScore());
                                        state = State.GAME_END;
                                        pexeso.mat = Game.inicializationMat(pexeso.mat.length,pexeso.mat[0].length);
                                        pexeso.exposeMat = Game.inicializationExposeMat(pexeso.exposeMat.length,pexeso.exposeMat[0].length);
                                        main.switchToEndGame();
                                        //return;
                                    } else if (msgParam[0].equals(("PLAY"))) {//----------------------------------------------PLAY-------------------------------------
                                        if(msgParam[1].equals("PLAY")){
                                            state = State.ON_MOVE1;
                                            imageController.whoPlay.setText("PLAY");
                                            if(msgParam.length == 3 && msgParam[2].equals("ON_MOVE2")){
                                                state = State.ON_MOVE2;
                                            }
                                        }
                                        else{
                                            state = State.WAITING;
                                            imageController.whoPlay.setText("WAIT");
                                        }
                                        //return;
                                    } else if (msgParam[0].equals(("GAME_START"))) {//----------------------------------------GAME_START-----------------------------
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
                                    } else if (msgParam[0].equals(("LOGIN"))) {//--------------------------------------------LOGIN------------------------------------
                                        if (msgParam[1].equals("OK")) {
                                            main.loginOk();
                                            state = State.JOIN;
                                        }else if(msgParam[1].equals("ERR")){
                                            main.loginErrorLabel = "Login error.";
                                            state = State.LOGIN;
                                            if(msgParam[2].equals("DUPLICITY")){
                                                try {
                                                    client.close();
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
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
                                    }else if (msgParam[0].equals(("RECONNECT"))) {//------------------------------RECONNECT------------------------
                                        if (msgParam[1].equals("OK")) {
                                            main.switchToGameImages();
                                            state = State.RECONNECT;
                                            imageController.whoPlay.setText("WAIT");
                                        }
                                        else{
                                            System.err.println("RECONNECT error");
                                        }

                                    }else if (msgParam[0].equals(("UNHIDE")) && state == State.RECONNECT) {//------------------------------UNHIDE FIELDS RECONNECT------------------------
                                        if (msgParam.length >= 3) {
                                            pexeso.permanetlyExposeFieldMat(Integer.valueOf(msgParam[1]), Integer.valueOf(msgParam[2]), Integer.valueOf(msgParam[3]));
                                            imageController.changePictures(Integer.valueOf(msgParam[1]), Integer.valueOf(msgParam[2]), Integer.valueOf(msgParam[3]));
                                        }
                                        else{
                                            System.err.println("UNHIDE error");
                                        }

                                    }
                                    else if (msgParam[0].equals(("GAME_INFO"))) {//------------------------------SCORE------------------------
                                        if (msgParam.length >=3) {
                                            imageController.setMyScore(msgParam[1]);
                                            imageController.setOpponentScore(msgParam[2]);
                                            imageController.setPlayer2Name(msgParam[3]);
                                        }
                                        else{
                                            System.err.println("GAME_INFO error");
                                        }

                                    }else if (msgParam[0].equals(("JOIN"))) {//------------------------------JOIN------------------------
                                        if (msgParam[1].equals("OK")) {
                                            state = State.WAITING;
                                        }
                                        else{
                                            System.err.println("JOIN error");
                                        }

                                    } else if (msgParam[0].equals(("OPPONENT"))) {//------------------------------OPPONENT DISCONNECT------------------------
                                        if (msgParam[1].equals("DISCONNECTED")) {
                                            imageController.opponentDisconn.setText("Opponent disconnected");
                                            imageController.OppConnect.setText("Disconnect");
                                            imageController.OppConnect.setTextFill(Color.RED);
                                            imageController.whoPlay.setText("WAIT");
                                            state = State.WAITING;
                                        }
                                        else if(msgParam[1].equals("RECONNECTED")){
                                            imageController.opponentDisconn.setText("");
                                            imageController.OppConnect.setText("connect");
                                            imageController.OppConnect.setTextFill(Color.GREEN);
                                        }
                                    }else if(msgParam[0].equals("")){
                                        System.out.println("Empty Msg");
                                        main.loginErrorLabel = "The server is unavailable";
                                        if(count_of_empty_msg == 10) {
                                            try {
                                                client.close();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            main.switchToLogin();
                                        }
                                        else{
                                            count_of_empty_msg++;
                                        }
                                    }

                                    if(!msgParam[0].equals("")){
                                        count_of_empty_msg = 0;
                                    }

                                    else{
                                        /*if(!msgParam[0].isEmpty()) {
                                            main.switchToLogin();
                                            main.loginErrorLabel = "Invalid msg from server (wrong server)";
                                            try {
                                                client.close();
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }*/
                                    }

                                    //Game.printGameMat(pexeso);
                                    if (state == State.JOIN) {
                                        String toSendJoin = "JOIN|\n"; //posilam join
                                        try {
                                            client.write(stringToByteBuffer(toSendJoin));
                                            logManager.outcomingMessage(toSendJoin, state); //logManager
                                            state = State.JOIN_EVALUATION;
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }

                                    } else if (state == State.JOIN_EVALUATION) {
                                        state = State.ON_MOVE1;
                                    } else if (state == State.MOVE1_EVALUATION) {
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
