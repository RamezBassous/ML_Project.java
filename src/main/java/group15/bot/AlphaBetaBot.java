package group15.bot;


import group15.Game;
import group15.Player;

import java.util.List;

/**
 * AlphaBetaBot is an implementation of a bot for the Nine Men's Morris game using the Alpha-Beta pruning
 * algorithm for decision-making during the game.
 */
public class AlphaBetaBot implements Bot {
    private int move_to = -1;

    /**
     * Determines the move for placing a piece on the board using Alpha-Beta pruning.
     *
     * @param game The current state of the game.
     * @return The index where the bot should place a piece.
     */
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

    /**
     * Determines which piece the bot should select based on the current game state.
     *
     * @param game The current state of the game.
     * @return The index of the piece to be selected.
     */
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

    /**
     * Determines the move that the selected piece should make.
     *
     * @param game         The current state of the game.
     * @param selectedPiece The piece to be moved.
     * @return The destination index for the selected piece to move to.
     */
    @Override
    public int determineMove(Game game, int selectedPiece) {
        return move_to;
    }

    /**
     * Determines which opponent's piece should be deleted based on the current game state.
     *
     * @param game The current state of the game.
     * @return The index of the opponent's piece to be deleted.
     */
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

    /**
     * Validates if the selected piece for deletion is a valid move according to the game rules.
     *
     * @param game     The current state of the game.
     * @param candidate The index of the piece to be deleted.
     * @return True if the deletion is valid, otherwise false.
     */
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

    /**
     * Finds any valid opponent piece that can be deleted based on the game rules.
     *
     * @param game The current state of the game.
     * @return The index of a valid piece to delete, or -1 if no valid piece is found.
     */
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

    /**
     * Executes a limited Alpha-Beta search to determine the best piece placement.
     *
     * @param state     The current game state.
     * @param depthLimit The maximum search depth.
     * @return An array with the best score and corresponding move.
     */
    private int[] placePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        return placePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
     * Maximizes the value during Alpha-Beta search for piece placement.
     *
     * @param state      The current game state.
     * @param alpha      The alpha value for pruning.
     * @param beta       The beta value for pruning.
     * @param depthLimit The maximum search depth.
     * @return An array with the best score and corresponding move.
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

    /**
     * Calculates a quick action score based on whether a piece placement results in forming a mill.
     *
     * @param state     The current game state.
     * @param position  The position to be evaluated.
     * @param player    The player performing the action.
     * @return The score for the action.
     */
    private int quickActionScore(GameState state, int position, Player player) {

        int score = 0;

        if (formsMillIfPlace(state, position, player)) {
            score += 100;
        }
        return score;
    }

    /**
     * Checks whether placing a piece at a given position forms a mill.
     *
     * @param state    The current game state.
     * @param position The position to be checked.
     * @param player   The player performing the action.
     * @return True if a mill is formed, otherwise false.
     */
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
     *
     * @param state The current state of the game.
     * @param alpha The alpha value for pruning.
     * @param beta The beta value for pruning.
     * @param depthLimit The maximum depth for the search.
     * @return An array where the first element is the evaluation score and the second 
     *         is the best move for the current player.
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
    * @param state The current state of the game.
    * @return true if the game state is terminal, false otherwise.
    */
    private boolean placePiece_iSterminal(GameState state) {
        return state.moveCountBlue == state.gameBoard.getRequiredPieces() && state.moveCountRed == state.gameBoard.getRequiredPieces();
    }

    /**
    * Checks if a given position on the board has a neighboring empty spot.
    *
    * @param state The current state of the game.
    * @param position The position on the board to check.
    * @return true if there is a neighboring empty spot, false otherwise.
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
    *
    * @param state The current state of the game.
    * @return The calculated heuristic score for the current board state.
    */
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

    /**
    * Calculates the mobility score for a given player in the current game state.
    * This is based on how many legal moves the player can make.
    *
    * @param state The current state of the game.
    * @param player The player whose mobility is being calculated.
    * @return The mobility score for the given player.
    */
    private int calcMobility(GameState state, Player player) {
        // If still in placing phase, we might ignore mobility or return 0
        if (state.moveCountBlue < state.gameBoard.getRequiredPieces()
                || state.moveCountRed < state.gameBoard.getRequiredPieces()) {
            return 0;
        }
        List<int[]> actions = state.selectActions(player);
        return actions.size();
    }

