package group15.bot;

import group15.Game;
import group15.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MonteCarloBot implements Bot {


    private static final int TIME_LIMIT_MS = 3000;

    /**
     * Exploration constant C in the UCB1 formula (slides mention typical values 0.4‐0.8, or sqrt(2)=1.414...).
     * Adjust this if you want to put more emphasis on exploration or exploitation.
     */
    private static final double EXPLORATION_CONSTANT = 1.41;

    @Override
    public int placePiece(Game game) {

        return runMCTS(game, ActionType.PLACE, -1);
    }

    @Override
    public int selectPiece(Game game) {

        return runMCTS(game, ActionType.SELECT, -1);
    }

    @Override
    public int determineMove(Game game, int piecePosition) {

        return runMCTS(game, ActionType.MOVE, piecePosition);
    }

    @Override
    public int determinePieceToDelete(Game game) {

        return runMCTS(game, ActionType.DELETE, -1);
    }

    /**
     * The main MCTS loop, repeated until our time budget runs out (slides: repeated X times).
     *
     * @param game        The current game state
     * @param actionType  Which action we must perform (PLACE, SELECT, MOVE, DELETE)
     * @param selectedPos The position of the piece we are moving (only relevant for MOVE)
     * @return The best board position to use for the specified action
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

        // After time is up, pick the child of root with the highest visit count (c=0 => purely exploit).
        MCTSNode bestChild = root.getBestChild(0.0);
        if (bestChild == null) {
            // If somehow no children exist (no possible actions), fallback to a valid random move
            List<Integer> possibleActions = getPossibleActions(root.actionType, root.game, root.selectedPos);
            if (possibleActions.isEmpty()) return -1;
            return possibleActions.get(0);
        }
        return bestChild.chosenMove;
    }

    /**
     * SELECTION step (slides: "The selection strategy is applied recursively…").
     * Uses UCB to pick the best child until reaching a leaf.
     */
    private MCTSNode select(MCTSNode node) {
        MCTSNode current = node;
        // Descend until we reach a node with no children
        while (!current.children.isEmpty()) {
            current = current.getBestChild(EXPLORATION_CONSTANT);
        }
        return current;
    }

    /**
     * PLAY-OUT (aka roll-out or simulation) step:
     *  - From the given node’s state, play random moves until the game ends or a depth limit is reached.
     *  - Return +1 if the original (start) player wins, -1 if they lose, 0 otherwise (draw or unknown).
     *
     * Slides mention “nearly-random moves” and “can be biased using heuristics or early termination.”
     */
    private double simulate(MCTSNode node) {

        Game rolloutGame = cloneGame(node.game);


        Player startPlayer = rolloutGame.getCurrentPlayer();

        int depth = 0;

        while (!isTerminalState(rolloutGame) && depth < 25) {

            ActionType at = getActionType(rolloutGame);


            List<Integer> possibleActions = getRandomActionList(rolloutGame, at);
            if (possibleActions.isEmpty()) break; // no moves => terminal


            int randomMove = possibleActions.get(new Random().nextInt(possibleActions.size()));


            makeRolloutMove(rolloutGame, at, randomMove);
            depth++;
        }


        if (didPlayerWin(rolloutGame, startPlayer)) {
            return 1.0;
        } else if (didPlayerLose(rolloutGame, startPlayer)) {
            return -1.0;
        }

        return 0.0;
    }

    /**
     * BACKPROPAGATION step (slides: "The result of this game is backpropagated…"):
     *  - Add 1 visit, and add the result to the cumulative wins.
     *  - For two-player zero-sum games, some code flips sign at each parent. Here we keep the result
     *    from the perspective of the node's original current player for simplicity.
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
     * Checks if the given game is in a terminal state. (The slides mention "The selection strategy
     * stops when an unknown position is reached," but we also need to know if it's a finished game.)
     */
    private boolean isTerminalState(Game game) {

        return (game.getMoveCountBlue() + game.getMoveCountRed() >= 18)
                && (game.getPhase() == 1 || game.getPhase() == 2)
                && (
                !hasAnyValidMove(game, Player.BLUE) ||
                        !hasAnyValidMove(game, Player.RED)  ||
                        getPieceCount(game, Player.BLUE) < 3 ||
                        getPieceCount(game, Player.RED) < 3
        );
    }

    private boolean didPlayerWin(Game game, Player p) {

        Player opp = p.opponent();
        return getPieceCount(game, opp) < 3 || !hasAnyValidMove(game, opp);
    }

    private boolean didPlayerLose(Game game, Player p) {

        return getPieceCount(game, p) < 3 || !hasAnyValidMove(game, p);
    }

    private boolean hasAnyValidMove(Game game, Player player) {

        for (int i = 0; i < 24; i++) {
            if (game.getBoardPositions()[i] == player) {
                List<Integer> moves = game.getValidMoves(i);
                if (!moves.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getPieceCount(Game game, Player player) {
        int count = 0;
        for (Player pos : game.getBoardPositions()) {
            if (pos == player) count++;
        }
        return count;
    }

    /**
     * Inner class for MCTS nodes storing:
     *  - Link to parent,
     *  - Children list,
     *  - The game state at this node,
     *  - The action that led to this node (chosenMove),
     *  - MCTS statistics (visits, wins),
     *  - Bookkeeping for expansion (expanded flag).
     */
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

        MCTSNode(MCTSNode parent,
                 Integer chosenMove,
                 Game game,
                 ActionType actionType,
                 int selectedPos) {
            this.parent = parent;
            this.chosenMove = (chosenMove == null) ? -1 : chosenMove;
            this.game = game;
            this.actionType = actionType;
            this.selectedPos = selectedPos;
        }

        boolean isTerminal() {
            return isTerminalState(game);
        }

        /**
         * EXPANSION step:
         *  - If not already expanded, generate children for all possible next moves.
         *  - Return one new child (the typical MCTS approach is to expand just one child at a time).
         */
        MCTSNode expand() {
            if (expanded) {
                // Already expanded
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

        /**
         * The SELECTION policy: UCT (Upper Confidence Bounds for Trees).
         *
         * UCB = (wins / visits) + c * sqrt( ln(parent.visits) / visits ).
         *
         * The slides mention the multi-armed bandit problem, selection policy for exploitation vs. exploration,
         * and that we want arg max of (v_i + C * sqrt( ln n_p / n_i )).
         */
        MCTSNode getBestChild(double c) {
            if (children.isEmpty()) return null;

            MCTSNode best = null;
            double bestValue = Double.NEGATIVE_INFINITY;

            for (MCTSNode child : children) {
                double averageWinRate = child.wins / (child.visits + 1e-6); // exploitation term
                double explorationTerm = Math.sqrt(
                        Math.log(this.visits + 1.0) / (child.visits + 1e-6)
                );
                double ucbValue = averageWinRate + c * explorationTerm;

                if (ucbValue > bestValue) {
                    bestValue = ucbValue;
                    best = child;
                }
            }
            return best;
        }
    }

    /**
     * Return the set of possible actions (board positions) for the given action type.
     * This is domain-specific logic for Nine/Twelve-Men's Morris.
     */
    private List<Integer> getPossibleActions(ActionType actionType, Game game, int selectedPos) {
        List<Integer> actions = new ArrayList<>();

        switch (actionType) {
            case PLACE:

                if (game.getPhase() == 0) {
                    for (int i = 0; i < 24; i++) {
                        if (game.getBoardPositions()[i] == null) {
                            actions.add(i);
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

                actions.addAll(game.getValidMoves(selectedPos));
                break;

            case DELETE:

                Player opp = game.getCurrentPlayer().opponent();
                boolean allInMills = true;
                for (int i = 0; i < 24; i++) {
                    if (game.getBoardPositions()[i] == opp && !game.formsMill(i, opp)) {
                        allInMills = false;
                        break;
                    }
                }
                for (int i = 0; i < 24; i++) {
                    if (game.getBoardPositions()[i] == opp) {
                        if (!allInMills) {
                            if (!game.formsMill(i, opp)) {
                                actions.add(i);
                            }
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
     * Apply a specific action to a game (mutates that game).
     * This is used for building child states in the tree (Expansion) and for rollouts (Simulation).
     */
    private void applyAction(Game childGame, ActionType actionType, int move, int selectedPos) {
        switch (actionType) {
            case PLACE:
                // Place a piece in an empty spot
                childGame.makeMove(move);
                break;

            case SELECT:
                // Select which piece to move
                childGame.makeMove(move);
                break;

            case MOVE:
                // We have a selected piece, now move it
                childGame.setSelectedPiece(selectedPos);
                childGame.makeMove(move);
                break;

            case DELETE:
                // Remove an opponent piece
                childGame.makeMove(move);
                break;
        }
    }

    /**
     * Determine the next action type by inspecting the game phase and selected piece.
     * (Slides show how we systematically pick the next step in the search/rollout.)
     */
    private ActionType getActionType(Game g) {
        int phase = g.getPhase();

        // Phase 0 => placing
        if (phase == 0) {
            return ActionType.PLACE;
        }
        // Phase 1 or 2 => either SELECT or MOVE
        if (phase == 1 || phase == 2) {
            if (g.getSelectedPiece() == -1) {
                // Need to choose which piece to move
                return ActionType.SELECT;
            } else {
                // We have a piece selected, so we move it
                return ActionType.MOVE;
            }
        }
        // Negative phase => we must DELETE an opponent piece (formed a mill)
        if (phase < 0) {
            return ActionType.DELETE;
        }
        // Fallback
        return ActionType.PLACE;
    }

    /**
     * For the rollout, gather possible actions from the current game state
     * and pick a random one. (Slides mention "random moves until end of the game.")
     */
    private List<Integer> getRandomActionList(Game rolloutGame, ActionType at) {
        return getPossibleActions(at, rolloutGame, rolloutGame.getSelectedPiece());
    }

    /**
     * Execute the chosen move in the rollout game.
     * Heuristics could be added here (slides mention "bias the moves to good ones").
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
    }

    /**
     * Create a deep copy of the game for simulation so we don't mutate the real game state.
     * Adjust or use a copy constructor as needed based on your Game class implementation.
     */
    private Game cloneGame(Game original) {
        Game copy = new Game();
        copy.setIn12MenMorrisVersion(original.isIn12MenMorrisVersion());
        copy.setPhase(original.getPhase());
        copy.setCurrentPlayer(original.getCurrentPlayer());
        copy.setMoveCountBlue(original.getMoveCountBlue());
        copy.setMoveCountRed(original.getMoveCountRed());
        copy.setSelectedPiece(original.getSelectedPiece());

        // Copy the board positions
        Player[] newBoardPositions = Arrays.copyOf(original.getBoardPositions(), 24);
        copy.setBoardPositions(newBoardPositions);

        return copy;
    }


    private enum ActionType {
        PLACE, SELECT, MOVE, DELETE
    }
}
