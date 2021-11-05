import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.prefs.Preferences;

public class MainMenu extends JFrame{
    private int snakeHighScore = 0, sapperHighScore = 0;
    // Key values for retrieving
    private final String snakeHighScoreKey = "snake", sapperHighScoreKey = "sapper";

    private JTextField textFieldWidth = new JTextField("11");
    private JTextField textFieldHeight = new JTextField("11");
    private JTextField textFieldMinesCount = new JTextField("20");

    /**
     * MainMenu class constructor, loads high scores for snake and sapper and initialize main menu UI
     */
    public MainMenu(){
        loadHighScore();
        initUI();
    }

    /**
     *Initialize UI of main menu with 3 parameters to customize game, and 2 buttons to run games.
     */
    private void initUI() {
        JLabel lblWidth =       new JLabel("Board width: ");
        JLabel lblHeight =      new JLabel("Board height:");
        JLabel lblMinesCount =  new JLabel("Mines count: ");
        JButton btnPlaySnake = new JButton("Play Snake");
        JButton btnPlaySapper = new JButton("Play Sapper");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        //Set up horizontal groups in layout
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblWidth)
                    .addComponent(textFieldWidth))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblHeight)
                    .addComponent(textFieldHeight))
                .addGroup(layout.createSequentialGroup()
                      .addComponent(lblMinesCount)
                      .addComponent(textFieldMinesCount))
                .addGroup(layout.createSequentialGroup()
                      .addComponent(btnPlaySnake)
                      .addComponent(btnPlaySapper))

        );

        layout.linkSize(SwingConstants.HORIZONTAL, btnPlaySnake, btnPlaySapper);

        //Set up vertical Groups in layout
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(lblWidth)
                        .addComponent(textFieldWidth))
                .addGroup(layout.createParallelGroup()
                        .addComponent(lblHeight)
                        .addComponent(textFieldHeight))
                .addGroup(layout.createParallelGroup()
                        .addComponent(lblMinesCount)
                        .addComponent(textFieldMinesCount))
                .addGroup(layout.createParallelGroup()
                        .addComponent(btnPlaySnake)
                        .addComponent(btnPlaySapper))
        );

        //Button action listeners to run games
        btnPlaySnake.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RunSnake();
            }
        });
        btnPlaySapper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RunSapper();
            }
        });


        setResizable(false);
        pack();

        setTitle("Main Menu");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    /**
    *Loads high scores values from preferences located in registry.
    *  Snakes to snakeHighScore with snakeHighScoreKey,
     *  and Sappers to sappersHighScore with sapperHighScoreKey
    */
    private void loadHighScore(){
        Preferences highScore = Preferences.userNodeForPackage(MainMenu.class);
        snakeHighScore = highScore.getInt(snakeHighScoreKey,0);
        sapperHighScore = highScore.getInt(sapperHighScoreKey,0);

    }
    /**
     *Saves high scores values to preferences located in registry.
     *  snakeHighScore with snakeHighScoreKey as Snakes value,
     *  and sappersHighScore with sapperHighScoreKey as Sappers value
     */
    private void SaveHighScore() {
        Preferences highScore = Preferences.userNodeForPackage(MainMenu.class);
        highScore.putInt(snakeHighScoreKey,snakeHighScore);
        highScore.getInt(sapperHighScoreKey,sapperHighScore);
    }

    /**
     * Run Snake Game with parameters specified in main menu UI by the user
     * Adds listener to save high score when game is closed
     */
    private void RunSnake(){
        Snake snake = new Snake(snakeHighScore,Integer.parseInt(textFieldWidth.getText()), Integer.parseInt(textFieldHeight.getText()));
        EventQueue.invokeLater(() -> {
            JFrame ex = snake;
            ex.setVisible(true);
            ex.addWindowListener(new WindowListener() {

                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    snakeHighScore = snake.getBoard().getHighScore();
                    SaveHighScore();
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
        });
    }
    /**
     * Run Sapper Game with parameters specified in main menu UI by the user
     * Adds listener to save high score when game is closed
     */
    private void RunSapper(){
        Sapper sapper = new Sapper(sapperHighScore,Integer.parseInt(textFieldWidth.getText()),
                Integer.parseInt(textFieldHeight.getText()),Integer.parseInt(textFieldMinesCount.getText()));
        EventQueue.invokeLater(() -> {
            JFrame ex = sapper;
            ex.setVisible(true);
            ex.addWindowListener(new WindowListener() {

                @Override
                public void windowOpened(WindowEvent e) {

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    sapperHighScore = sapper.getBoard().getHighScore();
                    SaveHighScore();
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });
        });
    }

    /**
     * Main entry of program,
     * Creates MainMenu instance
     * @param args none values are accepted
     */
    public static void main(String[] args) {
        MainMenu menu = new MainMenu();
        EventQueue.invokeLater(() -> {
            JFrame ex = menu;
            ex.setVisible(true);
        });
    }
}
