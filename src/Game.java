import javax.swing.*;

/**
 * Generic class for running Game in JFrame
 * where member 'board' has all the specific information about the game.
 */
public class Game extends JFrame {
    private Board board;

    public Game(String title, Board _board) {
        board = _board;
        initUI(title);
    }

    public Board getBoard() {
        return board;
    }

    private void initUI(String title) {
        add(board);
        setVisible(true);
        setResizable(false);
        pack();

        setTitle(title);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
