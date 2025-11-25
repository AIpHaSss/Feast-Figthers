import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GameLauncher {

    private static final String MENU_BG = "1.png";  // menu background image

    private JFrame frame;
    private MenuPanel menuPanel;

    public GameLauncher() {

        frame = new JFrame("Mini Adventure Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // ---------- START: make the launcher fullscreen ----------
        frame.setUndecorated(true);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(frame);
        // ---------- END: make the launcher fullscreen ----------

        menuPanel = new MenuPanel();
        frame.setContentPane(menuPanel);
        frame.setVisible(true);
    }

    private void startGame() {
        MiniAdventureStickman gamePanel = new MiniAdventureStickman();
        frame.setContentPane(gamePanel);
        frame.revalidate();
        frame.repaint();
        gamePanel.requestFocusInWindow();
    }

    // helper to start single-player mode
    private void startSinglePlayer() {
        SinglePlayer sp = new SinglePlayer();
        frame.setContentPane(sp);
        frame.revalidate();
        frame.repaint();
        sp.requestFocusInWindow();
    }

    // ---------------- MENU SCREEN -------------------

    private class MenuPanel extends JPanel {

        private Image bg;

        // Menu text and layout
        private final String[] options = { "SINGLE PLAYER", "TWO PLAYER", "OPTIONS", "EXIT" };
        private final Rectangle[] optionBounds = new Rectangle[options.length];
        private int baseX;      // x position for options
        private int baseY;      // starting y position for first option
        private final int gap = 30;
        private final Font optionFont = new Font("Arial", Font.BOLD, 48);

        // hover state
        private int hoverIndex = -1;

        public MenuPanel() {
            setLayout(null);

            // Load menu background image
            try {
                bg = ImageIO.read(new File(MENU_BG));
            } catch (IOException e) {
                bg = null;
            }

            // prepare rectangles, they will be positioned in resize/paint to center-left
            for (int i = 0; i < optionBounds.length; i++) {
                optionBounds[i] = new Rectangle();
            }

            // Add mouse listeners for hover & click
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int idx = getOptionIndexAt(e.getPoint());
                    if (idx == 0) { // SINGLE PLAYER
                        startSinglePlayer();
                    } else if (idx == 1) { // TWO PLAYER
                        startGame();
                    } else if (idx == 3) { // EXIT
                        System.exit(0);
                    }
                    // OPTIONS (idx==2) intentionally inert for now
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverIndex = -1;
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = getOptionIndexAt(e.getPoint());
                    if (idx != hoverIndex) {
                        hoverIndex = idx;
                        repaint();
                    }
                }
            });
        }

        // returns index of option at point, or -1
        private int getOptionIndexAt(Point p) {
            for (int i = 0; i < optionBounds.length; i++) {
                if (optionBounds[i].contains(p)) return i;
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            // compute positions for middle-left (approx 12% from left, vertically centered block)
            baseX = (int)(getWidth() * 0.12); // left column
            // compute total height of options
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(optionFont);
            FontMetrics fm = g2.getFontMetrics();
            int totalHeight = options.length * fm.getHeight() + (options.length - 1) * gap;
            baseY = (getHeight() - totalHeight) / 2;

            // draw each option and update its bounding rect
            for (int i = 0; i < options.length; i++) {
                String text = options[i];
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();

                int x = baseX;
                int y = baseY + i * (fm.getHeight() + gap) + textHeight;

                // set option bounds slightly larger for easier hovering/clicking
                int paddingX = 20;
                int paddingY = 8;
                optionBounds[i].x = x - paddingX;
                optionBounds[i].y = y - textHeight - paddingY;
                optionBounds[i].width = textWidth + paddingX * 2;
                optionBounds[i].height = textHeight + paddingY * 2;

                boolean isHover = (i == hoverIndex);
                boolean isClickable = (i == 0 || i == 1 || i == 3); // single, two, exit clickable

                // draw shadow for readability
                g2.setColor(new Color(0,0,0,120));
                g2.drawString(text, x + 3, y + 3);

                // choose color: clickable green-ish for single/two, muted for others
                if (isClickable) {
                    if (isHover) g2.setColor(new Color(200, 255, 120));
                    else g2.setColor(new Color(170, 230, 90));
                } else {
                    if (isHover) g2.setColor(new Color(180, 180, 180));
                    else g2.setColor(new Color(130, 130, 130));
                }

                g2.drawString(text, x, y);

                // small hint for clickable items
                if (i == 0 || i == 1) {
                    g2.setFont(new Font("Arial", Font.PLAIN, 14));
                    if (i == 0) {
                        g2.setColor(new Color(220, 220, 220, 180));
                    } else {
                        g2.setColor(new Color(220, 220, 220, 180));
                    }
                    g2.setFont(optionFont);
                }
            }
        }
    }
}
