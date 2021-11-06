import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;


enum SapperTiles {
    Empty(0),Covered(1),Flag(2),Mine(3),
    MineExplode(4);
    private final int value;
    private SapperTiles(int _value){ value = _value;}
    public int getValue(){return value;}
}

public class SapperBoard extends Board {

    private boolean[][] mines;
    private SapperTiles[][] board;
    private int minesCountAround[][];

    private boolean inGame = true;
    private boolean win = false;
    private int lives = 2;
    private long start, finish;

    private Image[] tiles;

    private static final String[] imagePathsBG = {"resources/TEmpty.png", "resources/TCovered.png", "resources/TFlag.png",
            "resources/mine.png", "resources/mineExplode.png"};

    private static final Color[] numberColors = {new Color(5, 6, 247),new Color(0, 134, 11),new Color(250, 0, 2),
                                            new Color(4, 5, 111), new Color(123, 4, 8),new Color(23, 101, 88),
                                            new Color(0, 3, 0),new Color(127, 127, 127)};


    public SapperBoard(int _highScore, int boardWidth, int boardHeight, int _mineCount) {
        super(_highScore,boardWidth,boardHeight,_mineCount);
    }

    @Override
    protected void SetWindowSize() {
        DOT_SIZE = 67;
        TOP_BEVEL_HEIGHT = 15;
        W_WIDTH = DOT_SIZE * (T_NUM_X + 2);
        W_HEIGHT =  DOT_SIZE * (T_NUM_Y + 2) + TOP_BEVEL_HEIGHT;
    }

