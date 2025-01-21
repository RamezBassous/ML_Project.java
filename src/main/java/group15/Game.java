package group15;
import java.util.*;
import java.util.function.Supplier;

import group15.bot.*;

/**
 * The Game class handles the core logic of the Nine/Twelve Men's Morris game, including managing the game board, 
 * game phases, player actions, undo/redo functionality, and draw/win conditions.
 */
public class Game {

    public GameEventListener listener; // For letting the controller know who won

    // Game board positions: null = empty
    public Player[] boardPositions = new Player[24];

    public GameBoard getBoardGraph() {
        return gameBoard;
    }

    // The boardGraph storing vertices and edges (G(V,E) as we know it)
    public GameBoard gameBoard;  // see initializeBoardGraph() for adjacency list

    public String gameMode = "LOCAL 2 PLAYER"; // for now only mode

    public int phase = 0; // 0 = placing phase, 1 = moving phase, 2 = flying phase

    public int placedPiecesBlue = 0;

    public int placedPiecesRed = 0;

    // Keeps track of the current player: 1 for blue, 2 for red
    public Player currentPlayer = Player.BLUE;
    public Player loser = null;
//    public Bot red = new RandomBot(); // Choose player type for a new game (bot, type of bot, player) types if necessary
//    public Bot blue = new RandomBot();
    public Bot red = new MeatBot(Player.RED);
    public Bot blue = new MeatBot(Player.BLUE);
    // method that sets gameover to true bor bot

    // Track the selected piece during the moving phase
    public int selectedPiece = -1;

    // Undo and redo stacks for storing board states
    private Stack<Move> undoStack = new Stack<>();
    private Stack<Move> redoStack = new Stack<>();

    // Draw conditions
    private Map<String, Integer> boardHistory = new HashMap<>();
    public int moveWithoutCapture = 0;
    public boolean drawAgreed = false;
    public boolean gameOver = false;

    public boolean canUndo = false; // Tracks if undo is allowed for the latest move

    /**
     * Constructs the game and initializes the board, sets the initial phase, and initializes the bot.
     */
    public int clickedPosition = -1;

    public Game() {
        resetBoard();
        gameBoard = GameBoardFactory.get(false);
        phase = 0;  // Start in the placing phase
    }

    public GameStrategy getStrategy() {
        if (blue instanceof MeatBot && red instanceof MeatBot) {
            return new HumanVsHumanStrategy(this);
        } else if (blue instanceof MeatBot && !(red instanceof MeatBot)) {
            return new HumanVsBotStrategy(this);
        } else {
            return new BotVsBotStrategy(this);
        }
    }

    /**
     * Sets the listener for game events (e.g., win, draw).
     *
     * @param listener The listener to notify of game events.
     */
    public void setGameEventListener(GameEventListener listener) {
        this.listener = listener;
    }

    /**
     * Saves the current game state for undo functionality.
     */
    public void saveStateForUndo() {
        boardHistory.put(currentBoard(), boardHistory.getOrDefault(currentBoard(), 0) + 1);
        Player[] boardStateCopy = Arrays.copyOf(boardPositions, boardPositions.length);
        undoStack.push(new Move(selectedPiece, currentPlayer, boardStateCopy, placedPiecesBlue, placedPiecesRed, phase));
        redoStack.clear();
        canUndo = true;
    }

    /**
     * Undoes the last move, reverting the board to its previous state.
     * 
     * @return true if the undo was successful, false otherwise.
     */
    public boolean undo() {
        if (canUndo && !undoStack.isEmpty()) {
            Move lastMove = undoStack.pop();
            redoStack.push(new Move(selectedPiece, currentPlayer, Arrays.copyOf(boardPositions, boardPositions.length), placedPiecesBlue, placedPiecesRed, phase));


            boardPositions = Arrays.copyOf(lastMove.boardState, lastMove.boardState.length);
            currentPlayer = lastMove.currentPlayer;
            placedPiecesBlue = lastMove.moveCountBlue;
            placedPiecesRed = lastMove.moveCountRed;
            phase = lastMove.phase;

            selectedPiece = -1;
            canUndo = false;
            return true;
        }
        return false;
    }

