package groupXX.bot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Match class represents a match of the Nine Men's Morris game.
 * It handles the game logic, action evaluation, and decision-making using neural networks.
 */
public class Match {
    private GameSituation gameCurrent; // Current game state
    private GameSituation gameNext; // Next game state after action
    private List<EvaluationResult> evaluationsPlaceBlue; // Stores evaluations and game states for placing pieces
    private List<EvaluationResult> evaluationsMoveFlyBlue; // Stores evaluations and game states for moving or flying pieces
    private List<EvaluationResult> evaluationsDelBlue; // Stores evaluations and game states for deleting pieces

    private List<EvaluationResult> evaluationsPlaceRed; // Stores evaluations and game states for placing pieces
    private List<EvaluationResult> evaluationsMoveFlyRed; // Stores evaluations and game states for moving or flying pieces
    private List<EvaluationResult> evaluationsDelRed; // Stores evaluations and game states for deleting pieces

    private NeuralNetwork myNet; // Neural network to evaluate game states
    private NeuralNetwork opponentNet; // Neural network to evaluate game states
    private BufferedWriter writer; // Writer to output logs to log.txt

    private boolean bLogFlage = false;

    /**
     * Constructor to initialize the game state and neural network.
     *
     * @param game Current game state
     * @param myNet Neural network for the current player
     * @param opponentNet Neural network for the opponent player
     */
    public Match(GameSituation game, NeuralNetwork myNet , NeuralNetwork opponentNet) {
        this.gameCurrent = game; // Initialize current game state
        this.myNet = myNet; // Initialize neural network
        this.opponentNet = opponentNet; // Initialize neural network
        try {
            // Initialize the BufferedWriter to write to log.txt in the current directory
            writer = new BufferedWriter(new FileWriter("log.txt", true)); // 'true' for appending to the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log method to write messages to log.txt.
     *
     * @param message The message to log
     */
    private void log(String message) {
        try {

            if (bLogFlage){
                writer.write(message); // Write message to the file
                writer.newLine(); // Add a new line after each message
                writer.flush(); // Ensure the message is written immediately
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Try all possible actions and evaluate the resulting game states.
     * Evaluates placement, movement, flying, and deletion actions based on the current phase.
     *
     * @param net The neural network used for evaluation
     */
    public void tryEveryActions(NeuralNetwork net) {
        log("tryEveryActions " + " begin...");
        evaluationsPlaceBlue = new ArrayList<>();
        evaluationsMoveFlyBlue = new ArrayList<>();
        evaluationsDelBlue = new ArrayList<>();
        evaluationsPlaceRed = new ArrayList<>();
        evaluationsMoveFlyRed = new ArrayList<>();
        evaluationsDelRed = new ArrayList<>();

        // Execute actions based on the current game phase
        if (gameCurrent.getPhase() == 0) { // Placement phase


            log("Placement phase Deleted count for Blue1:  " + gameCurrent.deletedCountBlue);
            log("Placement phase Deleted count for Red1:  " + gameCurrent.deletedCountRed);


            for (int position = 0; position < 24; position++) {
                gameNext = new GameSituation(gameCurrent); // Copy the current game state

                if (gameNext.doActionPlace(position)) { // Try placing a piece at the current position
                    System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
                    gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
                    double evaluation = net.forward(gameNext.inputForNet); // Evaluate the game state using the neural network

                    if (gameCurrent.getPhase() == 0) {
                        if (gameCurrent.currentPlayer == 1){
                            evaluationsPlaceBlue.add(new EvaluationResult(gameNext, evaluation,0,position,-99)); // Store the evaluation
                        }else if (gameCurrent.currentPlayer == 2){
                            evaluationsPlaceRed.add(new EvaluationResult(gameNext, evaluation,0,position,-99)); // Store the evaluation
                        }
                    }
                }
            }

            log("Placement phase Deleted count for Blue2:  " + gameCurrent.deletedCountBlue);
            log("Placement phase Deleted count for Red2:  " + gameCurrent.deletedCountRed);


        }

        if (gameCurrent.getPhase() == 1 || gameCurrent.getPhase() == 2) { // Moving or flying phase
            for (int sourcePosition = 0; sourcePosition < 24; sourcePosition++) {

                if (gameCurrent.boardPositions[sourcePosition] == gameCurrent.currentPlayer) {
                    for (int targetPosition = 0; targetPosition < 24; targetPosition++) {

                        if (sourcePosition != targetPosition) {
                            gameNext = new GameSituation(gameCurrent); // Copy the current game state
                            if (gameNext.doActionFlyMove(sourcePosition, targetPosition)) { // Try moving or flying the piece
                                System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
                                gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
                                double evaluation = net.forward(gameNext.inputForNet); // Evaluate the game state using the neural network

                                if (gameCurrent.currentPlayer == 1){
                                    evaluationsMoveFlyBlue.add(new EvaluationResult(gameNext, evaluation,gameCurrent.getPhase(),sourcePosition,targetPosition)); // Store the evaluation
                                }else if (gameCurrent.currentPlayer == 2){
                                    evaluationsMoveFlyRed.add(new EvaluationResult(gameNext, evaluation,gameCurrent.getPhase(),sourcePosition,targetPosition)); // Store the evaluation
                                }
                            }
                        }

                    }
                }
            }
        }

        if (gameCurrent.getPhase() < 0) { // Deletion phase
            for (int position = 0; position < 24; position++) {
                gameNext = new GameSituation(gameCurrent); // Copy the current game state

                if (gameNext.doActionDelete(position)) { // Try deleting a piece at the current position
                    System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
                    gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
                    double evaluation = net.forward(gameNext.inputForNet); // Evaluate the game state using the neural network

                    if (gameCurrent.currentPlayer == 1){
                        evaluationsDelBlue.add(new EvaluationResult(gameNext, evaluation, gameCurrent.getPhase(),position,-99)); // Store the evaluation
                    }else if (gameCurrent.currentPlayer == 2){
                        evaluationsDelRed.add(new EvaluationResult(gameNext, evaluation, gameCurrent.getPhase(),position,-99)); // Store the evaluation
                    }
                }
            }
        }

        if (gameCurrent.currentPlayer == 1){
            log("Number of evaluations (Place): " + evaluationsPlaceBlue.size()); // Log the number of evaluations for placement actions
            log("Number of evaluations (Move/Fly): " + evaluationsMoveFlyBlue.size()); // Log the number of evaluations for move/fly actions
            log("Number of evaluations (Delete): " + evaluationsDelBlue.size()); // Log the number of evaluations for delete actions
        }else if (gameCurrent.currentPlayer == 2){
            log("Number of evaluations (Place): " + evaluationsPlaceRed.size()); // Log the number of evaluations for placement actions
            log("Number of evaluations (Move/Fly): " + evaluationsMoveFlyRed.size()); // Log the number of evaluations for move/fly actions
            log("Number of evaluations (Delete): " + evaluationsDelRed.size()); // Log the number of evaluations for delete actions
        }
        log("tryEveryActions " + " end...");
    }

    /**
     * Perform the best action based on the highest evaluation.
     * It finds the highest evaluation among placement, moving/flying, and deletion actions,
     * and updates the game state accordingly.
     */
    public void doBestAction() {
        double bestEvaluation = Double.NEGATIVE_INFINITY;
        GameSituation bestGameSituationNext = null;

        // Find the best action in the placement evaluations
        if (gameCurrent.currentPlayer == 1){
            for (EvaluationResult result : evaluationsPlaceBlue) {
                if (result.getEvaluation() > bestEvaluation) {
                    bestEvaluation = result.getEvaluation();
                    bestGameSituationNext = result.getGameStatus();
                }
            }
        }else if (gameCurrent.currentPlayer == 2){
            for (EvaluationResult result : evaluationsPlaceRed) {
                if (result.getEvaluation() > bestEvaluation) {
                    bestEvaluation = result.getEvaluation();
                    bestGameSituationNext = result.getGameStatus();
                }
            }
        }

        // Find the best action in the move/fly evaluations
        if (gameCurrent.currentPlayer == 1){
            for (EvaluationResult result : evaluationsMoveFlyBlue) {
                if (result.getEvaluation() > bestEvaluation) {
                    bestEvaluation = result.getEvaluation();
                    bestGameSituationNext = result.getGameStatus();
                }
            }
        }else if (gameCurrent.currentPlayer == 2){
            for (EvaluationResult result : evaluationsMoveFlyRed) {
                if (result.getEvaluation() > bestEvaluation) {
                    bestEvaluation = result.getEvaluation();
                    bestGameSituationNext = result.getGameStatus();
                }
            }
        }

        // Find the best action in the delete evaluations
        if (gameCurrent.currentPlayer == 1){
            for (EvaluationResult result : evaluationsDelBlue) {
                if (result.getEvaluation() > bestEvaluation) {
                    bestEvaluation = result.getEvaluation();
                    bestGameSituationNext = result.getGameStatus();
                }
            }
        }else if (gameCurrent.currentPlayer == 2){
            for (EvaluationResult result : evaluationsDelRed) {
                if (result.getEvaluation() > bestEvaluation) {
                    bestEvaluation = result.getEvaluation();
                    bestGameSituationNext = result.getGameStatus();
                }
            }
        }

        // If a best game state is found, update the current game state
        if (bestGameSituationNext != null) {
            log("Before updating gameCurrent.boardPositions: " + java.util.Arrays.toString(gameCurrent.boardPositions)); // Log the state before update
            gameCurrent = bestGameSituationNext; // Update to the best next game state
            log("After updating gameCurrent.boardPositions:  " + java.util.Arrays.toString(gameCurrent.boardPositions)); // Log the state after update
        }
    }

    /**
    * Play one full match.
    * This method simulates a full game between the current player and the opponent.
    * The game continues until there is a winner or a draw.
    *
    * @return The outcome of the game: 1 for current player win, 0 for opponent win, 0.5 for draw
    */
    public double playOneMatch() {
        do {
            tryEveryActions(myNet); // Try all possible actions
            doBestAction(); // Perform the best action based on evaluations

            // Output move counts for both players
            log("Placed count for Blue: " + gameCurrent.moveCountBlue);
            log("Deleted count for Blue: " + gameCurrent.deletedCountBlue);
            log("Placed count for Red: " + gameCurrent.moveCountRed);
            log("Deleted count for Red: " + gameCurrent.deletedCountRed);
            log("moveWithoutCapture: " + gameCurrent.moveWithoutCapture);
            log("my turn end------------------------------------------------------");

            // Check win/loss or draw conditions for the current player
            if (gameCurrent.checkWinLossPlus()) {
                if (gameCurrent.currentPlayer == 1) {
                    return 1; // Current player wins
                } else {
                    return 0; // Opponent wins
                }
            } else if (gameCurrent.checkDrawConditionsPlus()) {
                return 0.5; // Game is a draw
            }

            // Opponent's turn
            tryEveryActions(opponentNet); // Try all possible actions
            doBestAction(); // Perform the best action based on evaluations

            log("Placed count for Blue: " + gameCurrent.moveCountBlue);
            log("Deleted count for Blue: " + gameCurrent.deletedCountBlue);
            log("Placed count for Red: " + gameCurrent.moveCountRed);
            log("Deleted count for Red: " + gameCurrent.deletedCountRed);
            log("moveWithoutCapture: " + gameCurrent.moveWithoutCapture);
            log("opponent turn end------------------------------------------------------");

            // Check win/loss or draw conditions for the opponent
            if (gameCurrent.checkWinLossPlus()) {
                if (gameCurrent.currentPlayer == 1) {
                    return 0; // Opponent wins
                } else {
                    return 1; // Current player wins
                }
            } else if (gameCurrent.checkDrawConditionsPlus()) {
                return 0.5; // Game is a draw
            }

        } while (true); // Continue until the game ends
    }

    /**
     * Closes the BufferedWriter to release resources.
     * Ensures proper resource cleanup when logging is complete.
     * 
     * @throws IOException if an error occurs while closing the writer.
     */
    public void closeLogger() {
        try {
            if (writer != null) {
                writer.close(); // Close the BufferedWriter
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
