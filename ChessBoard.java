// ChessBoard class - stores the game state and checks if moves are valid
// We decided to use uppercase for white and lowercase for black pieces
public class ChessBoard {
    private char[][] board;
    private boolean whiteTurn;
    
    public ChessBoard() {
        board = new char[8][8];
        whiteTurn = true;
        initializeBoard();
    }
    
    // Sets up the initial chess board position
    private void initializeBoard() {
        // First make everything empty
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = ' ';
            }
        }
        
        // Black pieces at the top (row 0 and 1)
        board[0][0] = 'r'; board[0][1] = 'n'; board[0][2] = 'b'; board[0][3] = 'q';
        board[0][4] = 'k'; board[0][5] = 'b'; board[0][6] = 'n'; board[0][7] = 'r';
        for (int i = 0; i < 8; i++) {
            board[1][i] = 'p';
        }
        
        // White pieces at the bottom (row 6 and 7)
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
    
    // Main function to try moving a piece
    // Returns true if the move worked, false if it didn't
    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        
        // Can't move an empty square
        if (piece == ' ') {
            return false;
        }
        
        // Make sure it's the right player's turn
        if (whiteTurn && !isWhitePiece(piece)) {
            return false;
        }
        if (!whiteTurn && !isBlackPiece(piece)) {
            return false;
        }
        
        // Can't capture your own pieces
        char destPiece = board[toRow][toCol];
        if (whiteTurn && isWhitePiece(destPiece)) {
            return false;
        }
        if (!whiteTurn && isBlackPiece(destPiece)) {
            return false;
        }
        
        // Check if this piece can actually move like that
        if (!isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
            return false;
        }
        
        // All good, make the move
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = ' ';
        switchTurn();
        return true;
    }
    
    // Checks if a piece can move to a square based on how that piece moves
    // NOTE: We're not doing check/checkmate stuff, just basic movement rules
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
        int direction = isWhite ? -1 : 1; // white goes up (negative), black goes down (positive)
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);
        
        // Normal move - one square forward
        if (colDiff == 0 && rowDiff == direction && board[toRow][toCol] == ' ') {
            return true;
        }
        
        // First move can be 2 squares
        int startRow = isWhite ? 6 : 1;
        if (colDiff == 0 && fromRow == startRow && rowDiff == 2 * direction 
            && board[toRow][toCol] == ' ' && board[fromRow + direction][fromCol] == ' ') {
            return true;
        }
        
        // Capturing - diagonal move
        if (colDiff == 1 && rowDiff == direction && board[toRow][toCol] != ' ') {
            return true;
        }
        
        return false;
    }
    
    // Make sure nothing is blocking the path (for rooks, bishops, queens)
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
    
    // For network play - just apply the move without checking if it's valid
    public void applyMove(int fromRow, int fromCol, int toRow, int toCol) {
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = ' ';
        switchTurn();
    }
    
    // This is for highlighting possible moves - checks if move is valid without actually moving
    public boolean isValidMoveCheck(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        
        if (piece == ' ') {
            return false;
        }
        
        if (whiteTurn && !isWhitePiece(piece)) {
            return false;
        }
        if (!whiteTurn && !isBlackPiece(piece)) {
            return false;
        }
        
        char destPiece = board[toRow][toCol];
        if (whiteTurn && isWhitePiece(destPiece)) {
            return false;
        }
        if (!whiteTurn && isBlackPiece(destPiece)) {
            return false;
        }
        
        return isValidMove(piece, fromRow, fromCol, toRow, toCol);
    }
    
    // Start a new game
    public void reset() {
        whiteTurn = true;
        initializeBoard();
    }
}
