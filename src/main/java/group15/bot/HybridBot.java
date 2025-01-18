package group15.bot;

import group15.Game;
import group15.Player;

/**
 * Definition: A Hybrid Bot combines rule-based decision-making (fixed workflows and conditions)
 * with AI-based approaches (like MCTS) to handle both structured and unstructured interactions.
 *
 * Purpose: The goal is to balance predictable responses with flexible, adaptive interactions.
 *
 * In this Nine Men's Morris example:
 *   - AlphaBetaBot (rule-based) is used for structured, predictable decision-making
 *     (especially in the early "placing" phase and for carefully selecting pieces to delete).
 *   - MonteCarloBot (an adaptive approach) is used during more complex mid-/late-game
 *     decisions to better handle branching factors and uncertain outcomes.
 */
public class HybridBot implements Bot {


    private final AlphaBetaBot alphaBetaBot;


    private final MonteCarloBot monteCarloBot;

    public HybridBot() {
        this.alphaBetaBot = new AlphaBetaBot();
        this.monteCarloBot = new MonteCarloBot();
    }

    /**
     * During the opening "placing" phase (phase 0), we use a rule-based (AlphaBeta) approach.
     */
    @Override
    public int placePiece(Game game) {
        // Phase 0 => placing pieces
        return alphaBetaBot.placePiece(game);
    }

    /**
     * In mid/late game (phase 1 or 2), which can be more complex,
     * we switch to our MCTS-based approach for greater adaptability.
     */
    @Override
    public int selectPiece(Game game) {
        int phase = game.getPhase();
        if (phase == 1 || phase == 2) {
            return monteCarloBot.selectPiece(game);
        } else {

            return alphaBetaBot.selectPiece(game);
        }
    }

    /**
     * Continue using MCTS for movement in phases 1 or 2 for adaptive decision-making.
     * Fallback to rule-based if needed (e.g., unusual states).
     */
    @Override
    public int determineMove(Game game, int piecePosition) {
        int phase = game.getPhase();
        if (phase == 1 || phase == 2) {
            return monteCarloBot.determineMove(game, piecePosition);
        } else {
            return alphaBetaBot.determineMove(game, piecePosition);
        }
    }

    /**
     * When a mill is formed (phase < 0), the delete action is critical.
     * Use AlphaBeta for a reliable, rule-based choice. If invalid, fallback to MCTS or a simple rule-based approach.
     */
    @Override
    public int determinePieceToDelete(Game game) {

        int candidate = alphaBetaBot.determinePieceToDelete(game);


        if (!isValidDeleteChoice(game, candidate)) {
            int mctsCandidate = monteCarloBot.determinePieceToDelete(game);
            if (isValidDeleteChoice(game, mctsCandidate)) {
                return mctsCandidate;
            }

            int fallback = findAnyValidDelete(game);
            if (fallback != -1) {
                return fallback;
            }
        }

        return candidate;
    }


    private boolean isValidDeleteChoice(Game game, int candidate) {
        if (candidate < 0 || candidate >= 24) {
            return false;
        }
        Player opponent = game.getCurrentPlayer().opponent();

        if (game.getBoardPositions()[candidate] != opponent) {
            return false;
        }

        if (!game.allPiecesAreInMills(opponent) && game.formsMill(candidate, opponent)) {
            return false;
        }
        return true;
    }


    private int findAnyValidDelete(Game game) {
        Player opponent = game.getCurrentPlayer().opponent();
        boolean allInMills = game.allPiecesAreInMills(opponent);

        for (int i = 0; i < 24; i++) {
            if (game.getBoardPositions()[i] == opponent) {

                if (allInMills) {
                    return i;
                }

                if (!game.formsMill(i, opponent)) {
                    return i;
                }
            }
        }
        return -1;
    }
}