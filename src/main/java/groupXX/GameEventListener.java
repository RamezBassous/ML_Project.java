package groupXX;

/**
 * Interface for listening to game events such as game win or draw.
 * Implementing classes can define specific behavior for when a game is won or when a draw occurs.
 */
public interface GameEventListener {
    /**
    * Called when the game has been won by a player.
    * 
    * @param winner A string representing the winner of the game. This could be the player's name or identifier.
    */
    void onGameWon(String winner);  // Notify when there's a winner

    /**
    * Called when the game ends in a draw.
    */
    void onGameDraw();              // Notify when there's a draw
}
