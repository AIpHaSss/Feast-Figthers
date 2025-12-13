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
import javax.swing.SwingUtilities;

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

    // ⭐ NEW — BANANA GIF (now using ImageIcon)
    private ImageIcon bananaIcon;

    // ⭐ NEW — BANANA EXPLOSION GIF
    private ImageIcon bananaExplosionIcon;
    private boolean explosionActive = false;
    private int explosionX = 0, explosionY = 0;
    private final int explosionDuration = 600; // ms

    // NEW: a dedicated single-shot timer for the explosion (non-blocking)
    private Timer explosionTimer;

    // item visibility flag (hidden while explosion plays)
    private boolean itemVisible = true;

    // ⭐ NEW — PLAYER RED (two-player mode) - still kept for two-player panel
    private ImageIcon playerRedIcon;
    private ImageIcon playerRedLeftIcon;

    // ⭐ NEW — PLAYER BLUE AS ANIMATED GIF (ImageIcon)
    private ImageIcon playerBlueIcon;
    private ImageIcon playerBlueLeftIcon;

    // ⭐ NEW — ENEMY (MOSQUITO) GIFs
    private ImageIcon mosqIcon;
    private ImageIcon mosqRightIcon;
    private ImageIcon mosqLeftIcon;

    // ⭐ NEW — APPLE BOOST GIF (speed boost visual)
    private ImageIcon appleBoostIcon;

    // Facing direction flags
    private boolean player1FacingRight = true;
    private boolean player2FacingRight = true;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean wPressed, sPressed, aPressed, dPressed;

    private int timeLeft = 120;
    private long lastTimeCheck = System.currentTimeMillis();

    // (pauseButton is kept for compatibility but will no longer be drawn or used)
    private Rectangle pauseButton = new Rectangle(20, 20, 80, 35);

    private Rectangle exitButton = new Rectangle(); // used when paused to exit to launcher

    private int starX, starY;
    private final int STAR_SIZE = 36;
    private boolean starActive = false;

    private int p1SpeedBoost = 0;
    private int p2SpeedBoost = 0;
    private long p1BoostEnd = 0;
    private long p2BoostEnd = 0;
    private int lastMilestone = 0;

    // --- WATERMELON (MelonSlow) fields ---
    private ImageIcon watermelonIcon;                 // expects file named "MelonSlow.gif"
    private boolean watermelonActive = false;
    private int watermelonX = 0, watermelonY = 0;
    private final int WATERMELON_SIZE = 48;
    private int lastWatermelonCount = 0;              // tracks last total score when watermelon was spawned

    // Slow effect fields (opponent is slowed)
    private int p1SpeedSlow = 0;
    private int p2SpeedSlow = 0;
    private long p1SlowEnd = 0;
    private long p2SlowEnd = 0;
    private final int WATERMELON_SLOW_AMOUNT = 6;     // how much to slow (pixels per tick)
    private final long WATERMELON_SLOW_DURATION = 5000; // ms
    // -----------------------------------------------------

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

        // ⭐ LOAD BANANA GIF (file must be named exactly "Banana.gif")
        File bananaFile = new File("Banana.gif");
        if (bananaFile.exists()) {
            bananaIcon = new ImageIcon("Banana.gif");
            System.out.println("Banana.gif loaded!");
        } else {
            bananaIcon = null;
            System.out.println("Banana.gif not found. Banana will not be drawn.");
        }

        // ⭐ LOAD BANANA EXPLOSION GIF (if exists)
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
            playerRedLeftIcon = playerRedIcon;
            if (playerRedLeftIcon != null) System.out.println("PlayerRedLeft uses right gif.");
            else System.out.println("PlayerRedLeft image not found. Will use circle.");
        }

        // ⭐ LOAD PLAYER BLUE GIFS (Animated)
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
            playerBlueLeftIcon = playerBlueIcon;
            if (playerBlueLeftIcon != null) System.out.println("PlayerBlueLeft uses right gif.");
            else System.out.println("PlayerBlueLeft (animated) not found. Will use fallback.");
        }

        // ⭐ LOAD MOSQ GIFS
        File mosqFile = new File("mosq.gif");
        if (mosqFile.exists()) {
            mosqIcon = new ImageIcon("mosq.gif");
            System.out.println("mosq.gif loaded for enemies!");
        } else {
            mosqIcon = null;
        }

        File mosqRightFile = new File("mosqRight.gif");
        if (mosqRightFile.exists()) {
            mosqRightIcon = new ImageIcon("mosqRight.gif");
            System.out.println("mosqRight.gif loaded!");
        } else {
            mosqRightIcon = null;
        }

        File mosqLeftFile = new File("mosqLeft.gif");
        if (mosqLeftFile.exists()) {
            mosqLeftIcon = new ImageIcon("mosqLeft.gif");
            System.out.println("mosqLeft.gif loaded!");
        } else {
            mosqLeftIcon = null;
        }

        // ⭐ LOAD APPLE BOOST GIF
        File appleFile = new File("AppleBoost.gif");
        if (appleFile.exists()) {
            appleBoostIcon = new ImageIcon("AppleBoost.gif");
            System.out.println("AppleBoost.gif loaded!");
        } else {
            appleBoostIcon = null;
        }

        // ⭐ LOAD MELON SLOW GIF (user may add file named "MelonSlow.gif")
        File watermelonFile = new File("MelonSlow.gif");
        if (watermelonFile.exists()) {
            watermelonIcon = new ImageIcon("MelonSlow.gif");
            System.out.println("MelonSlow.gif loaded!");
        } else {
            watermelonIcon = null;
            System.out.println("MelonSlow.gif not found. Watermelon will be drawn as a green circle.");
        }

        if (mosqIcon == null && mosqRightIcon == null && mosqLeftIcon == null) {
            System.out.println("No mosquito GIFs found. Enemies will be red squares.");
        }

        // prepare explosionTimer but DO NOT start it yet
        explosionTimer = new Timer(explosionDuration, ae -> {
            explosionActive = false;
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

        if (!gameOver) {

            if (itemVisible && bananaIcon != null) {
                Image banImg = bananaIcon.getImage();
                g2.drawImage(banImg, itemX, itemY, ITEM_SIZE, ITEM_SIZE, this);
            }

            if (explosionActive) {
                if (bananaExplosionIcon != null) {
                    Image expImg = bananaExplosionIcon.getImage();
                    int size = ITEM_SIZE * 2;
                    g2.drawImage(expImg, explosionX - (size-ITEM_SIZE)/2, explosionY - (size-ITEM_SIZE)/2, size, size, this);
                } else {
                    g2.setColor(Color.ORANGE);
                    int size = ITEM_SIZE * 2;
                    g2.fillOval(explosionX - (size-ITEM_SIZE)/2, explosionY - (size-ITEM_SIZE)/2, size, size);
                }
            }

            if (starActive) {
                if (appleBoostIcon != null) {
                    Image appleImg = appleBoostIcon.getImage();
                    g2.drawImage(appleImg, starX, starY, STAR_SIZE, STAR_SIZE, this);
                } else {
                    g2.setColor(Color.BLACK);
                    g2.fillOval(starX, starY, STAR_SIZE, STAR_SIZE);
                    g2.setColor(Color.WHITE);
                    g2.drawOval(starX, starY, STAR_SIZE, STAR_SIZE);
                }
            }

            if (watermelonActive) {
                if (watermelonIcon != null) {
                    Image mImg = watermelonIcon.getImage();
                    g2.drawImage(mImg, watermelonX, watermelonY, WATERMELON_SIZE, WATERMELON_SIZE, this);
                } else {
                    g2.setColor(new Color(30, 180, 60)); // green fill
                    g2.fillOval(watermelonX, watermelonY, WATERMELON_SIZE, WATERMELON_SIZE);
                    g2.setColor(new Color(20, 120, 40));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(watermelonX, watermelonY, WATERMELON_SIZE, WATERMELON_SIZE);
                }
            }

            // enemies
            for (int i = 0; i < enemyCount; i++) {
                if (mosqRightIcon != null || mosqLeftIcon != null) {
                    if (enemySpeedX[i] >= 0) {
                        if (mosqRightIcon != null) {
                            Image mosqImg = mosqRightIcon.getImage();
                            g2.drawImage(mosqImg, enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE, this);
                        } else if (mosqIcon != null) {
                            Image mosqImg = mosqIcon.getImage();
                            g2.drawImage(mosqImg, enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE, this);
                        } else {
                            g2.setColor(Color.RED);
                            g2.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
                        }
                    } else {
                        if (mosqLeftIcon != null) {
                            Image mosqImg = mosqLeftIcon.getImage();
                            g2.drawImage(mosqImg, enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE, this);
                        } else if (mosqIcon != null) {
                            Image mosqImg = mosqIcon.getImage();
                            g2.drawImage(mosqImg, enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE, this);
                        } else {
                            g2.setColor(Color.RED);
                            g2.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
                        }
                    }
                } else if (mosqIcon != null) {
                    Image mosqImg = mosqIcon.getImage();
                    g2.drawImage(mosqImg, enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE, this);
                } else {
                    g2.setColor(Color.RED);
                    g2.fillRect(enemyX[i], enemyY[i], ENEMY_SIZE, ENEMY_SIZE);
                }
            }

            // player1 (blue)
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

            // player2 (red)
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
                String pausedText = "PAUSED";
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int textWidth = g2.getFontMetrics().stringWidth(pausedText);

                g2.setColor(new Color(0, 0, 0, 180));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(Color.WHITE);
                g2.drawString(pausedText, centerX - textWidth/2, centerY - 20);

                String exitLabel = "Exit Match";
                g2.setFont(new Font("Arial", Font.PLAIN, 24));
                int exitWidth = g2.getFontMetrics().stringWidth(exitLabel) + 40;
                int exitHeight = g2.getFontMetrics().getHeight() + 16;
                int ex = centerX - exitWidth/2;
                int ey = centerY + 20;
                exitButton.setBounds(ex, ey, exitWidth, exitHeight);

                g2.setColor(new Color(220, 60, 60));
                g2.fillRoundRect(ex, ey, exitWidth, exitHeight, 12, 12);
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(ex, ey, exitWidth, exitHeight, 12, 12);

                g2.drawString(exitLabel, ex + 20, ey + exitHeight - 12);

                return;
            }

            if (!gameStarted) {
                g2.setFont(new Font("Arial", Font.BOLD, 32));
                g2.drawString("Press any key to start!", getWidth() / 2 - 200, getHeight() / 2);
            }
        } else {
            // Game over overlay
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            String mainText = "GAME OVER";
            Font mainFont = new Font("Monospaced", Font.BOLD, 80);
            g2.setFont(mainFont);

            g2.setColor(new Color(18, 18, 18));
            for (int dx = -3; dx <= 3; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    if (Math.abs(dx) + Math.abs(dy) == 0) continue;
                    g2.drawString(mainText, centerX - g2.getFontMetrics().stringWidth(mainText)/2 + dx,
                                  centerY - 130 + dy);
                }
            }

            g2.setColor(new Color(190, 160, 60));
            g2.setStroke(new BasicStroke(3));
            g2.drawString(mainText, centerX - g2.getFontMetrics().stringWidth(mainText)/2, centerY - 130);

            g2.setColor(new Color(255, 240, 160));
            g2.drawString(mainText, centerX - g2.getFontMetrics().stringWidth(mainText)/2, centerY - 130);

            int barWidth = 540;
            int barHeight = 180;
            int barX = centerX - barWidth/2;
            int barY = centerY - 40;
            g2.setColor(new Color(0, 0, 0, 190));
            g2.fillRoundRect(barX, barY, barWidth, barHeight, 14, 14);

            Font scoreFont = new Font("Monospaced", Font.BOLD, 30);
            g2.setFont(scoreFont);
            g2.setColor(Color.WHITE);
            String p1ScoreText = "P1 Final Score: " + score1;
            String p2ScoreText = "P2 Final Score: " + score2;
            int p1x = centerX - g2.getFontMetrics().stringWidth(p1ScoreText)/2;
            int p2x = centerX - g2.getFontMetrics().stringWidth(p2ScoreText)/2;
            g2.drawString(p1ScoreText, p1x, centerY - 5);
            g2.drawString(p2ScoreText, p2x, centerY + 35);

            String winnerText;
            if (score1 > score2) winnerText = "PLAYER 1 WINS!";
            else if (score2 > score1) winnerText = "PLAYER 2 WINS!";
            else winnerText = "IT'S A TIE!";

            Font winnerFont = new Font("Monospaced", Font.BOLD, 36);
            g2.setFont(winnerFont);

            int winnerWidth = g2.getFontMetrics().stringWidth(winnerText);
            int winnerX = centerX - winnerWidth/2;
            int winnerY = centerY + 100;

            int pad = 18;
            g2.setColor(new Color(220, 60, 60));
            g2.fillRoundRect(winnerX - pad, winnerY - g2.getFontMetrics().getAscent() - pad/2, winnerWidth + pad*2, g2.getFontMetrics().getHeight() + pad/2, 12, 12);

            g2.setColor(new Color(30, 30, 30));
            g2.drawString(winnerText, winnerX+2, winnerY+2);
            g2.setColor(Color.WHITE);
            g2.drawString(winnerText, winnerX, winnerY);

            Font instr = new Font("Monospaced", Font.PLAIN, 20);
            g2.setFont(instr);
            String restart = "Press R to Restart";
            int rx = centerX - g2.getFontMetrics().stringWidth(restart)/2;
            g2.setColor(new Color(255,255,255,220));
            g2.drawString(restart, rx, winnerY + 60);
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
    // BELOW THIS: game logic
    // --------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!initialized || gameOver || paused) return;

        long now = System.currentTimeMillis();

        // handle expiration of slow effects
        if (p1SpeedSlow > 0 && now >= p1SlowEnd) {
            p1SpeedSlow = 0;
            p1SlowEnd = 0;
        }
        if (p2SpeedSlow > 0 && now >= p2SlowEnd) {
            p2SpeedSlow = 0;
            p2SlowEnd = 0;
        }

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
            // apply boosts and slows. ensure minimum speed of 1
            int p1Speed = Math.max(1, (int)(moveSpeed + p1SpeedBoost - p1SpeedSlow));
            int p2Speed = Math.max(1, (int)(moveSpeed + p2SpeedBoost - p2SpeedSlow));

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

        // Spawn watermelon every time totalAfter reaches a multiple of 3 (3,6,9,...)
        if (totalAfter > lastWatermelonCount && totalAfter % 3 == 0) {
            lastWatermelonCount = totalAfter;
            respawnWatermelon();
            watermelonActive = true;
        }

        // handle watermelon pickup: slows the *opponent* of whoever picks it
        if (watermelonActive) {
            Rectangle watermelonRect = new Rectangle(watermelonX, watermelonY, WATERMELON_SIZE, WATERMELON_SIZE);
            if (p1Rect.intersects(watermelonRect)) {
                // player1 picked watermelon -> slow player2
                p2SpeedSlow = WATERMELON_SLOW_AMOUNT;
                p2SlowEnd = System.currentTimeMillis() + WATERMELON_SLOW_DURATION;
                watermelonActive = false;
            } else if (p2Rect.intersects(watermelonRect)) {
                // player2 picked watermelon -> slow player1
                p1SpeedSlow = WATERMELON_SLOW_AMOUNT;
                p1SlowEnd = System.currentTimeMillis() + WATERMELON_SLOW_DURATION;
                watermelonActive = false;
            }
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

    private void triggerBananaExplosion() {
        explosionX = itemX;
        explosionY = itemY;
        explosionActive = true;
        respawnItem();

        if (explosionTimer.isRunning()) explosionTimer.stop();
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

    private void respawnWatermelon() {
        watermelonX = rand.nextInt(Math.max(1, getWidth() - WATERMELON_SIZE));
        watermelonY = rand.nextInt(Math.max(1, getHeight() - WATERMELON_SIZE));
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
                player1FacingRight = false;
            }
            if (code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
                player1FacingRight = true;
            }

            if (code == KeyEvent.VK_W) wPressed = true;
            if (code == KeyEvent.VK_S) sPressed = true;
            if (code == KeyEvent.VK_A) {
                aPressed = true;
                player2FacingRight = false;
            }
            if (code == KeyEvent.VK_D) {
                dPressed = true;
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

        // reset watermelon/slow-related state
        p1SpeedSlow = 0;
        p2SpeedSlow = 0;
        p1SlowEnd = 0;
        p2SlowEnd = 0;
        lastWatermelonCount = 0;
        watermelonActive = false;
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
        if (paused) {
            if (exitButton.contains(e.getPoint())) {
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w instanceof Frame) {
                    final Frame oldFrame = (Frame) w;
                    SwingUtilities.invokeLater(() -> {
                        oldFrame.dispose();
                        new GameLauncher();
                    });
                } else {
                    SwingUtilities.invokeLater(() -> new GameLauncher());
                }
            }
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
