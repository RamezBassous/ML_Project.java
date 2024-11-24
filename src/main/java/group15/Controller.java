package group15;

import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.shape.Arc;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import group15.bot.Bot;
import group15.bot.EasyBot;

public class Controller {

    private static final String BLUE_COLOR = "-fx-stroke: #457b9d;";
    private static final String RED_COLOR = "-fx-stroke: #e63946;";
    private static final String BLACK_COLOR = "-fx-stroke: black;";
    // MENU OBJECTS IMPORTS
    @FXML
    private AnchorPane anchorMain;

    @FXML
    private Pane GameMenu, DrawPopUp, WinPopUpBlue, WinPopUpRed, bluePlayerWinsBox, redPlayerWinsBox, drawBox;

    @FXML
    private Circle MenuBigLeftArrow, MenuBigRightArrow, tinyCircle0, tinyCircle1;

    @FXML
    private Arc MenuSmallLeftArrow0, MenuSmallLeftArrow1, MenuSmallRightArrow0, MenuSmallRightArrow1;

    @FXML
    private Text gameNumText, pieceNumText, easyBotText, local2pText;

    // BOARD OBJECTS IMPORTS (Player 1 - Blue Pieces)
    @FXML
    private StackPane pieceB0, pieceB1, pieceB2, pieceB3, pieceB4, pieceB5, pieceB6, pieceB7, pieceB8, pieceB9, pieceB10, pieceB11, pieceB12, pieceB13, pieceB14, pieceB15, pieceB16, pieceB17, pieceB18, pieceB19, pieceB20, pieceB21, pieceB22, pieceB23;

    // Enemy Pieces (Player 2 - Red Pieces)
    @FXML
    private StackPane pieceR0, pieceR1, pieceR2, pieceR3, pieceR4, pieceR5, pieceR6, pieceR7, pieceR8, pieceR9, pieceR10, pieceR11, pieceR12, pieceR13, pieceR14, pieceR15, pieceR16, pieceR17, pieceR18, pieceR19, pieceR20, pieceR21, pieceR22, pieceR23;

    @FXML
    private Line diagonal1, diagonal2, diagonal3, diagonal4;

    @FXML
    private Line horizontal1, horizontal2, horizontal3, horizontal4, horizontal5, horizontal6, horizontal7, horizontal8;

    @FXML
    private Line vertical1, vertical2, vertical3, vertical4, vertical5, vertical6, vertical7, vertical8;

    @FXML
    private Text index0, index1, index2, index3, index4, index5, index6, index7, index8, index9, index10, index11, index12, index13, index14, index15, index16, index17, index18, index19, index20, index21, index22, index23;

    @FXML
    private StackPane onHandPiece0, onHandPiece1, onHandPiece2, onHandPiece3, onHandPiece4, onHandPiece5, onHandPiece6, onHandPiece7, onHandPiece8;

    // Add all indicators from indicator0 to indicator23
    @FXML
    private StackPane indicator0, indicator1, indicator2, indicator3, indicator4, indicator5, indicator6, indicator7, indicator8, indicator9,
            indicator10, indicator11, indicator12, indicator13, indicator14, indicator15, indicator16, indicator17, indicator18, indicator19,
            indicator20, indicator21, indicator22, indicator23;

    private List<StackPane> indicators;  // Store all indicators in a list for easy management

    @FXML
    private StackPane onHandPieceR0, onHandPieceR1, onHandPieceR2, onHandPieceR3, onHandPieceR4, onHandPieceR5, onHandPieceR6, onHandPieceR7, onHandPieceR8;

    @FXML
    private StackPane onHandPieceExtra0, onHandPieceExtra1, onHandPieceExtra2, onHandPieceRExtra0, onHandPieceRExtra1, onHandPieceRExtra2;

    @FXML
    private ToggleButton switchVersion, showIndex, TestIndicator, newGameDraw, newGameRedWins, newGameBlueWins;

    @FXML
    private Circle shadow0, shadow1, shadow2, shadow3, shadow4, shadow5, shadow6, shadow7, shadow8, shadow9, shadow10, shadow11, shadow12, shadow13, shadow14, shadow15, shadow16, shadow17, shadow18, shadow19, shadow20, shadow21, shadow22, shadow23;

    @FXML
    private Circle zone0, zone1, zone2, zone3, zone4, zone5, zone6, zone7, zone8, zone9, zone10, zone11, zone12, zone13, zone14, zone15, zone16, zone17, zone18, zone19, zone20, zone21, zone22, zone23;

    @FXML
    private Circle ringL0, ringL1, ringL2, ringL3, ringL4, ringL5, ringL6, ringL7, ringL8, ringL9, ringL10, ringL11, ringL12, ringL13, ringL14, ringL15, ringL16, ringL17, ringL18, ringL19, ringL20, ringL21, ringL22, ringL23;
    @FXML
    private Button UndoB, ZoomIn, ZoomOut, RedoB;


    private RotateTransition rotateTransition;
    private Game currentGame;
    private GameManager gameManager;


    // Arrays for FXML components
    private StackPane[] bluePieces;
    private StackPane[] redPieces;
    private StackPane[] onHandBluePieces;
    private StackPane[] onHandRedPieces;
    private Line[] diagonals;

    private Circle[] zones;
    private Circle[] shadows;
    private Circle[] rings;

    private Text[] indicies;

    @FXML
    private TitledPane manual;

    // Define scaling factors
    private static final double SCALE_FACTOR = 1.1;
    private Stage primaryStage; // Holds reference to the main stage


