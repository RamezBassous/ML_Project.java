package groupXX.bot;


import groupXX.Game;
import groupXX.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AlphaBetaBot is an implementation of a bot for the Nine Men's Morris game using the Alpha-Beta pruning
 * algorithm for decision-making during the game.
 */
public class AlphaBetaBot implements Bot {
    private int move_to = -1;


    @Override
    public int placePiece(Game game) {
        GameState state = new GameState(game);

        int maxDepth = 8;
        long endTime = System.currentTimeMillis() + 2000;

        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;

        for (int depth = 1; depth <= maxDepth; depth++) {
            if (System.currentTimeMillis() > endTime) {
                break;
            }
            // alpha-beta search for placing
            int[] result = placePiece_limited_alphabeta_search(state, depth);
            int score = result[0];
            int move = result[1];

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        return bestMove;
    }


    @Override
    public int selectPiece(Game game) {
        // 1) check canThree
        int[] result = canThree(new GameState(game), 6);
        // if canThree found a move
        if (result[0] != -1) {
            move_to = result[2];
            return result[1]; // 'from'
        }

        // 2) check crackThree
        result = crackThree(new GameState(game));
        if (result[0] != -1) {
            move_to = result[2];
            return result[1];
        }

        // 3) do alpha-beta for normal move
        GameState state = new GameState(game);
        int[] best = movePiece_limited_alphabeta_search(state, 10);
        // best => [score, fromPos, toPos]
        int fromPos = best[1];
        move_to = best[2];

        // ----- fallback for 'fromPos' -----
        if (fromPos < 0 || fromPos >= 24) {
            System.out.println("[AlphaBetaBot] fromPos=" + fromPos
                    + " => fallback to random piece");
            fromPos = pickRandomMoveSource(game);
            move_to = pickRandomMoveTarget(game, fromPos);
        }

        // ----- fallback for 'move_to' -----
        if (move_to < 0 || move_to >= 24) {
            System.out.println("[AlphaBetaBot] move_to=" + move_to
                    + " => fallback picking random target");
            move_to = pickRandomMoveTarget(game, fromPos);

            if (move_to < 0 || move_to >= 24) {
                System.out.println("[DEBUG] AlphaBetaBot STILL got invalid move_to = "
                        + move_to + ", giving up this turn...");
            }
        }

        return fromPos;
    }


    @Override
    public int determineMove(Game game, int selectedPiece) {
        return move_to;
    }


    @Override
    public int determinePieceToDelete(Game game) {
        // 1) Use your existing alpha-beta search to get a candidate
        GameState state = new GameState(game);
        int[] result = deletePiece_limited_alphabeta_search(state, 6);
        int candidate = result[1]; // suggested piece to delete

        // 2) Check if the candidate is valid under the rules
        if (!isValidDeleteChoice(game, candidate)) {
            int fallback = findAnyValidDelete(game);
            if (fallback != -1) {
                System.out.println("[AlphaBetaBot] Fallback deletion from " + candidate + " to " + fallback);
                return fallback;
            }
            System.out.println("[AlphaBetaBot] No valid piece found to delete. Returning candidate anyway.");
        }
        return candidate;
    }

    private boolean isValidDeleteChoice(Game game, int candidate) {
        if (candidate < 0 || candidate >= 24) return false;
        Player opp = game.getCurrentPlayer().opponent();
        if (game.getBoardPositions()[candidate] != opp) return false;
        // If not all opp pieces are in mills, can't delete a piece in a mill
        if (!game.allPiecesAreInMills(opp) && game.formsMill(candidate, opp)) {
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
                    return i; // delete any piece
                } else if (!game.formsMill(i, opponent)) {
                    return i; // must delete a piece not in a mill
                }
            }
        }
        return -1;
    }


    private int[] placePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        return placePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }


    private int[] placePiece_maxValue(GameState state, int alpha, int beta, int depthLimit) {

        if (placePiece_iSterminal(state)) {

            if (state.currentPlayer == Player.RED) {
                return new int[]{1000000000, -1};
            } else {
                return new int[]{-1000000000, -1};
            }
        }


        int v = Integer.MIN_VALUE;
        int bestMove = -1;


        Player p = state.currentPlayer;
        List<Integer> actions = state.actions();
        if (actions.isEmpty()) {
            return new int[]{Integer.MIN_VALUE, -1};
        }

        if (depthLimit == 0) {
            v = boardScore(state);
            return new int[]{v, bestMove};
        }

        for (int pos : actions) {
            GameState nextState = state.newState(pos, p);

            nextState.currentPlayer = (p == Player.RED) ? Player.BLUE : Player.RED;

            int[] childResult = placePiece_minValue(nextState, alpha, beta, depthLimit - 1);
            int childScore = childResult[0];

            if (childScore > v) {
                v = childScore;
                bestMove = pos;
                alpha = Math.max(alpha, v);
                if (v >= beta) {

                    break;
                }
            }
        }

        return new int[]{v, bestMove};
    }


    private int quickActionScore(GameState state, int position, Player player) {

        int score = 0;

        if (formsMillIfPlace(state, position, player)) {
            score += 100;
        }
        return score;
    }


    private boolean formsMillIfPlace(GameState state, int position, Player player) {
        Player old = state.boardPositions[position];
        state.boardPositions[position] = player;
        boolean mill = state.formsMill(position, player);
        state.boardPositions[position] = old;
        return mill;
    }

    /**
     * Determines the best move for the current player to place a piece on the board 
     * using a minimax algorithm with alpha-beta pruning.

     */
    private int[] placePiece_minValue(GameState state, int alpha, int beta, int depthLimit) {
        if (placePiece_iSterminal(state)) {
            if (state.currentPlayer == Player.RED) {
                return new int[]{1000000000, -1};
            } else {
                return new int[]{-1000000000, -1};
            }
        }

        int v = Integer.MAX_VALUE;
        int bestMove = -1;

        Player p = state.currentPlayer;
        List<Integer> actions = state.actions();
        if (actions.isEmpty()) {
            return new int[]{Integer.MAX_VALUE, -1};
        }

        if (depthLimit == 0) {
            v = boardScore(state);
            return new int[]{v, bestMove};
        }

        for (int pos : actions) {
            GameState nextState = state.newState(pos, p);
            nextState.currentPlayer = (p == Player.RED) ? Player.BLUE : Player.RED;


            int[] childResult = placePiece_maxValue(nextState, alpha, beta, depthLimit - 1);
            int childScore = childResult[0];

            if (childScore < v) {
                v = childScore;
                bestMove = pos;
                beta = Math.min(beta, v);
                if (v <= alpha) {
                    break;
                }
            }
        }

        return new int[]{v, bestMove};
    }

    /**
    * Checks whether the current game state is terminal (i.e., no further moves can be made).
    *

    */
    private boolean placePiece_iSterminal(GameState state) {
        // e.g. both players have placed all required pieces
        return (state.moveCountBlue == state.gameBoard.getRequiredPieces())
                && (state.moveCountRed == state.gameBoard.getRequiredPieces());
    }

    /**
    * Checks if a given position on the board has a neighboring empty spot.

    */
    public boolean hasNeighbour(GameState state, int position) {
        List<Integer> neighbors = state.gameBoard.getNeighbors(position);
        for (int n : neighbors) {
            if (state.boardPositions[n] == null) {
                return true;
            }
        }
        return false;
    }

    /**
    * Calculates the heuristic board score for the current state.
    * The score is influenced by factors such as piece count, potential mills, and mobility.

    */
    private int boardScore(GameState state) {
        double adjust_weight = 2.5;
        Player myPlayer  = state.currentPlayer;
        Player oppPlayer = myPlayer.opponent();

        boolean isPlacingPhase = state.isPlacingPhase();
        int required = state.gameBoard.getRequiredPieces(); // 9 or 12
        int totalMovesSoFar = state.moveCountBlue + state.moveCountRed;

        int basicScore = getHeuristicScore(state, myPlayer)
                - (int)(adjust_weight * getHeuristicScore(state, oppPlayer));


        int myCount  = countPieces(state, myPlayer);
        int oppCount = countPieces(state, oppPlayer);
        int pieceDiffScore = (myCount - oppCount) * 5;

        int myPotentialMills  = countPotentialMills(state, myPlayer);
        int oppPotentialMills = countPotentialMills(state, oppPlayer);
        int potentialMillScore = (myPotentialMills - oppPotentialMills) * 3;


        int mobilityScore = 0;
        if (!isPlacingPhase) {
            int myMobility  = calcMobility(state, myPlayer);
            int oppMobility = calcMobility(state, oppPlayer);
            mobilityScore   = (myMobility - oppMobility) * 2;
        }


        int openingScore = 0;
        if (isPlacingPhase) {

            if (totalMovesSoFar < required * 1.0) {

                openingScore += evaluateEarlyPlacement(state, myPlayer);
            }
        }


        int defenseScore = 0;

        if (oppPotentialMills > 2) {
            defenseScore -= 15;
        }


        int totalScore = basicScore
                + pieceDiffScore
                + potentialMillScore
                + mobilityScore
                + openingScore
                + defenseScore;

        return totalScore;
    }

    private int evaluateEarlyPlacement(GameState state, Player myPlayer) {
        int[] cornerPositions  = {0, 2, 6, 8, 15,17,21,23};
        int[] centerPositions  = {4, 19};

        int score = 0;

        for (int pos : cornerPositions) {
            if (state.boardPositions[pos] == myPlayer) {
                score += 3;
            }
        }

        for (int pos : centerPositions) {
            if (state.boardPositions[pos] == myPlayer) {
                score += 5;
            }
        }
        return score;
    }

    private int calcMobility(GameState state, Player player) {
        // If still in placing phase, we might ignore mobility or return 0
        if (state.moveCountBlue < state.gameBoard.getRequiredPieces()
                || state.moveCountRed < state.gameBoard.getRequiredPieces()) {
            return 0;
        }
        List<int[]> actions = state.selectActions(player);
        return actions.size();
    }


    private int countPotentialMills(GameState state, Player player) {
        int count = 0;
        int[][] paths = get(state.gameBoard.isIn12MenVer());

        for (int[] path : paths) {
            int pCount = 0;
            int nullCount = 0;
            for (int pos : path) {
                if (state.boardPositions[pos] == player) {
                    pCount++;
                } else if (state.boardPositions[pos] == null) {
                    nullCount++;
                }
            }
            if (pCount == 2 && nullCount == 1) {
                count++;
            }
        }
        return count;
    }

    /**
    * Counts the number of pieces of a specific player on the board.

    */
    private int countPieces(GameState state, Player p) {
        int count = 0;
        for (Player pos : state.boardPositions) {
            if (pos == p) {
                count++;
            }
        }
        return count;
    }

    /**
    * Calculates a heuristic score for the board for a given player. 
    * This score is based on various factors such as forming mills and blocking the opponent.

    */

    public int getHeuristicScore(GameState state, Player player) {
        Player opp = player.opponent();
        int score = 0;


        int[][] paths = get(state.gameBoard.isIn12MenVer());


        for (int[] path : paths) {
            Player pos0 = state.boardPositions[path[0]];
            Player pos1 = state.boardPositions[path[1]];
            Player pos2 = state.boardPositions[path[2]];


            if (pos0 == player && pos1 == player && pos2 == player) {

                score += 1000;
                continue;

            }

            int myCount = 0;
            int emptyCount = 0;
            if (pos0 == player) myCount++;
            else if (pos0 == null) emptyCount++;
            if (pos1 == player) myCount++;
            else if (pos1 == null) emptyCount++;
            if (pos2 == player) myCount++;
            else if (pos2 == null) emptyCount++;

            if (myCount == 2 && emptyCount == 1) {
                score += 15;
            }


            int oppCount = 0;
            int emptyCountOpp = 0;
            if (pos0 == opp) oppCount++;
            else if (pos0 == null) emptyCountOpp++;
            if (pos1 == opp) oppCount++;
            else if (pos1 == null) emptyCountOpp++;
            if (pos2 == opp) oppCount++;
            else if (pos2 == null) emptyCountOpp++;


            if (oppCount == 2 && emptyCountOpp == 1) {

                score += 5;
            }


            // (player,player,opp)
            if (pos0 == player && pos1 == player && pos2 == opp) {
                score -= 2;
            }
            // (player,opp,player)
            if (pos0 == player && pos1 == opp && pos2 == player) {
                score -= 2;
            }
            // (opp,player,player)
            if (pos0 == opp && pos1 == player && pos2 == player) {
                score -= 2;
            }

            // (opp,opp,player)
            if (pos0 == opp && pos1 == opp && pos2 == player) {
                score += 20;
            }
            // (opp,player,opp)
            if (pos0 == opp && pos1 == player && pos2 == opp) {
                score += 20;
            }
            // (player,opp,opp)
            if (pos0 == player && pos1 == opp && pos2 == opp) {
                score += 20;
            }

            // (opp,opp,null)
            if (pos0 == opp && pos1 == opp && pos2 == null) {
                score -= 10;
            }
            // (opp,null,opp)
            if (pos0 == opp && pos1 == null && pos2 == opp) {
                score -= 10;
            }
            // (null,opp,opp)
            if (pos0 == null && pos1 == opp && pos2 == opp) {
                score -= 10;
            }
        }

        return score;
    }

    ///////////////////////////////////////////////////////////////////////////
    int deleteDepthLimit = 0;

    /**
    * Performs a limited depth search for deleting a piece in the game.

    */
    private int[] deletePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        deleteDepthLimit = depthLimit;
        return deletePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
    * Performs the max-value part of the alpha-beta search for deleting a piece.

    */
    private int[] deletePiece_maxValue(GameState state, int alpha, int beta, int depthLimit) {
        Player p = state.currentPlayer;

        int v = Integer.MIN_VALUE;
        int bestMove = -1;

        List<Integer> actions = state.deleteActionsFor(p);
        if (actions.isEmpty()) {
            return new int[]{Integer.MIN_VALUE, -1};
        }

        if (depthLimit == 0) {
            int sc = boardScore(state);
            return new int[]{sc, bestMove};
        }

        for (int pos : actions) {
            GameState next = state.newStateForDelete(pos, p);

            next.currentPlayer = p.opponent();

            int[] child = deletePiece_minValue(next, alpha, beta, depthLimit - 1);
            int childScore = child[0];
            if (childScore > v) {
                v = childScore;
                bestMove = pos;
                alpha = Math.max(alpha, v);
                if (v >= beta) {
                    break;
                }
            }
        }
        return new int[]{v, bestMove};
    }

    /**
    * Performs the min-value part of the alpha-beta search for deleting a piece.
    * 

    */
    private int[] deletePiece_minValue(GameState state, int alpha, int beta, int depthLimit) {
        Player p = state.currentPlayer;

        int v = Integer.MAX_VALUE;
        int bestMove = -1;

        List<Integer> actions = state.deleteActionsFor(p);
        if (actions.isEmpty()) {
            return new int[]{Integer.MAX_VALUE, -1};
        }

        if (depthLimit == 0) {
            int sc = boardScore(state);
            return new int[]{sc, bestMove};
        }

        for (int pos : actions) {
            GameState next = state.newStateForDelete(pos, p);
            next.currentPlayer = p.opponent();

            int[] child = deletePiece_maxValue(next, alpha, beta, depthLimit - 1);
            int childScore = child[0];
            if (childScore < v) {
                v = childScore;
                bestMove = pos;
                beta = Math.min(beta, v);
                if (v <= alpha) {
                    break;
                }
            }
        }
        return new int[]{v, bestMove};
    }

    
    /**
     * Performs a limited depth search for moving a piece in the game.
     * 

     */
    private int[] movePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        return movePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
     * Performs the max-value part of the alpha-beta search for moving a piece.

     * @return An array containing the best value and the corresponding moves.
     */

    private int[] movePiece_maxValue(GameState state, int alpha, int beta, int depthLimit) {
        int v = Integer.MIN_VALUE;
        int bestFrom = -1, bestTo = -1;

        Player p = state.currentPlayer;
        List<int[]> actions = state.selectActions(p);

        if (actions.isEmpty()) {
            return new int[]{Integer.MIN_VALUE, -1, -1};
        }

        if (depthLimit == 0) {
            int eval = movePiece_boardScore(state);
            return new int[]{eval, bestFrom, bestTo};
        }

        for (int[] a : actions) {
            GameState nextState = state.newMoveState(a);
            nextState.currentPlayer = p.opponent();

            int[] child = movePiece_minValue(nextState, alpha, beta, depthLimit - 1);
            int childScore = child[0];
            if (childScore > v) {
                v = childScore;
                bestFrom = a[0];
                bestTo = a[1];
                alpha = Math.max(alpha, v);
                if (v >= beta) {
                    break;
                }
            }
        }
        return new int[]{v, bestFrom, bestTo};
    }

    /**
     * Performs the min-value part of the alpha-beta search for moving a piece.
     * 

     */
    private int[] movePiece_minValue(GameState state, int alpha, int beta, int depthLimit) {
        /*
        if (placePiece_iSterminal(state)) {
            if (state.currentPlayer == Player.RED) {
                return new int[]{1000000000, -1, -1};
            } else {
                return new int[]{-1000000000, -1, -1};
            }
        }
         */

        int v = Integer.MAX_VALUE;
        int bestFrom = -1;
        int bestTo = -1;

        Player p = state.currentPlayer;
        List<int[]> actions = state.selectActions(p);

        if (actions.isEmpty()) {
            return new int[]{Integer.MAX_VALUE, -1, -1};
        }

        if (depthLimit == 0) {
            int eval = movePiece_boardScore(state);
            return new int[]{eval, bestFrom, bestTo};
        }

        for (int[] a : actions) {
            GameState nextState = state.newMoveState(a);
            nextState.currentPlayer = p.opponent();

            int[] child = movePiece_maxValue(nextState, alpha, beta, depthLimit - 1);
            int childScore = child[0];
            if (childScore < v) {
                v = childScore;
                bestFrom = a[0];
                bestTo = a[1];
                beta = Math.min(beta, v);
                if (v <= alpha) {
                    break;
                }
            }
        }
        return new int[]{v, bestFrom, bestTo};
    }


    public int movePiece_boardScore(GameState state) {
        double adjust_weight = 2.5;
        Player cur = state.currentPlayer;
        Player opp = cur.opponent();
        return movePiece_getHeuristicScore(state, cur) -
                (int)(adjust_weight * movePiece_getHeuristicScore(state, opp));
    }


    public int movePiece_getHeuristicScore(GameState state, Player player) {
        Player other_player = player == Player.RED ? Player.BLUE : Player.RED;
        int score = 0;
        int [][] paths = get(state.gameBoard.isIn12MenVer());
        for (int[] path : paths) {
            if (state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == player) {
                score += 1000;
            }

            if (state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == null) {
                score += 10;
            }
            if (state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == null && state.boardPositions[path[2]] == player) {
                score += 10;
            }
            if (state.boardPositions[path[0]] == null && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == player) {
                score += 10;
            }

            if (state.boardPositions[path[0]] == other_player && state.boardPositions[path[1]] == other_player && state.boardPositions[path[2]] == player) {
                score += 20;
            }
            if (state.boardPositions[path[0]] == other_player && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == other_player) {
                score += 20;
            }
            if (state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == other_player && state.boardPositions[path[2]] == other_player) {
                score += 20;
            }

            if (state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == other_player) {
                score -= 2;
            }
            if (state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == other_player && state.boardPositions[path[2]] == player) {
                score -= 2;
            }
            if (state.boardPositions[path[0]] == other_player && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == player) {
                score -= 2;
            }

            if (state.boardPositions[path[0]] == other_player && state.boardPositions[path[1]] == other_player && state.boardPositions[path[2]] == null) {
                score -= 10;
            }
            if (state.boardPositions[path[0]] == other_player && state.boardPositions[path[1]] == null && state.boardPositions[path[2]] == other_player) {
                score -= 10;
            }
            if (state.boardPositions[path[0]] == null && state.boardPositions[path[1]] == other_player && state.boardPositions[path[2]] == other_player) {
                score -= 10;
            }
        }
        return score;
    }

    ///////////////////////////////////////////////////////////////////////////

    private static final int[][] NINE_PATHS = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23},
            {0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23}
    };

    private static final int[][] TWELVE_PATHS = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23},
            {0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23},
            {0, 3, 6}, {2, 5, 8}, {15, 18, 21}, {17, 20, 23}
    };


   public static int[][] get(boolean in12version) {
       return in12version ? TWELVE_PATHS : NINE_PATHS;
    }
    

    private int[] canThree(GameState state, int depth) {
        int[] result = new int[]{-1, -1, -1};
        Player player = state.currentPlayer;
        List<int []> actions = state.selectActions(player);
        //Player other_player = player == Player.RED ? Player.BLUE : Player.RED;
        int [][] paths = get(state.gameBoard.isIn12MenVer());
        for (int[] path : paths) {
            for (int[] move : actions) {
                if ((state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == player && path[2] == move[1] && path[0] != move[0] && path[1] != move[0])
                        || (state.boardPositions[path[0]] == player && path[1] == move[1] && path[0] != move[0] && path[2] != move[0] && state.boardPositions[path[2]] == player)
                        || (path[0] == move[1] && path[1] != move[0] && path[2] != move[0] && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == player)) {

                    result[0] = 1;
                    result[1] = move[0];
                    result[2] = move[1];
                    if (depth == 0) {
                        return result;
                    }
                    int[] result2 = canThree(state.newMoveState(move), depth - 1);
                    if (result2[0] == 1) {
                        return result;
                    }
                }
            }
        }
        return result;
    }


    private int[] crackThree(GameState state) {
        Player player = state.currentPlayer;
        List<int []> actions = state.selectActions(player);

        int [][] paths = get(state.gameBoard.isIn12MenVer());
        for (int[] path : paths) {
            if (state.boardPositions[path[0]] == player && state.boardPositions[path[1]] == player && state.boardPositions[path[2]] == player) {
                for (int[] move : actions) {
                    if (path[0] == move[0] || path[1] == move[0] || path[2] == move[0]) {
                        return new int[]{1, move[0], move[1]};
                    }
                }
            }
        }
        return new int[]{-1, -1, -1};
    }
    private int pickRandomMoveSource(Game game) {
        Player me = game.getCurrentPlayer();
        List<Integer> myPieces = new ArrayList<>();

        for (int i = 0; i < 24; i++) {

            if (game.getBoardPositions()[i] == me) {

                List<Integer> valid = getValidTargets(game, i);
                if (!valid.isEmpty()) {

                    myPieces.add(i);
                }
            }
        }
        if (myPieces.isEmpty()) {

            return -1;
        }

        return myPieces.get(new Random().nextInt(myPieces.size()));
    }

    private int pickRandomMoveTarget(Game game, int from) {
        if (from < 0 || from >= 24) {
            return -1;
        }

        List<Integer> validTos = getValidTargets(game, from);
        if (validTos.isEmpty()) {
            return -1;
        }
        return validTos.get(new Random().nextInt(validTos.size()));
    }

    /**
     *  Returns all target grids that can be moved/flyed to from position according to the game rules.
     * If the player is in the "flying" phase => can jump to any grid
     * Otherwise => can only move to adjacent grids
     */
    private List<Integer> getValidTargets(Game game, int position) {
        List<Integer> result = new ArrayList<>();

        Player me = game.getCurrentPlayer();


        int countPieces = 0;
        for (int i=0; i<24; i++) {
            if (game.getBoardPositions()[i] == me) {
                countPieces++;
            }
        }
        boolean flying = (countPieces <= 3);


        if (game.getBoardPositions()[position] != me) {
            return result;
        }


        if (flying) {
            for (int i = 0; i < 24; i++) {
                if (game.getBoardPositions()[i] == null) {
                    result.add(i);
                }
            }
        } else {

            for (Integer nei : game.getBoardGraph().getNeighbors(position)) {
                if (game.getBoardPositions()[nei] == null) {
                    result.add(nei);
                }
            }
        }

        return result;
    }


}