    /**
     * Redoes the last undone move, restoring the board to the state before it was undone.
     * 
     * @return true if the redo was successful, false otherwise.
     */
    public boolean redo() {
        if (!redoStack.isEmpty()) {
            Move lastMove = redoStack.pop();
            undoStack.push(new Move(selectedPiece, currentPlayer, Arrays.copyOf(boardPositions, boardPositions.length), placedPiecesBlue, placedPiecesRed, phase));

            boardPositions = Arrays.copyOf(lastMove.boardState, lastMove.boardState.length);
            currentPlayer = lastMove.currentPlayer;
            placedPiecesBlue = lastMove.moveCountBlue;
            placedPiecesRed = lastMove.moveCountRed;
            phase = lastMove.phase;

            selectedPiece = -1;
            canUndo = true;
            return true;
        }
        return false;
    }

    /**
     * Returns the redo stack for managing the redo operations.
     * 
     * @return The redo stack.
     */
    public Stack<Move> getRedoStack() {
        return redoStack;
    }

    public void putPiece(int placedPiece) {
        boardPositions[placedPiece] = currentPlayer;
        debug("placed piece at position " + placedPiece);

        if (currentPlayer == Player.BLUE) {
            placedPiecesBlue++;
        } else {
            placedPiecesRed++;
        }
    }

