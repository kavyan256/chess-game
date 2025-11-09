import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ChessGUI - Handles the graphical user interface for the chess game
 */
public class ChessGUI extends JFrame {
    private ChessBoard board;
    private JButton[][] squares;
    private JButton selectedSquare;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private JLabel statusLabel;
    private NetworkManager networkManager;
    
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color SELECTED_COLOR = new Color(186, 202, 68);
    
    public ChessGUI() {
        board = new ChessBoard();
        squares = new JButton[8][8];
        
        setTitle("Simple Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create chess board panel
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setPreferredSize(new Dimension(640, 640));
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton square = new JButton();
                square.setFont(new Font("Arial Unicode MS", Font.PLAIN, 48));
                square.setFocusPainted(false);
                
                // Alternate colors
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
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        
        JButton hostButton = new JButton("Host Game");
        hostButton.addActionListener(e -> hostGame());
        
        JButton joinButton = new JButton("Join Game");
        joinButton.addActionListener(e -> joinGame());
        
        JButton resetButton = new JButton("Reset Board");
        resetButton.addActionListener(e -> resetGame());
        
        statusLabel = new JLabel("White's Turn");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        controlPanel.add(hostButton);
        controlPanel.add(joinButton);
        controlPanel.add(resetButton);
        controlPanel.add(statusLabel);
        
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        updateBoard();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void handleSquareClick(int row, int col) {
        if (selectedRow == -1) {
            // First click - select a piece
            char piece = board.getPiece(row, col);
            if (piece != ' ') {
                // Check if it's the correct player's turn
                boolean isWhite = Character.isUpperCase(piece);
                if ((board.isWhiteTurn() && isWhite) || (!board.isWhiteTurn() && !isWhite)) {
                    selectedRow = row;
                    selectedCol = col;
                    selectedSquare = squares[row][col];
                    highlightSelected(row, col);
                }
            }
        } else {
            // Second click - try to move
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
                }
                clearSelection();
            }
        }
    }
    
    private void highlightSelected(int row, int col) {
        selectedSquare.setBackground(SELECTED_COLOR);
    }
    
    private void clearSelection() {
        if (selectedSquare != null && selectedRow != -1) {
            // Restore original color
            if ((selectedRow + selectedCol) % 2 == 0) {
                selectedSquare.setBackground(LIGHT_SQUARE);
            } else {
                selectedSquare.setBackground(DARK_SQUARE);
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
    
    private void hostGame() {
        networkManager = new NetworkManager(true, this);
        networkManager.start();
        JOptionPane.showMessageDialog(this, 
            "Hosting game on port 5555\nYour IP: " + NetworkManager.getLocalIP(),
            "Host Game", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void joinGame() {
        String host = JOptionPane.showInputDialog(this, 
            "Enter host IP address:", 
            "Join Game", 
            JOptionPane.QUESTION_MESSAGE);
        
        if (host != null && !host.trim().isEmpty()) {
            networkManager = new NetworkManager(false, this);
            networkManager.setHost(host.trim());
            networkManager.start();
        }
    }
    
    private void resetGame() {
        board.reset();
        updateBoard();
        updateStatus();
        clearSelection();
        
        if (networkManager != null) {
            networkManager.close();
            networkManager = null;
        }
    }
    
    /**
     * Apply a move received from the network
     */
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
