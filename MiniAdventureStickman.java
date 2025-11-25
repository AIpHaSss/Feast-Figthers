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

public class MiniAdventureStickman extends JPanel implements ActionListener, KeyListener, MouseListener {

    private int player1X, player1Y;
    private int player2X, player2Y;
    private int itemX, itemY;
    private int score1 = 0, score2 = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private boolean initialized = false;

    private boolean paused = false;

    private int enemyCount = 3;
    private final int MAX_ENEMIES = 50;
    private final int ENEMY_SIZE = 40;
    private final int ITEM_SIZE = 40;

    private int[] enemyX = new int[MAX_ENEMIES];
    private int[] enemyY = new int[MAX_ENEMIES];
    private int[] enemySpeedX = new int[MAX_ENEMIES];
    private int[] enemySpeedY = new int[MAX_ENEMIES];

    private Timer timer;
    private Random rand = new Random();

    private int player1Size = 45;
    private int player2Size = 45;

    private Image backgroundImage;

    // ⭐ NEW — PLAYER BLUE Image fallbacks
    private Image playerBlueImage;
    private Image playerBlueLeftImage;

    // ⭐ NEW — BANANA PNG IMAGE
    private Image bananaImage;

    // ⭐ NEW — BANANA EXPLOSION GIF (you can still add this file manually — it's unused by default)
    private ImageIcon bananaExplosionIcon;
    private boolean explosionActive = false;
    private int explosionX = 0, explosionY = 0;
    private final int explosionDuration = 600; // ms

    // NEW: a dedicated single-shot timer for the explosion (non-blocking)
    private Timer explosionTimer;

    // item visibility flag (hidden while explosion plays) — still kept for compatibility if needed
    private boolean itemVisible = true;

    // ⭐ NEW — PLAYER RED AS ANIMATED GIF (use ImageIcon to preserve animation)
    private ImageIcon playerRedIcon;
    private ImageIcon playerRedLeftIcon;

    // ⭐ NEW — PLAYER BLUE AS ANIMATED GIF (ImageIcon) - ADDED
    private ImageIcon playerBlueIcon;
    private ImageIcon playerBlueLeftIcon;

    // ⭐ NEW — ENEMY (MOSQUITO) GIF
    private ImageIcon mosqIcon;

    // Facing direction flags
    private boolean player1FacingRight = true;
    private boolean player2FacingRight = true;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean wPressed, sPressed, aPressed, dPressed;

    private int timeLeft = 120;
    private long lastTimeCheck = System.currentTimeMillis();

    private Rectangle pauseButton = new Rectangle(20, 20, 80, 35);

    private int starX, starY;
    private final int STAR_SIZE = 22;
    private boolean starActive = false;

    private int p1SpeedBoost = 0;
    private int p2SpeedBoost = 0;
    private long p1BoostEnd = 0;
    private long p2BoostEnd = 0;
    private int lastMilestone = 0;

