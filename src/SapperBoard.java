import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * Enum of possible game tiles
 * that holds value representing index in 'tiles' array
 */
enum SapperTiles {
    Empty(0),Covered(1),Flag(2),Mine(3), MineExplode(4);
    private final int value;
    private SapperTiles(int _value){ value = _value;}
    public int getValue(){return value;}
}

/**
 * Class responsible for showing sapper Board and responding to user inputs
 */
public class SapperBoard extends Board {

    private boolean[][] mines;          //array representing mines with ture and empty tiles with false
    private SapperTiles[][] board;      //array representing sapper board with all possible tiles
    private int[][] minesCountAround;   //array containing information about count of mines in 1 tile radius

    private boolean inGame = true;      //if game is still in playable state
    private boolean win = false;        //if player won the game
    private int lives = 2;              // variable to count lives that are left
    private long start, finish;         // time in milliseconds of start and game finish

    private Image[] tiles;              // array of all possible tile images, where index corresponds to SapperTiles Enum

    //paths to tiles images
    private static final String[] imagePathsBG = {"resources/TEmpty.png", "resources/TCovered.png", "resources/TFlag.png",
            "resources/mine.png", "resources/mineExplode.png"};

    //array of colors for numbers displayed on board, value at index - 1 is equal to color for number index
    private static final Color[] numberColors = {new Color(5, 6, 247),new Color(0, 134, 11),new Color(250, 0, 2),
                                            new Color(4, 5, 111), new Color(123, 4, 8),new Color(23, 101, 88),
                                            new Color(0, 3, 0),new Color(127, 127, 127)};

    /**
     * public constructor for SapperBoard calls base class constructor (Board)
     * @param _highScore high score value of the game
     * @param boardWidth width of the playable game board in number of tiles
     * @param boardHeight height of the playable game board in number of tiles
     * @param _mineCount  number of mines on board
     */
    public SapperBoard(int _highScore, int boardWidth, int boardHeight, int _mineCount) {
        super(_highScore,boardWidth,boardHeight,_mineCount);
    }

    @Override
    protected void SetWindowSize() {
        DOT_SIZE = 67;              //size of one tile
        TOP_BEVEL_HEIGHT = 15;      //size of top bevel displaying score
        W_WIDTH = DOT_SIZE * (T_NUM_X + 2);                     // +2 for bevels
        W_HEIGHT =  DOT_SIZE * (T_NUM_Y + 2) + TOP_BEVEL_HEIGHT;// +2 for bevels
    }

