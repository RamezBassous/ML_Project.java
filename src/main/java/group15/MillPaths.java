package group15;

/**
 * The MillPaths class contains predefined mill paths for both Nine Men's Morris and Twelve Men's Morris boards.
 * It provides a static method to retrieve the correct mill paths based on the board version (Nine or Twelve).
 */
class MillPaths {

  // Mill paths for Nine Men's Morris
  private static final int[][][] NINE_MEN_MILL_PATHS = {
    {{0, 1, 2}, {0, 9, 21}},     // Position 0
    {{0, 1, 2}, {1, 4, 7}},      // Position 1
    {{0, 1, 2}, {2, 14, 23}},    // Position 2
    {{3, 4, 5}, {3, 10, 18}},    // Position 3
    {{1, 4, 7}, {3, 4, 5}},      // Position 4
    {{3, 4, 5}, {5, 13, 20}},    // Position 5
    {{6, 7, 8}, {6, 11, 15}},    // Position 6
    {{1, 4, 7}, {6, 7, 8}},      // Position 7
    {{6, 7, 8}, {8, 12, 17}},    // Position 8
    {{0, 9, 21}, {9, 10, 11}},   // Position 9
    {{3, 10, 18}, {9, 10, 11}},  // Position 10
    {{6, 11, 15}, {9, 10, 11}},  // Position 11
    {{8, 12, 17}, {12, 13, 14}}, // Position 12
    {{5, 13, 20}, {12, 13, 14}}, // Position 13
    {{2, 14, 23}, {12, 13, 14}}, // Position 14
    {{6, 11, 15}, {15, 16, 17}}, // Position 15
    {{15, 16, 17}, {16, 19, 22}},// Position 16
    {{8, 12, 17}, {15, 16, 17}}, // Position 17
    {{3, 10, 18}, {18, 19, 20}}, // Position 18
    {{16, 19, 22}, {18, 19, 20}},// Position 19
    {{5, 13, 20}, {18, 19, 20}}, // Position 20
    {{0, 9, 21}, {21, 22, 23}},  // Position 21
    {{16, 19, 22}, {21, 22, 23}},// Position 22
    {{2, 14, 23}, {21, 22, 23}}  // Position 23
  };

  // Mill paths for Twelve Men's Morris
  private static final int[][][] TWELVE_MEN_MILL_PATHS = {
    {{0, 1, 2}, {0, 9, 21}, {0, 3, 6}},     // Position 0
    {{0, 1, 2}, {1, 4, 7}},      // Position 1
    {{0, 1, 2}, {2, 14, 23}, {2, 5, 8}},    // Position 2
    {{3, 4, 5}, {3, 10, 18}, {0, 3, 6}},    // Position 3
    {{1, 4, 7}, {3, 4, 5}},      // Position 4
    {{3, 4, 5}, {5, 13, 20}, {2, 5, 8}},    // Position 5
    {{6, 7, 8}, {6, 11, 15}, {0, 3, 6}},    // Position 6
    {{1, 4, 7}, {6, 7, 8}},      // Position 7
    {{6, 7, 8}, {8, 12, 17}, {2, 5, 8}},    // Position 8
    {{0, 9, 21}, {9, 10, 11}},   // Position 9
    {{3, 10, 18}, {9, 10, 11}},  // Position 10
    {{6, 11, 15}, {9, 10, 11}},  // Position 11
    {{8, 12, 17}, {12, 13, 14}}, // Position 12
    {{5, 13, 20}, {12, 13, 14}}, // Position 13
    {{2, 14, 23}, {12, 13, 14}}, // Position 14
    {{6, 11, 15}, {15, 16, 17}, {15, 18, 21}}, // Position 15
    {{15, 16, 17}, {16, 19, 22}},// Position 16
    {{8, 12, 17}, {15, 16, 17}, {17, 20, 23}}, // Position 17
    {{3, 10, 18}, {18, 19, 20}, {15, 18, 21}}, // Position 18
    {{16, 19, 22}, {18, 19, 20}},// Position 19
    {{5, 13, 20}, {18, 19, 20}, {17, 20, 23}}, // Position 20
    {{0, 9, 21}, {21, 22, 23}, {15, 18, 21}},  // Position 21
    {{16, 19, 22}, {21, 22, 23}},// Position 22
    {{2, 14, 23}, {21, 22, 23}, {17, 20, 23}}  // Position 23
  };

  /**
   * Retrieves the appropriate mill paths based on the board version (Nine or Twelve).
   * 
   * @param in12version A boolean value indicating whether the board is in Twelve Men's Morris version.
   * @return A 3D array representing the mill paths for the board version.
   */
  static int[][][] get(boolean in12version) {
    return in12version ? TWELVE_MEN_MILL_PATHS : NINE_MEN_MILL_PATHS;
  }

}
