import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MainMenu extends JFrame{
    private static int snakeHighScore = 0, sapperHighScore = 0;
    private static String scoreFilePath = "src/resources/score.txt";

    JTextField textFieldWidth = new JTextField("11");
    JTextField textFieldHeight = new JTextField("11");
    JTextField textFieldMinesCount = new JTextField("20");

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

    private static void loadHighScore(){
        File file = new File(scoreFilePath);
        try {
            if(file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.write("0 0");
                writer.close();
            }
            else {
                Scanner sc = new Scanner(file);
                snakeHighScore = sc.nextInt();
                sapperHighScore = sc.nextInt();
            }
        }
        catch (NoSuchElementException e){
            System.out.println("high score file error: wrong format");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void SaveHighScore() {
        File file = new File(scoreFilePath);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(String.format("%d %d", snakeHighScore, sapperHighScore));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static void main(String[] args) {
        loadHighScore();
        //RunSnake();
        MainMenu menu = new MainMenu();
        menu.initUI();
        EventQueue.invokeLater(() -> {
            JFrame ex = menu;
            ex.setVisible(true);
        });
    }
}
