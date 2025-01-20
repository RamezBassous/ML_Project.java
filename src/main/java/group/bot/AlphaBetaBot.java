package group.bot;

import group15.Game;
import group15.Player;

import java.util.List;

/**
 * AlphaBetaBot implements the Bot interface and uses the Alpha-Beta pruning
 * algorithm to decide moves in the game.
 */
public class AlphaBetaBot implements Bot {

    private int move_to = -1;

    /**
     * Places a piece for the current player.
     * Uses limited Alpha-Beta search to decide the best position to place the piece.
     *
     * @param game The current game state.
     * @return The position to place the piece.
     */
    @Override
    public int placePiece(Game game) {

        GameState state = new GameState(game);
        int[] result = placePiece_limited_alphabeta_search(state, 8);
        return result[1];
    }

    /**
     * Selects a piece for the current player.
     * First checks if the player can form a mill or if the opponent has a mill to crack.
     * If neither condition is met, it uses Alpha-Beta search to determine the best move.
     *
     * @param game The current game state.
     * @return The position of the piece to select.
     */
    @Override
    public int selectPiece(Game game) {
        int[] result;
        result = canThree(new GameState(game), 1);
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

    /**
     * Determines the next move for the current player after selecting a piece.
     *
     * @param game          The current game state.
     * @param selectedPiece The position of the selected piece.
     * @return The position where the selected piece should be moved.
     */
    @Override
    public int determineMove(Game game, int selectedPiece) {
        return move_to;
    }

    /**
     * Determines the piece to delete for the current player during the removal phase.
     * Uses Alpha-Beta search to determine the best piece to remove.
     *
     * @param game The current game state.
     * @return The position of the piece to delete.
     */
    @Override
    public int determinePieceToDelete(Game game) {
        GameState state = new GameState(game);
        int[] result = deletePiece_limited_alphabeta_search(state, 8);
        return result[1];
    }

    /**
     * Performs a limited Alpha-Beta search to place a piece and returns the best move.
     *
     * @param state      The current game state.
     * @param depthLimit The depth limit for the search.
     * @return An array containing the evaluation score and the best move.
     */
    private int[] placePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        return placePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
     * Alpha-Beta pruning function that maximizes the evaluation score for placing a piece.
     *
     * @param state      The current game state.
     * @param alpha      The current alpha value for pruning.
     * @param beta       The current beta value for pruning.
     * @param depthLimit The depth limit for the search.
     * @return An array containing the evaluation score and the best move.
     */
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
    
    /**
     * Checks whether the current game state is a terminal state for placing a piece.
     * A terminal state occurs when both players have placed their required pieces.
     * 
     * @param state The current game state.
     * @return true if the game state is terminal, false otherwise.
     */
    private boolean placePiece_iSterminal(GameState state) {
        return state.moveCountBlue == state.gameBoard.getRequiredPieces() && state.moveCountRed == state.gameBoard.getRequiredPieces();
    }

    /**
     * Determines if a given position on the board has a valid neighboring position that can be used for placing a piece.
     * 
     * @param state The current game state.
     * @param position The position to check.
     * @return true if the position has a valid neighbor, false otherwise.
     */
    public boolean hasNeighbour(GameState state, int position) {
        for (int i = 0; i < 24; i++) {
            if (state.boardPositions[i] == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates the board score for the current game state using a heuristic score.
     * The score is adjusted based on the current player's position and the opponent's position.
     * 
     * @param state The current game state.
     * @return The calculated board score.
     */
    public int boardScore(GameState state) {
        double adjust_weight = 2.5;
        Player other_player = state.currentPlayer == Player.RED ? Player.BLUE : Player.RED;
        return getHeuristicScore(state, state.currentPlayer) - (int)(adjust_weight * getHeuristicScore(state, other_player));
    }

    /**
     * Calculates the heuristic score for a specific player based on the game state.
     * The score is determined by evaluating potential winning paths and favorable positions.
     * 
     * @param state The current game state.
     * @param player The player for whom the heuristic score is calculated.
     * @return The calculated heuristic score for the specified player.
     */
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

    /**
     * Searches for the best piece to delete using the Alpha-Beta pruning algorithm with a depth limit.
     * 
     * @param state The current game state.
     * @param depthLimit The maximum depth for the search.
     * @return The best move (piece to delete) along with its score.
     */
    private int[] deletePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        deleteDepthLimit = depthLimit;
        return deletePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
     * Searches for the best piece to delete using the Alpha-Beta pruning algorithm (maximizing player).
     * 
     * @param state The current game state.
     * @param alpha The current alpha value (lower bound).
     * @param beta The current beta value (upper bound).
     * @param depthLimit The maximum depth for the search.
     * @return The best move (piece to delete) along with its score.
     */
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

    /**
     * Searches for the best piece to delete using the Alpha-Beta pruning algorithm (minimizing player).
     * 
     * @param state The current game state.
     * @param alpha The current alpha value (lower bound).
     * @param beta The current beta value (upper bound).
     * @param depthLimit The maximum depth for the search.
     * @return The best move (piece to delete) along with its score.
     */
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
    
    /**
     * Searches for the best move for a player during the move phase using the Alpha-Beta pruning algorithm with a depth limit.
     * 
     * @param state The current game state.
     * @param depthLimit The maximum depth for the search.
     * @return The best move (two positions to move) along with its score.
     */
    private int[] movePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        return movePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
     * Searches for the best move for a player during the move phase using the Alpha-Beta pruning algorithm (maximizing player).
     * 
     * @param state The current game state.
     * @param alpha The current alpha value (lower bound).
     * @param beta The current beta value (upper bound).
     * @param depthLimit The maximum depth for the search.
     * @return The best move (two positions to move) along with its score.
     */
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

    /**
     * Searches for the best move for a player during the move phase using the Alpha-Beta pruning algorithm (minimizing player).
     * 
     * @param state The current game state.
     * @param alpha The current alpha value (lower bound).
     * @param beta The current beta value (upper bound).
     * @param depthLimit The maximum depth for the search.
     * @return The best move (two positions to move) along with its score.
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

    /**
     * Calculates the board score for a move phase. Evaluates the state based on piece positioning and potential.
     * 
     * @param state The current game state.
     * @return The calculated board score.
     */
    public int movePiece_boardScore(GameState state) {
        double adjust_weight = 2.5;
        Player other_player = state.currentPlayer == Player.RED ? Player.BLUE : Player.RED;
        return movePiece_getHeuristicScore(state, state.currentPlayer) - (int)(adjust_weight * movePiece_getHeuristicScore(state, other_player));
    }

    /**
    * Calculates the heuristic score for a move phase, based on piece positioning and potential.
    * 
    * @param state The current game state.
    * @param player The player whose move is being evaluated.
    * @return The calculated heuristic score.
    */
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
    
    /**
    * Determines if a player can form a three-in-a-row configuration and potentially form a mill.
    * Searches for possible moves based on the current game state and depth.
    * 
    * @param state The current game state.
    * @param depth The depth limit for the recursive search.
    * @return An array containing the result of the search: [1, from, to] if a move is found, or [-1, -1, -1] if not.
    */
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
    
    /**
    * Determines if the current player can "crack" (remove) a piece from the opponent's mill.
    * This method checks if any of the player's pieces can form a mill with an available move.
    * 
    * @param state The current game state.
    * @return An array containing the result of the check: [1, from, to] if a move to crack a mill is found, or [-1, -1, -1] if not.
    */
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