    @FXML
    public void initialize() {
        gameManager = new GameManager(3, new GameEventListener() {
            @Override
            public void onGameWon(String winner) {
                if (winner.equals("Red")) {
                    WinPopUpRed.setVisible(true);
                } else if (winner.equals("Blue")) {
                    WinPopUpBlue.setVisible(true);
                }
            }

            @Override
            public void onGameDraw() {
                DrawPopUp.setVisible(true);
            }
        });
        currentGame = gameManager.getCurrentGame();

        WinPopUpRed.setVisible(false);
        WinPopUpBlue.setVisible(false);
        DrawPopUp.setVisible(false);

        setGameEventListeners();

        // Group FXML components into arrays
        bluePieces = new StackPane[]{pieceB0, pieceB1, pieceB2, pieceB3, pieceB4, pieceB5, pieceB6, pieceB7, pieceB8, pieceB9, pieceB10, pieceB11, pieceB12, pieceB13, pieceB14, pieceB15, pieceB16, pieceB17, pieceB18, pieceB19, pieceB20, pieceB21, pieceB22, pieceB23};
        redPieces = new StackPane[]{pieceR0, pieceR1, pieceR2, pieceR3, pieceR4, pieceR5, pieceR6, pieceR7, pieceR8, pieceR9, pieceR10, pieceR11, pieceR12, pieceR13, pieceR14, pieceR15, pieceR16, pieceR17, pieceR18, pieceR19, pieceR20, pieceR21, pieceR22, pieceR23};
        onHandBluePieces = new StackPane[]{onHandPiece0, onHandPiece1, onHandPiece2, onHandPiece3, onHandPiece4, onHandPiece5, onHandPiece6, onHandPiece7, onHandPiece8, onHandPieceExtra0, onHandPieceExtra1, onHandPieceExtra2};
        onHandRedPieces = new StackPane[]{onHandPieceR0, onHandPieceR1, onHandPieceR2, onHandPieceR3, onHandPieceR4, onHandPieceR5, onHandPieceR6, onHandPieceR7, onHandPieceR8, onHandPieceRExtra0, onHandPieceRExtra1, onHandPieceRExtra2};
        diagonals = new Line[]{diagonal1, diagonal2, diagonal3, diagonal4};
        indicies = new Text[]{index0, index1, index2, index3, index4, index5, index6, index7, index8, index9, index10, index11, index12, index13, index14, index15, index16, index17, index18, index19, index20, index21, index22, index23};
        zones = new Circle[]{zone0, zone1, zone2, zone3, zone4, zone5, zone6, zone7, zone8, zone9, zone10, zone11, zone12, zone13, zone14, zone15, zone16, zone17, zone18, zone19, zone20, zone21, zone22, zone23};
        shadows = new Circle[]{shadow0, shadow1, shadow2, shadow3, shadow4, shadow5, shadow6, shadow7, shadow8, shadow9, shadow10, shadow11, shadow12, shadow13, shadow14, shadow15, shadow16, shadow17, shadow18, shadow19, shadow20, shadow21, shadow22, shadow23};
        rings = new Circle[]{ringL0, ringL1, ringL2, ringL3, ringL4, ringL5, ringL6, ringL7, ringL8, ringL9, ringL10, ringL11, ringL12, ringL13, ringL14, ringL15, ringL16, ringL17, ringL18, ringL19, ringL20, ringL21, ringL22, ringL23};

        // Initialize the list of indicators
        indicators = Arrays.asList(indicator0, indicator1, indicator2, indicator3, indicator4, indicator5, indicator6, indicator7,
                indicator8, indicator9, indicator10, indicator11, indicator12, indicator13, indicator14, indicator15, indicator16,
                indicator17, indicator18, indicator19, indicator20, indicator21, indicator22, indicator23);

        setupIndicatorAnimations();

        initializeUI();

        // Add listeners for Undo and Redo buttons
        UndoB.setOnAction(this::handleUndoAction);
        RedoB.setOnAction(this::handleRedoAction);
    }

    //Initializes UI elements by setting initial visibility and states.
    private void initializeUI() {

        // Hiding UI Elements
        toggleIndex();

        onHandPieceExtra0.setVisible(false);
        onHandPieceExtra1.setVisible(false);
        onHandPieceExtra2.setVisible(false);
        onHandPieceRExtra0.setVisible(false);
        onHandPieceRExtra1.setVisible(false);
        onHandPieceRExtra2.setVisible(false);

        easyBotText.setVisible(false);

        // Hide diagonals and extra on-hand pieces initially
        Arrays.stream(diagonals).forEach(line -> line.setVisible(false));
        Arrays.stream(bluePieces).forEach(piece -> piece.setVisible(false));
        Arrays.stream(redPieces).forEach(piece -> piece.setVisible(false));
        indicators.forEach(indicator -> indicator.setVisible(false));

        //Setup Interactive UI Elements

        // Setup interactions for Blue on-hand pieces
        for (StackPane onHandBluePiece : onHandBluePieces) {
            setupOnHandPieceInteraction(onHandBluePiece);
        }

        // Setup interactions for Red on-hand pieces
        for (StackPane onHandRedPiece : onHandRedPieces) {
            setupOnHandPieceInteraction(onHandRedPiece);
        }

        for (int i = 0; i < zones.length; i++) {
            setupZoneInteraction(zones[i], shadows[i], rings[i], bluePieces[i], redPieces[i], i);
        }

        List<ToggleButton> toggleButtons = Arrays.asList(switchVersion, showIndex, TestIndicator,newGameDraw,newGameBlueWins,newGameRedWins);
        toggleButtons.forEach(this::setupToggleButtonInteraction);
        setupButtonInteraction(UndoB);
        setupButtonInteraction(RedoB);
        setupButtonInteraction(ZoomIn);
        setupButtonInteraction(ZoomOut);
        setupTitledPaneInteraction(manual);

        // Setup menu interactions
        setupMenuInteraction(GameMenu);
        setupMenuArrowInteraction(MenuBigLeftArrow);
        setupMenuArrowInteraction(MenuBigRightArrow);

        setupMenuArrows();
        setupSmallArrowAnimations();

        setupMenuInteraction(DrawPopUp);
        setupMenuInteraction(WinPopUpBlue);
        setupMenuInteraction(WinPopUpRed);

        setupMenuInteraction(bluePlayerWinsBox);
        setupMenuInteraction(redPlayerWinsBox);
        setupMenuInteraction(drawBox);
    }

