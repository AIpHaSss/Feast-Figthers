package prefi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Simple launcher UI.
 * - Shows one screen with a PLAY button.
 * - Clicking PLAY replaces the launcher panel with your MiniAdventureStickman panel.
 * - Keeps changes to your game code minimal / none.
 */
public class GameLauncher {

    // Put your chosen menu background filename here (you said you'll add "BG")
    private static final String MENU_BG = "BG.png";

    private JFrame frame;
    private MenuPanel menuPanel;

    public GameLauncher() {
        frame = new JFrame("Mini Adventure Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        menuPanel = new MenuPanel();
        frame.setContentPane(menuPanel);
        frame.setVisible(true);
    }

    // called when Play is pressed
    private void startGame() {
        // instantiate your game panel (unchanged)
        MiniAdventureStickman gamePanel = new MiniAdventureStickman();

        // swap the content
        frame.getContentPane().removeAll();
        frame.getContentPane().add(gamePanel);
        frame.revalidate();
        frame.repaint();

        // ensure keyboard focus goes to the game panel
        gamePanel.requestFocusInWindow();
    }

    // simple menu panel with background + Play button
    private class MenuPanel extends JPanel {
        private Image bg;

        public MenuPanel() {
            setLayout(null);

            // Load background if present (silent failure => black background)
            try {
                File f = new File(MENU_BG);
                if (f.exists()) bg = ImageIO.read(f);
            } catch (IOException ex) {
                bg = null;
            }

            JButton play = new JButton("PLAY");
            play.setFont(new Font("Arial", Font.BOLD, 36));
            play.setBounds( (getPreferredWidth()-300)/2, 300, 300, 100);
            play.addActionListener(e -> startGame());
            add(play);

            // let Enter also start
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "play");
            getActionMap().put("play", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { startGame(); }
            });

            // Esc -> exit
            getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quit");
            getActionMap().put("quit", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { System.exit(0); }
            });
        }

        // default preferred sizes used for button centering above
        private int getPreferredWidth() { return 1280; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            else {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,getWidth(),getHeight());
            }

            // title
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(new Font("Arial", Font.BOLD, 72));
            g2.setColor(new Color(255,240,200));
            String title = "MINI ADVENTURE";
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, (getWidth()-tw)/2, 180);
        }
    }

    // ▶ MAIN ENTRY POINT for launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameLauncher());
    }
}