    @Override
    protected void customBoardInit() {
        DELAY = 1000; //time delay in milliseconds necessary to update menu clock

        mines = new boolean[T_NUM_X][T_NUM_Y];
        board = new SapperTiles[T_NUM_X][T_NUM_Y];
        minesCountAround = new int[T_NUM_X][T_NUM_Y];

        //initialize all tiles to be Covered
        for(int x = 0; x < T_NUM_X; x ++){
            for(int y = 0; y < T_NUM_Y; y ++){
                board[x][y] = SapperTiles.Covered;
            }
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                //repaint on every click
                repaint();
                int mouseBtn = e.getButton();
                if(mouseBtn == MouseEvent.BUTTON1){
                    checkTile(CalcTileX(e.getX()), CalcTileY(e.getY()));
                }
                else if(mouseBtn == MouseEvent.BUTTON3){
                    placeFlag(CalcTileX(e.getX()), CalcTileY(e.getY()));
                }
                //check if game finished only if game is still on
                if(inGame) CheckIfFinished();
            }
        });
    }

    @Override
    protected void loadImages() {
        tiles = new Image[5];

        //Load images in paths('imagePathsBG') from resources to 'tiles' array
        ClassLoader cl = this.getClass().getClassLoader();
        int i = 0;
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
        //set start time
        start = System.currentTimeMillis();
    }

    /**
     * Converts milliseconds to minutes and seconds
     * @param milli amount of milliseconds to convert
     * @return string with minutes and seconds separated by ':'
     */
    private String millisecToString(long milli){
        return String.format("%d:%02d",milli/60000,(milli % 60000) /1000);
    }
    /**
     * Converts milliseconds to minutes, seconds and milliseconds
     * @param milli amount of milliseconds to convert
     * @return string in format {min}:{sec}:{milli}
     */
    private String millisecToStringPrecise(long milli){
        long min = milli/60000;
        long s = (milli % 60000) /1000;
        long ms = (milli % 1000);
        return String.format("%d:%02d.%03d",min,s,ms);
    }

    /**
     * Function for drawing top menu displaying info about the game
     * @param g graphic context to draw elements
     */
    private void DrawMenu(Graphics g){
        String s_score = "Mines left: " + minesCount;
        String s_highscore = "High score: " + millisecToStringPrecise(highScore);
        String s_lives = "Lives: " + (lives + 1);
        String s_time = "Time: " + millisecToString(System.currentTimeMillis() - start);

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
            //Draw every tile
            for(int x = 0; x < T_NUM_X; x ++){
                for(int y = 0; y < T_NUM_Y; y ++){
                    g.drawImage(tiles[board[x][y].getValue()], CalcWinX(x), CalcWinY(y), this);
                    //Draw number if necessary
                    if(board[x][y] == SapperTiles.Empty && minesCountAround[x][y] != 0){
                        String msg = Integer.toString(minesCountAround[x][y]);
                        g.setColor(numberColors[minesCountAround[x][y] - 1]);
                        g.setFont(t_font);
                        g.drawString(msg, CalcWinX(x) +  (DOT_SIZE - metr.stringWidth(msg)) / 2, (CalcWinY(y) +  (DOT_SIZE + metr.stringWidth(msg)) / 2 ));
                    }
                }
            }
            //sync graphics state for accurate graphic
            Toolkit.getDefaultToolkit().sync();

        } else {
            gameOver(g);
        }
    }

    /**
     * Function to manage drawing game over screen
     * @param g graphic context to draw elements
     */
    private void gameOver(Graphics g) {
        String msg;
        if(win) {
            //if loaded default highScore assign current value
            if(highScore != 0)
                highScore = Math.min((int)(finish - start), highScore);
            else
                highScore = (int)(finish - start);
            msg = "You Win, Time: " + millisecToStringPrecise(finish - start);
        }
        else{
            msg = "Game Over";
        }

        g.setColor(Color.white);
        g.setFont(t_font);
        g.drawString(msg, (W_WIDTH - metr.stringWidth(msg)) / 2, (W_HEIGHT + getBackgroundOffset())/ 2 );

    }

    /**
     * Function to randomly place 'minesCount' amount of mines in 'mines' array
     */
    private void generateMines(){
        if(minesCount > T_NUM_X*T_NUM_Y) {
            System.out.println("Error minesCount too big");
            return;
        }

        //temporary array to store indexes of free tile in one number;
        List<Integer> availableIndexes = new ArrayList<>();
        for(int i = 0; i < T_NUM_X; i++){
            for(int j = 0; j < T_NUM_Y; j++){
                availableIndexes.add(j + i*T_NUM_X);
            }
        }

        int x, y ,r;
        for(int i = 0; i < minesCount; i++){
            //make sure that every mine is unique
            r  = (int) (Math.random() * availableIndexes.size());
            x = availableIndexes.get(r) / T_NUM_X;
            y = availableIndexes.get(r) % T_NUM_Y;
            availableIndexes.remove(r);
            mines[x][y] = true;
        }
    }

    /**
     * Recursive fill algorithm to uncover all adjacent empty tiles
     * @param x start x position in board coordinates
     * @param y start t position in board coordinates
     */
    private void fill(int x, int y){
        //don't uncover flagged tiles
        if(board[x][y] == SapperTiles.Flag) return;
        //if empty with 0 mines around and not checked yet, uncover tiles around
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

    /**
     * Function to check if what to do after tile on x,y is left-clicked
     * @param x click x position in board coordinates
     * @param y click y position in board coordinates
     */
    private void checkTile(int x, int y) {
        if (inGame) {
            //check if clicked in board
            if(x < 0 || y < 0) return;
            if(x >= T_NUM_X || y >= T_NUM_Y) return;

            if(mines[x][y]){
                //manage clicking on mine
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
               // uncover adjacent tiles if it's possible
                fill(x,y);
            }
        }
    }

    /**
     * Function to place flag on board if possible
     * @param x click x position in board coordinates
     * @param y click y position in board coordinates
     */
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

    /**
     * Function to check if game finishing conditions are met
     */
    private void CheckIfFinished(){
        for(int x = 0; x < T_NUM_X; x++){
            for(int y = 0; y < T_NUM_Y; y++){
                if(board[x][y] == SapperTiles.Covered) {
                    return;
                }
            }
        }
        //all mine tiles were touched, now decide if it was a wining strategy
        if(minesCount == 0) win = true;
        inGame = false;
        finish = System.currentTimeMillis();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

}


