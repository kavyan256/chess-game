import javax.swing.*;

/**
 * ChessGame - Main entry point for the Simple Chess Game
 */
public class ChessGame {
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel if setting fails
        }
        
        // Create and show the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new ChessGUI();
        });
    }
}
