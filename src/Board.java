import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

abstract public class Board extends JPanel implements ActionListener {

    protected final int T_NUM_X; //number of tiles in x direction
    protected final int T_NUM_Y; //number of tiles in y direction
    protected int W_WIDTH; //window width in pixels
    protected int W_HEIGHT; //window height in pixels

    protected int highScore;
    protected int minesCount;
    protected Timer timer;
    protected Font t_font;
    protected FontMetrics metr;

    public Board(int _highScore, int boardWidth, int boardHeight, int _mineCount) {
        highScore = _highScore;
        T_NUM_X = boardWidth;
        T_NUM_Y = boardHeight;
        minesCount = _mineCount;
        SetWindowSize();
        initBoard();
    }

    protected abstract void SetWindowSize();

    protected void initBoard(){
        setBackground(new Color(100, 144, 67));
        setFocusable(true);
        setPreferredSize(new Dimension(W_WIDTH, W_HEIGHT));

        t_font = new Font("Helvetica", Font.BOLD, 28);
        metr = getFontMetrics(t_font);

        CustomInit();

        loadImages();
        initGame();
    }

    protected abstract void CustomInit();
    protected abstract void loadImages();
    protected abstract void initGame();
    protected abstract void doDrawing(Graphics g);

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

}
