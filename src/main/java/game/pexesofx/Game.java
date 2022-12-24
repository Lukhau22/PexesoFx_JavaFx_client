package game.pexesofx;

public class Game {
    int myScore;
    int opponentScore;

    int[][] mat;
    int[][] exposeMat; //-1 false(neodkryto), 0 waiting for result (docasne odkryto), 1 true(trvale odkryto)


    public Game() {
    }

    public Game(int myScore, int opponentScore, int rows, int cols) {
        this.myScore = myScore;
        this.opponentScore = opponentScore;
        this.mat=inicializationMat(rows, cols);
        this.exposeMat=inicializationExposeMat(rows, cols);
    }

    public static int [][] inicializationMat(int rows, int cols) {
        int[][] mat = new int[rows][cols];
        for (int i = 0;  i < mat.length;  i++) {
            for (int j = 0;  j < mat[i].length;  j++) {
                mat[i][j] = -1;
            }
        }
        return mat;
    }

    public static int [][] inicializationExposeMat(int rows, int cols) {
        int[][] exMat = new int[rows][cols];
        for (int i = 0;  i < exMat.length;  i++) {
            for (int j = 0;  j < exMat[i].length;  j++) {
                exMat[i][j] = -1;
            }
        }
        return exMat;
    }

    public void exposeFieldMat(int row, int col, int value) {
        if(row < this.mat.length && col < this.mat.length){
            this.mat[row][col]= value;
            this.exposeMat[row][col] = 0;
        }
        else{
            System.out.println("INVALID INDEX IN MATRIX");
        }
    }

    public void permanetlyExposeFieldMat(int row, int col, int value) {
        if(row < this.mat.length && col < this.mat.length){
            this.mat[row][col]= value;
            this.exposeMat[row][col] = 1;
        }
        else{
            System.out.println("INVALID INDEX IN MATRIX");
        }
    }

    public void matMoveScore() {

        synchronized (this){
            for (int i = 0; i < this.mat.length; i++) {
                for (int j = 0; j < this.mat[i].length; j++) {
                    if (this.exposeMat[i][j] == 0) {
                        this.exposeMat[i][j] = 1;
                    }
                }
            }
        }
    }

    public void matMoveNotScore(ImageController imageController) {
        for (int i = 0;  i < this.mat.length;  i++) {
            for (int j = 0;  j < this.mat[i].length;  j++) {
                if(this.exposeMat[i][j] == 0){
                    this.mat[i][j] = -1;
                    this.exposeMat[i][j] = -1;
                    imageController.changePictures(Integer.valueOf(i), Integer.valueOf(j), -1);
                }
            }
        }
    }

    static void printNumbersMat(int[][] mat) {
        for (int i = 0;  i < mat.length;  i++) {
            System.out.print("  " + (char)(65+i));
        }

        for (int i = 0;  i < mat.length;  i++) {
            System.out.println();
            System.out.print(i);
            for (int j = 0;  j < mat[i].length;  j++) {
                System.out.print(" " + mat[i][j]);
            }
        }
        System.out.println();
    }

    static void printGameMat(Game game) {
        System.out.println("Your score: " + game.getMyScore());
        System.out.println("Opponent score: " + game.getOpponentScore());
        System.out.print("   ");
        for (int i = 0;  i < game.mat.length;  i++) {
            System.out.print("  " + (char)(65+i));
        }

        for (int i = 0;  i < game.mat.length;  i++) {
            System.out.println();
            System.out.print(i + " |");
            for (int j = 0;  j < game.mat[i].length;  j++) {
                if(game.exposeMat[i][j] == 1 || game.exposeMat[i][j] == 0){
                    if(game.exposeMat[i][j] < 10) {
                        System.out.print("  " + game.mat[i][j]);
                    }
                    else{
                        System.out.print( game.mat[i][j]);
                    }

                }
                else {
                    System.out.print("  X");
                }
            }
        }
        System.out.println();
    }


    public int getMyScore() {
        return myScore;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public void setMyScore(int myScore) {
        this.myScore = myScore;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }

    public int[][] getMat() {
        return mat;
    }

    public int[][] getExposeMat() { return exposeMat; }
}
