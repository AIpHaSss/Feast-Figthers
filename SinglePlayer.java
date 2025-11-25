import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.ImageIcon;
import javax.swing.Timer;

public class SinglePlayer extends JPanel implements ActionListener, KeyListener, MouseListener {

    private int playerX, playerY;
    private int itemX, itemY;
    private int score = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private boolean initialized = false;

    private boolean paused = false;

    private int enemyCount = 3;
    private final int MAX_ENEMIES = 50;
    private final int ENEMY_SIZE = 30;
    private final int ITEM_SIZE = 40;

    private int[] enemyX = new int[MAX_ENEMIES];
    private int[] enemyY = new int[MAX_ENEMIES];
    private int[] enemySpeedX = new int[MAX_ENEMIES];
    private int[] enemySpeedY = new int[MAX_ENEMIES];

    private Timer timer;
    private Random rand = new Random();

    private int playerSize = 45;

    private Image backgroundImage;

    // Blue player images (icon fallback)
    private Image playerBlueImage;
    private Image playerBlueLeftImage;
    private ImageIcon playerBlueIcon;
    private ImageIcon playerBlueLeftIcon;

    // Banana image
    private Image bananaImage;

    // Facing direction flag
    private boolean playerFacingRight = true;

    private boolean upPressed, downPressed, leftPressed, rightPressed;

    private int timeLeft = 120;
    private long lastTimeCheck = System.currentTimeMillis();

    private Rectangle pauseButton = new Rectangle(20, 20, 80, 35);

    private int starX, starY;
    private final int STAR_SIZE = 22;
    private boolean starActive = false;

    private int speedBoost = 0;
    private long boostEnd = 0;
    private int lastMilestone = 0;

