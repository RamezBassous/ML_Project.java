package group.bot;

import group15.Game;
import group15.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EasyBot implements Bot {
    private Random random = new Random();

    @Override
    public int placePiece(Game game) {
        // Get list of empty positions
        List<Integer> emptyPositions = new ArrayList<>();
        Player[] boardPositions = game.getBoardPositions();
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == null) {
                emptyPositions.add(i);
            }
        }
        if (emptyPositions.isEmpty()) {
            return -1; // No moves available
        }
        // Choose a random empty position
        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }

    @Override
    public int selectPiece(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        Player[] boardPositions = game.getBoardPositions();
        List<Integer> movablePieces = new ArrayList<>();

        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == currentPlayer) {
                List<Integer> validMoves = game.getValidMoves(i);
                if (!validMoves.isEmpty()) {
                    movablePieces.add(i);
                }
            }
        }

        if (movablePieces.isEmpty()) {
            return -1; // No movable pieces
        }

        // Select a random piece
        return movablePieces.get(random.nextInt(movablePieces.size()));
    }

    @Override
    public int determineMove(Game game, int selectedPiece) {
        List<Integer> validMoves = game.getValidMoves(selectedPiece);
        if (validMoves.isEmpty()) {
            return -1; // No valid moves
        }

        // Choose a random valid move
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    @Override
    public int determinePieceToDelete(Game game) {
        List<Integer> deletablePositions = new ArrayList<>();
        Player[] boardPositions = game.getBoardPositions();
        Player opponent = game.getCurrentPlayer().opponent();

        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == opponent && !game.formsMill(i, opponent)) {
                deletablePositions.add(i);
            }
        }

        // If all opponent's pieces are in mills, can delete any opponent's piece
        if (deletablePositions.isEmpty()) {
            for (int i = 0; i < boardPositions.length; i++) {
                if (boardPositions[i] == opponent) {
                    deletablePositions.add(i);
                }
            }
        }
        if (deletablePositions.isEmpty()) {
            return -1; // No pieces to delete
        }

        // Choose a random deletable position
        return deletablePositions.get(random.nextInt(deletablePositions.size()));
    }
}