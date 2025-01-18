package group15.bot;

import group15.Game;
import group15.GameBoard;
import group15.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a snapshot of the game state: the board positions, current player,
 * how many pieces each player has placed, and the reference to the game board graph.
 */
public class GameState {
    public Player[] boardPositions = new Player[24];
    public Player currentPlayer;
    public int moveCountBlue = 0;
    public int moveCountRed = 0;
    public GameBoard gameBoard;

    public GameState(Game game) {
        gameBoard = game.getBoardGraph();
        boardPositions = Arrays.copyOf(game.boardPositions, game.boardPositions.length);
        currentPlayer = game.getCurrentPlayer();
        moveCountBlue = game.placedPiecesBlue;
        moveCountRed = game.placedPiecesRed;
    }

    /**
     * Clone constructor, placing a piece for a given player in a certain position.
     */
    public GameState(GameState state, int position, Player player) {
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        boardPositions[position] = player;
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        gameBoard = state.gameBoard;
    }

    /** Clone the entire state (e.g., before making a move). */
    public GameState(GameState state) {
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        gameBoard = state.gameBoard;
    }

    /**
     * Returns a list of valid place actions when still in the placing phase:
     * All empty spots on the board.
     */
    public List<Integer> actions() {
        if (isPlacingPhase()) {
            List<Integer> result = new ArrayList<>();
            for (int position = 0; position < 24; position++) {
                if (boardPositions[position] == null) {
                    result.add(position);
                }
            }
            return result;
        } else {
            // Not in placing phase, no "place piece" actions
            return new ArrayList<>();
        }
    }

    /**
     * Create a new GameState from this one, placing a piece of `player` at `position`.
     */
    public GameState newState(int position, Player player) {
        GameState result = new GameState(this, position, player);

        // If still in placing phase, increment that player's count
        if (result.isPlacingPhase()) {
            if (player == Player.RED) {
                result.moveCountRed++;
            } else {
                result.moveCountBlue++;
            }
        }
        return result;
    }

    /**
     * Returns a list of valid pieces the current player might delete from the opponent.
     * (Here, it just returns all BLUE pieces, but you may want to tailor for the current state.)
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
     * Returns a list of (from,to) moves for the given player, depending on
     * adjacency or flying if the player is down to 3 pieces.
     */
    public List<int[]> selectActions(Player player) {
        List<int[]> result = new ArrayList<>();
        int count = 0;
        for (Player p : boardPositions) {
            if (p == player) count++;
        }

        boolean isFlying = (count <= 3);

        for (int position = 0; position < 24; position++) {
            if (boardPositions[position] == player) {
                if (isFlying) {
                    // Flying: can move to any empty position
                    for (int i = 0; i < 24; i++) {
                        if (boardPositions[i] == null) {
                            result.add(new int[]{position, i});
                        }
                    }
                } else {
                    // Normal adjacency
                    for (Integer neighbor : gameBoard.getNeighbors(position)) {
                        if (boardPositions[neighbor] == null) {
                            result.add(new int[]{position, neighbor});
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return a new GameState reflecting a move from a[0] to a[1].
     */
    public GameState newMoveState(int[] a) {
        GameState result = new GameState(this);
        Player player = result.boardPositions[a[0]];
        result.boardPositions[a[0]] = null;
        result.boardPositions[a[1]] = player;
        return result;
    }

    /**
     * Check if placing `player` at `position` would form a mill.
     * Temporarily place the piece, check, then revert.
     */
    public boolean formsMillIfPlace(int position, Player player) {
        Player old = boardPositions[position];
        boardPositions[position] = player;

        boolean mill = formsMill(position, player);

        // revert
        boardPositions[position] = old;
        return mill;
    }

    /**
     * Check if `player` has formed a mill by occupying all positions in any of
     * the mill-paths that include `position`.
     */
    public boolean formsMill(int position, Player player) {
        // For each mill combination that includes `position`, check if it's fully occupied by `player`.
        for (int[] path : gameBoard.getMillPaths()[position]) {
            if (boardPositions[path[0]] == player
                    && boardPositions[path[1]] == player
                    && boardPositions[path[2]] == player) {
                return true;
            }
        }
        return false;
    }

    /** Check if we are still in the placing phase. */
    public boolean isPlacingPhase() {
        int required = gameBoard.getRequiredPieces(); // e.g., 9 or 12
        return (moveCountBlue < required || moveCountRed < required);
    }

    /** Once both players have placed all required pieces, we move to the "moving" phase. */
    public boolean isMovingPhase() {
        return !isPlacingPhase();
    }
}