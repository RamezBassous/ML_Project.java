package group15;

import java.util.List;
import java.util.Map;

/**
 * Represents the game board for the Nine or Twelve Men's Morris game.
 * The board is modeled as a graph where each position is connected to others.
 * The board can be either for the Nine Men's Morris version or the Twelve Men's Morris version.
 */
public class GameBoard {

  /** The graph representing the board's connections. */
  private final Map<Integer, List<Integer>> graph;

  /** Flag indicating if the game is in the Twelve Men's Morris version. */
  private final boolean isIn12MenVer;

  /**
   * Constructor for the GameBoard class.
   *
   * @param isIn12MenVer A boolean indicating whether the game is in the Twelve Men's Morris version (true) or Nine Men's Morris version (false).
   * @param graph A map representing the board's connections, where the key is a position on the board, and the value is a list of neighboring positions.
   */
  public GameBoard(boolean isIn12MenVer, Map<Integer, List<Integer>> graph) {
    this.isIn12MenVer = isIn12MenVer;
    this.graph = graph;
  }

  /**
   * Gets the number of pieces required for the game.
   * 
   * @return 12 if the game is in the Twelve Men's Morris version, or 9 if in the Nine Men's Morris version.
   */
  public int getRequiredPieces(){
    return isIn12MenVer ? 12 : 9;
  }

  /**
   * Retrieves the neighboring positions for a given position on the board.
   *
   * @param vertex The position for which to retrieve the neighbors.
   * @return A list of integers representing the neighboring positions, or an empty list if the position has no neighbors.
   */
  public List<Integer> getNeighbors(int vertex) {
    return graph.getOrDefault(vertex, List.of());
  }

  /**
   * Retrieves the mill paths for the current game version.
   * Mill paths are predefined paths where a player can form a mill.
   *
   * @return A 3D array representing the mill paths, specific to the Nine or Twelve Men's Morris game version.
   */
  public int[][][] getMillPaths(){
    return MillPaths.get(isIn12MenVer);
  }

  /**
   * Checks whether the game is in the Twelve Men's Morris version.
   * 
   * @return true if the game is in the Twelve Men's Morris version, false otherwise.
   */
  public boolean isIn12MenVer() {
    return isIn12MenVer;
  }
}