    @Override
    protected void customBoardInit() {
        DELAY = 1000;

        mines = new boolean[T_NUM_X][T_NUM_Y];
        board = new SapperTiles[T_NUM_X][T_NUM_Y];
        minesCountAround = new int[T_NUM_X][T_NUM_Y];

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                repaint();
                int mouseBtn = e.getButton();
                if(mouseBtn == MouseEvent.BUTTON1){
                    checkTile(CalcTileX(e.getX()), CalcTileY(e.getY()));
                }
                else if(mouseBtn == MouseEvent.BUTTON3){
                    placeFlag(CalcTileX(e.getX()), CalcTileY(e.getY()));
                }
                if(inGame) CheckIfFinished();
            }
        });

        for(int x = 0; x < T_NUM_X; x ++){
            for(int y = 0; y < T_NUM_Y; y ++){
                board[x][y] = SapperTiles.Covered;
            }
        }
    }

    @Override
    protected void loadImages() {
        tiles = new Image[5];

        int i = 0;
        ClassLoader cl = this.getClass().getClassLoader();
        for(String path : imagePathsBG) tiles[i++] = new ImageIcon(cl.getResource(path)).getImage();
    }

    @Override
    protected void initGame() {
        generateMines();
        //Precalculate mines around every tile;
        for(int x = 0; x < T_NUM_X; x ++){
            for(int y = 0; y < T_NUM_Y; y ++){
                for(int i = Math.max(x - 1, 0); i <= Math.min(x + 1,T_NUM_X - 1); i++){
                    for(int j = Math.max(y - 1, 0); j <= Math.min(y + 1, T_NUM_Y - 1); j++){
                        if(i == x && j ==y )continue;
                        if(mines[i][j]) minesCountAround[x][y]++;
                    }
                }
            }
        }
        start = System.currentTimeMillis();
    }

    private String milisecToStrnig(long mili){
        return String.format("%d:%02d",mili/60000,(mili % 60000) /1000);
    }
    private String milisecToStrnigPrecise(long mili){
        long min = mili/60000;
        long s = (mili % 60000) /1000;
        long ms = (mili % 1000);
        return String.format("%d:%02d.%03d",min,s,ms);
    }

    private void DrawMenu(Graphics g){
        String s_score = "Mines left: " + minesCount;
        String s_highscore = "High score: " + milisecToStrnigPrecise(highScore);
        String s_lives = "Lives: " + (lives + 1);
        String s_time = "Time: " + milisecToStrnig(System.currentTimeMillis() - start);

        g.setColor(Color.white);
        g.setFont(t_font);
        g.drawString(s_score, DOT_SIZE/2, (DOT_SIZE)/ 2);
        g.drawString(s_highscore, W_WIDTH  - (metr.stringWidth(s_highscore)) - DOT_SIZE/2, (DOT_SIZE)/ 2);
        g.drawString(s_lives, DOT_SIZE/2, (DOT_SIZE));
        g.drawString(s_time, W_WIDTH  - (metr.stringWidth(s_time)) - DOT_SIZE/2, (DOT_SIZE));

    }

    @Override
    protected void doDrawing(Graphics g) {
        if (inGame) {
            DrawMenu(g);
            for(int x = 0; x < T_NUM_X; x ++){
                for(int y = 0; y < T_NUM_Y; y ++){
                    g.drawImage(tiles[board[x][y].getValue()], CalcWinX(x), CalcWinY(y), this);
                    if(board[x][y] == SapperTiles.Empty && minesCountAround[x][y] != 0){
                        String msg = Integer.toString(minesCountAround[x][y]);
                        g.setColor(numberColors[minesCountAround[x][y] - 1]);
                        g.setFont(t_font);
                        g.drawString(msg, CalcWinX(x) +  (DOT_SIZE - metr.stringWidth(msg)) / 2, (CalcWinY(y) +  (DOT_SIZE + metr.stringWidth(msg)) / 2 ));
                    }
                }
            }

            Toolkit.getDefaultToolkit().sync();

        } else {
            gameOver(g);
        }
    }

    private void gameOver(Graphics g) {
        String msg;
        if(win) {
            if(highScore != 0)
                highScore = Math.min((int)(finish - start), highScore);
            else
                highScore = (int)(finish - start);
            msg = "You Win, Time: " + milisecToStrnigPrecise(finish - start);
        }
        else{
            msg = "Game Over";
        }

        g.setColor(Color.white);
        g.setFont(t_font);
        g.drawString(msg, (W_WIDTH - metr.stringWidth(msg)) / 2, (W_HEIGHT + getBackgroundOffset())/ 2 );

    }

    private void generateMines(){
        if(minesCount > T_NUM_X*T_NUM_Y) {
            System.out.println("Error minesCount too big");
            return;
        }
        int x, y;
        for(int i = 0; i < minesCount; i++){
            do {
                x = (int) (Math.random() * (T_NUM_X - 1));
                y = (int) (Math.random() * (T_NUM_Y - 1));
            }while(mines[x][y]);
            mines[x][y] = true;

        }
    }

    private void fill(int x, int y){
        if(minesCountAround[x][y] == 0 && board[x][y] == SapperTiles.Covered){
            board[x][y] = SapperTiles.Empty;
            if(x + 1 < T_NUM_X) fill(x + 1,y);
            if(y + 1 < T_NUM_Y) fill(x,y + 1);
            if(x - 1 >= 0) fill(x - 1,y);
            if(y - 1 >= 0) fill(x,y - 1);
        }
        else {
            board[x][y] = SapperTiles.Empty;
        }
    }

    private void checkTile(int x, int y) {
        if (inGame) {
            if(x < 0 || y < 0) return;
            if(x >= T_NUM_X || y >= T_NUM_Y) return;
            if(mines[x][y]){
                if(board[x][y] != SapperTiles.MineExplode) {
                    if (lives > 0) {
                        lives--;
                        minesCount--;
                    } else
                        inGame = false;
                    board[x][y] = SapperTiles.MineExplode;
                }
            }
            else{
                if(board[x][y] != SapperTiles.Flag)
                    fill(x,y);
            }
        }
    }

    private void placeFlag(int x, int y){
        if (inGame) {
            if(x < 0 || y < 0) return;
            if(x >= T_NUM_X || y >= T_NUM_Y) return;
            if(board[x][y] == SapperTiles.Covered && minesCount > 0) {
                board[x][y] = SapperTiles.Flag;
                minesCount--;
            }
            else if(board[x][y] == SapperTiles.Flag) {
                board[x][y] = SapperTiles.Covered;
                minesCount++;
            }
        }
    }

    private void CheckIfFinished(){
        for(int x = 0; x < T_NUM_X; x++){
            for(int y = 0; y < T_NUM_Y; y++){
                if(board[x][y] == SapperTiles.Covered) {
                    return;
                }
            }
        }
        if(minesCount == 0) win = true;
        inGame = false;
        finish = System.currentTimeMillis();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}


