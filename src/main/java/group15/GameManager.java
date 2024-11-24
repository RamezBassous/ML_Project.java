package group15;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private List<Game> games = new ArrayList<>();
    private int currentGameIndex = 0;

    public GameManager(int numberOfGames, GameEventListener listener) {
        for (int i = 0; i < numberOfGames; i++) {
            Game game = new Game();
            game.setGameEventListener(listener);  // Assign the listener to each game
            games.add(game);
        }
    }

    public Game getCurrentGame() {
        return games.get(currentGameIndex);
    }

    public void switchToNextGame() {
        currentGameIndex = (currentGameIndex + 1) % games.size();
    }

    public void switchToPreviousGame() {
        currentGameIndex = (currentGameIndex - 1 + games.size()) % games.size();
    }

    public int getCurrentGameIndex() {
        return currentGameIndex;
    }

    // Getter to access all games
    public List<Game> getGames() {
        return games;
    }
}

