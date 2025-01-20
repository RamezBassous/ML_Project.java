package group15;

/**
 * Enum representing the two players in the game: BLUE and RED.
 * Each player is associated with an index value for easy identification.
 */
public enum Player {
  
  /**
   * Represents Player 1, denoted by BLUE.
   */
  BLUE(1), 
  
  /**
   * Represents Player 2, denoted by RED.
   */
  RED(2); 

  private final int index;

  /**
   * Constructor for the Player enum.
   * 
   * @param index The index value associated with the player.
   */
  Player(int index) {
    this.index = index;
  }

  /**
   * Returns the index associated with the player.
   * 
   * @return The index of the player.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the opponent of the current player.
   * 
   * @return The opponent player (BLUE if the current player is RED, and vice versa).
   */
  public Player opponent() {
    return this == BLUE ? RED : BLUE;
  }
}
