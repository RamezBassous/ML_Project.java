package group.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group15.GameBoard;
import group15.Game;
import group15.Player;

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

    public GameState(GameState state, int position, Player player) {
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        boardPositions[position] = player;
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        gameBoard = state.gameBoard;
    }

    public GameState(GameState state) {
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        gameBoard = state.gameBoard;
    }

    public List<Integer> actions() {
        List<Integer> result = new ArrayList<>();
        for (int position = 0; position < 24; position++) {
            if (boardPositions[position] == null) {
                result.add(position);
            }
        }
        return result;
    }

    public GameState newState(int position, Player player) {
        GameState result = new GameState(this, position, player);
        if (player == Player.RED) {
            result.moveCountRed++;
        } else {
            result.moveCountBlue++;
        }
        return result;
    }

    public List<Integer> deleteActions() {
        List<Integer> result = new ArrayList<>();
        for (int position = 0; position < 24; position++) {
            if (boardPositions[position] == Player.BLUE) {
                result.add(position);
            }
        }
        return result;
    }

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

    public GameState newMoveState(int[] a) {
        GameState result = new GameState(this);
        Player player = result.boardPositions[a[0]];
        result.boardPositions[a[0]] = null;
        result.boardPositions[a[1]] = player;
        return result;
    }
}
