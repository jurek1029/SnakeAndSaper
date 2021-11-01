import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

enum Directions {
    Up(0), Down(1) , Left(2), Right(3);
    private final int value;
    private Directions(int _value){ value = _value;}
    public int getValue(){return value;}
}

enum SnakeBGimgIndex {
    Tile0(0),Tile1(1),Horizontal(2),Vertical(3),
    LeftUpCorner(4),LeftDownCorner(5),RightUpCorner(6),RightDownCorner(7);
    private final int value;
    private SnakeBGimgIndex(int _value){ value = _value;}
    public int getValue(){return value;}
}

public class SnakeBoard extends JPanel implements ActionListener {

    private final int DOT_SIZE = 64; //size of one tile
    private final int TOP_BEVEL_HEIGHT = 50; //size of top bevel displaying score

    private final int DELAY = 300; //time delay in milliseconds for every snake move/ game update

    private final int T_NUM_X; //number of tiles in x direction
    private final int T_NUM_Y; //number of tiles in y direction
    private final int W_WIDTH; //window width in pixels
    private final int W_HEIGHT; //window height in pixels

    // array of snake's body positions where first element is head,
    // this will never be a very big array in FullHD monitor there will be only 30x16 ints allocated
    private final int[] snakeX;
    private final int[] snakeY;

    private int snakeLength;
    private int apple_x, apple_y;

    private int vx = 1,vy = 0; //velocities in x and y directions
    private Directions direction = Directions.Right; // current moving direction
    private Directions nextDirection = Directions.Right; // direction after next game update

    private boolean inGame = true;
    private Timer timer;
    private int highScore;

    // indexes of images are adequate to directions:
    // 0:up , 1:down, 2:left, 3:right
    private Image[] tail;
    private Image[] head;
    // indexes of images are adequate to directions:
    // 0:BodyVertical,1:BodyHorizontal,2:BodyLeftToUp,3:BodyLeftToDown,4:BodyRightToUp,5:BodyLeftToDown
    private Image[] body;
    private Image[] background;
    private Image apple;

    private int[] mapDirectionToImageIndex =   {1,0,0,2,0,
                                                4,5,0,1,0,
                                                3,3,0,2,4,
                                                0,0,0,2,3,
                                                0,5,0,0,1}; // mapping for function: 12 + DiffPreviousX + DiffNextX*2 + (DiffPreviousY + DiffNextY*2) * 4

    // need paths to be in order of: HeadUp,HeadDown,HeadLeft,HeadRight,TailUp,TailDown,TailLeft,TailRight,
    //                               BodyVertical,BodyHorizontal,BodyLeftToUp,BodyLeftToDown,BodyRightToUp,BodyLeftToDown
    private final String[] imagePathsSnake = {"src/resources/HU.png", "src/resources/HD.png", "src/resources/HL.png", "src/resources/HR.png", //Head
                                         "src/resources/TU.png","src/resources/TD.png","src/resources/TL.png","src/resources/TR.png",    //Tail
                                         "src/resources/RR.png","src/resources/UU.png",                                                  //Strait body
                                         "src/resources/LU.png","src/resources/LD.png","src/resources/RU.png","src/resources/RD.png",};  //Curve body
    private final String[] imagePathsBG = {"src/resources/BG-0.png", "src/resources/BG-1.png", "src/resources/BG-H.png", "src/resources/BG-V.png",
                                           "src/resources/BG-LU.png", "src/resources/BG-LD.png", "src/resources/BG-RU.png", "src/resources/BG-RD.png"};

    private Font t_font;
    private FontMetrics metr;


    public SnakeBoard(int _highScore, int boardWidth, int boardHeight) {
        highScore = _highScore;
        T_NUM_X = boardWidth;
        T_NUM_Y = boardHeight;
        W_WIDTH = DOT_SIZE * (T_NUM_X + 2);
        W_HEIGHT =  DOT_SIZE * (T_NUM_Y + 2) + TOP_BEVEL_HEIGHT;

        int ALL_DOTS = T_NUM_X * T_NUM_Y;
        snakeX = new int[ALL_DOTS];
        snakeY = new int[ALL_DOTS];

        initBoard();
    }

