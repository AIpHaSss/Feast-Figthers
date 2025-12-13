import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * SinglePlayer wrapper that re-uses the existing MiniAdventureStickman panel
 * but disables the second player (red) and provides single-player controls.
 *
 * EXTRA SINGLE-PLAYER LOGIC:
 * - When the blue player eats a watermelon, ALL mosquitoes slow down
 *   by 3 speed for 5 seconds, then return to normal.
 */
public class SinglePlayer extends JPanel {

    private final MiniAdventureStickman gamePanel;

    // --- SINGLE PLAYER MELON → ENEMY SLOW ---
    private boolean lastWatermelonActive = false;
    private int[] baseEnemySpeedX;
    private int[] baseEnemySpeedY;
    private Timer enemySlowTimer;
    // --------------------------------------

    public SinglePlayer() {
        setLayout(new BorderLayout());

        gamePanel = new MiniAdventureStickman();
        add(gamePanel, BorderLayout.CENTER);

        for (KeyListener kl : gamePanel.getKeyListeners()) {
            gamePanel.removeKeyListener(kl);
        }

        try {
            setPrivateField(gamePanel, "player2Size", 0);
            setPrivateField(gamePanel, "player2X", -2000);
            setPrivateField(gamePanel, "player2Y", -2000);
            setPrivateField(gamePanel, "player2FacingRight", true);
        } catch (Exception ex) {
            System.err.println("SinglePlayer: reflection setup failed: " + ex);
        }

        // --- Monitor watermelon usage (polling, minimal intrusion)
        Timer monitorTimer = new Timer(60, e -> monitorWatermelon());
        monitorTimer.start();

        KeyAdapter singleKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                try {
                    if (code == KeyEvent.VK_UP) setPrivateField(gamePanel, "upPressed", true);
                    if (code == KeyEvent.VK_DOWN) setPrivateField(gamePanel, "downPressed", true);
                    if (code == KeyEvent.VK_LEFT) {
                        setPrivateField(gamePanel, "leftPressed", true);
                        setPrivateField(gamePanel, "player1FacingRight", false);
                    }
                    if (code == KeyEvent.VK_RIGHT) {
                        setPrivateField(gamePanel, "rightPressed", true);
                        setPrivateField(gamePanel, "player1FacingRight", true);
                    }

                    if (code == KeyEvent.VK_ESCAPE) togglePaused();
                    if (code == KeyEvent.VK_R) callPrivateReset();

                    if (code != KeyEvent.VK_R && code != KeyEvent.VK_ESCAPE) {
                        setPrivateField(gamePanel, "gameStarted", true);
                        setPrivateField(gamePanel, "lastTimeCheck", System.currentTimeMillis());
                    }

                } catch (Exception ex) {
                    System.err.println("SinglePlayer key error: " + ex);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    if (e.getKeyCode() == KeyEvent.VK_UP) setPrivateField(gamePanel, "upPressed", false);
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) setPrivateField(gamePanel, "downPressed", false);
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) setPrivateField(gamePanel, "leftPressed", false);
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) setPrivateField(gamePanel, "rightPressed", false);
                } catch (Exception ex) {}
            }
        };

        addKeyListener(singleKey);
        gamePanel.addKeyListener(singleKey);

        setFocusable(true);
        gamePanel.setFocusable(true);
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    // ---------------- WATERMELON → ENEMY SLOW ----------------
    private void monitorWatermelon() {
        try {
            boolean watermelonActive =
                (boolean)getPrivateField("watermelonActive");

            if (lastWatermelonActive && !watermelonActive) {
                slowEnemies();
            }
            lastWatermelonActive = watermelonActive;

        } catch (Exception ignored) {}
    }

    private void slowEnemies() throws Exception {
        int enemyCount = (int)getPrivateField("enemyCount");
        int[] enemySpeedX = (int[])getPrivateField("enemySpeedX");
        int[] enemySpeedY = (int[])getPrivateField("enemySpeedY");

        baseEnemySpeedX = enemySpeedX.clone();
        baseEnemySpeedY = enemySpeedY.clone();

        for (int i = 0; i < enemyCount; i++) {
            enemySpeedX[i] = adjust(enemySpeedX[i], -3);
            enemySpeedY[i] = adjust(enemySpeedY[i], -3);
        }

        enemySlowTimer = new Timer(5000, e -> restoreEnemies());
        enemySlowTimer.setRepeats(false);
        enemySlowTimer.start();
    }

    private void restoreEnemies() {
        try {
            int enemyCount = (int)getPrivateField("enemyCount");
            int[] enemySpeedX = (int[])getPrivateField("enemySpeedX");
            int[] enemySpeedY = (int[])getPrivateField("enemySpeedY");

            for (int i = 0; i < enemyCount; i++) {
                enemySpeedX[i] = baseEnemySpeedX[i];
                enemySpeedY[i] = baseEnemySpeedY[i];
            }
        } catch (Exception ignored) {}
    }

    private int adjust(int value, int delta) {
        return value > 0 ? Math.max(1, value + delta) : Math.min(-1, value - delta);
    }
    // --------------------------------------------------------

    private Object getPrivateField(String name) throws Exception {
        Field f = gamePanel.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(gamePanel);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void togglePaused() {
        try {
            Field f = gamePanel.getClass().getDeclaredField("paused");
            f.setAccessible(true);
            f.setBoolean(gamePanel, !f.getBoolean(gamePanel));
            gamePanel.repaint();
        } catch (Exception ignored) {}
    }

    private void callPrivateReset() {
        try {
            Method m = gamePanel.getClass().getDeclaredMethod("resetGame");
            m.setAccessible(true);
            m.invoke(gamePanel);

            setPrivateField(gamePanel, "player2Size", 0);
            setPrivateField(gamePanel, "player2X", -2000);
            setPrivateField(gamePanel, "player2Y", -2000);

        } catch (Exception ignored) {}
    }
}
