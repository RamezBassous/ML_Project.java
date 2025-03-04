package groupXX;

import groupXX.bot.Bot;
import groupXX.bot.MeatBot;

public interface GameStrategy {
  void handleMouseClickEvent(int position);

  default int makeBotMove(Game game, Bot bot) {
    return switch (game.getPhase()) {
      case 0 -> bot.placePiece(game);
      case 1, 2 -> game.selectedPiece == -1 ? bot.selectPiece(game) : bot.determineMove(game, game.selectedPiece);
      case -1, -2 -> bot.determinePieceToDelete(game);
      default -> throw new IllegalStateException("Unexpected phase:" + game.getPhase());
    };
  }
}

class HumanVsHumanStrategy implements GameStrategy {
  private final Game game;

  HumanVsHumanStrategy(Game game) {
    this.game = game;
  }

  public void handleMouseClickEvent(int position) {
    if (!game.isOver()) {
      ClickOnPositionHandler.handle(game, position);
    }
  }
}

class HumanVsBotStrategy implements GameStrategy {
  private final Game game;

  HumanVsBotStrategy(Game game) {
    this.game = game;
  }

  public void handleMouseClickEvent(int position) {
    if (game.isOver()) {
      return;
    }
    Bot currentBot = game.getCurrentBot();
    int invalidConsecutiveMoves = 0;
    do {
      if (!(currentBot instanceof MeatBot)) {
        position = makeBotMove(game, currentBot);
        if (position == -1) {
          System.out.println(game.phase + ": " + game.currentPlayer + ": "
            + "BOT returned -1, he says there are no valid moves, bot lost");
          invalidConsecutiveMoves++;
        } else {
          invalidConsecutiveMoves = 0;
        }
      }
      ClickOnPositionHandler.handle(game, position);
      currentBot = game.getCurrentBot();
    } while (!(currentBot instanceof MeatBot) && !game.isOver() && invalidConsecutiveMoves < 1_000);
    if (invalidConsecutiveMoves >= 1_000) {
      game.setBotLostNoValidMoves();
    }
  }

}

class BotVsBotStrategy implements GameStrategy {
  private final Game game;

  BotVsBotStrategy(Game game) {
    this.game = game;
  }

  public void handleMouseClickEvent(int position) {
    int invalidConsecutiveMoves = 0;
    while (!game.isOver() && invalidConsecutiveMoves < 1_000) {
      Bot currentBot = game.getCurrentBot();
      position = makeBotMove(game, currentBot);
      if (position == -1) {
        System.out.println(game.phase + ": " + game.currentPlayer + ": "
          + "BOT returned -1, he says there are no valid moves, bot lost");
        invalidConsecutiveMoves++;
      } else {
        invalidConsecutiveMoves = 0;
      }
      ClickOnPositionHandler.handle(game, position);
    }
    if (invalidConsecutiveMoves >= 1_000) {
      game.setBotLostNoValidMoves();
    }
  }

}