    private void initBoard() {
        addKeyListener(new TAdapter());
        setBackground(new Color(100, 144, 67));
        setFocusable(true);
        setPreferredSize(new Dimension(W_WIDTH, W_HEIGHT));

        t_font = new Font("Helvetica", Font.BOLD, 28);
        metr = getFontMetrics(t_font);
        loadImages();
        initGame();
    }


    private void loadImages() {
        head = new Image[4];
        tail = new Image[4];
        body = new Image[6];
        background = new Image[8];

        int i = 0;
        while(i < 4) head[i] = new ImageIcon(imagePathsSnake[i++]).getImage();
        while(i < 8) tail[i - 4] = new ImageIcon(imagePathsSnake[i++]).getImage();
        while(i < 14) body[i - 8] = new ImageIcon(imagePathsSnake[i++]).getImage();
        i = 0;
        for(String patch : imagePathsBG) background[i++] = new ImageIcon(patch).getImage();

        apple = new ImageIcon("src/resources/apple.png").getImage();
    }

    private void initGame() {
        snakeLength = 3;
        int XScreenCenter = (T_NUM_X - 1) / 2;
        int YScreenCenter = (T_NUM_Y - 1) / 2;

        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = XScreenCenter - i;
            snakeY[i] = YScreenCenter;
        }

        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private int GetBodyPartTypeIndex(int current){
        int px = (snakeX[current] - snakeX[current - 1]);
        int py = (snakeY[current] - snakeY[current - 1]);
        int nx = 0, ny = 0;
        if(current + 1 < snakeLength) {
            nx = (snakeX[current + 1] - snakeX[current]);
            ny = (snakeY[current + 1] - snakeY[current]);
        }
        int value = 12 + (px + nx*2 + (py + ny*2) * 4);
        if(value < 0 || value >= mapDirectionToImageIndex.length) {
            System.out.println("Something has gone terrible wrong with snake movement");
            return 0;
        }
        return mapDirectionToImageIndex[value];
    }
    private int getBackgroundOffset(){
        return TOP_BEVEL_HEIGHT;
    }
    private void DrawBackground(Graphics g){
        //Corners
        g.drawImage(background[SnakeBGimgIndex.LeftUpCorner.getValue()],0, getBackgroundOffset(),this);
        g.drawImage(background[SnakeBGimgIndex.LeftDownCorner.getValue()],0,(T_NUM_Y + 1) * DOT_SIZE + getBackgroundOffset(),this);
        g.drawImage(background[SnakeBGimgIndex.RightUpCorner.getValue()],(T_NUM_X + 1) * DOT_SIZE, getBackgroundOffset(),this);
        g.drawImage(background[SnakeBGimgIndex.RightDownCorner.getValue()],(T_NUM_X + 1) * DOT_SIZE,(T_NUM_Y + 1) * DOT_SIZE + getBackgroundOffset(),this);

        //Horizontal wall
        for(int i = 1; i < T_NUM_X + 1; i ++) {
            g.drawImage(background[SnakeBGimgIndex.Horizontal.getValue()], i * DOT_SIZE, getBackgroundOffset(), this);
            g.drawImage(background[SnakeBGimgIndex.Horizontal.getValue()], i * DOT_SIZE, (T_NUM_Y + 1) * DOT_SIZE + getBackgroundOffset(), this);
        }

        //Vertical wall
        for(int i = 1; i < T_NUM_Y + 1; i ++) {
            g.drawImage(background[SnakeBGimgIndex.Vertical.getValue()], 0, i * DOT_SIZE + getBackgroundOffset(), this);
            g.drawImage(background[SnakeBGimgIndex.Vertical.getValue()], (T_NUM_X + 1) * DOT_SIZE, i * DOT_SIZE + getBackgroundOffset(), this);
        }

        //Checkers pattern
        for(int i = 1; i < T_NUM_X + 1; i ++){
            for(int j = 1; j < T_NUM_Y + 1; j ++){
                if((i + j) % 2 == 0){
                    g.drawImage(background[SnakeBGimgIndex.Tile0.getValue()], i*DOT_SIZE, j*DOT_SIZE + getBackgroundOffset(), this);
                } else{
                    g.drawImage(background[SnakeBGimgIndex.Tile1.getValue()], i*DOT_SIZE, j*DOT_SIZE + getBackgroundOffset(), this);
                }
            }
        }
    }