    private void debug(String msg){
        System.out.println(phase + ": " + currentPlayer + ": " +
          selectedPiece + ": " + msg);
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

    public List<Integer> getValidMoves(Player player) {
        if (phase == 0) {
            return getEmptyPositions();
        }
        if (isInDeletePhase()) {
            return getRemovableOpponentPositions(player);
        }
        else {
            return selectedPiece == -1 ? getCurrentPlayerPiecesThatCanMove(player) :
              join(getCurrentPlayerPiecesThatCanMove(player), getPossibleMovesForSelectedPiece());
        }
    }

    /**
     * Gets the valid moves for a given position on the board.
     * 
     * @return A list of valid move positions.
     */
    public List<Integer> getValidMoves() {
        return getValidMoves(currentPlayer);
    }

    private List<Integer> getPossibleMovesForSelectedPiece() {
        if (isInFlyingPhase()) {
            return getEmptyPositions();
        }
        // Regular moving phase, only allow adjacent moves
        List<Integer> result = new ArrayList<>();
        for (Integer neighbor : gameBoard.getNeighbors(selectedPiece)) {
            if (boardPositions[neighbor] == null) { // If the neighbor position is empty
                result.add(neighbor);
            }
        }
        return result;
    }

    private List<Integer> join(List<Integer> list1, List<Integer> list2) {
        List<Integer> result = new ArrayList<>();
        result.addAll(list1);
        result.addAll(list2);
        return result;
    }

    private List<Integer> getCurrentPlayerPiecesThatCanMove(Player player) {
        if (isInFlyingPhase()) {
            return getPlayerPieces(player);
        }
        List<Integer> currentPlayerPieces = new ArrayList<>();
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == player && hasEmptyNeighbor(i)) {
                currentPlayerPieces.add(i);
            }
        }
        return currentPlayerPieces;
    }

    private List<Integer> getPlayerPieces(Player player) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == player) {
                result.add(i);
            }
        }
        return result;
    }

    private boolean hasEmptyNeighbor(int position){
        List<Integer> neighbors = gameBoard.getNeighbors(position);
        for (Integer neighbor : neighbors) {
            if(boardPositions[neighbor] == null){
                return true;
            }
        }
        return false;
    }

    private List<Integer> getRemovableOpponentPositions(Player player) {
        List<Integer> result = new ArrayList<>();
        Player opponent = player.opponent();
        boolean allAreInMills = allPiecesAreInMills(opponent);
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == opponent && (allAreInMills || !formsMill(i, opponent))) {
                result.add(i);
            }
        }
        return result;
    }

    private List<Integer> getEmptyPositions() {
        List<Integer> emptyPositions = new ArrayList<>();
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == null) {
                emptyPositions.add(i);
            }
        }
        return emptyPositions;
    }

    /**
     * Checks if a mill is formed at a given position for a given player.
     * 
     * @param position The position to check.
     * @param player The player to check for a mill.
     * @return true if a mill is formed, false otherwise.
     */
    public boolean formsMill(int position, Player player) {
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
     * Checks if all pieces of a given player are in mills.
     * 
     * @param player The player to check.
     * @return true if all pieces are in mills, false otherwise.
     */
    public boolean allPiecesAreInMills(Player player) {
        for (int position = 0; position < boardPositions.length; position++) {
            if (boardPositions[position] == player && !formsMill(position, player)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resets the board to its initial state.
     */
    public void resetBoard() {
        Arrays.fill(boardPositions, null);
    }

    /**
     * Makes a move at a given position, handling different game phases and rules.
     * 
     * @param position The position to move to.
     */
    public void makeMove(int position) {
        if (!getValidMoves().contains(position)) { // Should be removed If bots are OK with it
            System.out.println("Invalid move!");
            return;
        }
        System.out.println("PHASE: " + phase);
        if (phase == 0) {
            saveStateForUndo();
            putPiece(position);
            checkIfMillOrSwitchPlayer(position);
        } else if (phase == 1 || phase == 2) {
            // Moving or flying phase logic
            if (selectedPiece == -1) {
                // No piece selected, select a piece
                selectPiece(position);
            } else {
                // If the player selects the same player's piece again, allow reselection
                if (boardPositions[position] == currentPlayer) {
                    selectPiece(position);
                } else {
                    saveStateForUndo();
                    moveSelectedPiece(position);
                    checkIfMillOrSwitchPlayer(position);
                    selectedPiece = -1; // Reset selection after move
                }
            }
        } else if (isInDeletePhase()) {
            // Delete phase logic
            saveStateForUndo();
            deletePiece(position);
            switchPlayer();
            phase = getPlacingOrMovingPhase();
        }
    }

    private void checkIfMillOrSwitchPlayer(int position) {
        if (formsMill(position, currentPlayer)) {
            debug("formed mill at position " + position);
            phase = -currentPlayer.getIndex(); // Enter delete phase for right player
        } else {
            switchPlayer();
        }
    }

    private int getPlacingOrMovingPhase() {
        // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
        int requiredPieces = gameBoard.getRequiredPieces();
        if (placedPiecesBlue == requiredPieces && placedPiecesRed == requiredPieces) {
            return currentPlayer == Player.BLUE ? 1 : 2; // Transition to the moving phase
        } else {
            return 0; // Go back to placing phase
        }
    }

    private void deletePiece(int position) {
        boardPositions[position] = null;
        debug("Deleted piece " + position);
    }

    private void moveSelectedPiece(int position) {
        debug("moved piece from " + selectedPiece + " to " + position);
        boardPositions[selectedPiece] = null;
        boardPositions[position] = currentPlayer;
    }

    private void selectPiece(int position) {
        selectedPiece = position;
        debug("selected piece at position " + position);
    }

    /**
     * Gets the number of pieces of a given player on the board.
     * 
     * @param player The player to count pieces for.
     * @return The number of pieces the player has on the board.
     */
    private int getPiecesOnBoardCount(Player player) {
        int count = 0;
        for (Player pos : boardPositions) {
            if (pos == player) {
                count++;
            }
        }
        return count;
    }

    /**
     * Checks the win/loss conditions based on the number of pieces and available moves.
     * Notifies the listener if a win/loss is detected.
     */
    private void checkWinLoss() {
        if (loser != null) {
            displayWinner();
            return;
        }
        if (phase <= 0) {
            return;
        }

        int bluePieceCount = getPiecesOnBoardCount(Player.BLUE); // Blue player is 1
        int redPieceCount = getPiecesOnBoardCount(Player.RED);  // Red player is 2

        // Condition 1: A player has less than 3 pieces
        if (bluePieceCount < 3) {
            loser = Player.BLUE;
        } else if (redPieceCount < 3) {
            loser = Player.RED;
        }
        if (loser != null) {
            displayWinner();
            gameOver = true;
            return;
        }

        // Condition 2: A player cannot make any valid moves
        if (getValidMoves().isEmpty()) {
            loser = currentPlayer;
            displayWinner();
            gameOver = true;
        }
    }

    private void displayWinner() {
        System.out.println(loser.opponent().name() + " wins! " + loser.name() +
          " has less than 3 pieces OR cannot make any valid moves");
        if (listener != null) listener.onGameWon(loser.opponent().name()); // Notify the controller
    }

    /**
     * Returns the current state of the board as a string.
     *
     * @return The current board state as a string.
     */
    private String currentBoard(){
        return Arrays.toString(boardPositions);
    }

    /**
     * A map that holds different draw conditions and their corresponding validation logic.
     */
    private final Map<String, Supplier<Boolean>> drawDetectors = Map.of(
            "Threefold Repetition", () -> phase > 0 && boardHistory.getOrDefault(currentBoard(), 0) >= 3,
            "Insufficient Material", () -> {
                int bluePieces = getPiecesOnBoardCount(Player.BLUE);
                int redPieces = getPiecesOnBoardCount(Player.RED);
                System.out.println("BLUE" + bluePieces + "RED" + redPieces);
                boolean bothPlayersPlacedAllPieces= isIn12MenMorrisVersion() ?
                  placedPiecesBlue == 12 && placedPiecesRed == 12 : placedPiecesBlue == 9 && placedPiecesRed == 9;
                return bluePieces == 3 && redPieces == 3 && bothPlayersPlacedAllPieces;
            },
            "50-Move Rule", () -> moveWithoutCapture >= 50,
            "No Legal Moves", () -> {
                if (phase <= 0) {
                    return false;
                }
                boolean blueHasValidMoves = hasValidMoves(Player.BLUE);
                boolean redHasValidMoves = hasValidMoves(Player.RED);
                return !blueHasValidMoves && !redHasValidMoves;
            },
            "Agreement of Both Players", () -> drawAgreed,
            "Repetition in Endgame with Limited Pieces", () -> {
                int bluePieces = getPiecesOnBoardCount(Player.BLUE);
                int redPieces = getPiecesOnBoardCount(Player.RED);
                return bluePieces == 3 && redPieces == 3 && boardHistory.getOrDefault(currentBoard(), 0) >= 3;
            }
    );

    /**
     * Checks for any draw conditions, notifying the controller if a draw is detected.
     */
    public void checkDrawConditions() {
        for (Map.Entry<String, Supplier<Boolean>> entry : drawDetectors.entrySet()) {
            String predicateDesc = entry.getKey();
            Supplier<Boolean> drawDetector = entry.getValue();
            if (drawDetector.get()) {
                System.out.println("Draw by " + predicateDesc + "!");
                if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
                gameOver = true;
                return;
            }
        }
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
     * Allows both players to agree on a draw.
     */
    public void agreeToDraw() {
        drawAgreed = true;
    }

    /**
     * Checks if the player has any valid moves available.
     *
     * @param player The player to check for valid moves.
     * @return true if the player has valid moves, false otherwise.
     */
    private boolean hasValidMoves(Player player) {
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == player) {
                List<Integer> validMoves = getValidMoves(player);
                if (!validMoves.isEmpty()) {
                    return true;  // Player has at least one valid move
                }
            }
        }
        return false;  // No valid moves available
    }

    /**
     * Switches the current player to the opponent.
     */
    public void switchPlayer() {
        currentPlayer = currentPlayer.opponent();
        int currentPlayerPlacedPieces = currentPlayer == Player.BLUE ? getPlacedPiecesBlue() : getPlacedPiecesRed();
        if (gameBoard.getRequiredPieces() != currentPlayerPlacedPieces) {
            phase = 0;
        } else {
            phase = currentPlayer == Player.BLUE ? 1 : 2;
        }
        selectedPiece = -1;
    }

    /**
     * Resets the game to its initial state, clearing the board and setting the phase to placing.
     */
    public void resetGame() {
        resetBoard();               // Reset all board positions
        placedPiecesBlue = 0;          // Reset blue move count
        placedPiecesRed = 0;           // Reset red move count
        phase = 0;                  // Reset phase to placing
        currentPlayer = Player.BLUE;          // Reset to player 1's turn
        gameOver = false;
        loser = null;
        // Optionally, reinitialize any game-specific logic or data
    }

    /**
     * Getter for the current state of the board.
     *
     * @return The current board state.
     */
    public Player[] getBoardPositions() {
        return boardPositions;
    }

    /**
     * Setter for the board positions.
     *
     * @param boardPositions The new board positions.
     */
    public void setBoardPositions(Player[] boardPositions) {
        this.boardPositions = boardPositions;
    }

    /**
     * Getter for the game version (12-men or 9-men Morris).
     *
     * @return true if it is the 12-men Morris version, false otherwise.
     */
    public boolean isIn12MenMorrisVersion() {
        return gameBoard.isIn12MenVer();
    }

    /**
     * Setter for the game version (12-men or 9-men Morris).
     *
     * @param in12MenMorrisVersion true if it is the 12-men Morris version, false otherwise.
     */
    public void setIn12MenMorrisVersion(boolean in12MenMorrisVersion) {
        this.gameBoard = GameBoardFactory.get(in12MenMorrisVersion);
    }

    /**
     * Getter for the current player.
     *
     * @return The current player.
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Setter for the current player.
     *
     * @param currentPlayer The player to set as the current player.
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Getter for the number of pieces placed by the blue player.
     *
     * @return The number of blue pieces placed.
     */
    public int getPlacedPiecesBlue() {
        return placedPiecesBlue;
    }

    /**
     * Setter for the number of pieces placed by the blue player.
     *
     * @param placedPiecesBlue The number of blue pieces placed.
     */
    public void setPlacedPiecesBlue(int placedPiecesBlue) {
        this.placedPiecesBlue = placedPiecesBlue;
    }

    /**
     * Getter for the number of pieces placed by the red player.
     *
     * @return The number of red pieces placed.
     */
    public int getPlacedPiecesRed() {
        return placedPiecesRed;
    }

    /**
     * Setter for the number of pieces placed by the red player.
     *
     * @param placedPiecesRed The number of red pieces placed.
     */
    public void setPlacedPiecesRed(int placedPiecesRed) {
        this.placedPiecesRed = placedPiecesRed;
    }

    /**
     * Getter for the current game phase.
     *
     * @return The current game phase.
     */
    public int getPhase() {
        return phase;
    }

    /**
     * Setter for the game phase.
     *
     * @param phase The phase to set.
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }

    /**
     * Getter for the selected piece index.
     *
     * @return The index of the selected piece.
     */
    public int getSelectedPiece() {
        return selectedPiece;
    }

    /**
     * Setter for the selected piece index.
     *
     * @param i The index of the selected piece.
     */
    public void setSelectedPiece(int i) {
        selectedPiece = i;
    }

    /**
     * Determines if the game is in the delete phase.
     *
     * @return true if the game is in the delete phase, false otherwise.
     */
    public boolean isInDeletePhase() {
        return getPhase() == -1 || getPhase() == -2;
    }

    /**
     * Determines if the game is in the flying phase (player has only 3 pieces left).
     *
     * @return true if the game is in the flying phase, false otherwise.
     */
    public boolean isInFlyingPhase() {
        int placedPieces = currentPlayer == Player.BLUE ? placedPiecesBlue : placedPiecesRed;
        int initialPieceCount = gameBoard.getRequiredPieces();
        boolean playerUsedAllPieces = initialPieceCount == placedPieces;
        int piecesOnBoard = getPiecesOnBoardCount(currentPlayer);

        return playerUsedAllPieces && piecesOnBoard == 3;
    }

    public boolean isOver() {
        if (gameOver) {
            return true;
        }
        checkWinLoss();
        if (gameOver) {
            return true;
        }
        checkDrawConditions();
        return gameOver;
    }

    public Bot getCurrentBot() {
        return currentPlayer == Player.BLUE ? blue : red;
    }

    public void setBotLostNoValidMoves() {
        gameOver = true;
        loser = currentPlayer;
    }

}
