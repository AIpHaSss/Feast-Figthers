import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Use Main as the single entry point that launches the GameLauncher on the EDT
        SwingUtilities.invokeLater(() -> new GameLauncher());
    }
}
