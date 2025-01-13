package group15;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class BoardGraphFactory {

  public static Map<Integer, List<Integer>> get(boolean in12Version) {
    Map<Integer, List<Integer>> boardGraph = new HashMap<>();
    // add to vertex i a list of its Neighbors index's

    boardGraph.put(1, asList(0, 2, 4));
    boardGraph.put(4, asList(3, 5, 7, 1));
    boardGraph.put(7, asList(6, 4, 8));
    boardGraph.put(9, asList(0, 10, 21));
    boardGraph.put(10, asList(11, 9, 3, 18));
    boardGraph.put(11, asList(6, 10, 15));
    boardGraph.put(12, asList(8, 17, 13));
    boardGraph.put(13, asList(12, 14, 5, 20));
    boardGraph.put(14, asList(13, 2, 23));
    boardGraph.put(16, asList(15, 17, 19));
    boardGraph.put(19, asList(16, 18, 20, 22));
    boardGraph.put(22, asList(21, 19, 23));

    if (in12Version) {
      boardGraph.put(0, asList(1, 9, 3));
      boardGraph.put(2, asList(1, 14, 5));
      boardGraph.put(3, asList(4, 10, 0, 6));
      boardGraph.put(5, asList(4, 13, 8, 2));
      boardGraph.put(6, asList(11, 7, 3));
      boardGraph.put(8, asList(7, 12, 5));
      boardGraph.put(15, asList(11, 16, 18));
      boardGraph.put(17, asList(16, 12, 20));
      boardGraph.put(18, asList(10, 19, 15, 21));
      boardGraph.put(20, asList(19, 13, 17, 23));
      boardGraph.put(21, asList(22, 9, 18));
      boardGraph.put(23, asList(22, 14, 20));
    } else {
      boardGraph.put(0, asList(1, 9));
      boardGraph.put(2, asList(1, 14));
      boardGraph.put(3, asList(4, 10));
      boardGraph.put(5, asList(4, 13));
      boardGraph.put(6, asList(11, 7));
      boardGraph.put(8, asList(7, 12));
      boardGraph.put(15, asList(11, 16));
      boardGraph.put(17, asList(16, 12));
      boardGraph.put(18, asList(10, 19));
      boardGraph.put(20, asList(19, 13));
      boardGraph.put(21, asList(22, 9));
      boardGraph.put(23, asList(22, 14));
    }
    return boardGraph;
  }
}
