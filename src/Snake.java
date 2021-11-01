
import javax.swing.JFrame;


public class Snake extends JFrame {

    private SnakeBoard board;

    public Snake(int highScore, int boardWidth, int boardHeight) {
        initUI(highScore, boardWidth, boardHeight);
    }

    public SnakeBoard getBoard() {
        return board;
    }

    private void initUI(int highScore, int boardWidth, int boardHeight) {
        board = new SnakeBoard(highScore, boardWidth, boardHeight);
        add(board);

        setResizable(false);
        pack();

        setTitle("Snake");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}