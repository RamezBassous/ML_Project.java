package group.bot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import group15.BoardGraphFactory;
import group15.Game;
import group15.Player;

public class GameState {
    public boolean in12MenMorrisVersion;
    public int requiredPieces;
    public Player[] boardPositions = new Player[24];
    public Player currentPlayer;
    public int moveCountBlue = 0;
    public int moveCountRed = 0;
    public Map<Integer, List<Integer>> boardGraph;

    public GameState(Game game) {
        in12MenMorrisVersion = game.in12MenMorrisVersion;
        requiredPieces = game.in12MenMorrisVersion ? 12 : 9;
        boardPositions = Arrays.copyOf(game.boardPositions, game.boardPositions.length);
        currentPlayer = game.getCurrentPlayer();
        moveCountBlue = game.moveCountBlue;
        moveCountRed = game.moveCountRed;
        boardGraph = BoardGraphFactory.get(in12MenMorrisVersion);
    }

    public GameState(GameState state, int position, Player player) {
        in12MenMorrisVersion = state.in12MenMorrisVersion;
        requiredPieces = state.requiredPieces;
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        boardPositions[position] = player;
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        boardGraph = BoardGraphFactory.get(in12MenMorrisVersion);
    }

    public GameState(GameState state) {
        in12MenMorrisVersion = state.in12MenMorrisVersion;
        requiredPieces = state.requiredPieces;
        boardPositions = Arrays.copyOf(state.boardPositions, state.boardPositions.length);
        currentPlayer = state.currentPlayer;
        moveCountBlue = state.moveCountBlue;
        moveCountRed = state.moveCountRed;
        boardGraph = BoardGraphFactory.get(in12MenMorrisVersion);
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

    public List<int []> selectActions(Player player) {
        List<int []> result = new ArrayList<>();
        for (int position = 0; position < 24; position++) {
            if (boardPositions[position] == player) {
                if (boardGraph.containsKey(position)) {
                    for (Integer neighbor : boardGraph.get(position)) {
                        if (boardPositions[neighbor] == null) { // If the neighbor position is empty
                            result.add(new int[]{position, neighbor});
                        }
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
