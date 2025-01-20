package group.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group15.GameBoard;
import group15.Game;
import group15.Player;

/**
 * Represents the state of the game at a given moment, including the board positions,
 * the current player, and the number of placed pieces for each player.
 */
public class GameState {
    public Player[] boardPositions = new Player[24];
    public Player currentPlayer;
    public int moveCountBlue = 0;
    public int moveCountRed = 0;
    public GameBoard gameBoard;

    /**
     * Constructs a new GameState from an existing Game object.
     * 
     * @param game The current game object.
     */
    public GameState(Game game) {
        gameBoard = game.getBoardGraph();
        boardPositions = Arrays.copyOf(game.boardPositions, game.boardPositions.length);
        currentPlayer = game.getCurrentPlayer();
        moveCountBlue = game.placedPiecesBlue;
        moveCountRed = game.placedPiecesRed;
    }

    /**
     * Constructs a new GameState by modifying an existing state with a new piece placed
     * at the specified position by the specified player.
     * 
     * @param state The current game state.
     * @param position The position on the board where the new piece is placed.
     * @param player The player who is placing the piece.
     */
    public GameState(GameState state, int position, Player player) {
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        boardPositions[position] = player;
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        gameBoard = state.gameBoard;
    }

    /**
     * Constructs a new GameState by copying an existing state.
     * 
     * @param state The current game state to copy.
     */
    public GameState(GameState state) {
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        gameBoard = state.gameBoard;
    }

    /**
     * Returns a list of valid actions that can be performed in the current state,
     * represented by the positions where pieces can be placed.
     * 
     * @return A list of positions where pieces can be placed.
     */
    public List<Integer> actions() {
        List<Integer> result = new ArrayList<>();
        for (int position = 0; position < 24; position++) {
            if (boardPositions[position] == null) {
                result.add(position);
            }
        }
        return result;
    }

    /**
     * Creates a new game state by placing a piece at the specified position by the specified player,
     * and updates the move count for the respective player.
     * 
     * @param position The position where the piece is placed.
     * @param player The player placing the piece.
     * @return A new GameState object with the updated position and move count.
     */
    public GameState newState(int position, Player player) {
        GameState result = new GameState(this, position, player);
        if (player == Player.RED) {
            result.moveCountRed++;
        } else {
            result.moveCountBlue++;
        }
        return result;
    }

    /**
     * Returns a list of positions where pieces can be deleted (i.e., positions occupied by the blue player).
     * 
     * @return A list of positions where pieces can be deleted.
     */
    public List<Integer> deleteActions() {
        List<Integer> result = new ArrayList<>();
        for (int position = 0; position < 24; position++) {
            if (boardPositions[position] == Player.BLUE) {
                result.add(position);
            }
        }
        return result;
    }

    /**
     * Returns a list of possible actions where a player can move a piece to a neighboring empty position.
     * 
     * @param player The player whose pieces are being considered for movement.
     * @return A list of actions represented by pairs of positions (from, to).
     */
    public List<int[]> selectActions(Player player) {
        List<int[]> result = new ArrayList<>();
        for (int position = 0; position < 24; position++) {
            if (boardPositions[position] == player) {
                for (Integer neighbor : gameBoard.getNeighbors(position)) {
                    if (boardPositions[neighbor] == null) { // If the neighbor position is empty
                        result.add(new int[]{position, neighbor});
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates a new game state by moving a piece from one position to another.
     * 
     * @param a An array representing the move: the first element is the current position, and the second element is the target position.
     * @return A new GameState object reflecting the move.
     */
    public GameState newMoveState(int[] a) {
        GameState result = new GameState(this);
        Player player = result.boardPositions[a[0]];
        result.boardPositions[a[0]] = null;
        result.boardPositions[a[1]] = player;
        return result;
    }
}
