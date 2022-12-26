package game.pexesofx;

public class LogManager {

    public void incomingMessage(String inputMsg, ClientSelect.State state) {
        if(inputMsg.length()>1) {
            inputMsg = inputMsg.substring(0,inputMsg.length() - 1);
        }
        System.out.println("INCOMING MESSAGE [Msg: " + inputMsg + " ] [State:" + state + "]");

    }

    public void outcomingMessage(String outputMsg, ClientSelect.State state) {
        if(outputMsg.length()>1) {
            outputMsg = outputMsg.substring(0, outputMsg.length() - 1);
        }
        System.out.println("OUTCOMING MESSAGE [Msg: " + outputMsg + " ] [State:" + state + "]");
    }


}
