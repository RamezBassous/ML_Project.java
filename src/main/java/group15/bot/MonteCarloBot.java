package group15.bot;

import group15.Game;
import group15.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MonteCarloBot implements Bot {


    private static final int TIME_LIMIT_MS = 3000;


    private static final double EXPLORATION_CONSTANT = 1.41;


    private static final int MAX_ROLLOUT_DEPTH = 50;

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


    private MCTSNode select(MCTSNode node) {
        MCTSNode current = node;
        while (!current.children.isEmpty()) {
            current = current.getBestChild(EXPLORATION_CONSTANT);
        }
        return current;
    }

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


    private void backpropagate(MCTSNode node, double result) {
        MCTSNode current = node;
        while (current != null) {
            current.visits++;
            current.wins += result;
            current = current.parent;
        }
    }


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
                for (int move : game.getValidMoves(selectedPos)) {
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

    private int pickRolloutAction(Game rolloutGame, ActionType actionType, List<Integer> possibleActions) {

        Random r = new Random();
        if (!possibleActions.isEmpty() && r.nextDouble() < 0.5) {
            return possibleActions.get(0);
        }
        return possibleActions.get(r.nextInt(possibleActions.size()));
    }


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
                count += game.getValidMoves(i).size();
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