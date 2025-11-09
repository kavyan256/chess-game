# Chess Game - Java OOP Project

A simple chess game implementation in Java using Swing for the GUI, demonstrating Object-Oriented Programming principles.

## Project Overview

This is a beginner-friendly chess game that implements basic chess piece movements without complex rules like check, checkmate, castling, or en-passant. The game supports both local and LAN multiplayer modes with optional timer functionality.

## Features

- **Clean OOP Design**: Separation of concerns with distinct classes for board logic, GUI, network, and settings
- **Local Gameplay**: Play chess on the same computer
- **LAN Multiplayer**: Host or join games over a local network
- **Timer System**: Optional countdown timer for timed matches
- **Player Customization**: Set player names before starting
- **Visual Feedback**: 
  - Highlights possible moves when selecting a piece
  - Different colors for regular moves (green) and capture moves (red)
  - Active player indication with color highlighting

## Object-Oriented Programming Concepts Demonstrated

### 1. **Encapsulation**
- `ChessBoard` class encapsulates board state and game logic
- `GameSettings` class encapsulates game configuration
- Private fields with public getter/setter methods

### 2. **Abstraction**
- Chess board represented as an 8×8 char array
- Complex movement validation abstracted into simple methods
- Network communication abstracted in `NetworkManager`

### 3. **Separation of Concerns**
- **Model**: `ChessBoard` - Game logic and state
- **View**: `ChessGUI`, `MainMenu` - User interface
- **Controller**: Event handlers managing user interactions
- **Utility**: `NetworkManager` - Network functionality
- **Data**: `GameSettings` - Configuration storage

### 4. **Inheritance**
- GUI classes extend `JFrame` from Swing
- `NetworkManager` extends `Thread` for concurrent operations

### 5. **Polymorphism**
- Different piece types handled through switch statements
- ActionListener interfaces for event handling

## Class Structure

```
ChessGame.java         - Main entry point
MainMenu.java          - Welcome screen and game configuration
ChessGUI.java          - Game board interface and game loop
ChessBoard.java        - Chess logic and piece movement validation
GameSettings.java      - Game configuration storage
NetworkManager.java    - LAN multiplayer networking
```

## Piece Representation

- **White Pieces**: `K` (King), `Q` (Queen), `R` (Rook), `B` (Bishop), `N` (Knight), `P` (Pawn)
- **Black Pieces**: `k` (king), `q` (queen), `r` (rook), `b` (bishop), `n` (knight), `p` (pawn)
- **Empty Square**: ` ` (space)

## How to Compile and Run

```bash
# Compile all Java files
javac *.java

# Run the game
java ChessGame
```

## How to Play

### Local Game
1. Run the application
2. Enter player names
3. Optionally enable timer and set duration
4. Click "Local Game"
5. Click a piece to select it, then click destination to move

### Online Game (LAN)
1. **Host**: Click "Host Online Game" - note your IP address
2. **Join**: Click "Join Online Game" - enter host's IP address
3. Take turns making moves - moves are synchronized automatically

## Game Rules Implemented

- **Pawn**: Moves forward one square, two squares from starting position, captures diagonally
- **Rook**: Moves horizontally or vertically any number of squares
- **Knight**: Moves in L-shape (2+1 squares)
- **Bishop**: Moves diagonally any number of squares
- **Queen**: Combines rook and bishop movements
- **King**: Moves one square in any direction

**Note**: Advanced rules like check, checkmate, castling, and en-passant are NOT implemented.

## Technologies Used

- **Java SE**: Core programming language
- **Swing**: GUI framework for the user interface
- **Java Sockets**: Network communication for multiplayer
- **AWT**: Abstract Window Toolkit for graphics and events

## Project Structure (OOP Principles)

- **Single Responsibility**: Each class has one clear purpose
- **Loose Coupling**: Classes interact through well-defined interfaces
- **High Cohesion**: Related functionality grouped within classes
- **Information Hiding**: Internal implementation details kept private

## Future Enhancements

- Implement check and checkmate detection
- Add move history and undo functionality
- Implement special moves (castling, en-passant)
- Add AI opponent for single-player mode
- Save/load game functionality
- Move validation for check situations

## Author

Created as an Object-Oriented Programming project demonstrating core OOP concepts in Java.

## License

This project is open source and available for educational purposes.
