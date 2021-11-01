import javax.swing.*;

public class Sapper extends JFrame {

    private SapperBoard board;

    public Sapper(int highScore, int boardWidth, int boardHeight, int mineCount) {
        initUI(highScore, boardWidth, boardHeight, mineCount);
    }

    public SapperBoard getBoard() {
        return board;
    }

    private void initUI(int highScore, int boardWidth, int boardHeight, int mineCount) {
        board = new SapperBoard(highScore, boardWidth, boardHeight, mineCount);
        add(board);

        setResizable(false);
        pack();

        setTitle("Sapper");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