    /**
    * Counts the potential mills for a given player. A potential mill is formed by two of 
    * the player's pieces and an empty spot in a valid row.
    *
    * @param state The current state of the game.
    * @param player The player whose potential mills are being counted.
    * @return The number of potential mills for the given player.
    */
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
    *
    * @param state The current state of the game.
    * @param p The player whose pieces are being counted.
    * @return The number of pieces of the given player on the board.
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
    *
    * @param state The current state of the game.
    * @param player The player whose heuristic score is being calculated.
    * @return The heuristic score for the given player in the current game state.
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
    * Performs a limited depth search for deleting a piece in the game.
    * 
    * @param state The current game state.
    * @param depthLimit The maximum depth limit for the search.
    * @return An array containing the best value and the corresponding move.
    */
    private int[] deletePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        deleteDepthLimit = depthLimit;
        return deletePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
    * Performs the max-value part of the alpha-beta search for deleting a piece.
    * 
    * @param state The current game state.
    * @param alpha The alpha value for alpha-beta pruning.
    * @param beta The beta value for alpha-beta pruning.
    * @param depthLimit The current depth limit for the search.
    * @return An array containing the best value and the corresponding move.
    */
    private int[] deletePiece_maxValue(GameState state, int alpha, int beta, int depthLimit) {
        Player p = state.currentPlayer;
        // 可能要判断 if (isDeleteTerminal(state)) { ... }

        int v = Integer.MIN_VALUE;
        int bestMove = -1;

        // 当前玩家能删除哪些位置
        List<Integer> actions = state.deleteActionsFor(p);
        // 你可自定义: e.g. state.deleteActions() 里再判断 currentPlayer
        if (actions.isEmpty()) {
            // 无可删 => 说明当前player无动作 => 视为输
            return new int[]{Integer.MIN_VALUE, -1};
        }

        if (depthLimit == 0) {
            // 估值
            v = boardScore(state);
            return new int[]{v, bestMove};
        }

        for (int pos : actions) {
            // newState 可能传 (pos, null) or (pos, p) => 取决于你已有的写法
            GameState next = state.newStateForDelete(pos, p);
            // 下回合是 p.opponent()
            next.currentPlayer = (p == Player.RED) ? Player.BLUE : Player.RED;

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
    * @param state The current game state.
    * @param alpha The alpha value for alpha-beta pruning.
    * @param beta The beta value for alpha-beta pruning.
    * @param depthLimit The current depth limit for the search.
    * @return An array containing the best value and the corresponding move.
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
            v = boardScore(state);
            return new int[]{v, bestMove};
        }

        for (int pos : actions) {
            GameState next = state.newStateForDelete(pos, p);
            next.currentPlayer = (p == Player.RED) ? Player.BLUE : Player.RED;

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
     * @param state The current game state.
     * @param depthLimit The maximum depth limit for the search.
     * @return An array containing the best value and the corresponding moves.
     */
    private int[] movePiece_limited_alphabeta_search(GameState state, int depthLimit) {
        return movePiece_maxValue(state, Integer.MIN_VALUE, Integer.MAX_VALUE, depthLimit);
    }

    /**
     * Performs the max-value part of the alpha-beta search for moving a piece.
     * 
     * @param state The current game state.
     * @param alpha The alpha value for alpha-beta pruning.
     * @param beta The beta value for alpha-beta pruning.
     * @param depthLimit The current depth limit for the search.
     * @return An array containing the best value and the corresponding moves.
     */
    private int[] movePiece_maxValue(GameState state, int alpha, int beta, int depthLimit) {
        int v = Integer.MIN_VALUE;
        int move1 = -1, move2 = -1;

        Player current = state.currentPlayer;
        List<int[]> actions = state.selectActions(current);

        if (actions.isEmpty()) {
            // No moves => losing state => return -1
            return new int[]{Integer.MIN_VALUE, -1, -1};
        }

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
     * Performs the min-value part of the alpha-beta search for moving a piece.
     * 
     * @param state The current game state.
     * @param alpha The alpha value for alpha-beta pruning.
     * @param beta The beta value for alpha-beta pruning.
     * @param depthLimit The current depth limit for the search.
     * @return An array containing the best value and the corresponding moves.
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
     * Calculates the board score for a given game state based on the piece positions and a heuristic weight.
     * The score is calculated as the difference between the current player's score and the opponent's score,
     * adjusted by a weight factor.
     * 
     * @param state The current game state.
     * @return The score representing the desirability of the current game state for the current player.
     */
    public int movePiece_boardScore(GameState state) {
        double adjust_weight = 2.5;
        Player other_player = state.currentPlayer == Player.RED ? Player.BLUE : Player.RED;
        return movePiece_getHeuristicScore(state, state.currentPlayer) - (int)(adjust_weight * movePiece_getHeuristicScore(state, other_player));
    }

    /**
     * Calculates a heuristic score for the given player based on their piece positions on the board.
     * The score is based on how well the player is positioned to form mills (three-in-a-row configurations),
     * as well as any potential threats or advantages relative to the opponent.
     * 
     * @param state The current game state.
     * @param player The player whose heuristic score is being calculated.
     * @return The heuristic score for the specified player.
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

    /**
    * Returns the predefined set of paths for either the Nine Men's Morris or Twelve Men's Morris game,
    * based on the specified version.
    * 
    * @param in12version A boolean flag indicating whether to return the Twelve Men's Morris paths
    *                    (true) or the Nine Men's Morris paths (false).
    * @return A 2D array of integers representing the paths for the chosen game version.
    */
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
