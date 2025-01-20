package group15.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import group15.*;

/**
 * A simple RandomBot implementation that uses a neural network for decision-making.
 * The bot selects moves and placements based on evaluations made by the neural network.
 */
public class RandomBot implements Bot {

    private Random random = new Random();
    private List<EvaluationResult> evaluations;

    /**
     * Creates a copy of the current game state for evaluation.
     * 
     * @param game The current game state to copy.
     * @return A new instance of the game state with the current game's attributes.
     */
    private GameOri copyGame(Game game) {
        GameOri gameOri = new GameOri();
        //gameOri.boardPositions = Arrays.copyOf(game.boardPositions, game.boardPositions.length);
        for (int i = 0; i < game.boardPositions.length; i++) {
            gameOri.boardPositions[i] = (game.boardPositions[i] != null) ? game.boardPositions[i].getIndex() : 0;
        }
        gameOri.gameMode = game.gameMode;
        gameOri.phase = game.phase;
        gameOri.moveCountBlue = game.placedPiecesBlue;
        gameOri.moveCountRed = game.placedPiecesRed;
        gameOri.currentPlayer = (game.currentPlayer == Player.BLUE) ? 1: 2;
        gameOri.selectedPiece = game.selectedPiece;
        gameOri.moveWithoutCapture = game.moveWithoutCapture;
        gameOri.drawAgreed = game.drawAgreed;
        return gameOri;
    }

    /**
     * Selects a position to place a piece during the placement phase.
     * The bot evaluates possible placements using a neural network and selects the best one.
     * 
     * @param game The current game state.
     * @return The position to place the piece.
     */
    @Override
    public int placePiece(Game game) {
        // Define the nets directory
        Path netsDir = Paths.get("bestnet");

        // Get the first .txt file in the nets directory
        try (Stream<Path> files = Files.list(netsDir)) {
            Path netFile = files
                    .filter(file -> file.toString().endsWith(".txt")) // Select only .txt files
                    .findFirst()  // Find the first matching file
                    .orElseThrow(() -> new IOException("No .txt file found in nets directory"));

            // Print the filename
            System.out.println("Loading network from file: " + netFile.getFileName());

            // Create and load the neural network
            NeuralNetwork myNet = new NeuralNetwork();
            myNet.initNet(); // Initialize the neural network
            myNet.loadNet(netFile.toString()); // Pass the file path as a string to loadNet method

            GameOri gameCurrent = copyGame(game);
            List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

            // Placement phase
            for (int position = 0; position < 24; position++) {
                GameSituation gameNext = new GameSituation(gameCurrent); // Copy the current game state

                if (gameNext.doActionPlace(position)) { // Try placing a piece at the current position
                    System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
                    gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
                    double evaluation = myNet.forward(gameNext.inputForNet); // Evaluate the game state using the neural network
                    evaluations.add(new EvaluationResult(gameNext, evaluation, 0, position, -99)); // Store the evaluation
                }
            }

            // Find the position with the maximum evaluation using a simple loop
            double maxEvaluation = Double.NEGATIVE_INFINITY; // Initialize to a very low value
            int bestPosition = -1; // Default to -1 if no valid positions are found
            for (EvaluationResult result : evaluations) {
                if (result.getEvaluation() > maxEvaluation) {
                    maxEvaluation = result.getEvaluation(); // Update the maximum evaluation
                    bestPosition = result.position1; // Update the best position
                }
            }

            return bestPosition;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*@Override
    public int placePiece(Game game) {
        // Get list of empty positions
        List<Integer> emptyPositions = new ArrayList<>();
        Player[] boardPositions = game.getBoardPositions();
        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == null) {
                emptyPositions.add(i);
            }
        }
        if (emptyPositions.isEmpty()) {
            return -1; // No moves available
        }
        // Choose a random empty position
        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }*/

