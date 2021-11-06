import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Abstract class for
 */
abstract public class Board extends JPanel implements ActionListener {

    protected int DOT_SIZE; //size of one tile
    protected int TOP_BEVEL_HEIGHT; //size of top bevel displaying score

    protected int T_NUM_X; //number of tiles in x direction
    protected int T_NUM_Y; //number of tiles in y direction
    protected int W_WIDTH; //window width in pixels
    protected int W_HEIGHT; //window height in pixels

    protected int DELAY;// Time interval for every 'actionPerformed' function call

    protected int highScore;
    protected int minesCount;
    protected Timer timer;
    protected Font t_font;// Main font for text
    protected FontMetrics metr;// Font metrics for calculating correct position of text.

    /**
     * Board constructor that runs initBoard method
     * @param _highScore high score value of the game
     * @param boardWidth width of the playable game board in number of tiles
     * @param boardHeight height of the playable game board in number of tiles
     * @param _mineCount
     */
    public Board(int _highScore, int boardWidth, int boardHeight, int _mineCount) {
        highScore = _highScore;
        T_NUM_X = boardWidth;
        T_NUM_Y = boardHeight;
        minesCount = _mineCount;
        SetWindowSize(); //entry point for function to customize window size depending on the game
        initBoard();
    }

    /**
     * abstract function for initializing custom window size before is set to JPanel
     */
    protected abstract void SetWindowSize();

    /**
     * function initializing default board
     * it runs 'customBoardInit' then 'loadImages' and finally 'initGame'
     */
    private void initBoard(){
        setBackground(new Color(100, 144, 67));
        setFocusable(true);
        setPreferredSize(new Dimension(W_WIDTH, W_HEIGHT));

        //load default font as 'Helvetica'
        t_font = new Font("Helvetica", Font.BOLD, 28);
        metr = getFontMetrics(t_font);

        customBoardInit();//entry point for function to customize board initialization

        loadImages();// entry point for function to load graphics
        initGame(); // entry point for function to initialize game parameters

        //start timer to call 'actionPerformed'
        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * function to customize board initialization run from 'initBoard'
     */
    protected abstract void customBoardInit();

    /**
     * function to load graphics run from 'initBoard'
     */
    protected abstract void loadImages();

    /**
     * function to initialize game parameters run from 'initBoard'
     */
    protected abstract void initGame();

    /**
     * function that will be called every time something needs to be redrawn
     * @param g grapic context to draw elements
     */
    protected abstract void doDrawing(Graphics g);

    /**
     * func to get top background offset in pixels
     * @return top background offset in pixels
     */
    protected int getBackgroundOffset(){
        return TOP_BEVEL_HEIGHT;
    }

    /**
     * Calculates position in window in pixels
     * @param x index on board
     * @return position in window in pixels
     */
    protected int CalcWinX(int x){
        return (x + 1) * DOT_SIZE;
    }
    /**
     * Calculates position in window in pixels
     * @param y index on board
     * @return position in window in pixels
     */
    protected int CalcWinY(int y){
        return (y + 1) * DOT_SIZE + getBackgroundOffset();
    }
    /**
     * Calculates index on board based on pixes position 'x' in window
     * @param x pixes position in window
     * @return index position on board
     */
    protected int CalcTileX(int x){
        return x / DOT_SIZE - 1;
    }

    /**
     * Calculates index on board based on pixes position 'y' in window
     * @param y pixes position in window
     * @return index position on board
     */
    protected int CalcTileY(int y){ return (y - getBackgroundOffset()) / DOT_SIZE - 1 ;}

    public int getHighScore() { return highScore; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

}
