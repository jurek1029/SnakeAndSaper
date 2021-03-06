import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

/**
 * Enum of all directions that snake can move
 * that holds value representing index in 'head' array
 */
enum Directions {
    Up(0), Down(1) , Left(2), Right(3);
    private final int value;
    private Directions(int _value){ value = _value;}
    public int getValue(){return value;}
}

/**
 * Enum of all background tiles possible
 * that holds value representing index in 'background' array
 */
enum SnakeBGimgIndex {
    Tile0(0),Tile1(1),Horizontal(2),Vertical(3),
    LeftUpCorner(4),LeftDownCorner(5),RightUpCorner(6),RightDownCorner(7);
    private final int value;
    private SnakeBGimgIndex(int _value){ value = _value;}
    public int getValue(){return value;}
}

/**
 * Class responsible for showing snake Board and responding to user inputs
 */
public class SnakeBoard extends Board {

    // array of snake's body positions where first element is head,
    // this will never be a very big array in FullHD monitor there will be only 30x16 ints allocated
    private int[] snakeX;
    private int[] snakeY;

    private int snakeLength;
    private int apple_x, apple_y;

    private int vx = 1,vy = 0;                          //velocities in x and y directions
    private Directions direction = Directions.Right;    // current moving direction
    private Directions nextDirection = Directions.Right; // direction after next game update
    private boolean inGame = true;

    // indexes of images are adequate to directions:
    // 0:up , 1:down, 2:left, 3:right
    private Image[] tail;
    private Image[] head;
    // indexes of images are adequate to directions:
    // 0:BodyVertical,1:BodyHorizontal,2:BodyLeftToUp,3:BodyLeftToDown,4:BodyRightToUp,5:BodyLeftToDown
    private Image[] body;
    private Image[] background;
    private Image apple;

    // precomputed mapping for function: 12 + DiffPreviousX + DiffNextX*2 + (DiffPreviousY + DiffNextY*2) * 4 to index of snake graphic
    private final int[] mapDirectionToImageIndex =   {1,0,0,2,0,
                                                4,5,0,1,0,
                                                3,3,0,2,4,
                                                0,0,0,2,3,
                                                0,5,0,0,1};

    // need paths to be in order of: HeadUp,HeadDown,HeadLeft,HeadRight,TailUp,TailDown,TailLeft,TailRight,
    //                               BodyVertical,BodyHorizontal,BodyLeftToUp,BodyLeftToDown,BodyRightToUp,BodyLeftToDown
    private static final String[] imagePathsSnake = {"resources/HU.png", "resources/HD.png", "resources/HL.png", "resources/HR.png", //Head
            "resources/TU.png", "resources/TD.png", "resources/TL.png", "resources/TR.png",    //Tail
            "resources/RR.png", "resources/UU.png",                                                  //Strait body
            "resources/LU.png", "resources/LD.png", "resources/RU.png", "resources/RD.png",};  //Curve body
    private static final String[] imagePathsBG = {"resources/BG-0.png", "resources/BG-1.png", "resources/BG-H.png", "resources/BG-V.png",
            "resources/BG-LU.png", "resources/BG-LD.png", "resources/BG-RU.png", "resources/BG-RD.png"};


    /**
     * public constructor for SnakeBoard calls base class constructor (Board)
     * @param _highScore high score value of the game
     * @param boardWidth width of the playable game board in number of tiles
     * @param boardHeight height of the playable game board in number of tiles
     */
    public SnakeBoard(int _highScore, int boardWidth, int boardHeight) {
        super(_highScore, boardWidth, boardHeight, 0);
    }

    @Override
    protected void SetWindowSize() {
        DOT_SIZE = 64;          //size of one tile
        TOP_BEVEL_HEIGHT = 50;  //size of top bevel displaying score
        W_WIDTH = DOT_SIZE * (T_NUM_X + 2); // +2 for bevels
        W_HEIGHT =  DOT_SIZE * (T_NUM_Y + 2) + TOP_BEVEL_HEIGHT; // +2 for bevels
    }

