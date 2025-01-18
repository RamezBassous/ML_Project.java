package group15.bot;


import group15.Game;
import group15.Player;

import java.util.List;

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
        int[] result;
        result = canThree(new GameState(game), 1);

        if (move_to < 0 || move_to >= 24) {
            System.out.println("[DEBUG] AlphaBetaBot got an invalid move_to: " + move_to);
            // either return -1 or do fallback...
        }

        if (result[0] != -1) {
            move_to = result[2];
            // System.out.println(result[1] + " ---can---> " + result[2]);
            return result[1];
        }

        result = crackThree(new GameState(game));
        if (result[0] != -1) {
            move_to = result[2];
            // System.out.println(result[1] + " ---crack---> " + result[2]);
            return result[1];
        }

        GameState state = new GameState(game);
        result = movePiece_limited_alphabeta_search(state, 8);
        move_to = result[2];
        return result[1];
    }

    @Override
    public int determineMove(Game game, int selectedPiece) {
        return move_to;
    }

    @Override
    public int determinePieceToDelete(Game game) {
        // 1) Use your existing alpha-beta search to get a candidate
        GameState state = new GameState(game);
        int[] result = deletePiece_limited_alphabeta_search(state, 12);
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
        if (candidate < 0 || candidate >= 24) {
            return false;
        }
        Player opponent = game.getCurrentPlayer().opponent();
        if (game.getBoardPositions()[candidate] != opponent) {
            return false;
        }
        // If not all of opponent's pieces are in mills, can't delete a piece that's in a mill
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
                    return i; // delete any piece
                } else if (!game.formsMill(i, opponent)) {
                    return i; // must delete a piece not in a mill
                }
            }
        }
        return -1; // no valid piece found
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
        int move = -1;
        List<Integer> actions = state.actions();
        actions.sort((a1, a2) -> {
            int score1 = quickActionScore(state, a1, Player.RED);
            int score2 = quickActionScore(state, a2, Player.RED);

            return Integer.compare(score2, score1);
        });
        for (int a : actions) {
            if (hasNeighbour(state, a)) {
                if (depthLimit == 0) {
                    v = boardScore(state);
                    return new int[]{v, move};
                }
                if (depthLimit > 0) {
                    int[] v2AndMove = placePiece_minValue(state.newState(a, Player.RED), alpha, beta, depthLimit - 1);
                    int v2 = v2AndMove[0];
                    if (v2 > v) {
                        v = v2;
                        move = a;
                        alpha = Math.max(alpha, v);
                        if (v >= beta) {return new int[]{v, move};}
                    }
                }
            } else {
                v = boardScore(state);
                return new int[]{v, move};
            }
        }

        return new int[]{v, move};
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

    private int[] placePiece_minValue(GameState state, int alpha, int beta, int depthLimit) {
        if (placePiece_iSterminal(state)) {
            if (state.currentPlayer == Player.RED) {
                return new int[]{1000000000, -1};
            } else {
                return new int[]{-1000000000, -1};
            }
        }

        int v = Integer.MAX_VALUE;
        int move = -1;
        List<Integer> actions = state.actions();
        for (int a : actions) {
            if (hasNeighbour(state, a)) {
                if (depthLimit == 0) {
                    v = boardScore(state);
                    return new int[]{v, move};
                }
                if (depthLimit > 0) {
                    int[] v2AndMove = placePiece_maxValue(state.newState(a, Player.BLUE), alpha, beta, depthLimit - 1);
                    int v2 = v2AndMove[0];
                    if (v2 < v) {
                        v = v2;
                        move = a;
                        beta = Math.min(beta, v);
                        if (v <= alpha) {return new int[]{v, move};}
                    }
                }
            } else {
                v = boardScore(state);
                return new int[]{v, move};
            }
        }

        return new int[]{v, move};
    }

    private boolean placePiece_iSterminal(GameState state) {
        return state.moveCountBlue == state.gameBoard.getRequiredPieces() && state.moveCountRed == state.gameBoard.getRequiredPieces();
    }

    public boolean hasNeighbour(GameState state, int position) {
        List<Integer> neighbors = state.gameBoard.getNeighbors(position);
        for (int n : neighbors) {
            if (state.boardPositions[n] == null) {
                return true;
            }
        }
        return false;
    }

    public int boardScore(GameState state) {
        double adjust_weight = 2.5;

        Player myPlayer  = state.currentPlayer;
        Player oppPlayer = (myPlayer == Player.RED) ? Player.BLUE : Player.RED;

        // 1) Retain your existing heuristic difference
        int basicScore = getHeuristicScore(state, myPlayer)
                - (int)(adjust_weight * getHeuristicScore(state, oppPlayer));

        // 2) Count piece difference
        int myCount  = countPieces(state, myPlayer);
        int oppCount = countPieces(state, oppPlayer);
        int pieceDiffScore = (myCount - oppCount) * 5;

        // 3) Potential mills: positions where you have 2 pieces + 1 empty slot
        int myPotentialMills  = countPotentialMills(state, myPlayer);
        int oppPotentialMills = countPotentialMills(state, oppPlayer);
        int potentialMillScore = (myPotentialMills - oppPotentialMills) * 3;

        // 4) Mobility (in moving/flying phase)
        int myMobility  = calcMobility(state, myPlayer);
        int oppMobility = calcMobility(state, oppPlayer);
        int mobilityScore = (myMobility - oppMobility) * 2;

        // 5) Combine them
        int totalScore = basicScore + pieceDiffScore + potentialMillScore + mobilityScore;
        return totalScore;
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

    private int countPieces(GameState state, Player p) {
        int count = 0;
        for (Player pos : state.boardPositions) {
            if (pos == p) {
                count++;
            }
        }
        return count;
    }

    public int getHeuristicScore(GameState state, Player player) {
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
    int deleteDepthLimit = 0;
    private int[] deletePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        deleteDepthLimit = depthLimit;
        return deletePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    private int[] deletePiece_maxValue(GameState state, int alpha, int beta, int depthLimit) {
        /*
        if (placePiece_iSterminal(state)) {
            if (state.currentPlayer == Player.RED) {
                return new int[]{1000000000, -1};
            } else {
                return new int[]{-1000000000, -1};
            }
        }*/

        int v = Integer.MIN_VALUE;
        int move = -1;
        List<Integer> actions = state.deleteActions();
        for (int a : actions) {
            if (hasNeighbour(state, a)) {
                if (depthLimit == 0) {
                    v = boardScore(state);
                    return new int[]{v, move};
                }
                if (depthLimit > 0) {
                    int[] v2AndMove;
                    if (depthLimit == deleteDepthLimit) {
                        v2AndMove = deletePiece_minValue(state.newState(a, null), alpha, beta, depthLimit - 1);
                    } else {
                        v2AndMove = deletePiece_minValue(state.newState(a, Player.RED), alpha, beta, depthLimit - 1);
                    }
                    int v2 = v2AndMove[0];
                    if (v2 > v) {
                        v = v2;
                        move = a;
                        alpha = Math.max(alpha, v);
                        if (v >= beta) {return new int[]{v, move};}
                    }
                }
            } else {
                v = boardScore(state);
                return new int[]{v, move};
            }
        }

        return new int[]{v, move};
    }

    private int[] deletePiece_minValue(GameState state, int alpha, int beta, int depthLimit) {
        /*
        if (placePiece_iSterminal(state)) {
            if (state.currentPlayer == Player.RED) {
                return new int[]{1000000000, -1};
            } else {
                return new int[]{-1000000000, -1};
            }
        }
        */

        int v = Integer.MAX_VALUE;
        int move = -1;
        List<Integer> actions = state.actions();
        for (int a : actions) {
            if (hasNeighbour(state, a)) {
                if (depthLimit == 0) {
                    v = boardScore(state);
                    return new int[]{v, move};
                }
                if (depthLimit > 0) {
                    int[] v2AndMove = deletePiece_maxValue(state.newState(a, Player.BLUE), alpha, beta, depthLimit - 1);
                    int v2 = v2AndMove[0];
                    if (v2 < v) {
                        v = v2;
                        move = a;
                        beta = Math.min(beta, v);
                        if (v <= alpha) {return new int[]{v, move};}
                    }
                }
            } else {
                v = boardScore(state);
                return new int[]{v, move};
            }
        }

        return new int[]{v, move};
    }

    ///////////////////////////////////////////////////////////////////////////
    private int[] movePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        return movePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    private int[] movePiece_maxValue(GameState state, int alpha, int beta, int depthLimit) {
        /*
        if (placePiece_iSterminal(state)) {
            if (state.currentPlayer == Player.RED) {
                return new int[]{1000000000, -1, -1};
            } else {
                return new int[]{-1000000000, -1, -1};
            }
        }
         */

        int v = Integer.MIN_VALUE;
        int move1 = -1;
        int move2 = -1;
        List<int []> actions = state.selectActions(Player.RED);
        for (int[] a : actions) {
            int av = Integer.MIN_VALUE;
            if (depthLimit == 0) {
                v = movePiece_boardScore(state);
                return new int[]{v, move1, move2};
            }
            if (depthLimit > 0) {
                int[] v2AndMove;
                v2AndMove = movePiece_minValue(state.newMoveState(a), alpha, beta, depthLimit - 1);
                int v2 = v2AndMove[0];
                if (v2>av) {
                    av = v2;
                }
                if (v2 > v) {
                    v = v2;
                    move1 = a[0];
                    move2 = a[1];
                    alpha = Math.max(alpha, v);
                    if (v >= beta) {return new int[]{v, move1, move2};}
                }
            }
        }

        return new int[]{v, move1, move2};
    }

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
        int move1 = -1;
        int move2 = -1;
        List<int []> actions = state.selectActions(Player.BLUE);
        for (int[] a : actions) {
            if (depthLimit == 0) {
                v = movePiece_boardScore(state);
                return new int[]{v, move1, move2};
            }
            if (depthLimit > 0) {
                int[] v2AndMove = movePiece_maxValue(state.newMoveState(a), alpha, beta, depthLimit - 1);
                int v2 = v2AndMove[0];
                if (v2 < v) {
                    v = v2;
                    move1 = a[0];
                    move2 = a[1];
                    beta = Math.min(beta, v);
                    if (v <= alpha) {return new int[]{v, move1, move2};}
                }
            }
        }

        return new int[]{v, move1, move2};
    }

    public int movePiece_boardScore(GameState state) {
        double adjust_weight = 2.5;
        Player other_player = state.currentPlayer == Player.RED ? Player.BLUE : Player.RED;
        return movePiece_getHeuristicScore(state, state.currentPlayer) - (int)(adjust_weight * movePiece_getHeuristicScore(state, other_player));
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
        //Player other_player = player == Player.RED ? Player.BLUE : Player.RED;
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
}
