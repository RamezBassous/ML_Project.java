package groupXX.bot;

import groupXX.Game;
import groupXX.GameOri;
import groupXX.Player;

import java.util.List;

public class MeatBot implements Bot {

  private List<EvaluationResult> evaluations;
  private final Player player;

  public MeatBot(Player player) {
    this.player = player;
  }

  private GameOri copyGame(Game game) {
    GameOri gameOri = new GameOri();
    //gameOri.boardPositions = Arrays.copyOf(game.boardPositions, game.boardPositions.length);
    for (int i = 0; i < game.boardPositions.length; i++) {
      gameOri.boardPositions[i] = (game.boardPositions[i] != null) ? game.boardPositions[i].getIndex() : 0;
    }
    gameOri.gameMode = game.gameMode;
    gameOri.phase = game.phase;
    gameOri.moveCountBlue = game.placedPiecesBlue;
    gameOri.moveCountRed = game.placedPiecesRed;
    gameOri.currentPlayer = (game.currentPlayer == Player.BLUE) ? 1 : 2;
    gameOri.selectedPiece = game.selectedPiece;
    gameOri.moveWithoutCapture = game.moveWithoutCapture;
    gameOri.drawAgreed = game.drawAgreed;
    return gameOri;
  }

  @Override
  public int placePiece(Game game) {
    System.out.println("Place piece MEATBOT");
    Player[] boardPositions = game.boardPositions;
    if (boardPositions[game.clickedPosition] != null) {
      System.out.println("Position " + game.clickedPosition + " is already occupied.");  // Print for occupied position
      return -1; // Position is already occupied
    }
    return game.clickedPosition;
  }

  @Override
  public int selectPiece(Game game) {
    return game.boardPositions[game.clickedPosition] == player ? game.clickedPosition : -1;
  }

  @Override
  public int determineMove(Game game, int selectedPiece) {
    if (game.selectedPiece == -1) {
      return -1;
    }
    if (game.boardPositions[game.clickedPosition] != null &&
      game.boardPositions[game.clickedPosition] != player) {
      return -1;
    }
    if (game.isInFlyingPhase() ||
      game.gameBoard.getNeighbors(game.selectedPiece).contains(game.clickedPosition) ||
      game.boardPositions[game.clickedPosition] == player) {
      return game.clickedPosition;
    }
    return -1;
  }

  @Override
  public int determinePieceToDelete(Game game) {
    if (game.boardPositions[game.clickedPosition] == null ||
      game.boardPositions[game.clickedPosition] != player.opponent()) {
      return -1;
    }
    if (game.allPiecesAreInMills(game.currentPlayer.opponent())) {
      return game.clickedPosition;
    }
    if (game.formsMill(game.clickedPosition, game.currentPlayer.opponent())) {
      return -1;
    }
    return game.clickedPosition;
  }

}