    public MiniAdventureStickman() {
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

        // ⭐ LOAD PLAYER BLUE IMAGE (right) - fallback using ImageIO (non-animated)
        try {
            playerBlueImage = ImageIO.read(new File("PlayerBlueRight.gif"));
            System.out.println("PlayerBlueRight (gif) loaded as Image fallback!");
        } catch (IOException ex) {
            System.out.println("PlayerBlueRight image not found. Falling back to circle.");
        }

        // ⭐ LOAD PLAYER BLUE IMAGE (left) - fallback
        try {
            playerBlueLeftImage = ImageIO.read(new File("PlayerBlueLeft.gif"));
            System.out.println("PlayerBlueLeft (gif) loaded as Image fallback!");
        } catch (IOException ex) {
            System.out.println("PlayerBlueLeft image not found. Will use right-facing image or circle.");
        }

        // ⭐ LOAD BANANA IMAGE
        try {
            bananaImage = ImageIO.read(new File("Banana.png"));
            System.out.println("Banana loaded!");
        } catch (IOException ex) {
            System.out.println("Banana image not found. Using yellow circle instead.");
        }

        // ⭐ LOAD BANANA EXPLOSION GIF (if exists) - name this file exactly "BananaExplosion.gif"
        File explosionFile = new File("BananaExplosion.gif");
        if (explosionFile.exists()) {
            bananaExplosionIcon = new ImageIcon("BananaExplosion.gif");
            System.out.println("BananaExplosion (gif) loaded!");
        } else {
            bananaExplosionIcon = null;
            System.out.println("BananaExplosion.gif not found. Explosion will be a simple flash.");
        }

        // ⭐ LOAD PLAYER RED GIF (right)
        File redFile = new File("PlayerRedRight.gif");
        if (redFile.exists()) {
            playerRedIcon = new ImageIcon("PlayerRedRight.gif");
            System.out.println("PlayerRedRight (gif) loaded!");
        } else {
            playerRedIcon = null;
            System.out.println("PlayerRedRight image not found. Falling back to pink circle.");
        }

        // ⭐ LOAD PLAYER RED GIF (left) - if no separate left gif, reuse right gif
        File redLeftFile = new File("PlayerRedLeft.gif");
        if (redLeftFile.exists()) {
            playerRedLeftIcon = new ImageIcon("PlayerRedLeft.gif");
            System.out.println("PlayerRedLeft (gif) loaded!");
        } else {
            playerRedLeftIcon = playerRedIcon; // reuse same gif if left not provided
            if (playerRedLeftIcon != null) System.out.println("PlayerRedLeft uses right gif.");
            else System.out.println("PlayerRedLeft image not found. Will use circle.");
        }

        // ⭐ LOAD PLAYER BLUE GIFS (Animated) - NEW: uses ImageIcon to preserve animation
        File blueFile = new File("PlayerBlueRight.gif");
        if (blueFile.exists()) {
            playerBlueIcon = new ImageIcon("PlayerBlueRight.gif");
            System.out.println("PlayerBlueRight (gif) loaded as animated ImageIcon!");
        } else {
            playerBlueIcon = null;
            System.out.println("PlayerBlueRight (animated) not found. Will use static fallback or circle.");
        }

        File blueLeftFile = new File("PlayerBlueLeft.gif");
        if (blueLeftFile.exists()) {
            playerBlueLeftIcon = new ImageIcon("PlayerBlueLeft.gif");
            System.out.println("PlayerBlueLeft (gif) loaded as animated ImageIcon!");
        } else {
            playerBlueLeftIcon = playerBlueIcon; // reuse right gif if left not provided
            if (playerBlueLeftIcon != null) System.out.println("PlayerBlueLeft uses right gif.");
            else System.out.println("PlayerBlueLeft (animated) not found. Will use fallback.");
        }

        // ⭐ LOAD MOSQ GIF FOR ENEMIES (name the file exactly "mosq.gif")
        File mosqFile = new File("mosq.gif");
        if (mosqFile.exists()) {
            mosqIcon = new ImageIcon("mosq.gif");
            System.out.println("mosq.gif loaded for enemies!");
        } else {
            mosqIcon = null;
            System.out.println("mosq.gif not found — enemies will be drawn as red squares.");
        }

        // prepare explosionTimer but DO NOT start it yet; single-shot to clear explosion only
        explosionTimer = new Timer(explosionDuration, ae -> {
            explosionActive = false;
            // do NOT respawn here — banana was already respawned instantly
        });
        explosionTimer.setRepeats(false);

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

            // ⭐ DRAW BANANA PNG INSTEAD OF YELLOW ORB (only if visible and not exploding)
            if (itemVisible) {
                if (bananaImage != null) {
                    g2.drawImage(bananaImage, itemX, itemY, ITEM_SIZE, ITEM_SIZE, this);
                } else {
                    g2.setColor(Color.YELLOW);
                    g2.fillOval(itemX, itemY, ITEM_SIZE, ITEM_SIZE);
                }
            }

            // draw explosion GIF if active (explosion is at the previous banana location)
            if (explosionActive) {
                if (bananaExplosionIcon != null) {
                    Image expImg = bananaExplosionIcon.getImage();
                    int size = ITEM_SIZE * 2;
                    g2.drawImage(expImg, explosionX - (size-ITEM_SIZE)/2, explosionY - (size-ITEM_SIZE)/2, size, size, this);
                } else {
                    // fallback: simple flash circle
                    g2.setColor(Color.ORANGE);
                    int size = ITEM_SIZE * 2;
                    g2.fillOval(explosionX - (size-ITEM_SIZE)/2, explosionY - (size-ITEM_SIZE)/2, size, size);
                }
            }

            if (starActive) {
                g2.setColor(Color.BLACK);
                g2.fillOval(starX, starY, STAR_SIZE, STAR_SIZE);
                g2.setColor(Color.WHITE);
                g2.drawOval(starX, starY, STAR_SIZE, STAR_SIZE);
            }

            // ---------- ENEMIES: draw mosq.gif if available, otherwise red squares ----------
            for (int i = 0; i < enemyCount; i++) {
                if (mosqIcon != null) {
                    Image mosqImg = mosqIcon.getImage();
                    g2.drawImage(mosqImg, enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE, this);
                } else {
                    g2.setColor(Color.RED);
                    g2.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
                }
            }
            // ------------------------------------------------------------------------------

            // ⭐ PLAYER 1 NOW USES GIF IMAGE BASED ON FACING (ImageIcon -> Image)
            if (player1FacingRight) {
                if (playerBlueIcon != null) {
                    Image img = playerBlueIcon.getImage();
                    g2.drawImage(img, player1X, player1Y, player1Size, player1Size, this);
                } else if (playerBlueImage != null) {
                    g2.drawImage(playerBlueImage, player1X, player1Y, player1Size, player1Size, this);
                } else {
                    drawPlayerCircle(g2, player1X, player1Y, player1Size, Color.BLUE);
                }
            } else {
                if (playerBlueLeftIcon != null) {
                    Image img = playerBlueLeftIcon.getImage();
                    g2.drawImage(img, player1X, player1Y, player1Size, player1Size, this);
                } else if (playerBlueLeftImage != null) {
                    g2.drawImage(playerBlueLeftImage, player1X, player1Y, player1Size, player1Size, this);
                } else if (playerBlueImage != null) {
                    g2.drawImage(playerBlueImage, player1X, player1Y, player1Size, player1Size, this);
                } else {
                    drawPlayerCircle(g2, player1X, player1Y, player1Size, Color.BLUE);
                }
            }

            // ⭐ PLAYER 2 NOW USES GIF IMAGE BASED ON FACING (ImageIcon -> Image)
            if (player2FacingRight) {
                if (playerRedIcon != null) {
                    Image img = playerRedIcon.getImage();
                    g2.drawImage(img, player2X, player2Y, player2Size, player2Size, this);
                } else {
                    drawPlayerCircle(g2, player2X, player2Y, player2Size, Color.PINK);
                }
            } else {
                if (playerRedLeftIcon != null) {
                    Image img = playerRedLeftIcon.getImage();
                    g2.drawImage(img, player2X, player2Y, player2Size, player2Size, this);
                } else if (playerRedIcon != null) {
                    Image img = playerRedIcon.getImage();
                    g2.drawImage(img, player2X, player2Y, player2Size, player2Size, this);
                } else {
                    drawPlayerCircle(g2, player2X, player2Y, player2Size, Color.PINK);
                }
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("P1 Score: " + score1, 20, 80);
            g2.drawString("P2 Score: " + score2, getWidth() - 180, 80);

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

                String winnerText;
                if (score1 > score2) winnerText = "Player 1 wins!";
                else if (score2 > score1) winnerText = "Player 2 wins!";
                else winnerText = "It's a tie!";

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

    // --------------------------
    // BELOW THIS: NOTHING CHANGED
    // --------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!initialized || gameOver || paused) return;

        long now = System.currentTimeMillis();

        // NOTE: explosionTimer handles clearing the explosion.
        // explosionActive is used only for rendering the explosion overlay.

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

        if (p1SpeedBoost > 0 && now >= p1BoostEnd) {
            p1SpeedBoost = 0;
            p1BoostEnd = 0;
        }
        if (p2SpeedBoost > 0 && now >= p2BoostEnd) {
            p2SpeedBoost = 0;
            p2BoostEnd = 0;
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
            int p1Speed = (int)(moveSpeed + p1SpeedBoost);
            int p2Speed = (int)(moveSpeed + p2SpeedBoost);

            if (upPressed && player1Y > 0) player1Y -= p1Speed;
            if (downPressed && player1Y < getHeight() - player1Size) player1Y += p1Speed;
            if (leftPressed && player1X > 0) player1X -= p1Speed;
            if (rightPressed && player1X < getWidth() - player1Size) player1X += p1Speed;

            if (wPressed && player2Y > 0) player2Y -= p2Speed;
            if (sPressed && player2Y < getHeight() - player2Size) player2Y += p2Speed;
            if (aPressed && player2X > 0) player2X -= p2Speed;
            if (dPressed && player2X < getWidth() - player2Size) player2X += p2Speed;

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

        // only allow collection when item is visible and not currently exploding
        if (itemVisible && !explosionActive) {
            if (p1Rect.intersects(itemRect)) {
                // start explosion at current banana location, then immediately move the banana
                score1++;
                player1Size += 5;
                triggerBananaExplosion();
            } else if (p2Rect.intersects(itemRect)) {
                score2++;
                player2Size += 5;
                triggerBananaExplosion();
            }
        }

        int totalAfter = score1 + score2;

        int milestoneNow = totalAfter / 5;
        if (milestoneNow > lastMilestone && milestoneNow > 0) {
            lastMilestone = milestoneNow;
            respawnStar();
            starActive = true;
        }

        if (starActive) {
            Rectangle starRect = new Rectangle(starX, starY, STAR_SIZE, STAR_SIZE);

            if (p1Rect.intersects(starRect)) {
                p1SpeedBoost += 9;
                p1BoostEnd = System.currentTimeMillis() + 5000;
                starActive = false;
            }
            if (p2Rect.intersects(starRect)) {
                p2SpeedBoost += 9;
                p2BoostEnd = System.currentTimeMillis() + 5000;
                starActive = false;
            }
        }

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

    // start explosion: explosion is placed at the banana's current position,
    // the banana is immediately respawned to a new location (so explosion plays in front of previous location)
    private void triggerBananaExplosion() {
        // place explosion at current banana coordinates
        explosionX = itemX;
        explosionY = itemY;

        // show explosion overlay
        explosionActive = true;

        // instantly move (respawn) the banana to a new location so players can continue collecting
        respawnItem();

        // ensure any previous timer stopped:
        if (explosionTimer.isRunning()) explosionTimer.stop();
        // start single-shot timer that will clear the explosion overlay
        explosionTimer.setInitialDelay(explosionDuration);
        explosionTimer.start();
    }

    private void respawnItem() {
        itemX = rand.nextInt(Math.max(1, getWidth() - ITEM_SIZE));
        itemY = rand.nextInt(Math.max(1, getHeight() - ITEM_SIZE));
        itemVisible = true;
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
                // Player 1 now facing left
                player1FacingRight = false;
            }
            if (code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
                // Player 1 now facing right
                player1FacingRight = true;
            }

            if (code == KeyEvent.VK_W) wPressed = true;
            if (code == KeyEvent.VK_S) sPressed = true;
            if (code == KeyEvent.VK_A) {
                aPressed = true;
                // Player 2 now facing left
                player2FacingRight = false;
            }
            if (code == KeyEvent.VK_D) {
                dPressed = true;
                // Player 2 now facing right
                player2FacingRight = true;
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

        if (code == KeyEvent.VK_W) wPressed = false;
        if (code == KeyEvent.VK_S) sPressed = false;
        if (code == KeyEvent.VK_A) aPressed = false;
        if (code == KeyEvent.VK_D) dPressed = false;
    }

    @Override public void keyTyped(KeyEvent e) {}

    private void resetGame() {
        score1 = 0;
        score2 = 0;
        player1Size = 45;
        player2Size = 45;
        enemyCount = 3;
        gameOver = false;
        gameStarted = false;
        paused = false;
        timeLeft = 120;
        lastTimeCheck = System.currentTimeMillis();

        p1SpeedBoost = 0;
        p2SpeedBoost = 0;
        p1BoostEnd = 0;
        p2BoostEnd = 0;
        lastMilestone = 0;
        starActive = false;

        // reset facing directions to default (right)
        player1FacingRight = true;
        player2FacingRight = true;

        explosionActive = false;
        itemVisible = true;

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

    // NOTE: Removed the standalone main method so this panel is used inside GameLauncher frame.
}