    private void setGameEventListeners() {
        // Assigning listener for each game instance
        for (Game game : gameManager.getGames()) {
            game.setGameEventListener(new GameEventListener() {
                @Override
                public void onGameWon(String winner) {
                    if (winner.equals("Red")) {
                        WinPopUpRed.setVisible(true);
                    } else if (winner.equals("Blue")) {
                        WinPopUpBlue.setVisible(true);
                    }
                }

                @Override
                public void onGameDraw() {
                    DrawPopUp.setVisible(true);
                }
            });
        }
    }

    @FXML
    void handleUndoAction(ActionEvent event) {
        if (currentGame.undo()) {
            updateBoardUI();  // Refresh the board UI after undo
        }
    }

    @FXML
    void handleRedoAction(ActionEvent event) {
        if (currentGame.redo()) {
            updateBoardUI();  // Refresh the board UI after redo
        }
    }

    private void handleOpponentPieceRemoval(ScaleTransition scaleTransition, Circle ring, int position) {
        Player playerInPosition = currentGame.getBoardPositions()[position];
        Player opponentPlayer = currentGame.getCurrentPlayer().opponent();

        if (playerInPosition != opponentPlayer) {
            return;
        }

        if (!currentGame.formsMill(position, opponentPlayer) || currentGame.allPiecesAreInMills(opponentPlayer)) {
            ring.setStyle(opponentPlayer == Player.RED ? RED_COLOR : BLUE_COLOR);
            ring.setVisible(true);
            scaleTransition.playFromStart();  // Animate the ring
        }

    }

    // call only when phase = -1 || -2
    private void updateBoardUIOnPieceRemoval(Circle ring, int position) {
      Player playerInPosition = currentGame.getBoardPositions()[position];
      Player opponentPlayer = currentGame.getCurrentPlayer().opponent();

      // Show deletion indicators before handling click
      showDeletionIndicators(opponentPlayer);

      // Hide all indicators first
      hideAllIndicators(); // This line ensures that indicators are reset on click

      if (playerInPosition == opponentPlayer) {
        if (!currentGame.formsMill(position, opponentPlayer) || currentGame.allPiecesAreInMills(opponentPlayer)) {
          currentGame.saveStateForUndo();
          if (currentGame.makeMove(position)) {
            updateBoardUI();
            updateLineColors();
            ring.setVisible(false);
          }
        } else {
          System.out.println("Cannot remove a piece in a mill. Choose another piece.");
        }
      }
    }