    public SinglePlayer() {
        setFocusable(true);
        setBackground(Color.BLACK);
        addKeyListener(this);
        addMouseListener(this);
        timer = new Timer(30, this);
        timer.start();

        try {
            backgroundImage = ImageIO.read(new File("BG.png"));
            System.out.println("Background loaded successfully!");
        } catch (IOException ex) {
            System.out.println("Background image not found: " + ex.getMessage());
        }

        // load blue static fallback
        try {
            playerBlueImage = ImageIO.read(new File("PlayerBlueRight.gif"));
        } catch (IOException ex) {
            playerBlueImage = null;
        }
        try {
            playerBlueLeftImage = ImageIO.read(new File("PlayerBlueLeft.gif"));
        } catch (IOException ex) {
            playerBlueLeftImage = null;
        }

        // load banana
        try {
            bananaImage = ImageIO.read(new File("Banana.png"));
        } catch (IOException ex) {
            bananaImage = null;
        }

        // animated blue icons if present
        File blueFile = new File("PlayerBlueRight.gif");
        if (blueFile.exists()) playerBlueIcon = new ImageIcon("PlayerBlueRight.gif");
        else playerBlueIcon = null;

        File blueLeftFile = new File("PlayerBlueLeft.gif");
        if (blueLeftFile.exists()) playerBlueLeftIcon = new ImageIcon("PlayerBlueLeft.gif");
        else playerBlueLeftIcon = playerBlueIcon;

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

        playerX = centerX - 20;
        playerY = centerY;

        itemX = centerX - ITEM_SIZE / 2;
        itemY = centerY - 60;

        for (int i = 0; i < enemyCount; i++) {
            int distance = 200 + rand.nextInt(200);
            int angle = rand.nextInt(360);
            enemyX[i] = centerX + (int)(Math.cos(Math.toRadians(angle)) * distance);
            enemyY[i] = centerY + (int)(Math.sin(Math.toRadians(angle)) * distance);
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

        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);
        g2.setColor(Color.WHITE);
        g2.drawRect(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);
        g2.drawString(paused ? "Resume" : "Pause", pauseButton.x + 10, pauseButton.y + 23);

        if (!gameOver) {

            // draw item/banana
            if (bananaImage != null) {
                g2.drawImage(bananaImage, itemX, itemY, ITEM_SIZE, ITEM_SIZE, this);
            } else {
                g2.setColor(Color.YELLOW);
                g2.fillOval(itemX, itemY, ITEM_SIZE, ITEM_SIZE);
            }

            // star
            if (starActive) {
                g2.setColor(Color.BLACK);
                g2.fillOval(starX, starY, STAR_SIZE, STAR_SIZE);
                g2.setColor(Color.WHITE);
                g2.drawOval(starX, starY, STAR_SIZE, STAR_SIZE);
            }

            // enemies
            g2.setColor(Color.RED);
            for (int i = 0; i < enemyCount; i++) {
                g2.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
            }

            // draw player (blue) based on facing and available icons
            if (playerFacingRight) {
                if (playerBlueIcon != null) {
                    Image img = playerBlueIcon.getImage();
                    g2.drawImage(img, playerX, playerY, playerSize, playerSize, this);
                } else if (playerBlueImage != null) {
                    g2.drawImage(playerBlueImage, playerX, playerY, playerSize, playerSize, this);
                } else {
                    drawPlayerCircle(g2, playerX, playerY, playerSize, Color.BLUE);
                }
            } else {
                if (playerBlueLeftIcon != null) {
                    Image img = playerBlueLeftIcon.getImage();
                    g2.drawImage(img, playerX, playerY, playerSize, playerSize, this);
                } else if (playerBlueLeftImage != null) {
                    g2.drawImage(playerBlueLeftImage, playerX, playerY, playerSize, playerSize, this);
                } else if (playerBlueImage != null) {
                    g2.drawImage(playerBlueImage, playerX, playerY, playerSize, playerSize, this);
                } else {
                    drawPlayerCircle(g2, playerX, playerY, playerSize, Color.BLUE);
                }
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("Score: " + score, 20, 80);

            g2.drawString("Time Left: " + timeLeft + "s", getWidth() / 2 - 50, 40);

            if (paused) {
                g2.setFont(new Font("Arial", Font.BOLD, 60));
                g2.drawString("PAUSED", getWidth() / 2 - 140, getHeight() / 2);
                return;
            }

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
            }

            g2.drawString("Final Score: " + score, getWidth() / 2 - 150, getHeight() / 2);
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
        if (!initialized || gameOver || paused) return;

        long now = System.currentTimeMillis();

        if (gameStarted) {
            if (now - lastTimeCheck >= 1000) {
                timeLeft--;
                lastTimeCheck = now;
                if (timeLeft <= 0) {
                    gameOver = true;
                    timer.stop();
                }
            }
        }

        if (speedBoost > 0 && now >= boostEnd) {
            speedBoost = 0;
            boostEnd = 0;
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

            double moveSpeed = 8;
            int pSpeed = (int)(moveSpeed + speedBoost);

            if (upPressed && playerY > 0) playerY -= pSpeed;
            if (downPressed && playerY < getHeight() - playerSize) playerY += pSpeed;
            if (leftPressed && playerX > 0) playerX -= pSpeed;
            if (rightPressed && playerX < getWidth() - playerSize) playerX += pSpeed;

            Rectangle playerRect = new Rectangle(playerX, playerY, playerSize, playerSize);

            Rectangle itemRect = new Rectangle(itemX, itemY, ITEM_SIZE, ITEM_SIZE);

            int before = score;
            if (playerRect.intersects(itemRect)) {
                score++;
                playerSize += 5;
                respawnItem();
            }
            int after = score;

            int milestoneNow = after / 5;
            if (milestoneNow > lastMilestone && milestoneNow > 0) {
                lastMilestone = milestoneNow;
                respawnStar();
                starActive = true;
            }

            if (starActive) {
                Rectangle starRect = new Rectangle(starX, starY, STAR_SIZE, STAR_SIZE);
                if (playerRect.intersects(starRect)) {
                    speedBoost += 9;
                    boostEnd = System.currentTimeMillis() + 5000;
                    starActive = false;
                }
            }

            if (after / 5 > before / 5 && enemyCount < MAX_ENEMIES) {
                spawnNewEnemy();
            }

            for (int i = 0; i < enemyCount; i++) {
                Rectangle enemyRect = new Rectangle(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
                if (playerRect.intersects(enemyRect)) {
                    gameOver = true;
                    timer.stop();
                }
            }
        }

        repaint();
    }

    private void respawnItem() {
        itemX = rand.nextInt(Math.max(1, getWidth() - ITEM_SIZE));
        itemY = rand.nextInt(Math.max(1, getHeight() - ITEM_SIZE));
    }

    private void respawnStar() {
        starX = rand.nextInt(Math.max(1, getWidth() - STAR_SIZE));
        starY = rand.nextInt(Math.max(1, getHeight() - STAR_SIZE));
    }

    private void spawnNewEnemy() {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int distance = 250 + rand.nextInt(250);
        int angle = rand.nextInt(360);
        enemyX[enemyCount] = centerX + (int)(Math.cos(Math.toRadians(angle)) * distance);
        enemyY[enemyCount] = centerY + (int)(Math.sin(Math.toRadians(angle)) * distance);
        enemySpeedX[enemyCount] = rand.nextInt(3) + 4;
        enemySpeedY[enemyCount] = rand.nextInt(3) + 4;
        enemyCount++;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (!gameStarted && !gameOver) {
            gameStarted = true;
            lastTimeCheck = System.currentTimeMillis();
        }

        if (code == KeyEvent.VK_ESCAPE && !gameOver) {
            paused = !paused;
            repaint();
            return;
        }

        if (!gameOver && !paused) {
            if (code == KeyEvent.VK_UP) upPressed = true;
            if (code == KeyEvent.VK_DOWN) downPressed = true;
            if (code == KeyEvent.VK_LEFT) {
                leftPressed = true;
                playerFacingRight = false;
            }
            if (code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
                playerFacingRight = true;
            }
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
    }

    @Override public void keyTyped(KeyEvent e) {}

    private void resetGame() {
        score = 0;
        playerSize = 45;
        enemyCount = 3;
        gameOver = false;
        gameStarted = false;
        paused = false;
        timeLeft = 120;
        lastTimeCheck = System.currentTimeMillis();

        speedBoost = 0;
        boostEnd = 0;
        lastMilestone = 0;
        starActive = false;

        playerFacingRight = true;

        centerObjects();
        timer.start();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (pauseButton.contains(e.getPoint()) && !gameOver) {
            paused = !paused;
            repaint();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
