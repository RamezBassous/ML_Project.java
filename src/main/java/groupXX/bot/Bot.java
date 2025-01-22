package groupXX.bot;

import groupXX.Game;

/**
 * Interface that defines the behavior of a Bot in the Nine/Twelve Men's Morris game.
 * The Bot makes decisions during the placing, moving/flying, and deletion phases of the game.
 */
public interface Bot {

    /**
     * Method to determine where to place a piece during the placing phase.
     *
     * @param game the current game instance
     * @return the position index where the bot chooses to place the piece
     */
    int placePiece(Game game);

    /**
     * Method to select a piece to move during the moving or flying phases.
     *
     * @param game the current game instance
     * @return the index of the piece to be selected for movement
     */
    int selectPiece(Game game);

    /**
     * Method to determine where to move the selected piece during the moving or flying phases.
     *
     * @param game the current game instance
     * @param selectedPiece the index of the piece chosen for movement
     * @return the position index where the selected piece will be moved
     */
    int determineMove(Game game, int selectedPiece);

    /**
     * Method to determine which opponent's piece should be deleted when required.
     *
     * @param game the current game instance
     * @return the index of the opponent's piece to be deleted
     */
    int determinePieceToDelete(Game game);
}