    private void DrawMenu(Graphics g){
        String s_score = "Score: " + snakeLength;
        String s_highscore = "High score: " + highScore;

        g.setColor(Color.white);
        g.setFont(t_font);
        g.drawString(s_score, DOT_SIZE/2, (DOT_SIZE)/ 2);
        g.drawString(s_highscore, W_WIDTH  - (metr.stringWidth(s_highscore)) - DOT_SIZE/2, (DOT_SIZE)/ 2);

    }
    private int CalcWinX(int x){
        return (x + 1) * DOT_SIZE;
    }
    private int CalcWinY(int y){
        return (y + 1) * DOT_SIZE + getBackgroundOffset();
    }

    private void doDrawing(Graphics g) {
        DrawBackground(g);

        if (inGame) {
            DrawMenu(g);
            g.drawImage(apple, CalcWinX(apple_x), CalcWinY(apple_y), this);

            g.drawImage(head[nextDirection.getValue()], CalcWinX(snakeX[0]), CalcWinY(snakeY[0]), this);
            int i = 1;
            for (; i < snakeLength - 1; i++) {
                    g.drawImage(body[GetBodyPartTypeIndex(i)], CalcWinX(snakeX[i]), CalcWinY(snakeY[i]), this);
            }
            g.drawImage(tail[GetBodyPartTypeIndex(i)], CalcWinX(snakeX[i]), CalcWinY(snakeY[i]), this);

            Toolkit.getDefaultToolkit().sync();

        } else {
            gameOver(g);
        }
    }

    private void gameOver(Graphics g) {
        highScore = Math.max(snakeLength, highScore);
        DrawMenu(g);
        String msg = "Game Over \n  Score: " + snakeLength;

        g.setColor(Color.white);
        g.setFont(t_font);
        g.drawString(msg, (W_WIDTH - metr.stringWidth(msg)) / 2, (W_HEIGHT + getBackgroundOffset())/ 2 );


    }

    private void checkApple() {
        if ((snakeX[0] == apple_x) && (snakeY[0] == apple_y)) {
            snakeLength++;
            locateApple();
        }
    }

    private void move() {
        for (int i = snakeLength; i > 0; i--) {
            snakeX[i] = snakeX[(i - 1)];
            snakeY[i] = snakeY[(i - 1)];
        }
        snakeX[0] += vx;
        snakeY[0] += vy;

        direction = nextDirection;
    }

    private void checkCollision() {

        for (int i = snakeLength; i > 0; i--) {
            if ((snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                snakeLength = i;
                break;
            }
        }
        inGame &= snakeY[0] < T_NUM_Y && snakeY[0] >= 0;
        inGame &= snakeX[0] < T_NUM_X && snakeX[0] >= 0;

        if (!inGame) {
            timer.stop();
        }
    }

    private void locateApple() {
        apple_x = (int) (Math.random() * (T_NUM_X - 1));
        apple_y = (int) (Math.random() * (T_NUM_Y - 1));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }

        repaint();
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_LEFT) && (direction != Directions.Right)) {
                vy = 0;
                vx = -1;
                nextDirection = Directions.Left;
            }

            if ((key == KeyEvent.VK_RIGHT) && (direction != Directions.Left)) {
                vy = 0;
                vx = 1;
                nextDirection = Directions.Right;
            }

            if ((key == KeyEvent.VK_UP) && (direction != Directions.Down)) {
                vy = -1;
                vx = 0;
                nextDirection = Directions.Up;
            }

            if ((key == KeyEvent.VK_DOWN) && (direction != Directions.Up)) {
                vy = 1;
                vx = 0;
                nextDirection = Directions.Down;
            }
        }
    }

    public int getHighScore() {
        return highScore;
    }
}

