/**
 * ChessBoard - Represents the chess board state and handles basic piece movement validation
 * Uppercase = White pieces, Lowercase = Black pieces
 */
public class ChessBoard {
    private char[][] board;
    private boolean whiteTurn;
    
    public ChessBoard() {
        board = new char[8][8];
        whiteTurn = true;
        initializeBoard();
    }
    
    private void initializeBoard() {
        // Initialize empty squares
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = ' ';
            }
        }
        
        // Setup black pieces (top)
        board[0][0] = 'r'; board[0][1] = 'n'; board[0][2] = 'b'; board[0][3] = 'q';
        board[0][4] = 'k'; board[0][5] = 'b'; board[0][6] = 'n'; board[0][7] = 'r';
        for (int i = 0; i < 8; i++) {
            board[1][i] = 'p';
        }
        
        // Setup white pieces (bottom)
        board[7][0] = 'R'; board[7][1] = 'N'; board[7][2] = 'B'; board[7][3] = 'Q';
        board[7][4] = 'K'; board[7][5] = 'B'; board[7][6] = 'N'; board[7][7] = 'R';
        for (int i = 0; i < 8; i++) {
            board[6][i] = 'P';
        }
    }
    
    public char getPiece(int row, int col) {
        return board[row][col];
    }
    
    public boolean isWhiteTurn() {
        return whiteTurn;
    }
    
    public void switchTurn() {
        whiteTurn = !whiteTurn;
    }
    
    private boolean isWhitePiece(char piece) {
        return piece >= 'A' && piece <= 'Z';
    }
    
    private boolean isBlackPiece(char piece) {
        return piece >= 'a' && piece <= 'z';
    }
    
    /**
     * Attempts to move a piece from (fromRow, fromCol) to (toRow, toCol)
     * Returns true if move is valid and executed, false otherwise
     */
    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        
        // Check if there's a piece to move
        if (piece == ' ') {
            return false;
        }
        
        // Check if it's the correct player's turn
        if (whiteTurn && !isWhitePiece(piece)) {
            return false;
        }
        if (!whiteTurn && !isBlackPiece(piece)) {
            return false;
        }
        
        // Check if destination has own piece
        char destPiece = board[toRow][toCol];
        if (whiteTurn && isWhitePiece(destPiece)) {
            return false;
        }
        if (!whiteTurn && isBlackPiece(destPiece)) {
            return false;
        }
        
        // Validate movement based on piece type
        if (!isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        
        // Execute the move
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = ' ';
        switchTurn();
        return true;
    }
    
    /**
     * Validates if a move is legal based on piece movement patterns
     * Does NOT check for check/checkmate
     */
    private boolean isValidMove(char piece, int fromRow, int fromCol, int toRow, int toCol) {
        char pieceType = Character.toLowerCase(piece);
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        
        switch (pieceType) {
            case 'p': // Pawn
                return isValidPawnMove(piece, fromRow, fromCol, toRow, toCol);
                
            case 'r': // Rook
                return (rowDiff == 0 || colDiff == 0) && isPathClear(fromRow, fromCol, toRow, toCol);
                
            case 'n': // Knight
                return (Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 1) || 
                       (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 2);
                
            case 'b': // Bishop
                return Math.abs(rowDiff) == Math.abs(colDiff) && isPathClear(fromRow, fromCol, toRow, toCol);
                
            case 'q': // Queen
                return ((rowDiff == 0 || colDiff == 0) || (Math.abs(rowDiff) == Math.abs(colDiff))) 
                       && isPathClear(fromRow, fromCol, toRow, toCol);
                
            case 'k': // King
                return Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1;
                
            default:
                return false;
        }
    }
    
    private boolean isValidPawnMove(char piece, int fromRow, int fromCol, int toRow, int toCol) {
        boolean isWhite = isWhitePiece(piece);
        int direction = isWhite ? -1 : 1; // White moves up (-1), Black moves down (+1)
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);
        
        // Move forward one square
        if (colDiff == 0 && rowDiff == direction && board[toRow][toCol] == ' ') {
            return true;
        }
        
        // Move forward two squares from starting position
        int startRow = isWhite ? 6 : 1;
        if (colDiff == 0 && fromRow == startRow && rowDiff == 2 * direction 
            && board[toRow][toCol] == ' ' && board[fromRow + direction][fromCol] == ' ') {
            return true;
        }
        
        // Capture diagonally
        if (colDiff == 1 && rowDiff == direction && board[toRow][toCol] != ' ') {
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if the path between two squares is clear (no pieces blocking)
     * Used for Rook, Bishop, and Queen movements
     */
    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        
        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;
        
        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != ' ') {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return true;
    }
    
    /**
     * Apply a move without validation (used for network moves)
     */
    public void applyMove(int fromRow, int fromCol, int toRow, int toCol) {
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = ' ';
        switchTurn();
    }
    
    /**
     * Check if a move would be valid (without executing it)
     */
    public boolean isValidMoveCheck(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        
        // Check if there's a piece to move
        if (piece == ' ') {
            return false;
        }
        
        // Check if it's the correct player's turn
        if (whiteTurn && !isWhitePiece(piece)) {
            return false;
        }
        if (!whiteTurn && !isBlackPiece(piece)) {
            return false;
        }
        
        // Check if destination has own piece
        char destPiece = board[toRow][toCol];
        if (whiteTurn && isWhitePiece(destPiece)) {
            return false;
        }
        if (!whiteTurn && isBlackPiece(destPiece)) {
            return false;
        }
        
        // Validate movement based on piece type
        return isValidMove(piece, fromRow, fromCol, toRow, toCol);
    }
    
    /**
     * Reset the board to starting position
     */
    public void reset() {
        whiteTurn = true;
        initializeBoard();
    }
}
