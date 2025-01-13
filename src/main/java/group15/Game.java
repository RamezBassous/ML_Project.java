package group15;
import java.util.*;
import java.util.function.Supplier;

import group15.BoardGraphFactory;
import group15.bot.Bot;
import group15.bot.EasyBot;

public class Game {

    public Bot bot;

    public GameEventListener listener; // For letting the controller know who won

    // Game board positions: null = empty
    public Player[] boardPositions = new Player[24];

    // The boardGraph storing vertices and edges (G(V,E) as we know it)
    private Map<Integer, List<Integer>> boardGraph;  // see initializeBoardGraph() for adjacency list

    public String gameMode = "LOCAL 2 PLAYER"; // for now only mode

    public boolean in12MenMorrisVersion = false;

    public int phase = 0; // 0 = placing phase, 1 = moving phase, 2 = flying phase

    public int moveCountBlue = 0;

    public int moveCountRed = 0;

    // Keeps track of the current player: 1 for blue, 2 for red
    public Player currentPlayer = Player.BLUE;

    // Track the selected piece during the moving phase
    public int selectedPiece = -1;

    // Undo and redo stacks for storing board states
    private Stack<Move> undoStack = new Stack<>();
    private Stack<Move> redoStack = new Stack<>();

    // Draw conditions
    private Map<String, Integer> boardHistory = new HashMap<>();
    public int moveWithoutCapture = 0;
    public boolean drawAgreed = false;

    public boolean canUndo = false; // Tracks if undo is allowed for the latest move

    // Initialize the game, reset board positions
    public Game() {
        resetBoard();
        boardGraph = BoardGraphFactory.get(in12MenMorrisVersion);
        phase = 0;  // Start in the placing phase
        bot = new EasyBot();
    }


    public void setGameEventListener(GameEventListener listener) {
        this.listener = listener;
    }


    public void saveStateForUndo() {
        Player[] boardStateCopy = Arrays.copyOf(boardPositions, boardPositions.length);
        undoStack.push(new Move(selectedPiece, currentPlayer, boardStateCopy, moveCountBlue, moveCountRed, phase));
        redoStack.clear();
        canUndo = true;
    }

    public boolean undo() {
        if (canUndo && !undoStack.isEmpty()) {
            Move lastMove = undoStack.pop();
            redoStack.push(new Move(selectedPiece, currentPlayer, Arrays.copyOf(boardPositions, boardPositions.length), moveCountBlue, moveCountRed, phase));


            boardPositions = Arrays.copyOf(lastMove.boardState, lastMove.boardState.length);
            currentPlayer = lastMove.currentPlayer;
            moveCountBlue = lastMove.moveCountBlue;
            moveCountRed = lastMove.moveCountRed;
            phase = lastMove.phase;

            selectedPiece = -1;
            canUndo = false;
            return true;
        }
        return false;
    }


    public boolean redo() {
        if (!redoStack.isEmpty()) {
            Move lastMove = redoStack.pop();
            undoStack.push(new Move(selectedPiece, currentPlayer, Arrays.copyOf(boardPositions, boardPositions.length), moveCountBlue, moveCountRed, phase));


            boardPositions = Arrays.copyOf(lastMove.boardState, lastMove.boardState.length);
            currentPlayer = lastMove.currentPlayer;
            moveCountBlue = lastMove.moveCountBlue;
            moveCountRed = lastMove.moveCountRed;
            phase = lastMove.phase;

            selectedPiece = -1;
            canUndo = true;
            return true;
        }
        return false;
    }
    public Stack<Move> getRedoStack() {
        return redoStack;
    }

    // Represents a move for undo/redo
    private static class Move {
        int position;
        Player currentPlayer;
        Player[] boardState;
        int moveCountBlue;
        int moveCountRed;
        int phase;

        Move(int position, Player currentPlayer, Player[] boardState, int moveCountBlue, int moveCountRed, int phase) {
            this.position = position;
            this.currentPlayer = currentPlayer;
            this.boardState = boardState;
            this.moveCountBlue = moveCountBlue;
            this.moveCountRed = moveCountRed;
            this.phase = phase;
        }
    }

    // Get valid moves for a piece at a position (returns empty neighbors or all empty spots during flying phase)
    public List<Integer> getValidMoves(int position) {
        List<Integer> validMoves = new ArrayList<>();

        // Check if the game is in the flying phase and the current player has 3 or fewer pieces
        if (phase == 2 && getPieceCount(currentPlayer) <= 3) {
            // If in the flying phase, allow the player to move to any empty position
            for (int i = 0; i < boardPositions.length; i++) {
                if (boardPositions[i] == null) {
                    validMoves.add(i);
                }
            }
        } else {
            // Regular moving phase, only allow adjacent moves
            if (boardGraph.containsKey(position)) {
                for (Integer neighbor : boardGraph.get(position)) {
                    if (boardPositions[neighbor] == null) { // If the neighbor position is empty
                        validMoves.add(neighbor);
                    }
                }
            }
        }
        return validMoves;
    }