    /**
     * Selects a piece to move during the game phase.
     * The bot evaluates possible moves using a neural network and selects the best piece to move.
     * 
     * @param game The current game state.
     * @return The position of the selected piece to move.
     */
    @Override
    public int selectPiece(Game game) {
        // Define the nets directory
        Path netsDir = Paths.get("bestnet");

        // Get the first .txt file in the nets directory
        try (Stream<Path> files = Files.list(netsDir)) {
            Path netFile = files
                    .filter(file -> file.toString().endsWith(".txt")) // Select only .txt files
                    .findFirst()  // Find the first matching file
                    .orElseThrow(() -> new IOException("No .txt file found in nets directory"));

            // Print the filename
            System.out.println("Loading network from file: " + netFile.getFileName());

            // Create and load the neural network
            NeuralNetwork myNet = new NeuralNetwork();
            myNet.initNet(); // Initialize the neural network
            myNet.loadNet(netFile.toString()); // Pass the file path as a string to loadNet method

            GameOri gameCurrent = copyGame(game);
            List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

            for (int sourcePosition = 0; sourcePosition < 24; sourcePosition++) {

                if (gameCurrent.boardPositions[sourcePosition] == gameCurrent.currentPlayer) {
                    for (int targetPosition = 0; targetPosition < 24; targetPosition++) {

                        if (sourcePosition != targetPosition) {
                            GameSituation gameNext = new GameSituation(gameCurrent); // Copy the current game state
                            if (gameNext.doActionFlyMove(sourcePosition, targetPosition)) { // Try moving or flying the piece
                                System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
                                gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
                                double evaluation = myNet.forward(gameNext.inputForNet); // Evaluate the game state using the neural network

                                evaluations.add(new EvaluationResult(gameNext, evaluation,gameCurrent.getPhase(),sourcePosition,targetPosition)); // Store the evaluation

                            }
                        }

                    }
                }
            }

            // Find the position with the maximum evaluation using a simple loop
            double maxEvaluation = Double.NEGATIVE_INFINITY; // Initialize to a very low value
            int bestPosition = -1; // Default to -1 if no valid positions are found
            for (EvaluationResult result : evaluations) {
                if (result.getEvaluation() > maxEvaluation) {
                    maxEvaluation = result.getEvaluation(); // Update the maximum evaluation
                    bestPosition = result.position1; // Update the best position
                }
            }

            return bestPosition;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*@Override
    public int selectPiece(Game game) {
        Player currentPlayer = game.getCurrentPlayer();
        Player[] boardPositions = game.getBoardPositions();
        List<Integer> movablePieces = new ArrayList<>();

        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == currentPlayer) {
                List<Integer> validMoves = game.getValidMoves(i);
                if (!validMoves.isEmpty()) {
                    movablePieces.add(i);
                }
            }
        }

        if (movablePieces.isEmpty()) {
            return -1; // No movable pieces
        }

        // Select a random piece
        return movablePieces.get(random.nextInt(movablePieces.size()));
    }*/

