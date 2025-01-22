package groupXX.bot;

import groupXX.Game;
import groupXX.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MonteCarloBot implements Bot {


    private static final int TIME_LIMIT_MS = 3000;
    private static final double EXPLORATION_CONSTANT = 1.41;
    private static final int MAX_ROLLOUT_DEPTH = 50;

    /**
     * Places a piece on the board using MCTS.
     *
     * @param game the current game state
     * @return the position to place the piece
     */
    @Override
    public int placePiece(Game game) {

        return runMCTS(game, ActionType.PLACE, -1);
    }

    /**
     * Selects a piece to move using MCTS.
     *
     * @param game the current game state
     * @return the position of the selected piece
     */
    @Override
    public int selectPiece(Game game) {

        return runMCTS(game, ActionType.SELECT, -1);
    }

    /**
     * Determines the move to make based on the selected piece.
     *
     * @param game the current game state
     * @param piecePosition the position of the selected piece
     * @return the position to move the piece to
     */
    @Override
    public int determineMove(Game game, int piecePosition) {

        return runMCTS(game, ActionType.MOVE, piecePosition);
    }

    /**
     * Determines which piece to delete using MCTS.
     *
     * @param game the current game state
     * @return the position of the piece to delete
     */
    @Override
    public int determinePieceToDelete(Game game) {

        return runMCTS(game, ActionType.DELETE, -1);
    }


    /**
     * Runs the MCTS algorithm for a given action type and game state.
     *
     * @param game the current game state
     * @param actionType the type of action to perform (place, select, move, delete)
     * @param selectedPos the selected position (for select and move actions)
     * @return the best move determined by MCTS
     */
    private int runMCTS(Game game, ActionType actionType, int selectedPos) {
        long endTime = System.currentTimeMillis() + TIME_LIMIT_MS;


        MCTSNode root = new MCTSNode(null, null, game, actionType, selectedPos);

        while (System.currentTimeMillis() < endTime) {

            MCTSNode leaf = select(root);

            if (!leaf.isTerminal()) {
                leaf = leaf.expand();
            }

            double result = simulate(leaf);

            backpropagate(leaf, result);
        }


        MCTSNode bestChild = root.getBestChild(0.0);
        if (bestChild == null) {

            List<Integer> possibleActions = getPossibleActions(root.actionType, root.game, root.selectedPos);
            if (possibleActions.isEmpty()) return -1;
            return possibleActions.get(0);
        }
        return bestChild.chosenMove;
    }

    /**
     * Selects the best node to expand based on the UCB formula.
     *
     * @param node the current node to select from
     * @return the best child node
     */
    private MCTSNode select(MCTSNode node) {
        MCTSNode current = node;
        while (!current.children.isEmpty()) {
            current = current.getBestChild(EXPLORATION_CONSTANT);
        }
        return current;
    }

    /**
     * Simulates a random game from the current node.
     *
     * @param node the current node to simulate from
     * @return the result of the simulation (1 for win, -1 for loss, 0 for draw)
     */
    private double simulate(MCTSNode node) {
        Game rolloutGame = cloneGame(node.game);
        Player startPlayer = rolloutGame.getCurrentPlayer();

        int depth = 0;
        while (!isTerminalState(rolloutGame) && depth < MAX_ROLLOUT_DEPTH) {
            ActionType actionType = getActionType(rolloutGame);
            List<Integer> possibleActions = getPossibleActions(actionType, rolloutGame, rolloutGame.getSelectedPiece());
            if (possibleActions.isEmpty()) {

                break;
            }

            int chosenAction = pickRolloutAction(rolloutGame, actionType, possibleActions);

            makeRolloutMove(rolloutGame, actionType, chosenAction);

            depth++;
        }

        // Evaluate the final state from the perspective of "startPlayer".
        if (didPlayerWin(rolloutGame, startPlayer)) {
            return 1.0;
        } else if (didPlayerLose(rolloutGame, startPlayer)) {
            return -1.0;
        } else {

            return evaluateBoardState(rolloutGame, startPlayer);
        }
    }

    /**
     * Backpropagates the result of a simulation to update the node's statistics.
     *
     * @param node the node to backpropagate from
     * @param result the result of the simulation
     */
    private void backpropagate(MCTSNode node, double result) {
        MCTSNode current = node;
        while (current != null) {
            current.visits++;
            current.wins += result;
            current = current.parent;
        }
    }

    /**
     * Determines if the game is in a terminal state.
     *
     * @param game the current game state
     * @return true if the game is in a terminal state, false otherwise
     */
    private boolean isTerminalState(Game game) {

        int totalPiecesNeeded = game.isIn12MenMorrisVersion() ? 24 : 18;


        return (game.getPlacedPiecesBlue() + game.getPlacedPiecesRed() >= totalPiecesNeeded)
          && (game.getPhase() == 1 || game.getPhase() == 2)
          && (
          !hasAnyValidMove(game, Player.BLUE) ||
            !hasAnyValidMove(game, Player.RED)  ||
            getPieceCount(game, Player.BLUE) < 3 ||
            getPieceCount(game, Player.RED) < 3
        );
    }

    /**
     * Checks if the player has won the game.
     *
     * @param game the current game state
     * @param p the player to check
     * @return true if the player has won, false otherwise
     */
    private boolean didPlayerWin(Game game, Player p) {
        Player opp = p.opponent();
        return getPieceCount(game, opp) < 3 || !hasAnyValidMove(game, opp);
    }

    /**
     * Checks if the player has lost the game.
     *
     * @param game the current game state
     * @param p the player to check
     * @return true if the player has lost, false otherwise
     */
    private boolean didPlayerLose(Game game, Player p) {
        return getPieceCount(game, p) < 3 || !hasAnyValidMove(game, p);
    }

    /**
     * Determines if a player has any valid moves available.
     *
     * @param game the current game state
     * @param player the player to check
     * @return true if the player has at least one valid move, false otherwise
     */
    private boolean hasAnyValidMove(Game game, Player player) {
        for (int i = 0; i < 24; i++) {
            if (game.getBoardPositions()[i] == player) {
                List<Integer> moves = game.getValidMoves();
                if (!moves.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the number of pieces of a player on the board.
     *
     * @param game the current game state
     * @param player the player to count the pieces of
     * @return the number of pieces the player has on the board
     */
    private int getPieceCount(Game game, Player player) {
        int count = 0;
        for (Player pos : game.getBoardPositions()) {
            if (pos == player) count++;
        }
        return count;
    }

    // MCTS Node class definition with essential methods (e.g., expand, getBestChild, etc.)
    private class MCTSNode {
        MCTSNode parent;
        List<MCTSNode> children = new ArrayList<>();
        Game game;
        int visits = 0;
        double wins = 0.0;

        int chosenMove;
        ActionType actionType;
        int selectedPos;
        boolean expanded = false;

        MCTSNode(MCTSNode parent, Integer chosenMove, Game game, ActionType actionType, int selectedPos) {
            this.parent = parent;
            this.chosenMove = (chosenMove == null) ? -1 : chosenMove;
            this.game = game;
            this.actionType = actionType;
            this.selectedPos = selectedPos;
        }

        boolean isTerminal() {
            return isTerminalState(game);
        }


        MCTSNode expand() {
            if (expanded) {
                return this;
            }
            expanded = true;

            List<Integer> possibleMoves = getPossibleActions(actionType, game, selectedPos);
            if (possibleMoves.isEmpty()) {
                return this;
            }

            for (int move : possibleMoves) {
                Game childGame = cloneGame(game);

                applyAction(childGame, actionType, move, selectedPos);

                ActionType nextActionType = getActionType(childGame);
                int nextSelectedPos = -1;
                if (actionType == ActionType.SELECT) {
                    nextSelectedPos = move;
                }

                MCTSNode child = new MCTSNode(this, move, childGame, nextActionType, nextSelectedPos);
                children.add(child);
            }


            return children.get(new Random().nextInt(children.size()));
        }


        MCTSNode getBestChild(double c) {
            if (children.isEmpty()) return null;

            MCTSNode best = null;
            double bestValue = Double.NEGATIVE_INFINITY;

            for (MCTSNode child : children) {
                double avgWinRate = child.wins / (child.visits + 1e-6);
                double explorationTerm = Math.sqrt(Math.log(this.visits + 1.0) / (child.visits + 1e-6));
                double ucbValue = avgWinRate + c * explorationTerm;


                if (game.formsMill(child.chosenMove, game.getCurrentPlayer())) {
                    ucbValue += 0.1;
                }

                if (game.formsMill(child.chosenMove, game.getCurrentPlayer().opponent())) {
                    ucbValue -= 0.1;
                }

                if (ucbValue > bestValue) {
                    bestValue = ucbValue;
                    best = child;
                }
            }
            return best;
        }
    }

    /**
     * Retrieves the list of possible actions for the AI to take, based on the current game phase
     * and the provided action type.
     *
     * @param actionType The type of action (PLACE, SELECT, MOVE, DELETE).
     * @param game The current game state.
     * @param selectedPos The position of the currently selected piece, if applicable.
     * @return A list of possible actions that the AI can take.
     */
    private List<Integer> getPossibleActions(ActionType actionType, Game game, int selectedPos) {
        List<Integer> actions = new ArrayList<>();
        switch (actionType) {
            case PLACE:
                if (game.getPhase() == 0) {
                    for (int i = 0; i < 24; i++) {
                        if (game.getBoardPositions()[i] == null) {
                            if (game.formsMill(i, game.getCurrentPlayer())) {
                                actions.add(0, i);
                            } else {
                                actions.add(i);
                            }
                        }
                    }
                }
                break;
            case SELECT:
                for (int i = 0; i < 24; i++) {
                    if (game.getBoardPositions()[i] == game.getCurrentPlayer()) {
                        actions.add(i);
                    }
                }
                break;
            case MOVE:
                for (int move : game.getValidMoves()) {
                    if (game.formsMill(move, game.getCurrentPlayer())) {
                        actions.add(0, move);
                    } else {
                        actions.add(move);
                    }
                }
                break;
            case DELETE:
                Player opponent = game.getCurrentPlayer().opponent();
                for (int i = 0; i < 24; i++) {
                    if (game.getBoardPositions()[i] == opponent) {
                        if (!game.formsMill(i, opponent)) {

                            actions.add(0, i);
                        } else {
                            actions.add(i);
                        }
                    }
                }
                break;
        }
        return actions;
    }

    /**
     * Applies the specified action to a copy of the game state.
     *
     * @param childGame The copy of the game state to which the action will be applied.
     * @param actionType The type of action to apply (PLACE, SELECT, MOVE, DELETE).
     * @param move The move to be executed.
     * @param selectedPos The position of the selected piece, if applicable.
     */
    private void applyAction(Game childGame, ActionType actionType, int move, int selectedPos) {
        switch (actionType) {
            case PLACE:
            case SELECT:
            case DELETE:
                childGame.makeMove(move);
                break;
            case MOVE:
                childGame.setSelectedPiece(selectedPos);
                childGame.makeMove(move);
                break;
        }

        if (actionType != ActionType.DELETE
          && childGame.formsMill(move, childGame.getCurrentPlayer())) {
            childGame.setPhase(-1);
        }
    }

    /**
     * Determines the action type to be taken based on the current game phase.
     *
     * @param g The current game state.
     * @return The corresponding action type (PLACE, SELECT, MOVE, DELETE).
     */
    private ActionType getActionType(Game g) {
        int phase = g.getPhase();

        if (phase == 0) {
            return ActionType.PLACE;
        }

        if (phase == 1 || phase == 2) {
            if (g.getSelectedPiece() == -1) {
                return ActionType.SELECT;
            } else {
                return ActionType.MOVE;
            }
        }

        if (phase < 0) {
            return ActionType.DELETE;
        }

        return ActionType.PLACE;
    }

    /**
     * Chooses an action randomly from a list of possible actions, with a bias toward the first action.
     *
     * @param rolloutGame The current game state during the rollout.
     * @param actionType The action type to be applied.
     * @param possibleActions A list of possible actions.
     * @return The chosen action (move position).
     */
    private int pickRolloutAction(Game rolloutGame, ActionType actionType, List<Integer> possibleActions) {

        Random r = new Random();
        if (!possibleActions.isEmpty() && r.nextDouble() < 0.5) {
            return possibleActions.get(0);
        }
        return possibleActions.get(r.nextInt(possibleActions.size()));
    }

    /**
     * Makes a move on the provided game state based on the given action type.
     *
     * @param g The current game state.
     * @param at The action type to be applied.
     * @param move The move to be executed.
     */
    private void makeRolloutMove(Game g, ActionType at, int move) {
        switch (at) {
            case PLACE:
            case SELECT:
            case DELETE:
                g.makeMove(move);
                break;
            case MOVE:
                int oldSelected = g.getSelectedPiece();
                g.setSelectedPiece(oldSelected);
                g.makeMove(move);
                break;
        }

        if (at != ActionType.DELETE && g.formsMill(move, g.getCurrentPlayer())) {
            g.setPhase(-1);
        }
    }

    /**
     * Creates a deep copy of the provided game state.
     *
     * @param original The original game state to be cloned.
     * @return A new instance of the game with the same state as the original.
     */
    private Game cloneGame(Game original) {
        Game copy = new Game();
        copy.setIn12MenMorrisVersion(original.isIn12MenMorrisVersion());
        copy.setPhase(original.getPhase());
        copy.setCurrentPlayer(original.getCurrentPlayer());
        copy.setPlacedPiecesBlue(original.getPlacedPiecesBlue());
        copy.setPlacedPiecesRed(original.getPlacedPiecesRed());
        copy.setSelectedPiece(original.getSelectedPiece());


        Player[] newBoardPositions = Arrays.copyOf(original.getBoardPositions(), 24);
        copy.setBoardPositions(newBoardPositions);

        return copy;
    }

    /**
     * Evaluates the current board state and returns a score representing the advantage of the player.
     *
     * @param game The current game state.
     * @param player The player for whom the evaluation is performed.
     * @return A score between -1.0 and 1.0 representing the advantage for the given player.
     */
    private double evaluateBoardState(Game game, Player player) {
        Player opp = player.opponent();

        int myCount = getPieceCount(game, player);
        int oppCount = getPieceCount(game, opp);

        int pieceDiff = myCount - oppCount;

        int myMoves = countAllPossibleMoves(game, player);
        int oppMoves = countAllPossibleMoves(game, opp);


        double score = 0.5 * pieceDiff + 0.5 * (myMoves - oppMoves);

        double maxPossible = 24.0;
        double minPossible = -24.0;
        double normalized = (score - minPossible) / (maxPossible - minPossible);

        return 2.0 * normalized - 1.0;
    }

    /**
     * Counts all possible valid moves for the given player.
     *
     * @param game The current game state.
     * @param p The player for whom the valid moves are counted.
     * @return The total number of valid moves available for the given player.
     */
    private int countAllPossibleMoves(Game game, Player p) {
        int count = 0;

        if (game.getPhase() == 0) {
            for (Player pos : game.getBoardPositions()) {
                if (pos == null) count++;
            }
            return count;
        }

        for (int i = 0; i < 24; i++) {
            if (game.getBoardPositions()[i] == p) {
                count += game.getValidMoves().size();
            }
        }
        return count;
    }

    /**
     * Action types the MCTS goes through.
     */
    private enum ActionType {
        PLACE, SELECT, MOVE, DELETE
    }
}