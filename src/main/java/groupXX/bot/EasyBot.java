package groupXX.bot;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import groupXX.*;

/**
 * The EasyBot class implements the Bot interface, providing a simple AI for our game.
 * It uses a neural network to evaluate potential game states and select moves during various phases of the game,
 * such as placing pieces, moving pieces, and deleting opponent pieces.
 * The bot interacts with the game using a trained neural network model, loaded from a file.
 */
public class EasyBot implements Bot {

  private Random random = new Random();
  private List<EvaluationResult> evaluations;

  private static final String FILE_NAME = getFileName();
  private static final List<String> FILE_CONTENT;

  static {
    try {
      FILE_CONTENT = Files.readAllLines(Paths.get(FILE_NAME), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

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
    gameOri.currentPlayer = (game.currentPlayer == Player.BLUE) ? 1 : 2;
    gameOri.selectedPiece = game.selectedPiece;
    gameOri.moveWithoutCapture = game.moveWithoutCapture;
    gameOri.drawAgreed = game.drawAgreed;
    return gameOri;
  }

  private static String getFileName() {
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

      return netFile.toString(); // Pass the file path as a string to loadNet method
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Determines the best position to place a piece on the board during the placement phase
   * by utilizing a trained neural network to evaluate potential game states.
   *
   * @param game The current game state object containing board positions and the active player.
   * @return The index of the best position to place a piece on the board.
   * @throws RuntimeException if no valid neural network file is found or other IO issues occur.
   */
  @Override
  public int placePiece(Game game) {
    // Create and load the neural network
    NeuralNetwork myNet = new NeuralNetwork();
    myNet.initNet(); // Initialize the neural network
    myNet.loadNetFromLines(FILE_CONTENT); // Pass the file path as a string to loadNet method

    GameOri gameCurrent = copyGame(game);
    List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

    // Placement phase
    for (int position : game.getValidMoves()) {
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
  }

  /**
   * Selects the best piece to move based on a neural network evaluation.
   * This method evaluates all possible moves for the current player and selects the piece
   * that maximizes the evaluation score as determined by a trained neural network.
   *
   * @param game The current state of the game, represented by the {@link Game} object.
   * @return The index of the best piece to move based on the evaluation or -1 if no valid moves are found.
   * @throws RuntimeException If no .txt file is found in the neural network directory
   *                          or if an I/O error occurs while accessing the directory.
   */
  @Override
  public int selectPiece(Game game) {
    // Create and load the neural network
    NeuralNetwork myNet = new NeuralNetwork();
    myNet.initNet(); // Initialize the neural network
    myNet.loadNetFromLines(FILE_CONTENT); // Pass the file path as a string to loadNet method

    GameOri gameCurrent = copyGame(game);
    List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

    for (int sourcePosition : game.getValidMoves()) {

      if (gameCurrent.boardPositions[sourcePosition] == gameCurrent.currentPlayer) {
        for (int targetPosition = 0; targetPosition < 24; targetPosition++) {

          if (sourcePosition != targetPosition) {
            GameSituation gameNext = new GameSituation(gameCurrent); // Copy the current game state
            if (gameNext.doActionFlyMove(sourcePosition, targetPosition)) { // Try moving or flying the piece
              System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, 24); // Copy board positions
              gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
              double evaluation = myNet.forward(gameNext.inputForNet); // Evaluate the game state using the neural network

              evaluations.add(new EvaluationResult(gameNext, evaluation, gameCurrent.getPhase(), sourcePosition, targetPosition)); // Store the evaluation

            }
          }

        }
      }
    }

    // Find the position with the maximum evaluation using a simple loop
    double maxEvaluation = Double.NEGATIVE_INFINITY; // Initialize to a very low value
    int bestPosition = -1; // Default to -1 if no valid positions are found
    for (EvaluationResult result : evaluations) {
      if (!game.getValidMoves().contains(result.position1)) {
        System.out.println("Cannot select piece: " + result.position1);
        continue;
      }
      if (result.getEvaluation() > maxEvaluation) {
        maxEvaluation = result.getEvaluation(); // Update the maximum evaluation
        bestPosition = result.position1; // Update the best position
      }
    }

    return bestPosition;

  }

  /**
   * Determines the best move for a selected piece based on a neural network evaluation.
   * This method evaluates all possible target positions for the given piece and selects
   * the move that maximizes the evaluation score computed by the neural network.
   *
   * @param game          The current state of the game.
   * @param selectedPiece The position of the piece selected by the player.
   * @return The best target position for the selected piece, or -1 if no valid moves are found.
   * @throws RuntimeException If no .txt file is found in the "bestnet" directory or if an I/O error occurs.
   */
  @Override
  public int determineMove(Game game, int selectedPiece) {
    // Create and load the neural network
    NeuralNetwork myNet = new NeuralNetwork();
    myNet.initNet(); // Initialize the neural network
    myNet.loadNetFromLines(FILE_CONTENT); // Pass the file path as a string to loadNet method

    GameOri gameCurrent = copyGame(game);
    List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

    if (gameCurrent.boardPositions[selectedPiece] == gameCurrent.currentPlayer) {
      for (int targetPosition : game.getValidMoves()) {

        if (selectedPiece != targetPosition) {
          GameSituation gameNext = new GameSituation(gameCurrent); // Copy the current game state
          if (gameNext.doActionFlyMove(selectedPiece, targetPosition)) { // Try moving or flying the piece
            System.arraycopy(gameNext.boardPositions, 0, gameNext.inputForNet, 0, gameNext.boardPositions.length); // Copy board positions
            gameNext.inputForNet[24] = gameCurrent.getCurrentPlayer(); // Set the current player
            double evaluation = myNet.forward(gameNext.inputForNet); // Evaluate the game state using the neural network
            evaluations.add(new EvaluationResult(gameNext, evaluation, gameCurrent.getPhase(), selectedPiece, targetPosition)); // Store the evaluation
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
  }


  /**
   * Determines the best piece to delete from the opponent's pieces based on a neural network evaluation.
   * This method evaluates all possible positions of the opponent's pieces and selects the piece
   * whose removal maximizes the evaluation score computed by the neural network.
   *
   * @param game The current state of the game.
   * @return The position of the best piece to delete, or -1 if no valid deletions are found.
   * @throws RuntimeException If no .txt file is found in the "bestnet" directory or if an I/O error occurs.
   */
  @Override
  public int determinePieceToDelete(Game game) {
    // Create and load the neural network
    NeuralNetwork myNet = new NeuralNetwork();
    myNet.initNet(); // Initialize the neural network
    myNet.loadNetFromLines(FILE_CONTENT); // Pass the file path as a string to loadNet method

    GameOri gameCurrent = copyGame(game);
    List<EvaluationResult> evaluations = new ArrayList<>(); // Store evaluation results

    // Placement phase
    for (int position : game.getValidMoves()) {
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
      if (!game.getValidMoves().contains(result.position1)) {
        System.out.println("Cannot delete piece: " + result.position1);
        continue;
      }
      if (result.getEvaluation() > maxEvaluation) {
        maxEvaluation = result.getEvaluation(); // Update the maximum evaluation
        bestPosition = result.position1; // Update the best position
      }
    }
    return bestPosition;

  }
}