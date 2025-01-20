package group15;

import java.util.*;

/**
 * Represents the main logic of the Nine/Twelve Men's Morris game, handling all phases and gameplay mechanics.
 * The game has three main phases: Placing, Moving, and Flying. Players take turns placing, moving, and flying pieces
 * according to the rules of the game.
 */
public class GameOri {

    public GameEventListener listener; // For letting the controller know who won

    // Game board positions: 0 = empty, 1 = blue, 2 = red
    public int[] boardPositions = new int[24];

    public GameBoard getBoardGraph() {
        return gameBoard;
    }

    // The boardGraph storing vertices and edges (G(V,E) as we know it)
    private GameBoard gameBoard;  // see initializeBoardGraph() for adjacency list

    public String gameMode = "LOCAL 2 PLAYER"; // for now only mode

    public int phase = 0; // 0 = placing phase, 1 = moving phase, 2 = flying phase

    //placed pieces count
    public int moveCountBlue = 0;

    public int moveCountRed = 0;

    // Keeps track of the current player: 1 for blue, 2 for red
    public int currentPlayer = 1;

    // Track the selected piece during the moving phase
    public int selectedPiece = -1;

    // Undo and redo stacks for storing board states
    private Stack<int[]> undoStack = new Stack<>();
    private Stack<int[]> redoStack = new Stack<>();

    // Draw conditions
    public Map<String, Integer> boardHistory = new HashMap<>();
    public int moveWithoutCapture = 0;
    public boolean drawAgreed = false;

    /**
     * Initializes the game and sets up the game board. The game starts in the placing phase.
     */
    public GameOri() {
        resetBoard();
        gameBoard = GameBoardFactory.get(false);
        phase = 0;  // Start in the placing phase
    }

    /**
     * Sets the listener for the game to notify the controller of game events.
     * 
     * @param listener The listener to be set
     */
    public void setGameEventListener(GameEventListener listener) {
        this.listener = listener;
    }

    /**
     * Saves the current board state to the undo stack for potential undo action.
     */
    public void saveStateForUndo() {
        undoStack.push(Arrays.copyOf(boardPositions, boardPositions.length));  // Deep copy of board state
        redoStack.clear();  // Clear redo stack since this is a new action
    }

