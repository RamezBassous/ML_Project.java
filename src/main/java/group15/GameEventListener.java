package group15;

public interface GameEventListener {
    void onGameWon(String winner);  // Notify when there's a winner
    void onGameDraw();              // Notify when there's a draw
}
