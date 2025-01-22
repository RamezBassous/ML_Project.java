package groupXX;

class ClickOnPositionHandler {
  static void handle(Game game, int position) {
    if (game.getValidMoves().contains(position)) {
      game.makeMove(position);
    } else {
      System.out.println(game.getCurrentPlayer() + ": Invalid move: " + position);
    }
  }
}
