package group15;

import java.util.List;
import java.util.Map;

public class GameBoard {

  private final Map<Integer, List<Integer>> graph;
  private final boolean isIn12MenVer;

  public GameBoard(boolean isIn12MenVer, Map<Integer, List<Integer>> graph) {
    this.isIn12MenVer = isIn12MenVer;
    this.graph = graph;
  }

  public int getRequiredPieces(){
    return isIn12MenVer ? 12 : 9;
  }

  public List<Integer> getNeighbors(int vertex) {
    return graph.getOrDefault(vertex, List.of());
  }

  public int[][][] getMillPaths(){
    return MillPaths.get(isIn12MenVer);
  }

  public boolean isIn12MenVer() {
    return isIn12MenVer;
  }

}