    private void setupZoneInteraction(Circle zone, Circle shadow, Circle ring, StackPane playerPiece, StackPane enemyPiece, int position) {
        // Set initial visibility of shadow and ring to false
        shadow.setVisible(false);
        ring.setVisible(false);

        // Create scale animation for the ring (optional for visual effect)
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), ring);
        scaleTransition.setByX(0.1); // Scale up slightly
        scaleTransition.setByY(0.1); // Scale up slightly
        scaleTransition.setAutoReverse(true); // Scale back down automatically
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE); // Repeat indefinitely

        // Show shadow or ring when the mouse enters the zone
    zone.setOnMouseEntered(event -> {
        // Show the shadow if the corresponding position on the board is empty (for both phases)
        if (currentGame.getBoardPositions()[position] == null) {
            shadow.setVisible(true);
            ring.setVisible(false);  // Hide ring if shadow is shown
            scaleTransition.stop();  // Stop any ring animation
        } else {
            // Show the ring for the current player's pieces during the moving and flying phases
            if (currentGame.getPhase() >= 1 && currentGame.getBoardPositions()[position] == currentGame.getCurrentPlayer()) {
                ring.setVisible(true);
                shadow.setVisible(false);  // Don't show shadow if ring is shown
                scaleTransition.playFromStart(); // Start the animation
            } else handleOpponentPieceRemoval(scaleTransition, ring, position);
        }
    });

    // Hide shadow and ring when the mouse exits the zone
    zone.setOnMouseExited(event -> {
        shadow.setVisible(false);
        ring.setVisible(false);
        scaleTransition.stop(); // Stop the animation
        ring.setScaleX(1.0); // Reset scale to normal size
        ring.setScaleY(1.0); // Reset scale to normal size
        ring.setStyle("-fx-stroke: white;"); // Reset the stroke color
    });

    // Ensure the animation is stopped when the ring is hidden
    ring.visibleProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue) {
            scaleTransition.stop();
            ring.setScaleX(1.0); // Reset scale to normal size
            ring.setScaleY(1.0); // Reset scale to normal size
        }
    });

    // Show rings around deletable pieces during delete phases
    if (currentGame.getPhase() == -1 && currentGame.getBoardPositions()[position] == Player.RED) {
    // Check if the red piece can be deleted (not part of a mill)
        if (!currentGame.formsMill(position, Player.RED)) {
            // Show the ring for red pieces that can be deleted in phase -1
            ring.setVisible(true);
            scaleTransition.playFromStart();  // Animate the ring
        } else {
            // Hide the ring if it's part of a mill
            ring.setVisible(false);
        }
    } else if (currentGame.getPhase() == -2 && currentGame.getBoardPositions()[position] == Player.BLUE) {
        // Check if the blue piece can be deleted (not part of a mill)
        if (!currentGame.formsMill(position, Player.BLUE)) {
            // Show the ring for blue pieces that can be deleted in phase -2
            ring.setVisible(true);
            scaleTransition.playFromStart();  // Animate the ring
        } else {
            // Hide the ring if it's part of a mill
            ring.setVisible(false);
        }
    } else {
        // Hide the ring for all other cases
        ring.setVisible(false);
    }


        zone.setOnMouseClicked(event -> {
            int currentPhase = currentGame.getPhase();
            Player[] boardPositions = currentGame.getBoardPositions();
            Player currentPlayer = currentGame.getCurrentPlayer();

            if (currentPhase == 1 || currentPhase == 2) {
                // Moving phase or flying phase logic
                if (boardPositions[position] == currentPlayer) {
                    currentGame.saveStateForUndo();
                    if (currentGame.makeMove(position)) {
                        List<Integer> validMoves = currentGame.getValidMoves(position);
                        showValidMoveIndicators(validMoves);  // Visual indicator for valid moves
                    }
                } else if (boardPositions[position] == null && currentGame.getSelectedPiece() != -1) {
                    int oldPosition = currentGame.getSelectedPiece();

                    // Handle regular move and flying phase moves
                    if (currentGame.makeMove(position)) {
                        hideAllIndicators();
                        updateBoardUI();
                        updateLineColors();

                        // After the player's move, check if it's the bot's turn
                        if (currentGame.gameMode.equals("PLAYER VS BOT") && currentGame.getCurrentPlayer() == Player.RED) {
                            makeBotMove();
                        }
                    }
                }
            } else if (currentPhase == 0) {
                // Placing phase logic
                if (boardPositions[position] == null) {
                    currentGame.saveStateForUndo();
                    if (currentGame.makeMove(position)) {
                        updateBoardUI();
                        updateOnHandPieces();
                        updateLineColors();

                        // After the player's move, check if it's the bot's turn
                        if (currentGame.gameMode.equals("PLAYER VS BOT") && currentGame.getCurrentPlayer() == Player.RED) {
                            makeBotMove();
                        }
                    }
                }
            } else if (currentPhase == -1 || currentPhase == -2) {
                updateBoardUIOnPieceRemoval(ring, position);

                // After the player deletes a piece, check if it's the bot's turn
                if (currentGame.gameMode.equals("PLAYER VS BOT") && currentGame.getCurrentPlayer() == Player.RED) {
                    makeBotMove();
                }
            }
        });
    }

    private Bot bot = new EasyBot();

    private void makeBotMove() {
        if (bot == null) {
            return; // Bot is not active in the current game mode
        }

        Timeline botMoveDelay = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            int phase = currentGame.getPhase();

            if (phase == 0) {
                // Placing phase
                int botPlacement = bot.placePiece(currentGame);
                if (botPlacement != -1) {
                    currentGame.saveStateForUndo();
                    if (currentGame.makeMove(botPlacement)) {
                        updateBoardUI();
                        updateOnHandPieces();
                        updateLineColors();

                        // After the bot's move, check if it needs to delete a piece
                        if (currentGame.getPhase() == -2) { // Bot is RED player
                            makeBotDeleteMove();
                        } else {
                            // If it's still the bot's turn, make another move
                            if (currentGame.getCurrentPlayer() == Player.RED && currentGame.gameMode.equals("PLAYER VS BOT")) {
                                makeBotMove();
                            }
                        }
                    }
                } else {
                    System.out.println("Bot has no valid positions to place a piece.");
                }
            } else if (phase == 1 || phase == 2) {
                // Moving or Flying phase
                int selectedPiece = bot.selectPiece(currentGame);
                if (selectedPiece != -1) {
                    // Simulate selecting the piece
                    currentGame.saveStateForUndo();
                    if (currentGame.makeMove(selectedPiece)) {
                        // Now determine where to move it
                        int destination = bot.determineMove(currentGame, selectedPiece);
                        if (destination != -1) {
                            currentGame.saveStateForUndo();
                            if (currentGame.makeMove(destination)) {
                                updateBoardUI();
                                updateLineColors();

                                // After the bot's move, check if it needs to delete a piece
                                if (currentGame.getPhase() == -2) { // Bot is RED player
                                    makeBotDeleteMove();
                                } else {
                                    // If it's still the bot's turn, make another move
                                    if (currentGame.getCurrentPlayer() == Player.RED && currentGame.gameMode.equals("PLAYER VS BOT")) {
                                        makeBotMove();
                                    }
                                }
                            }
                        } else {
                            System.out.println("Bot has no valid moves from selected piece.");
                            // Reset the selected piece
                            currentGame.setSelectedPiece(-1);
                        }
                    }
                } else {
                    System.out.println("Bot has no movable pieces.");
                }
            }
        }));
        botMoveDelay.play();
    }

    private void makeBotDeleteMove() {
        if (bot == null) {
            return; // Bot is not active in the current game mode
        }

        Timeline botDeleteDelay = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            int positionToDelete = bot.determinePieceToDelete(currentGame);
            if (positionToDelete != -1) {
                currentGame.saveStateForUndo();
                if (currentGame.makeMove(positionToDelete)) {
                    updateBoardUI();
                    updateOnHandPieces();
                    updateLineColors();

                    // After deleting, check if the bot needs to make another move
                    if (currentGame.getCurrentPlayer() == Player.RED && currentGame.gameMode.equals("PLAYER VS BOT")) {
                        makeBotMove();
                    }
                }
            } else {
                System.out.println("Bot has no valid pieces to delete.");
            }
        }));
        botDeleteDelay.play();
    }

    // Function to show indicators corresponding to valid move positions
    private void showValidMoveIndicators(List<Integer> validMoves) {
        // Hide all indicators first
        indicators.forEach(indicator -> indicator.setVisible(false));

        // Show indicators at valid move positions
        for (Integer position : validMoves) {
            indicators.get(position).setVisible(true);
        }
    }

    private void showDeletionIndicators(Player opponentPlayer) {
        // Hide all indicators first to reset the board state
        indicators.forEach(indicator -> indicator.setVisible(false));

        // Show indicators only for opponent pieces not in a mill
        Player[] boardPositions = currentGame.getBoardPositions();
        for (int position = 0; position < boardPositions.length; position++) {
            if (boardPositions[position] == opponentPlayer) {
                // Check if the piece does NOT form a mill
                if (!currentGame.formsMill(position, opponentPlayer)) {
                    indicators.get(position).setVisible(true); // Show indicator
                }
            }
        }
    }

    // Function to hide all indicators after a move is made
    private void hideAllIndicators() {
        indicators.forEach(indicator -> indicator.setVisible(false));
    }


    private void updateBoardUI() {
        // Update the current game reference to the new game instance
        currentGame = gameManager.getCurrentGame();

        // Update the visibility of diagonal lines and extra pieces based on the version
        boolean isIn12Version = currentGame.isIn12MenMorrisVersion();
        diagonal1.setVisible(isIn12Version);
        diagonal2.setVisible(isIn12Version);
        diagonal3.setVisible(isIn12Version);
        diagonal4.setVisible(isIn12Version);

        onHandPieceExtra0.setVisible(isIn12Version);
        onHandPieceExtra1.setVisible(isIn12Version);
        onHandPieceExtra2.setVisible(isIn12Version);

        onHandPieceRExtra0.setVisible(isIn12Version);
        onHandPieceRExtra1.setVisible(isIn12Version);
        onHandPieceRExtra2.setVisible(isIn12Version);

        // Get the current board positions and move count
        Player[] boardPositions = currentGame.getBoardPositions();

        // Set visibility of each blue and red piece based on its position
        for (int i = 0; i < boardPositions.length; i++) {
            bluePieces[i].setVisible(boardPositions[i] == Player.BLUE);
            redPieces[i].setVisible(boardPositions[i] == Player.RED);
        }

        // Clear all move indicators
        for (StackPane indicator : indicators) {
            indicator.setVisible(false);
        }

        // If a piece is selected, display indicators for valid moves
        int selectedPiece = currentGame.getSelectedPiece();
        if (selectedPiece != -1) {
            List<Integer> validMoves = currentGame.getValidMoves(selectedPiece);
            for (Integer move : validMoves) {
                indicators.get(move).setVisible(true);  // Assume each move indicator corresponds to a board position
            }
        }

        // Update the game number text based on the current game index
        gameNumText.setText("GAME" + (gameManager.getCurrentGameIndex() + 1));
        pieceNumText.setText((currentGame.isIn12MenMorrisVersion() ? "12 " : "9 ") + "PIECES");
        if (currentGame.isIn12MenMorrisVersion()) {
            tinyCircle1.setLayoutX(135);
        } else {
            tinyCircle1.setLayoutX(119);
        }

        // Update Game mode menu bar
        if(Objects.equals(currentGame.gameMode, "LOCAL 2 PLAYER")){
            easyBotText.setVisible(false);
            local2pText.setVisible(true);
            tinyCircle0.setLayoutX(95);
        }else{
            easyBotText.setVisible(true);
            local2pText.setVisible(false);
            tinyCircle0.setLayoutX(110);
        }

        updateLineColors();
        updateOnHandPieces();
    }


    private void updateLineColors() {
        Player[] boardPositions = currentGame.getBoardPositions();

        // Check diagonals
        checkLine(diagonal1, boardPositions[0], boardPositions[3], boardPositions[6]);
        checkLine(diagonal2, boardPositions[2], boardPositions[5], boardPositions[8]);
        checkLine(diagonal3, boardPositions[15], boardPositions[18], boardPositions[21]);
        checkLine(diagonal4, boardPositions[17], boardPositions[20], boardPositions[23]);

        // Check horizontal lines
        checkLine(horizontal1, boardPositions[0], boardPositions[1], boardPositions[2]);
        checkLine(horizontal2, boardPositions[3], boardPositions[4], boardPositions[5]);
        checkLine(horizontal3, boardPositions[6], boardPositions[7], boardPositions[8]);
        checkLine(horizontal4, boardPositions[9], boardPositions[10], boardPositions[11]);
        checkLine(horizontal5, boardPositions[12], boardPositions[13], boardPositions[14]);
        checkLine(horizontal6, boardPositions[15], boardPositions[16], boardPositions[17]);
        checkLine(horizontal7, boardPositions[18], boardPositions[19], boardPositions[20]);
        checkLine(horizontal8, boardPositions[21], boardPositions[22], boardPositions[23]);

        // Check vertical lines
        checkLine(vertical1, boardPositions[0], boardPositions[9], boardPositions[21]);
        checkLine(vertical2, boardPositions[3], boardPositions[10], boardPositions[18]);
        checkLine(vertical3, boardPositions[6], boardPositions[11], boardPositions[15]);
        checkLine(vertical4, boardPositions[1], boardPositions[4], boardPositions[7]);
        checkLine(vertical5, boardPositions[16], boardPositions[19], boardPositions[22]);
        checkLine(vertical6, boardPositions[8], boardPositions[12], boardPositions[17]);
        checkLine(vertical7, boardPositions[5], boardPositions[13], boardPositions[20]);
        checkLine(vertical8, boardPositions[2], boardPositions[14], boardPositions[23]);
    }

    private void checkLine(Line line, Player... positions) {
        if (positions[0] == Player.BLUE && positions[1] == Player.BLUE && positions[2] == Player.BLUE) {
            line.setStyle(BLUE_COLOR);
        } else if (positions[0] == Player.RED && positions[1] == Player.RED && positions[2] == Player.RED) {
            line.setStyle(RED_COLOR);
        } else {
            line.setStyle(BLACK_COLOR);
        }
    }


    private void updateOnHandPieces() {
        StackPane[] onHandBluePieces;
        StackPane[] onHandRedPieces;

        // Check if the game is in 12 Men's Morris version
        if (currentGame.isIn12MenMorrisVersion()) {
            // Include extra pieces for 12 Men's Morris
            onHandBluePieces = new StackPane[]{onHandPieceExtra0, onHandPieceExtra1, onHandPiece0, onHandPiece1, onHandPiece2, onHandPiece3,
                    onHandPiece4, onHandPiece5, onHandPiece6, onHandPiece7, onHandPiece8, onHandPieceExtra2};

            onHandRedPieces = new StackPane[]{onHandPieceRExtra0, onHandPieceRExtra1, onHandPieceR0, onHandPieceR1, onHandPieceR2, onHandPieceR3,
                    onHandPieceR4, onHandPieceR5, onHandPieceR6, onHandPieceR7, onHandPieceR8, onHandPieceRExtra2};
        } else {
            // Exclude extra pieces for 9 Men's Morris
            onHandBluePieces = new StackPane[]{onHandPiece0, onHandPiece1, onHandPiece2, onHandPiece3,
                    onHandPiece4, onHandPiece5, onHandPiece6, onHandPiece7, onHandPiece8};

            onHandRedPieces = new StackPane[]{onHandPieceR0, onHandPieceR1, onHandPieceR2, onHandPieceR3,
                    onHandPieceR4, onHandPieceR5, onHandPieceR6, onHandPieceR7, onHandPieceR8};
        }

        // Get the current move counts from the Game class
        int moveCountBlue = currentGame.getMoveCountBlue();
        int moveCountRed = currentGame.getMoveCountRed();

        // Update the visibility of blue pieces based on move count
        for (int i = 0; i < onHandBluePieces.length; i++) {
            onHandBluePieces[i].setVisible(i >= moveCountBlue);
        }

        // Update the visibility of red pieces based on move count
        for (int i = 0; i < onHandRedPieces.length; i++) {
            onHandRedPieces[i].setVisible(i >= moveCountRed);
        }
    }



    // Interactive pieces (held on hand) logic
    private void setupOnHandPieceInteraction(StackPane onHandPiece) {
        // Scale up when mouse enters
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), onHandPiece);
        scaleUp.setToX(1.1); // Scale up to 110%
        scaleUp.setToY(1.1);

        // Scale down when mouse exits, then back to normal
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), onHandPiece);
        scaleDown.setToX(0.9); // Shrink down to 90%
        scaleDown.setToY(0.9);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), onHandPiece);
        scaleBackToNormal.setToX(1.0); // Return to original size
        scaleBackToNormal.setToY(1.0);

        // Chain the transitions when mouse exits
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Add event handlers to trigger the transitions
        onHandPiece.setOnMouseEntered(event -> {
            if (onHandPiece.isVisible()) {
                scaleUp.play();
            }
        });

        onHandPiece.setOnMouseExited(event -> {
            if (onHandPiece.isVisible()) {
                scaleDown.play();
            }
        });

        // Pause animation if the object becomes invisible
        onHandPiece.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                scaleUp.pause();
                scaleDown.pause();
                scaleBackToNormal.pause();
            } else {
                scaleUp.playFromStart();
                scaleDown.playFromStart();
                scaleBackToNormal.playFromStart();
            }
        });
    }

    private void setupIndicatorAnimations() {
        for (StackPane indicator : indicators) {
            RotateTransition rt = new RotateTransition(Duration.seconds(2), indicator);
            rt.setByAngle(360);
            rt.setCycleCount(RotateTransition.INDEFINITE);
            rt.setAutoReverse(true);
            indicator.setVisible(false); // Ensure all indicators start as invisible

            // Play rotation if visible, pause when invisible
            indicator.visibleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    resetAllAnimations(); // Reset all animations and rotations
                    rt.playFromStart(); // Start rotation from the beginning
                } else {
                    rt.stop(); // Stop the animation when it becomes invisible
                }
            });
        }
    }

    private void resetAllAnimations() {
        for (StackPane indicator : indicators) {
            if (indicator.isVisible()) {
                RotateTransition rt = new RotateTransition(Duration.seconds(2), indicator);
                rt.setByAngle(360);
                rt.setCycleCount(RotateTransition.INDEFINITE);
                rt.setAutoReverse(true);
                rt.stop(); // Stop the current animation
                indicator.setRotate(0); // Reset the rotation of the StackPane
                rt.playFromStart(); // Start the animation from the beginning
            } else {
                indicator.setRotate(0); // Reset rotation for hidden indicators as well
            }
        }
    }

    private void setupMenuArrows() {
        MenuBigLeftArrow.setOnMouseClicked(event -> {
            gameManager.switchToPreviousGame();
            updateBoardUI();
        });
        MenuBigRightArrow.setOnMouseClicked(event -> {
            gameManager.switchToNextGame();
            updateBoardUI();
        });
    }


    private void updateBotBasedOnGameMode() {
        if (currentGame.gameMode.equals("PLAYER VS BOT")) {
            if (bot == null) {
                bot = new EasyBot();
            }
        } else {
            bot = null; // Disable the bot in other modes
        }
    }

    private void setupSmallArrowAnimations() {
        // Small left arrow 0 behavior (similar to big left arrow)
        MenuSmallLeftArrow0.setOnMouseClicked(event -> {
            currentGame.gameMode = "LOCAL 2 PLAYER";
            easyBotText.setVisible(false);
            local2pText.setVisible(true);
            tinyCircle0.setLayoutX(95);
            updateBotBasedOnGameMode();
        });

        // Small right arrow 0 behavior (similar to big right arrow)
        MenuSmallRightArrow0.setOnMouseClicked(event -> {
            currentGame.gameMode = "PLAYER VS BOT";
            easyBotText.setVisible(true);
            local2pText.setVisible(false);
            tinyCircle0.setLayoutX(110);
            updateBotBasedOnGameMode();
        });

        // Small left arrow 1 toggles version using the wrapper
        MenuSmallLeftArrow1.setOnMouseClicked(this::handleMouseClickForSwitchVersion);

        // Small right arrow 1 toggles version using the wrapper
        MenuSmallRightArrow1.setOnMouseClicked(this::handleMouseClickForSwitchVersion);

        // Optional: You can also add animations similar to the big arrows, e.g., scaling up/down:
        setupArrowHoverAnimation(MenuSmallLeftArrow0);
        setupArrowHoverAnimation(MenuSmallRightArrow0);
        setupArrowHoverAnimation(MenuSmallLeftArrow1);
        setupArrowHoverAnimation(MenuSmallRightArrow1);
    }


    private void handleMouseClickForSwitchVersion(MouseEvent event) {
        // Call the switchVersionAction, ignoring the MouseEvent but simulating the ActionEvent
        switchVersionAction(new ActionEvent());
    }


    private void setupArrowHoverAnimation(Arc arrow) {
        // Create a hover scale animation, similar to the big arrows
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), arrow);
        scaleUp.setToX(1.1); // Scale up to 115%
        scaleUp.setToY(1.1);

        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), arrow);
        scaleDown.setToX(0.6); // Shrink down to 95%
        scaleDown.setToY(0.6);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), arrow);
        scaleBackToNormal.setToX(0.7); // Reset to original size
        scaleBackToNormal.setToY(0.7);

        // Chain the transitions
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Set event handlers for mouse enter/exit
        arrow.setOnMouseEntered(event -> scaleUp.play());
        arrow.setOnMouseExited(event -> scaleDown.play());
    }

    // Interactive buttons logic
    private void setupToggleButtonInteraction(ToggleButton toggleButton) {
        // Scale up when mouse enters
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), toggleButton);
        scaleUp.setToX(1.1); // Scale up to 110%
        scaleUp.setToY(1.1);

        // Scale down when mouse exits, then back to normal
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), toggleButton);
        scaleDown.setToX(0.9); // Shrink down to 90%
        scaleDown.setToY(0.9);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), toggleButton);
        scaleBackToNormal.setToX(1.0); // Return to original size
        scaleBackToNormal.setToY(1.0);

        // Chain the transitions when mouse exits
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Add event handlers to trigger the transitions
        toggleButton.setOnMouseEntered(event -> {
            if (toggleButton.isVisible()) {
                scaleUp.play();
            }
        });

        toggleButton.setOnMouseExited(event -> {
            if (toggleButton.isVisible()) {
                scaleDown.play();
            }
        });

    }

    private void setupButtonInteraction(Button button) {
        // Scale up when mouse enters
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), button);
        scaleUp.setToX(1.1); // Scale up to 110%
        scaleUp.setToY(1.1);

        // Scale down when mouse exits, then back to normal
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), button);
        scaleDown.setToX(0.9); // Shrink down to 90%
        scaleDown.setToY(0.9);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), button);
        scaleBackToNormal.setToX(1.0); // Return to original size
        scaleBackToNormal.setToY(1.0);

        // Chain the transitions when mouse exits
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Add event handlers to trigger the transitions
        button.setOnMouseEntered(event -> {
            if (button.isVisible()) {
                scaleUp.play();
            }
        });

        button.setOnMouseExited(event -> {
            if (button.isVisible()) {
                scaleDown.play();
            }
        });

    }

    // Interactive menu Pane
    private void setupMenuInteraction(Pane menuObject) {
        // Scale up when mouse enters
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), menuObject);
        scaleUp.setToX(1.05); // Scale up to 110%
        scaleUp.setToY(1.05);

        // Scale down when mouse exits, then back to normal
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), menuObject);
        scaleDown.setToX(0.95); // Shrink down to 90%
        scaleDown.setToY(0.95);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), menuObject);
        scaleBackToNormal.setToX(1.0); // Return to original size
        scaleBackToNormal.setToY(1.0);

        // Chain the transitions when mouse exits
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Add event handlers to trigger the transitions
        menuObject.setOnMouseEntered(event -> {
            if (menuObject.isVisible()) {
                scaleUp.play();
            }
        });

        menuObject.setOnMouseExited(event -> {
            if (menuObject.isVisible()) {
                scaleDown.play();
            }
        });
    }

    private void setupTitledPaneInteraction(TitledPane titledPane) {
        // Scale up when mouse enters
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), titledPane);
        scaleUp.setToX(1.02); // Scale up to 105%
        scaleUp.setToY(1.02);

        // Scale down when mouse exits, then back to normal
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), titledPane);
        scaleDown.setToX(0.98); // Shrink down to 95%
        scaleDown.setToY(0.98);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), titledPane);
        scaleBackToNormal.setToX(1.0); // Return to original size
        scaleBackToNormal.setToY(1.0);

        // Chain the transitions when mouse exits
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Add event handlers to trigger the transitions
        titledPane.setOnMouseEntered(event -> {
            if (titledPane.isVisible()) {
                scaleUp.play();
            }
        });

        titledPane.setOnMouseExited(event -> {
            if (titledPane.isVisible()) {
                scaleDown.play();
            }
        });
    }

    private void setupMenuArrowInteraction(Circle menuObject) {
        // Scale up when mouse enters
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), menuObject);
        scaleUp.setToX(1.15); // Scale up to 110%
        scaleUp.setToY(1.15);

        // Scale down when mouse exits, then back to normal
        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), menuObject);
        scaleDown.setToX(0.95); // Shrink down to 90%
        scaleDown.setToY(0.95);

        ScaleTransition scaleBackToNormal = new ScaleTransition(Duration.seconds(0.2), menuObject);
        scaleBackToNormal.setToX(1.0); // Return to original size
        scaleBackToNormal.setToY(1.0);

        // Chain the transitions when mouse exits
        scaleDown.setOnFinished(event -> scaleBackToNormal.play());

        // Add event handlers to trigger the transitions
        menuObject.setOnMouseEntered(event -> {
            if (menuObject.isVisible()) {
                scaleUp.play();
            }
        });

        menuObject.setOnMouseExited(event -> {
            if (menuObject.isVisible()) {
                scaleDown.play();
            }
        });
    }


    // Logic when Buttons are clicked:
    @FXML
    void switchVersionAction(ActionEvent event) {
        // Toggle the version in the current game instance
        boolean newVersion = !currentGame.isIn12MenMorrisVersion();
        currentGame.setIn12MenMorrisVersion(newVersion);

        // Reset the game but keep the version field intact
        currentGame.resetGame();

        // Reset the UI to reflect the new game state
        updateBoardUI();
    }

    @FXML
    void TestIndicatorAction(ActionEvent event) {
        //Here is a game simulation:
        currentGame.makeMove(1);
        currentGame.makeMove(2);
        currentGame.makeMove(3);
        currentGame.makeMove(4);

        currentGame.makeMove(11);
        currentGame.makeMove(12);
        currentGame.makeMove(13);
        currentGame.makeMove(14);

        currentGame.makeMove(21);
        currentGame.makeMove(22);
        currentGame.makeMove(23);
        currentGame.makeMove(5);

        currentGame.makeMove(7);
        currentGame.makeMove(8);
        currentGame.makeMove(9);
        currentGame.makeMove(10);

        currentGame.makeMove(15);
        currentGame.makeMove(16);
        updateBoardUI();
    }

    @FXML
    void showIndexAction(ActionEvent event) {
        toggleIndex();
    }

    // Called when "New Game" is clicked
    @FXML
    void newGameClicked(ActionEvent event) {
        WinPopUpRed.setVisible(false);
        WinPopUpBlue.setVisible(false);
        DrawPopUp.setVisible(false);

        currentGame.resetGame();        // Reset the game logic
        updateBoardUI();
    }

    private void toggleIndex() {
        Arrays.stream(indicies).forEach(text -> text.setVisible(!index23.isVisible()));
    }

    //SCALING

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    @FXML
    private void zoomIn(ActionEvent event) {
        // Scale the anchorMain
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), anchorMain);
        scaleTransition.setToX(anchorMain.getScaleX() * SCALE_FACTOR);
        scaleTransition.setToY(anchorMain.getScaleY() * SCALE_FACTOR);
        scaleTransition.play();

        // Increase the window size
        if (primaryStage != null) {
            primaryStage.setWidth(primaryStage.getWidth() * SCALE_FACTOR);
            primaryStage.setHeight(primaryStage.getHeight() * SCALE_FACTOR);
        }
    }

    // Method to zoom out
    @FXML
    private void zoomOut(ActionEvent event) {
        // Scale down the anchorMain
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), anchorMain);
        scaleTransition.setToX(anchorMain.getScaleX() / SCALE_FACTOR);
        scaleTransition.setToY(anchorMain.getScaleY() / SCALE_FACTOR);
        scaleTransition.play();

        // Decrease the window size
        if (primaryStage != null) {
            primaryStage.setWidth(primaryStage.getWidth() / SCALE_FACTOR);
            primaryStage.setHeight(primaryStage.getHeight() / SCALE_FACTOR);
        }
    }

}
