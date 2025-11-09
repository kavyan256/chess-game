import javax.swing.*;
import java.awt.*;

// Main menu screen - where you set up the game before playing
public class MainMenu extends JFrame {
    private GameSettings settings;
    
    public MainMenu() {
        settings = new GameSettings();
        
        setTitle("Chess Game - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(500, 600);
        
        // Big title at the top
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(40, 40, 40));
        JLabel titleLabel = new JLabel("♔ CHESS GAME ♚");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // All the settings and buttons go here
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        contentPanel.setBackground(new Color(240, 240, 240));
        
        // Player Names Section
        contentPanel.add(createSectionLabel("Player Names"));
        contentPanel.add(Box.createVerticalStrut(10));
        
        JPanel whitePlayerPanel = new JPanel(new BorderLayout(10, 0));
        whitePlayerPanel.setBackground(new Color(240, 240, 240));
        whitePlayerPanel.add(new JLabel("White Player:"), BorderLayout.WEST);
        JTextField whiteNameField = new JTextField(settings.getWhitePlayerName());
        whitePlayerPanel.add(whiteNameField, BorderLayout.CENTER);
        contentPanel.add(whitePlayerPanel);
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        JPanel blackPlayerPanel = new JPanel(new BorderLayout(10, 0));
        blackPlayerPanel.setBackground(new Color(240, 240, 240));
        blackPlayerPanel.add(new JLabel("Black Player:"), BorderLayout.WEST);
        JTextField blackNameField = new JTextField(settings.getBlackPlayerName());
        blackPlayerPanel.add(blackNameField, BorderLayout.CENTER);
        contentPanel.add(blackPlayerPanel);
        
        contentPanel.add(Box.createVerticalStrut(30));
        
        // Timer Section
        contentPanel.add(createSectionLabel("Timer Settings"));
        contentPanel.add(Box.createVerticalStrut(10));
        
        JCheckBox timerCheckbox = new JCheckBox("Enable Timer", settings.isTimerEnabled());
        timerCheckbox.setBackground(new Color(240, 240, 240));
        timerCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        contentPanel.add(timerCheckbox);
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerPanel.setBackground(new Color(240, 240, 240));
        timerPanel.add(new JLabel("Time per player (minutes):"));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 60, 1);
        JSpinner timeSpinner = new JSpinner(spinnerModel);
        timeSpinner.setPreferredSize(new Dimension(60, 25));
        timerPanel.add(timeSpinner);
        contentPanel.add(timerPanel);
        
        timeSpinner.setEnabled(timerCheckbox.isSelected());
        timerCheckbox.addActionListener(e -> timeSpinner.setEnabled(timerCheckbox.isSelected()));
        
        contentPanel.add(Box.createVerticalStrut(30));
        
        // Game Mode Buttons
        contentPanel.add(createSectionLabel("Select Game Mode"));
        contentPanel.add(Box.createVerticalStrut(15));
        
        // JButton localGameButton = createStyledButton("Local Game", new Color(76, 175, 80));
        // localGameButton.addActionListener(e -> {
        //     updateSettings(whiteNameField, blackNameField, timerCheckbox, timeSpinner);
        //     settings.setOnlineGame(false);
        //     startGame();
        // });
        // contentPanel.add(localGameButton);
        
        // contentPanel.add(Box.createVerticalStrut(10));
        
        JButton hostGameButton = createStyledButton("Host Online Game", new Color(33, 150, 243));
        hostGameButton.addActionListener(e -> {
            updateSettings(whiteNameField, blackNameField, timerCheckbox, timeSpinner);
            settings.setOnlineGame(true);
            settings.setHost(true);
            startGame();
        });
        contentPanel.add(hostGameButton);
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        JButton joinGameButton = createStyledButton("Join Online Game", new Color(156, 39, 176));
        joinGameButton.addActionListener(e -> {
            String host = JOptionPane.showInputDialog(this, 
                "Enter host IP address:", 
                "Join Game", 
                JOptionPane.QUESTION_MESSAGE);
            
            if (host != null && !host.trim().isEmpty()) {
                updateSettings(whiteNameField, blackNameField, timerCheckbox, timeSpinner);
                settings.setOnlineGame(true);
                settings.setHost(false);
                startGame(host.trim());
            }
        });
        contentPanel.add(joinGameButton);
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        JButton exitButton = createStyledButton("Exit", new Color(244, 67, 54));
        exitButton.addActionListener(e -> System.exit(0));
        contentPanel.add(exitButton);
        
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(400, 45));
        button.setOpaque(true);
        button.setBorderPainted(false);
        return button;
    }
    
    private void updateSettings(JTextField whiteNameField, JTextField blackNameField, 
                                JCheckBox timerCheckbox, JSpinner timeSpinner) {
        settings.setWhitePlayerName(whiteNameField.getText().trim());
        settings.setBlackPlayerName(blackNameField.getText().trim());
        settings.setTimerEnabled(timerCheckbox.isSelected());
        settings.setTimePerPlayerMinutes((Integer) timeSpinner.getValue());
        
        // Set default names if empty
        if (settings.getWhitePlayerName().isEmpty()) {
            settings.setWhitePlayerName("White Player");
        }
        if (settings.getBlackPlayerName().isEmpty()) {
            settings.setBlackPlayerName("Black Player");
        }
    }
    
    private void startGame() {
        startGame(null);
    }
    
    private void startGame(String hostIp) {
        SwingUtilities.invokeLater(() -> {
            new ChessGUI(settings, hostIp);
            dispose();
        });
    }
}
