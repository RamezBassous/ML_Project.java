package group15.bot;

import group15.Game;

public interface Bot {
    // Method to place a piece during the placing phase
    int placePiece(Game game);

    // Method to select a piece to move during the moving/flying phases
    int selectPiece(Game game);

    // Method to determine where to move the selected piece
    int determineMove(Game game, int selectedPiece);

    // Method to determine which opponent's piece to delete
    int determinePieceToDelete(Game game);
}