    @Override
    protected void customBoardInit() {
        DELAY = 300; //time delay in milliseconds for every snake move/ game update

        int ALL_DOTS = T_NUM_X * T_NUM_Y;
        snakeX = new int[ALL_DOTS];
        snakeY = new int[ALL_DOTS];

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                // change direction of snake movement according to key pressed, not allowing to move in backwards direction
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
        });
    }

    @Override
    protected void loadImages() {
        head = new Image[4];
        tail = new Image[4];
        body = new Image[6];
        background = new Image[8];

        //Load images in paths('imagePathsSnake') from resources to corresponding arrays
        ClassLoader cl = this.getClass().getClassLoader();
        int i = 0;
        while(i < 4) head[i] = new ImageIcon(cl.getResource(imagePathsSnake[i++])).getImage();
        while(i < 8) tail[i - 4] = new ImageIcon(cl.getResource(imagePathsSnake[i++])).getImage();
        while(i < 14) body[i - 8] = new ImageIcon(cl.getResource(imagePathsSnake[i++])).getImage();
        i = 0;
        for(String path : imagePathsBG) background[i++] = new ImageIcon(cl.getResource(path)).getImage();

        //Load apple image
        apple = new ImageIcon(cl.getResource("resources/apple.png")).getImage();
    }

    @Override
    protected void initGame() {
        snakeLength = 3;
        int XScreenCenter = (T_NUM_X - 1) / 2;
        int YScreenCenter = (T_NUM_Y - 1) / 2;

        //Create snake in center with snake length of 3
        for (int i = 0; i < snakeLength; i++) {
            snakeX[i] = XScreenCenter - i;
            snakeY[i] = YScreenCenter;
        }

        locateApple();
    }

    /**
     *Function to get proper index for graphics depending on the previous and next snake tile
     * it assumes to be used on tile different then head
     * @param current index of tile to calculate graphics index
     * @return index in 'body' array of graphics for current snake tile
     */
    private int GetBodyPartTypeIndex(int current){
        //calculate difference between previous tile and current
        int px = (snakeX[current] - snakeX[current - 1]);
        int py = (snakeY[current] - snakeY[current - 1]);
        // if next tile exist calculate difference between current and next
        int nx = 0, ny = 0;
        if(current + 1 < snakeLength) {
            nx = (snakeX[current + 1] - snakeX[current]);
            ny = (snakeY[current + 1] - snakeY[current]);
        }
        //function to generate correct value for 'mapDirectionToImageIndex' array
        int value = 12 + (px + nx*2 + (py + ny*2) * 4);
        //if somehow it got wrong print error
        if(value < 0 || value >= mapDirectionToImageIndex.length) {
            System.out.println("Something has gone terrible wrong with snake movement");
            return 0;
        }
        return mapDirectionToImageIndex[value];
    }

    /**
     * Function to draw snake background borders
     * @param g graphic context to draw elements
     */
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

    /**
     * Funciton to draw snake menu displaying score and current snake length
     * @param g graphic context to draw elements
     */
    private void DrawMenu(Graphics g){
        String s_score = "Score: " + snakeLength;
        String s_highscore = "High score: " + highScore;

        g.setColor(Color.white);
        g.setFont(t_font);
        g.drawString(s_score, DOT_SIZE/2, (DOT_SIZE)/ 2);
        g.drawString(s_highscore, W_WIDTH  - (metr.stringWidth(s_highscore)) - DOT_SIZE/2, (DOT_SIZE)/ 2);

    }
    @Override
    protected void doDrawing(Graphics g) {
        DrawBackground(g);

        if (inGame) {
            DrawMenu(g);
            //draw apple
            g.drawImage(apple, CalcWinX(apple_x), CalcWinY(apple_y), this);
            //draw head
            g.drawImage(head[nextDirection.getValue()], CalcWinX(snakeX[0]), CalcWinY(snakeY[0]), this);
            //draw body
            int i = 1;
            for (; i < snakeLength - 1; i++) {
                    g.drawImage(body[GetBodyPartTypeIndex(i)], CalcWinX(snakeX[i]), CalcWinY(snakeY[i]), this);
            }
            //draw tail
            g.drawImage(tail[GetBodyPartTypeIndex(i)], CalcWinX(snakeX[i]), CalcWinY(snakeY[i]), this);

            //sync graphics state for accurate graphic
            Toolkit.getDefaultToolkit().sync();

        } else {
            gameOver(g);
        }
    }

    /**
     * Function to draw game over screen
     * @param g graphic context to draw elements
     */
    private void gameOver(Graphics g) {
        highScore = Math.max(snakeLength, highScore);
        DrawMenu(g);
        String msg = "Game Over \n  Score: " + snakeLength;

        g.setColor(Color.white);
        g.setFont(t_font);
        g.drawString(msg, (W_WIDTH - metr.stringWidth(msg)) / 2, (W_HEIGHT + getBackgroundOffset())/ 2 );


    }

    /**
     * Function to test for collision with apple
     */
    private void checkApple() {
        //check if snake head is on same coordinates as apple
        if ((snakeX[0] == apple_x) && (snakeY[0] == apple_y)) {
            snakeLength++;
            locateApple();
        }
    }

    /**
     * Function to perform snake move update, depending on it current velocity
     */
    private void move() {
        //update all body segments with value of previous one
        for (int i = snakeLength; i > 0; i--) {
            snakeX[i] = snakeX[(i - 1)];
            snakeY[i] = snakeY[(i - 1)];
        }
        //move head
        snakeX[0] += vx;
        snakeY[0] += vy;

        // update direction
        direction = nextDirection;
    }

    /**
     * Function to check for snake collision with its self and border walls
     */
    private void checkCollision() {
        //check for collision with its self
        for (int i = snakeLength; i > 0; i--) {
            if ((snakeX[0] == snakeX[i]) && (snakeY[0] == snakeY[i])) {
                snakeLength = i;
                break;
            }
        }
        //check collision with bordes
        inGame &= snakeY[0] < T_NUM_Y && snakeY[0] >= 0;
        inGame &= snakeX[0] < T_NUM_X && snakeX[0] >= 0;

        if (!inGame) {
            timer.stop();
        }
    }

    /**
     * Function to generate new apple position
     */
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
}