    // Check if a move forms a mill (checks along the path for a mill)

    public int[][][] getMillPaths() {
        return MillPaths.get(in12MenMorrisVersion);
    }

    public boolean formsMill(int position, Player player) {
        // Iterate through each mill path that includes this position
        for (int[] path : getMillPaths()[position]) {
            // Check if all positions in this mill path are occupied by the same player
            if (boardPositions[path[0]] == player && boardPositions[path[1]] == player && boardPositions[path[2]] == player) {
                return true; // Mill formed
            }
        }
        return false; // No mill formed
    }

    // returns true iff all pieces are in mills, otherwise false
    public boolean allPiecesAreInMills(Player player) {
        for (int position = 1; position <= 16; position++) {
            if (boardPositions[position] == player && !formsMill(position, player)) {
                return false;
            }
        }
        return true;
    }


    // Reset board to initial state
    public void resetBoard() {
        Arrays.fill(boardPositions, null);
    }

    public boolean makeMove(int position) {
        System.out.println("PHASE: " + phase);
        if (position < 0 || position >= 24) {
            System.out.println("Invalid position: " + position);  // Print for invalid position
            return false; // Invalid move
        }

        if (phase == 0) {
            // Placing phase logic
            if (boardPositions[position] != null) {
                System.out.println("Position " + position + " is already occupied.");  // Print for occupied position
                return false; // Position is already occupied
            }
            boardPositions[position] = currentPlayer;

            System.out.println("Player " + currentPlayer + " placed piece at position " + position);

            // Increment move count during the placing phase
            if (currentPlayer == Player.BLUE) {
                moveCountBlue++;
            } else {
                moveCountRed++;
            }

            // Check if a mill is formed
            if (formsMill(position, currentPlayer)) {
                System.out.println("Mill formed by Player " + currentPlayer + " at position " + position);
                phase = -currentPlayer.getIndex(); // Enter delete phase for right player
                return true;
            }

            // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
            int requiredPieces = in12MenMorrisVersion ? 12 : 9;
            if (moveCountBlue == requiredPieces && moveCountRed == requiredPieces) {
                phase = 1; // Transition to the moving phase
                System.out.println("Transitioning to the moving phase!");
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
                boardPositions[selectedPiece] = null;
                boardPositions[position] = currentPlayer;

                // Check if a mill is formed
                if (formsMill(position, currentPlayer)) {
                    System.out.println("Mill formed by Player " + currentPlayer + " at position " + position);

                    // Check if Game is won by the move
                    if (getPieceCount(Player.BLUE) <= 3 && currentPlayer == Player.RED) {
                        if (listener != null) listener.onGameWon("Red"); // Notify the controller

                    } else if (getPieceCount(Player.RED) <= 3 && currentPlayer == Player.BLUE) {
                        if (listener != null) listener.onGameWon("Blue"); // Notify the controller
                    }

                    phase = -currentPlayer.getIndex(); // Enter delete phase for right player
                } else {
                    switchPlayer();
                }

                selectedPiece = -1;  // Reset selection after move

                // Only check win/loss and draw conditions in the moving and flying phases
                checkWinLoss();
                checkDrawConditions();
                return true;
            }

        } else if (phase == -2 || phase == -1) {
            // Delete phase logic
            if (boardPositions[position] == currentPlayer.opponent()) {
                boardPositions[position] = null;
                System.out.println("Deleted piece " + position + " by Player " + currentPlayer);

                phase = 0;

                // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
                int requiredPieces = in12MenMorrisVersion ? 12 : 9;
                if (moveCountBlue == requiredPieces && moveCountRed == requiredPieces) {
                    phase = 1; // Transition to the moving phase
                    System.out.println("Transitioning to the moving phase!");
                }

                switchPlayer();
                checkDrawConditions();
                return true;
            }

            return false;

        }

        return false;
    }


    // Helper method to get the number of pieces of a player on the board
    private int getPieceCount(Player player) {
        int count = 0;
        for (Player pos : boardPositions) {
            if (pos == player) {
                count++;
            }
        }
        return count;
    }

