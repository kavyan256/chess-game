import javax.swing.*;
import java.awt.*;

// Main game window - this handles all the UI stuff for the chess board
public class ChessGUI extends JFrame {
    private final ChessBoard board;
    private final JButton[][] squares;
    private JButton selectedSquare;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private final JLabel statusLabel;
    private NetworkManager networkManager;
    private final GameSettings settings;
    
    // Timer stuff for timed matches
    private final JLabel whiteTimerLabel;
    private final JLabel blackTimerLabel;
    private final JLabel whiteNameLabel;
    private final JLabel blackNameLabel;
    private Timer whiteTimer;
    private Timer blackTimer;
    private int whiteTimeRemaining;
    private int blackTimeRemaining;
    private boolean gameOver = false;
    
    // Colors for the board - tried to make it look nice!
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_COLOR = new Color(246, 246, 130);
    private static final Color POSSIBLE_MOVE_COLOR = new Color(186, 202, 68);  
    private static final Color CAPTURE_MOVE_COLOR = new Color(255, 100, 100); // red-ish for captures
    
    public ChessGUI(GameSettings settings, String hostIp) {
        this.settings = settings;
        board = new ChessBoard();
        squares = new JButton[8][8];
        
        setTitle("Chess Game - " + settings.getWhitePlayerName() + " vs " + settings.getBlackPlayerName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(50, 50, 50));
        
        // Setup timer if they enabled it
        if (settings.isTimerEnabled()) {
            whiteTimeRemaining = settings.getTimePerPlayerSeconds();
            blackTimeRemaining = settings.getTimePerPlayerSeconds();
        } else {
            whiteTimeRemaining = 0;
            blackTimeRemaining = 0;
        }
        
        // Black player panel at top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(50, 50, 50));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        blackNameLabel = new JLabel("♚ " + settings.getBlackPlayerName());
        blackNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        blackNameLabel.setForeground(Color.WHITE);
        
        blackTimerLabel = new JLabel(formatTime(blackTimeRemaining));
        blackTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        blackTimerLabel.setForeground(new Color(200, 200, 200));
        blackTimerLabel.setVisible(settings.isTimerEnabled());
        
        topPanel.add(blackNameLabel, BorderLayout.WEST);
        topPanel.add(blackTimerLabel, BorderLayout.EAST);
        
        // The actual chess board
        JPanel boardPanel = new JPanel(new GridLayout(8, 8, 0, 0)); // 0 gaps so squares touch
        boardPanel.setPreferredSize(new Dimension(640, 640));
        boardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(101, 67, 33), 8), // border to make it look like wood
            BorderFactory.createLineBorder(new Color(139, 90, 43), 3)
        ));
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton square = new JButton();
                square.setFont(new Font("Arial Unicode MS", Font.PLAIN, 48));
                square.setFocusPainted(false);
                square.setOpaque(true);
                square.setBorderPainted(false); // no borders so it looks cleaner
                square.setContentAreaFilled(true);
                square.setMargin(new Insets(0, 0, 0, 0));
                
                // Make the checkerboard pattern
                if ((row + col) % 2 == 0) {
                    square.setBackground(LIGHT_SQUARE);
                } else {
                    square.setBackground(DARK_SQUARE);
                }
                
                final int r = row;
                final int c = col;
                square.addActionListener(e -> handleSquareClick(r, c));
                
                squares[row][col] = square;
                boardPanel.add(square);
            }
        }
        
        // White player panel at bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(50, 50, 50));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        whiteNameLabel = new JLabel("♔ " + settings.getWhitePlayerName());
        whiteNameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        whiteNameLabel.setForeground(Color.WHITE);
        
        whiteTimerLabel = new JLabel(formatTime(whiteTimeRemaining));
        whiteTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        whiteTimerLabel.setForeground(new Color(200, 200, 200));
        whiteTimerLabel.setVisible(settings.isTimerEnabled());
        
        bottomPanel.add(whiteNameLabel, BorderLayout.WEST);
        bottomPanel.add(whiteTimerLabel, BorderLayout.EAST);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBackground(new Color(50, 50, 50));
        
        statusLabel = new JLabel("White's Turn");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);
        
        JButton resetButton = new JButton("New Game");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 12));
        resetButton.addActionListener(e -> newGame());
        
        JButton menuButton = new JButton("Main Menu");
        menuButton.setFont(new Font("Arial", Font.PLAIN, 12));
        menuButton.addActionListener(e -> backToMenu());
        
        controlPanel.add(statusLabel);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(resetButton);
        controlPanel.add(menuButton);
        
        // Combine bottom section
        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setBackground(new Color(50, 50, 50));
        bottomSection.add(bottomPanel, BorderLayout.NORTH);
        bottomSection.add(controlPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(bottomSection, BorderLayout.SOUTH);
        
        updateBoard();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Setup network if online game
        if (settings.isOnlineGame()) {
            if (settings.isHost()) {
                hostGame();
            } else if (hostIp != null) {
                joinGame(hostIp);
            }
        }
        
        // Start timer if enabled
        if (settings.isTimerEnabled()) {
            startWhiteTimer();
        }
        
        updatePlayerHighlight();
    }
    
    private void handleSquareClick(int row, int col) {
        if (gameOver) return;
        
        if (selectedRow == -1) {
            // First click - picking up a piece
            char piece = board.getPiece(row, col);
            if (piece != ' ') {
                // Make sure they're moving their own piece
                boolean isWhite = Character.isUpperCase(piece);
                if ((board.isWhiteTurn() && isWhite) || (!board.isWhiteTurn() && !isWhite)) {
                    selectedRow = row;
                    selectedCol = col;
                    selectedSquare = squares[row][col];
                    highlightSelected(row, col);
                }
            }
        } else {
            // Second click - putting piece down
            if (row == selectedRow && col == selectedCol) {
                // Clicked same square - deselect
                clearSelection();
            } else {
                // Try to move the piece
                boolean moved = board.movePiece(selectedRow, selectedCol, row, col);
                if (moved) {
                    // Send move to opponent if networked
                    if (networkManager != null) {
                        String move = positionToNotation(selectedRow, selectedCol) + 
                                     positionToNotation(row, col);
                        networkManager.sendMove(move);
                    }
                    updateBoard();
                    updateStatus();
                    switchTimer();
                    updatePlayerHighlight();
                }
                clearSelection();
            }
        }
    }
    
    private void highlightSelected(int row, int col) {
        selectedSquare.setBackground(SELECTED_COLOR);
        
        // Show all the places this piece can move
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board.isValidMoveCheck(row, col, r, c)) {
                    // Different color if it's a capture move
                    boolean isCapture = board.getPiece(r, c) != ' ';
                    Color originalColor = (r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                    Color highlightColor = isCapture ? CAPTURE_MOVE_COLOR : POSSIBLE_MOVE_COLOR;
                    squares[r][c].setBackground(blendColors(originalColor, highlightColor));
                }
            }
        }
    }
    
    // Mix two colors together for the highlighting effect
    private Color blendColors(Color base, Color overlay) {
        int r = (int)(base.getRed() * 0.5 + overlay.getRed() * 0.5);
        int g = (int)(base.getGreen() * 0.5 + overlay.getGreen() * 0.5);
        int b = (int)(base.getBlue() * 0.5 + overlay.getBlue() * 0.5);
        return new Color(r, g, b);
    }
    
    private void clearSelection() {
        if (selectedSquare != null && selectedRow != -1) {
            // Restore all squares to their original colors
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Color originalColor;
                    if ((row + col) % 2 == 0) {
                        originalColor = LIGHT_SQUARE;
                    } else {
                        originalColor = DARK_SQUARE;
                    }
                    squares[row][col].setBackground(originalColor);
                    squares[row][col].setOpaque(true); // Ensure square is opaque
                }
            }
        }
        selectedRow = -1;
        selectedCol = -1;
        selectedSquare = null;
    }
    
    private void updateBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char piece = board.getPiece(row, col);
                squares[row][col].setText(getPieceSymbol(piece));
            }
        }
    }
    
    private String getPieceSymbol(char piece) {
        switch (piece) {
            case 'K': return "♔";
            case 'Q': return "♕";
            case 'R': return "♖";
            case 'B': return "♗";
            case 'N': return "♘";
            case 'P': return "♙";
            case 'k': return "♚";
            case 'q': return "♛";
            case 'r': return "♜";
            case 'b': return "♝";
            case 'n': return "♞";
            case 'p': return "♟";
            default: return "";
        }
    }
    
    private void updateStatus() {
        if (board.isWhiteTurn()) {
            statusLabel.setText("White's Turn");
        } else {
            statusLabel.setText("Black's Turn");
        }
    }
    
    private void updatePlayerHighlight() {
        if (board.isWhiteTurn()) {
            whiteNameLabel.setForeground(Color.YELLOW);
            blackNameLabel.setForeground(Color.WHITE);
        } else {
            whiteNameLabel.setForeground(Color.WHITE);
            blackNameLabel.setForeground(Color.YELLOW);
        }
    }
    
    private void hostGame() {
        networkManager = new NetworkManager(true, this);
        networkManager.start();
        JOptionPane.showMessageDialog(this, 
            "Hosting game on port 5555\nYour IP: " + NetworkManager.getLocalIP(),
            "Host Game", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void joinGame(String host) {
        networkManager = new NetworkManager(false, this);
        networkManager.setHost(host);
        networkManager.start();
    }
    
    private void newGame() {
        stopTimers();
        board.reset();
        updateBoard();
        updateStatus();
        clearSelection();
        gameOver = false;
        
        if (settings.isTimerEnabled()) {
            whiteTimeRemaining = settings.getTimePerPlayerSeconds();
            blackTimeRemaining = settings.getTimePerPlayerSeconds();
            whiteTimerLabel.setText(formatTime(whiteTimeRemaining));
            blackTimerLabel.setText(formatTime(blackTimeRemaining));
            startWhiteTimer();
        }
        
        updatePlayerHighlight();
    }
    
    private void backToMenu() {
        stopTimers();
        if (networkManager != null) {
            networkManager.close();
        }
        dispose();
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
    
    // Timer methods
    private void startWhiteTimer() {
        stopTimers();
        whiteTimer = new Timer(1000, e -> {
            whiteTimeRemaining--;
            whiteTimerLabel.setText(formatTime(whiteTimeRemaining));
            if (whiteTimeRemaining <= 10) {
                whiteTimerLabel.setForeground(Color.RED);
            }
            if (whiteTimeRemaining <= 0) {
                endGame(settings.getBlackPlayerName() + " wins - Time out!");
            }
        });
        whiteTimer.start();
    }
    
    private void startBlackTimer() {
        stopTimers();
        blackTimer = new Timer(1000, e -> {
            blackTimeRemaining--;
            blackTimerLabel.setText(formatTime(blackTimeRemaining));
            if (blackTimeRemaining <= 10) {
                blackTimerLabel.setForeground(Color.RED);
            }
            if (blackTimeRemaining <= 0) {
                endGame(settings.getWhitePlayerName() + " wins - Time out!");
            }
        });
        blackTimer.start();
    }
    
    private void stopTimers() {
        if (whiteTimer != null) {
            whiteTimer.stop();
        }
        if (blackTimer != null) {
            blackTimer.stop();
        }
    }
    
    private void switchTimer() {
        if (!settings.isTimerEnabled()) return;
        
        if (board.isWhiteTurn()) {
            startWhiteTimer();
            blackTimerLabel.setForeground(new Color(200, 200, 200));
        } else {
            startBlackTimer();
            whiteTimerLabel.setForeground(new Color(200, 200, 200));
        }
    }
    
    private void endGame(String message) {
        gameOver = true;
        stopTimers();
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String formatTime(int seconds) {
        if (seconds < 0) seconds = 0;
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    public void applyNetworkMove(String move) {
        if (move.length() == 4) {
            int fromRow = notationToRow(move.charAt(1));
            int fromCol = notationToCol(move.charAt(0));
            int toRow = notationToRow(move.charAt(3));
            int toCol = notationToCol(move.charAt(2));
            
            board.applyMove(fromRow, fromCol, toRow, toCol);
            
            SwingUtilities.invokeLater(() -> {
                updateBoard();
                updateStatus();
                switchTimer();
                updatePlayerHighlight();
            });
        }
    }
    
    private String positionToNotation(int row, int col) {
        char colChar = (char) ('a' + col);
        char rowChar = (char) ('8' - row);
        return "" + colChar + rowChar;
    }
    
    private int notationToRow(char rowChar) {
        return '8' - rowChar;
    }
    
    private int notationToCol(char colChar) {
        return colChar - 'a';
    }
}
