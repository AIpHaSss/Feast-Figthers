import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class MiniAdventureStickman extends JPanel implements ActionListener, KeyListener {

    private int player1X, player1Y;
    private int player2X, player2Y;
    private int itemX, itemY;
    private int score1 = 0, score2 = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private boolean initialized = false;

    private int enemyCount = 3;
    private final int MAX_ENEMIES = 50;
    private final int ENEMY_SIZE = 30;
    private final int ITEM_SIZE = 20;

    private int[] enemyX = new int[MAX_ENEMIES];
    private int[] enemyY = new int[MAX_ENEMIES];
    private int[] enemySpeedX = new int[MAX_ENEMIES];
    private int[] enemySpeedY = new int[MAX_ENEMIES];

    private Timer timer;
    private Random rand = new Random();

    private int player1Size = 30;
    private int player2Size = 30;

    private Image backgroundImage;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean wPressed, sPressed, aPressed, dPressed;

    private int timeLeft = 120;
    private long lastTimeCheck = System.currentTimeMillis();

    public MiniAdventureStickman() {
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(this);
        timer = new Timer(30, this);
        timer.start();

        try {
            backgroundImage = ImageIO.read(new File("background.jpg"));
            System.out.println("Background loaded successfully!");
        } catch (IOException ex) {
            System.out.println("Background image not found: " + ex.getMessage());
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!initialized && getWidth() > 0 && getHeight() > 0) {
                    centerObjects();
                    initialized = true;
                }
            }
        });
    }

    private void centerObjects() {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        player1X = centerX - 100;
        player1Y = centerY;
        player2X = centerX + 60;
        player2Y = centerY;

        itemX = centerX - ITEM_SIZE / 2;
        itemY = centerY - 60;

        for (int i = 0; i < enemyCount; i++) {
            int distance = 200 + rand.nextInt(200);
            int angle = rand.nextInt(360);
            enemyX[i] = centerX + (int) (Math.cos(Math.toRadians(angle)) * distance);
            enemyY[i] = centerY + (int) (Math.sin(Math.toRadians(angle)) * distance);
            enemySpeedX[i] = rand.nextInt(3) + 1;
            enemySpeedY[i] = rand.nextInt(3) + 1;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!initialized) return;

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        Graphics2D g2 = (Graphics2D) g;

        if (!gameOver) {

            g2.setColor(Color.YELLOW);
            g2.fillOval(itemX, itemY, ITEM_SIZE, ITEM_SIZE);


            g2.setColor(Color.RED);
            for (int i = 0; i < enemyCount; i++) {
                g2.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
            }

            drawPlayerCircle(g2, player1X, player1Y, player1Size, Color.BLUE);
            drawPlayerCircle(g2, player2X, player2Y, player2Size, Color.PINK);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("P1 Score: " + score1, 20, 40);
            g2.drawString("P2 Score: " + score2, getWidth() - 180, 40);


            g2.drawString("Time Left: " + timeLeft + "s", getWidth() / 2 - 50, 40);

            if (!gameStarted) {
                g2.setFont(new Font("Arial", Font.BOLD, 32));
                g2.drawString("Press any key to start!", getWidth() / 2 - 200, getHeight() / 2);
            }
        } else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 48));
            g2.drawString("GAME OVER", getWidth() / 2 - 150, getHeight() / 2 - 100);
            g2.setFont(new Font("Arial", Font.PLAIN, 28));

            if (timeLeft <= 0) {
                g2.drawString("⏰ Time's Up!", getWidth() / 2 - 100, getHeight() / 2 - 50);

                String winnerText;
                if (score1 > score2) {
                    winnerText = "Player 1 wins!";
                } else if (score2 > score1) {
                    winnerText = "Player 2 wins!";
                } else {
                    winnerText = "It's a tie!";
                }
                g2.drawString(winnerText, getWidth() / 2 - 100, getHeight() / 2 + 80);
            }

            g2.drawString("P1 Final Score: " + score1, getWidth() / 2 - 150, getHeight() / 2);
            g2.drawString("P2 Final Score: " + score2, getWidth() / 2 - 150, getHeight() / 2 + 40);
            g2.drawString("Press R to Restart", getWidth() / 2 - 150, getHeight() / 2 + 120);
        }
    }

    private void drawPlayerCircle(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        g2.fillOval(x, y, size, size);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, size, size);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!initialized || gameOver) return;

        if (gameStarted) {
            long now = System.currentTimeMillis();
            if (now - lastTimeCheck >= 1000) {
                timeLeft--;
                lastTimeCheck = now;
                if (timeLeft <= 0) {
                    gameOver = true;
                    timer.stop();
                }
            }
        }

        if (gameStarted) {
            for (int i = 0; i < enemyCount; i++) {
                enemyX[i] += enemySpeedX[i];
                enemyY[i] += enemySpeedY[i];
                if (enemyX[i] < 0 || enemyX[i] > getWidth() - ENEMY_SIZE)
                    enemySpeedX[i] = -enemySpeedX[i];
                if (enemyY[i] < 0 || enemyY[i] > getHeight() - ENEMY_SIZE)
                    enemySpeedY[i] = -enemySpeedY[i];
            }

            int moveSpeed = 8;
            if (upPressed && player1Y > 0) player1Y -= moveSpeed;
            if (downPressed && player1Y < getHeight() - player1Size) player1Y += moveSpeed;
            if (leftPressed && player1X > 0) player1X -= moveSpeed;
            if (rightPressed && player1X < getWidth() - player1Size) player1X += moveSpeed;
            if (wPressed && player2Y > 0) player2Y -= moveSpeed;
            if (sPressed && player2Y < getHeight() - player2Size) player2Y += moveSpeed;
            if (aPressed && player2X > 0) player2X -= moveSpeed;
            if (dPressed && player2X < getWidth() - player2Size) player2X += moveSpeed;

            Rectangle p1Rect = new Rectangle(player1X, player1Y, player1Size, player1Size);
            Rectangle p2Rect = new Rectangle(player2X, player2Y, player2Size, player2Size);

            if (p1Rect.intersects(p2Rect)) {
                int overlapX = Math.min(player1X + player1Size, player2X + player2Size)
                             - Math.max(player1X, player2X);
                int overlapY = Math.min(player1Y + player1Size, player2Y + player2Size)
                             - Math.max(player1Y, player2Y);

                if (overlapX < overlapY) {
                    if (player1X < player2X) {
                        player1X -= overlapX / 2;
                        player2X += overlapX / 2;
                    } else {
                        player1X += overlapX / 2;
                        player2X -= overlapX / 2;
                    }
                } else {
                    if (player1Y < player2Y) {
                        player1Y -= overlapY / 2;
                        player2Y += overlapY / 2;
                    } else {
                        player1Y += overlapY / 2;
                        player2Y -= overlapY / 2;
                    }
                }
            }
        }

        Rectangle itemRect = new Rectangle(itemX, itemY, ITEM_SIZE, ITEM_SIZE);
        Rectangle p1Rect = new Rectangle(player1X, player1Y, player1Size, player1Size);
        Rectangle p2Rect = new Rectangle(player2X, player2Y, player2Size, player2Size);

        int totalBefore = score1 + score2;

        if (p1Rect.intersects(itemRect)) {
            score1++;
            player1Size += 5;
            respawnItem();
        }
        if (p2Rect.intersects(itemRect)) {
            score2++;
            player2Size += 5;
            respawnItem();
        }

        int totalAfter = score1 + score2;
        if (totalAfter / 5 > totalBefore / 5 && enemyCount < MAX_ENEMIES) {
            spawnNewEnemy();
        }

        for (int i = 0; i < enemyCount; i++) {
            Rectangle enemyRect = new Rectangle(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
            if (p1Rect.intersects(enemyRect) || p2Rect.intersects(enemyRect)) {
                gameOver = true;
                timer.stop();
            }
        }

        repaint();
    }

    private void respawnItem() {
        itemX = rand.nextInt(Math.max(1, getWidth() - ITEM_SIZE));
        itemY = rand.nextInt(Math.max(1, getHeight() - ITEM_SIZE));
    }

    private void spawnNewEnemy() {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int distance = 250 + rand.nextInt(250);
        int angle = rand.nextInt(360);
        enemyX[enemyCount] = centerX + (int) (Math.cos(Math.toRadians(angle)) * distance);
        enemyY[enemyCount] = centerY + (int) (Math.sin(Math.toRadians(angle)) * distance);
        enemySpeedX[enemyCount] = rand.nextInt(3) + 1 + 3; 
        enemySpeedY[enemyCount] = rand.nextInt(3) + 1 + 3;
        enemyCount++;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (!gameStarted && !gameOver) {
            gameStarted = true;
            lastTimeCheck = System.currentTimeMillis();
        }

        if (!gameOver) {
            if (code == KeyEvent.VK_UP) upPressed = true;
            if (code == KeyEvent.VK_DOWN) downPressed = true;
            if (code == KeyEvent.VK_LEFT) leftPressed = true;
            if (code == KeyEvent.VK_RIGHT) rightPressed = true;

            if (code == KeyEvent.VK_W) wPressed = true;
            if (code == KeyEvent.VK_S) sPressed = true;
            if (code == KeyEvent.VK_A) aPressed = true;
            if (code == KeyEvent.VK_D) dPressed = true;
        } else if (code == KeyEvent.VK_R) {
            resetGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) upPressed = false;
        if (code == KeyEvent.VK_DOWN) downPressed = false;
        if (code == KeyEvent.VK_LEFT) leftPressed = false;
        if (code == KeyEvent.VK_RIGHT) rightPressed = false;

        if (code == KeyEvent.VK_W) wPressed = false;
        if (code == KeyEvent.VK_S) sPressed = false;
        if (code == KeyEvent.VK_A) aPressed = false;
        if (code == KeyEvent.VK_D) dPressed = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    private void resetGame() {
        score1 = 0;
        score2 = 0;
        player1Size = 30;
        player2Size = 30;
        enemyCount = 3;
        gameOver = false;
        gameStarted = false;
        timeLeft = 120;
        lastTimeCheck = System.currentTimeMillis();
        centerObjects();
        timer.start();
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mini Adventure Circles");
        MiniAdventureStickman game = new MiniAdventureStickman();
        frame.add(game);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        frame.setUndecorated(true);
        gd.setFullScreenWindow(frame);

        frame.setVisible(true);
    }
}