    /**
     * Determines the best move for a selected piece during the game phase.
     * The bot evaluates possible target positions for the selected piece and selects the best one.
     * 
     * @param game The current game state.
     * @param selectedPiece The position of the selected piece.
     * @return The position to move the selected piece to.
     */
    @Override
    public int determineMove(Game game, int selectedPiece) {
        // Define the nets directory
        Path netsDir = Paths.get("bestnet");

        // Get the first .txt file in the nets directory
        try (Stream<Path> files = Files.list(netsDir)) {
            Path netFile = files
                    .filter(file -> file.toString().endsWith(".txt")) // Select only .txt files
                    .findFirst()  // Find the first matching file
                    .orElseThrow(() -> new IOException("No .txt file found in nets directory"));

            // Print the filename
            System.out.println("Loading network from file: " + netFile.getFileName());

            // Create and load the neural network
            NeuralNetwork myNet = new NeuralNetwork();
            myNet.initNet(); // Initialize the neural network
            myNet.loadNet(netFile.toString()); // Pass the file path as a string to loadNet method

            GameOri gameCurrent = copyGame(game);
            List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

            if (gameCurrent.boardPositions[selectedPiece] == gameCurrent.currentPlayer) {
                for (int targetPosition = 0; targetPosition < 24; targetPosition++) {

                    if (selectedPiece != targetPosition) {
                        GameSituation gameNext = new GameSituation(gameCurrent); // Copy the current game state
                        if (gameNext.doActionFlyMove(selectedPiece, targetPosition)) { // Try moving or flying the piece
                            System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
                            gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
                            double evaluation = myNet.forward(gameNext.inputForNet); // Evaluate the game state using the neural network

                            evaluations.add(new EvaluationResult(gameNext, evaluation,gameCurrent.getPhase(),selectedPiece,targetPosition)); // Store the evaluation

                        }
                    }

                }
            }

            // Find the position with the maximum evaluation using a simple loop
            double maxEvaluation = Double.NEGATIVE_INFINITY; // Initialize to a very low value
            int bestPosition = -1; // Default to -1 if no valid positions are found
            for (EvaluationResult result : evaluations) {
                if (result.getEvaluation() > maxEvaluation) {
                    maxEvaluation = result.getEvaluation(); // Update the maximum evaluation
                    bestPosition = result.position2; // Update the best position
                }
            }

            return bestPosition;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /*    @Override
        public int determineMove(Game game, int selectedPiece) {
            List<Integer> validMoves = game.getValidMoves(selectedPiece);
            if (validMoves.isEmpty()) {
                return -1; // No valid moves
            }

            // Choose a random valid move
            return validMoves.get(random.nextInt(validMoves.size()));
        }*/

    /**
     * Determines which opponent's piece to delete based on the evaluation of the current game state.
     * The bot evaluates possible deletions and selects the best piece to remove.
     * 
     * @param game The current game state.
     * @return The position of the opponent's piece to delete.
     */
    @Override
    public int determinePieceToDelete(Game game) {
        // Define the nets directory
        Path netsDir = Paths.get("bestnet");

        // Get the first .txt file in the nets directory
        try (Stream<Path> files = Files.list(netsDir)) {
            Path netFile = files
                    .filter(file -> file.toString().endsWith(".txt")) // Select only .txt files
                    .findFirst()  // Find the first matching file
                    .orElseThrow(() -> new IOException("No .txt file found in nets directory"));

            // Print the filename
            System.out.println("Loading network from file: " + netFile.getFileName());

            // Create and load the neural network
            NeuralNetwork myNet = new NeuralNetwork();
            myNet.initNet(); // Initialize the neural network
            myNet.loadNet(netFile.toString()); // Pass the file path as a string to loadNet method

            GameOri gameCurrent = copyGame(game);
            List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

            // Placement phase
            for (int position = 0; position < 24; position++) {
                GameSituation gameNext = new GameSituation(gameCurrent); // Copy the current game state

                if (gameNext.doActionDelete(position)) { // Try placing a piece at the current position
                    System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
                    gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
                    double evaluation = myNet.forward(gameNext.inputForNet); // Evaluate the game state using the neural network
                    evaluations.add(new EvaluationResult(gameNext, evaluation, 0, position, -99)); // Store the evaluation
                }
            }

            // Find the position with the maximum evaluation using a simple loop
            double maxEvaluation = Double.NEGATIVE_INFINITY; // Initialize to a very low value
            int bestPosition = -1; // Default to -1 if no valid positions are found
            for (EvaluationResult result : evaluations) {
                if (result.getEvaluation() > maxEvaluation) {
                    maxEvaluation = result.getEvaluation(); // Update the maximum evaluation
                    bestPosition = result.position1; // Update the best position
                }
            }

            return bestPosition;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
/*    @Override
    public int determinePieceToDelete(Game game) {
        List<Integer> deletablePositions = new ArrayList<>();
        Player[] boardPositions = game.getBoardPositions();
        Player opponent = game.getCurrentPlayer().opponent();

        for (int i = 0; i < boardPositions.length; i++) {
            if (boardPositions[i] == opponent && !game.formsMill(i, opponent)) {
                deletablePositions.add(i);
            }
        }

        // If all opponent's pieces are in mills, can delete any opponent's piece
        if (deletablePositions.isEmpty()) {
            for (int i = 0; i < boardPositions.length; i++) {
                if (boardPositions[i] == opponent) {
                    deletablePositions.add(i);
                }
            }
        }
        if (deletablePositions.isEmpty()) {
            return -1; // No pieces to delete
        }

        // Choose a random deletable position
        return deletablePositions.get(random.nextInt(deletablePositions.size()));
    }*/
}