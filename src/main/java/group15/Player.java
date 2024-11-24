package group15;

public enum Player {
  BLUE(1), //for current player 1
  RED(2); // for 2

  private final int index;

  Player(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public Player opponent() {
    return this == BLUE ? RED : BLUE;
  }

}
