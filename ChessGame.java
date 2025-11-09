import javax.swing.*;

// Main class - this is where the program starts
public class ChessGame {
    public static void main(String[] args) {
        // Try to use the system's look and feel so it looks native
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If it doesn't work, just use default
        }
        
        // Start the main menu
        SwingUtilities.invokeLater(() -> {
            new MainMenu();
        });
    }
}
