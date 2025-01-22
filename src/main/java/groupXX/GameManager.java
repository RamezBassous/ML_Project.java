package groupXX;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages multiple games, allowing the switching between them.
 * Each game can be controlled independently, and the listener is notified of events for each game.
 */
public class GameManager {
    
    private List<Game> games = new ArrayList<>();
    private int currentGameIndex = 0;

    /**
     * Constructs a GameManager that initializes the specified number of games.
     * 
     * @param numberOfGames The number of games to create and manage.
     * @param listener A listener that will be notified of game events (win, draw, etc.).
     */
    public GameManager(int numberOfGames, GameEventListener listener) {
        for (int i = 0; i < numberOfGames; i++) {
            Game game = new Game();
            game.setGameEventListener(listener);  // Assign the listener to each game
            games.add(game);
        }
    }

    /**
     * Returns the current game being managed.
     * 
     * @return The current game.
     */
    public Game getCurrentGame() {
        return games.get(currentGameIndex);
    }

    /**
     * Switches to the next game in the list. If the current game is the last one,
     * it wraps around to the first game.
     */
    public void switchToNextGame() {
        currentGameIndex = (currentGameIndex + 1) % games.size();
    }

    /**
     * Switches to the previous game in the list. If the current game is the first one,
     * it wraps around to the last game.
     */
    public void switchToPreviousGame() {
        currentGameIndex = (currentGameIndex - 1 + games.size()) % games.size();
    }

    /**
     * Returns the index of the current game.
     * 
     * @return The index of the current game.
     */
    public int getCurrentGameIndex() {
        return currentGameIndex;
    }

    /**
     * Getter for the list of all games managed by this GameManager.
     * 
     * @return A list of all games.
     */
    public List<Game> getGames() {
        return games;
    }
}