    public void checkWinLoss() {
        int bluePieceCount = getPieceCount(Player.BLUE); // Blue player is 1
        int redPieceCount = getPieceCount(Player.RED);  // Red player is 2

        // Condition 1: A player has less than 3 pieces (loss condition)
        if (bluePieceCount < 3) {
            System.out.println("Red wins! Blue has less than 3 pieces.");
            if (listener != null) listener.onGameWon("Red"); // Notify the controller
            return;
        } else if (redPieceCount < 3) {
            System.out.println("Blue wins! Red has less than 3 pieces.");
            if (listener != null) listener.onGameWon("Blue"); // Notify the controller
            return;
        }

        // Condition 2: A player cannot make any valid moves
        boolean blueHasValidMoves = hasValidMoves(Player.BLUE); // Check if Blue can move
        boolean redHasValidMoves = hasValidMoves(Player.RED);  // Check if Red can move

        if (!blueHasValidMoves) {
            System.out.println("Red wins! Blue cannot make any valid moves.");
            if (listener != null) listener.onGameWon("Red"); // Notify the controller
        } else if (!redHasValidMoves) {
            System.out.println("Blue wins! Red cannot make any valid moves.");
            if (listener != null) listener.onGameWon("Blue"); // Notify the controller
        }
    }


    private String currentBoard(){
        return Arrays.toString(boardPositions);
    }

    private final Map<String, Supplier<Boolean>> drawDetectors = Map.of(
            "Threefold Repetition", () -> boardHistory.get(currentBoard()) >= 3,
            "Insufficient Material", () -> {
                int bluePieces = getPieceCount(Player.BLUE);
                int redPieces = getPieceCount(Player.RED);
                System.out.println("BLUE" + bluePieces + "RED" + redPieces);
                boolean bothPlayersPlacedAllPieces= isIn12MenMorrisVersion() ?
                  moveCountBlue == 12 && moveCountRed == 12 : moveCountBlue == 9 && moveCountRed == 9;
                return bluePieces == 3 && redPieces == 3 && bothPlayersPlacedAllPieces;
            },
            "50-Move Rule", () -> moveWithoutCapture >= 50,
            "No Legal Moves", () -> {
                boolean blueHasValidMoves = hasValidMoves(Player.BLUE);
                boolean redHasValidMoves = hasValidMoves(Player.RED);
                return !blueHasValidMoves && !redHasValidMoves;
            },
            "Agreement of Both Players", () -> drawAgreed,
            "Repetition in Endgame with Limited Pieces", () -> {
                int bluePieces = getPieceCount(Player.BLUE);
                int redPieces = getPieceCount(Player.RED);
                return bluePieces == 3 && redPieces == 3 && boardHistory.getOrDefault(currentBoard(), 0) >= 3;
            }
    );

    public void checkDrawConditions() {
        boardHistory.put(currentBoard(), boardHistory.getOrDefault(currentBoard(), 0) + 1);

        for (Map.Entry<String, Supplier<Boolean>> entry : drawDetectors.entrySet()) {
            String predicateDesc = entry.getKey();
            Supplier<Boolean> drawDetector = entry.getValue();
            if (drawDetector.get()) {
                System.out.println("Draw by " + predicateDesc + "!");
                if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
                return;
            }
        }
    }

    // Helper method to reset the counter for the 50-move rule when a mill is formed
    public void resetMoveWithoutCapture() {
        moveWithoutCapture = 0;
    }

    // Helper method to increment the counter for the 50-move rule when no mill is formed
    public void incrementMoveWithoutCapture() {
        moveWithoutCapture++;
    }

    // Helper method for players to agree on a draw
    public void agreeToDraw() {
        drawAgreed = true;
    }

    // Helper method to get valid moves for a player
    private boolean hasValidMoves(Player player) {
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


    // Switch between players
    private void switchPlayer() {
        currentPlayer = currentPlayer.opponent();
    }

    public void resetGame() {
        resetBoard();               // Reset all board positions
        moveCountBlue = 0;          // Reset blue move count
        moveCountRed = 0;           // Reset red move count
        phase = 0;                  // Reset phase to placing
        currentPlayer = Player.BLUE;          // Reset to player 1's turn
        // Optionally, reinitialize any game-specific logic or data
    }

    // Getter for boardPositions
    public Player[] getBoardPositions() {
        return boardPositions;
    }

    // Setter for boardPositions
    public void setBoardPositions(Player[] boardPositions) {
        this.boardPositions = boardPositions;
    }

    // Getter for in12MenMorrisVersion
    public boolean isIn12MenMorrisVersion() {
        return in12MenMorrisVersion;
    }

    // Setter for in12MenMorrisVersion
    public void setIn12MenMorrisVersion(boolean in12MenMorrisVersion) {
        this.in12MenMorrisVersion = in12MenMorrisVersion;
    }

    // Getter for currentPlayer
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    // Setter for currentPlayer
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getMoveCountBlue() {
        return moveCountBlue;
    }

    public void setMoveCountBlue(int moveCountBlue) {
        this.moveCountBlue = moveCountBlue;
    }

    public int getMoveCountRed() {
        return moveCountRed;
    }

    public void setMoveCountRed(int moveCountRed) {
        this.moveCountRed = moveCountRed;
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public int getSelectedPiece() {
        return selectedPiece;
    }

    public void setSelectedPiece(int i) {
        selectedPiece = i;
    }
}