    /**
     * Undoes the last move by restoring the board state from the undo stack.
     * 
     * @return true if the undo was successful, false otherwise
     */
    public boolean undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(Arrays.copyOf(boardPositions, boardPositions.length));  // Save current state to redo stack
            boardPositions = undoStack.pop();  // Restore previous state
            return true;
        }
        return false;
    }

    /**
     * Redoes the last undone move by restoring the board state from the redo stack.
     * 
     * @return true if the redo was successful, false otherwise
     */
    public boolean redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(Arrays.copyOf(boardPositions, boardPositions.length));  // Save current state to undo stack
            boardPositions = redoStack.pop();  // Restore state from redo stack
            return true;
        }
        return false;
    }

    /**
     * Gets the list of valid moves for a piece at the given position.
     * During the flying phase, the player can move to any empty position.
     * 
     * @param position The position of the piece for which valid moves are to be checked
     * @return A list of valid move positions
     */
    public List<Integer> getValidMoves(int position) {
        List<Integer> validMoves = new ArrayList<>();

        // Check if the game is in the flying phase and the current player has 3 or fewer pieces
        if (phase == 2 && getPieceCount(currentPlayer) <= 3) {
            // If in the flying phase, allow the player to move to any empty position
            for (int i = 0; i < boardPositions.length; i++) {
                if (boardPositions[i] == 0) {
                    validMoves.add(i);
                }
            }
        } else {
            // Regular moving phase, only allow adjacent moves
            for (Integer neighbor : gameBoard.getNeighbors(position)) {
                if (boardPositions[neighbor] == 0) { // If the neighbor position is empty
                    validMoves.add(neighbor);
                }
            }
        }
        return validMoves;
    }

    /**
     * Checks if a mill is formed by a player's piece at the specified position.
     * A mill is formed when a player's three pieces are aligned in a specific path.
     * 
     * @param position The position of the piece to check
     * @param player The player (1 for blue, 2 for red) whose mill formation is being checked
     * @return true if a mill is formed, false otherwise
     */
   public boolean formsMill(int position, int player) {
        // Iterate through each mill path that includes this position
        for (int[] path : gameBoard.getMillPaths()[position]) {
            // Check if all positions in this mill path are occupied by the same player
            if (boardPositions[path[0]] == player && boardPositions[path[1]] == player && boardPositions[path[2]] == player) {
                return true; // Mill formed
            }
        }
        return false; // No mill formed
    }

    /**
     * Resets the board to the initial state with no pieces placed.
     */
    public void resetBoard() {
        Arrays.fill(boardPositions, 0);
    }
    
    /**
     * Makes a move at the specified position, handling all phases of the game: placing, moving, and flying.
     * It checks if the move is valid, updates the board, and handles the phase transitions.
     * 
     * @param position The position where the move is made
     * @return true if the move was successful, false otherwise
     */
    public boolean makeMove(int position) {
        System.out.println("PHASE: " + phase);
        if (position < 0 || position >= 24) {
            System.out.println("Invalid position: " + position);  // Print for invalid position
            return false; // Invalid move
        }
    
        if (phase == 0) {
            // Placing phase logic
            if (boardPositions[position] != 0) {
                System.out.println("Position " + position + " is already occupied.");  // Print for occupied position
                return false; // Position is already occupied
            }
            boardPositions[position] = currentPlayer;
    
            System.out.println("Player " + currentPlayer + " placed piece at position " + position);
    
            // Increment move count during the placing phase
            if (currentPlayer == 1) {
                moveCountBlue++;
            } else {
                moveCountRed++;
            }

            // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
            int requiredPieces = gameBoard.getRequiredPieces();
            if (moveCountBlue == requiredPieces && moveCountRed == requiredPieces) {
                phase = 1; // Transition to the moving phase
                System.out.println("Transitioning to the moving phase!");
            }
    
            // Check if a mill is formed
            if (formsMill(position, currentPlayer)) {
                System.out.println("Mill formed by Player " + currentPlayer + " at position " + position);
                phase = -currentPlayer; // Enter delete phase for right player
            }
    
            switchPlayer();
            return true;
    
        } else if (phase == 1 || phase == 2) {
            // Moving or flying phase logic

            // Check if current player needs to enter the flying phase
            if (getPieceCount(currentPlayer) <= 3) {
                phase = 2; // Enter the flying phase
                System.out.println("Player " + currentPlayer + " is in the flying phase!");
            }

            if (selectedPiece == -1) {
                // No piece selected, select a piece
                if (boardPositions[position] != currentPlayer) {
                    System.out.println("Invalid selection. Player " + currentPlayer + " cannot select opponent's piece at position " + position);
                    return false;
                }

                selectedPiece = position;
                System.out.println("Player " + currentPlayer + " selected piece at position " + position);
                return true;

            } else {
                // If the player selects the same player's piece again, allow reselection
                if (boardPositions[position] == currentPlayer) {
                    selectedPiece = position;
                    System.out.println("Player " + currentPlayer + " reselected piece at position " + position);
                    return true;
                }

                // Move the selected piece to a new position
                List<Integer> validMoves = getValidMoves(selectedPiece);
                if (!validMoves.contains(position)) {
                    System.out.println("Invalid move. Position " + position + " is not a valid move for piece at position " + selectedPiece);
                    return false;
                }

                // Move piece logic
                System.out.println("Player " + currentPlayer + " moved piece from " + selectedPiece + " to " + position);
                boardPositions[selectedPiece] = 0;
                boardPositions[position] = currentPlayer;

                // Check if a mill is formed
                if (formsMill(position, currentPlayer)) {
                    System.out.println("Mill formed by Player " + currentPlayer + " at position " + position);

                    // Check if Game is won by the move
                    if (getPieceCount(1) <= 3 && currentPlayer == 2) {
                        if (listener != null) listener.onGameWon("Red"); // Notify the controller

                    } else if (getPieceCount(2) <= 3 && currentPlayer == 1) {
                        if (listener != null) listener.onGameWon("Blue"); // Notify the controller
                    }
                    checkDrawConditions();

                    phase = -currentPlayer; // Enter delete phase for right player
                }

                selectedPiece = -1;  // Reset selection after move

                switchPlayer();

                // Only check win/loss and draw conditions in the moving and flying phases
                checkWinLoss();
                checkDrawConditions();
                return true;
            }
    
        } else if (phase == -2) {
            // Delete phase logic
            if (boardPositions[position] == 1) {
                boardPositions[position] = 0;
                System.out.println("Deleted piece " + position + " by Player 1");

                phase = 0;

                // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
                int requiredPieces = gameBoard.getRequiredPieces();
                if (moveCountBlue == requiredPieces && moveCountRed == requiredPieces) {
                    phase = 1; // Transition to the moving phase
                    System.out.println("Transitioning to the moving phase!");
                }

                return true;
            }

            return false;

        } else if (phase == -1) {
            // Delete phase logic
            if (boardPositions[position] == 2) {
                boardPositions[position] = 0;
                System.out.println("Deleted piece " + position + " by Player 2");

                phase = 0;

                // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
                int requiredPieces = gameBoard.getRequiredPieces();
                if (moveCountBlue == requiredPieces && moveCountRed == requiredPieces) {
                    phase = 1; // Transition to the moving phase
                    System.out.println("Transitioning to the moving phase!");
                }
                return true;
            }

            return false;
        }
    
        return false;
    }
    
    /**
     * Helper method to get the number of pieces of a player on the board.
     *
     * @param player The player to count pieces for (1 for blue, 2 for red).
     * @return The number of pieces for the specified player.
     */
    public int getPieceCount(int player) {
        int count = 0;
        for (int pos : boardPositions) {
            if (pos == player) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks the win/loss conditions for the game.
     *
     * @return true if the game is ongoing, false if a winner has been determined.
     */
    public boolean checkWinLoss() {
        int bluePieceCount = getPieceCount(1); // Blue player is 1
        int redPieceCount = getPieceCount(2);  // Red player is 2

        // Condition 1: A player has less than 3 pieces (loss condition)
        if (bluePieceCount < 3) {
            System.out.println("Red wins! Blue has less than 3 pieces.");
            if (listener != null) listener.onGameWon("Red"); // Notify the controller
            return false;
        } else if (redPieceCount < 3) {
            System.out.println("Blue wins! Red has less than 3 pieces.");
            if (listener != null) listener.onGameWon("Blue"); // Notify the controller
            return false;
        }

        // Condition 2: A player cannot make any valid moves
        boolean blueHasValidMoves = hasValidMoves(1); // Check if Blue can move
        boolean redHasValidMoves = hasValidMoves(2);  // Check if Red can move

        if (!blueHasValidMoves) {
            System.out.println("Red wins! Blue cannot make any valid moves.");
            if (listener != null) listener.onGameWon("Red"); // Notify the controller
        } else if (!redHasValidMoves) {
            System.out.println("Blue wins! Red cannot make any valid moves.");
            if (listener != null) listener.onGameWon("Blue"); // Notify the controller
        }
        return blueHasValidMoves;
    }

    /**
     * Checks the draw conditions for the game.
     *
     * @return true if any of the draw conditions are met, otherwise false.
     */
    public boolean checkDrawConditions() {

        // 1. Threefold Repetition
        String currentBoard = Arrays.toString(boardPositions);
        boardHistory.put(currentBoard, boardHistory.getOrDefault(currentBoard, 0) + 1);
        if (boardHistory.get(currentBoard) >= 3) {
            System.out.println("Draw by Threefold Repetition!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }
    
        // 2. Insufficient Material
        int bluePieces = getPieceCount(1);
        int redPieces = getPieceCount(2);
        System.out.println("BLUE"+ bluePieces + "RED" + redPieces);
        if (bluePieces == 3 && redPieces == 3) {
            System.out.println("Draw by Insufficient Material!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }
    
        // 3. 50-Move Rule (optional)
        if (moveWithoutCapture >= 50) {
            System.out.println("Draw by 50-Move Rule!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }
    
        // 4. No Legal Moves
        boolean blueHasValidMoves = hasValidMoves(1);
        boolean redHasValidMoves = hasValidMoves(2);
        if (!blueHasValidMoves && !redHasValidMoves) {
            System.out.println("Draw by No Legal Moves!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }
    
        // 5. Agreement by Both Players
        if (drawAgreed) {
            System.out.println("Draw by Agreement of Both Players!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }
    
        // 6. Repetition in Endgame with Limited Pieces
        if (bluePieces <= 3 && redPieces <= 3) {
            if (boardHistory.getOrDefault(currentBoard, 0) >= 3) {
                System.out.println("Draw by Repetition in Endgame with Limited Pieces!");
                if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            }
        }

        return false;
    }
    
    /**
     * Resets the counter for the 50-move rule when a mill is formed.
     */
    public void resetMoveWithoutCapture() {
        moveWithoutCapture = 0;
    }
    
    /**
     * Increments the counter for the 50-move rule when no mill is formed.
     */
    public void incrementMoveWithoutCapture() {
        moveWithoutCapture++;
    }
    
    /**
     * Helper method for players to agree on a draw.
     */
    public void agreeToDraw() {
        drawAgreed = true;
    }

    /**
     * Helper method to get valid moves for a player.
     *
     * @param player The player for whom to check valid moves.
     * @return true if the player has valid moves, false otherwise.
     */
    public boolean hasValidMoves(int player) {
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == player) {
                List<Integer> validMoves = getValidMoves(i);
                if (!validMoves.isEmpty()) {
                    return true;  // Player has at least one valid move
                }
            }
        }
        return false;  // No valid moves available
    }
    

    /**
     * Switches the current player between player 1 and player 2.
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    /**
     * Resets the game, clearing the board and resetting game-specific variables.
     */
    public void resetGame() {
        resetBoard();               // Reset all board positions
        moveCountBlue = 0;          // Reset blue move count
        moveCountRed = 0;           // Reset red move count
        phase = 0;                  // Reset phase to placing
        currentPlayer = 1;          // Reset to player 1's turn
        // Optionally, reinitialize any game-specific logic or data
    }

    /**
     * Getter for boardPositions.
     *
     * @return The current positions of the pieces on the board.
     */
    public int[] getBoardPositions() {
        return boardPositions;
    }

    /**
     * Setter for boardPositions.
     *
     * @param boardPositions The new positions of the pieces on the board.
     */
    public void setBoardPositions(int[] boardPositions) {
        this.boardPositions = boardPositions;
    }

    /**
     * Getter for the current player.
     *
     * @return The current player (1 for blue, 2 for red).
     */
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Setter for the current player.
     *
     * @param currentPlayer The player to set as the current player (1 for blue, 2 for red).
     */
    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Getter for the move count of the blue player.
     *
     * @return The number of moves made by the blue player.
     */
    public int getMoveCountBlue() {
        return moveCountBlue;
    }

    /**
     * Setter for the move count of the blue player.
     *
     * @param moveCountBlue The number of moves made by the blue player.
     */
    public void setMoveCountBlue(int moveCountBlue) {
        this.moveCountBlue = moveCountBlue;
    }

    /**
     * Getter for the move count of the red player.
     *
     * @return The number of moves made by the red player.
     */
    public int getMoveCountRed() {
        return moveCountRed;
    }

    /**
     * Setter for the move count of the red player.
     *
     * @param moveCountRed The number of moves made by the red player.
     */
    public void setMoveCountRed(int moveCountRed) {
        this.moveCountRed = moveCountRed;
    }

    /**
     * Getter for the current game phase.
     *
     * @return The current game phase (0 for placing, 1 for moving, etc.).
 */
    public int getPhase() {
        return phase;
    }

    /**
     * Setter for the current game phase.
     *
     * @param phase The game phase to set (0 for placing, 1 for moving, etc.).
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }

    /**
     * Getter for the selected piece.
     *
     * @return The index of the selected piece.
     */
    public int getSelectedPiece() {
        return selectedPiece;
    }
}
