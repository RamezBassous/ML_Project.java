package group15;

import group15.bot.AlphaBetaBot;
import group15.bot.MonteCarloBot;
import javafx.animation.KeyFrame;
import javafx.animation.KeyFrame;
import group15.bot.MeatBot;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
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

import group15.bot.Bot;
import group15.bot.EasyBot;


/**
 * Controller class responsible for managing the user interface and handling interactions 
 * for the Nine Men's Morris game. It connects the FXML components with the game logic 
 * and provides methods to update the UI based on game events and user actions.
 */
public class Controller {

    private static final String BLUE_COLOR = "-fx-stroke: #457b9d;";
    private static final String RED_COLOR = "-fx-stroke: #e63946;";
    private static final String BLACK_COLOR = "-fx-stroke: black;";

    // MENU OBJECTS IMPORTS
    @FXML
    private AnchorPane anchorMain;

    @FXML
    private Pane GameMenu, DrawPopUp, WinPopUpBlue, WinPopUpRed, bluePlayerWinsBox, redPlayerWinsBox, drawBox, turnIndicatorGlowBlue, turnIndicatorGlowRed;

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

    private int currentGameModeIndex = 0; // Index to track the current mode
    private int currentBotMode = 0; // Tracks which bot mode is active (0 = first bot, 1 = second bot, 2 = third bot)


    // Define scaling factors
    private static final double SCALE_FACTOR = 1.1;
    private Stage primaryStage; // Holds reference to the main stage


    /**
     * Initializes the game and sets up the UI components and event listeners.
     * This method is called automatically when the FXML file is loaded.
     */
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

        hidePopups();

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

        updateGameMode();
    }

    /**
     * Initializes the user interface by hiding unnecessary elements and configuring interactions.
     */
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

        // Set initial visibility
        turnIndicatorGlowBlue.setVisible(true); // Blue player starts
        turnIndicatorGlowRed.setVisible(false);
        setupPaneAnimation(turnIndicatorGlowBlue);
        setupPaneAnimation(turnIndicatorGlowRed);

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

    /**
     * Sets listeners for game events such as winning or drawing.
     */
    private void setGameEventListeners() {
        // Assigning listener for each game instance
        for (Game game : gameManager.getGames()) {
            game.setGameEventListener(new GameEventListener() {
                @Override
                public void onGameWon(String winner) {
                    if (winner.equalsIgnoreCase("Red")) {
                        WinPopUpRed.setVisible(true);
                    } else if (winner.equalsIgnoreCase("Blue")) {
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

    /**
     * Updates the state of the undo and redo buttons based on the current game state.
     */
    private void updateUndoRedoButtons() {
        UndoB.setDisable(!currentGame.canUndo);
        RedoB.setDisable(currentGame.getRedoStack().isEmpty());
    }

    /**
     * Handles the Undo button click event. Attempts to undo the last move.
     * If successful, updates the board UI to reflect the undone move.
     *
     * @param event The action event triggered by the Undo button.
     */
    @FXML
    void handleUndoAction(ActionEvent event) {
        if (currentGame.undo()) {
            updateBoardUI();  // Refresh the board UI after undo
        }
    }

    /**
     * Handles the Redo button click event. Attempts to redo the last undone move.
     * If successful, updates the board UI to reflect the redone move.
     *
     * @param event The action event triggered by the Redo button.
     */
    @FXML
    void handleRedoAction(ActionEvent event) {
        if (currentGame.redo()) {
            updateBoardUI();  // Refresh the board UI after redo
        }
    }

    /**
     * Sets up a growing and shrinking animation for the specified pane.
     *
     * @param targetPane The pane to which the animation will be applied.
     */
    private void setupPaneAnimation(Pane targetPane) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.9), targetPane);
        scaleTransition.setByX(0.03); // Grow by 3%
        scaleTransition.setByY(0.03); // Grow by 3%
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
        scaleTransition.play(); // Start animation
    }

    /**
     * Toggles the visibility of the turn indicators to show the current player's turn.
     */
    private void toggleTurnIndicator() {
        if (currentGame.getCurrentPlayer() == Player.BLUE) {
            turnIndicatorGlowBlue.setVisible(true);
            turnIndicatorGlowRed.setVisible(false);
        } else if (currentGame.getCurrentPlayer() == Player.RED) {
            turnIndicatorGlowBlue.setVisible(false);
            turnIndicatorGlowRed.setVisible(true);
        }
    }

    /**
     * Updates the board UI and handles the removal of a piece during the deletion phase.
     * Ensures that the removal adheres to the game's rules.
     * Call only when phase = -1 || -2
     *
     * @param ring     The ring element used to highlight the piece.
     * @param position The board position of the piece to be removed.
     */
    private void updateBoardUIOnPieceRemoval(Circle ring, int position) {
      Player playerInPosition = currentGame.getBoardPositions()[position];
      Player opponentPlayer = currentGame.getCurrentPlayer().opponent();

      // Show deletion indicators before handling click
      showDeletionIndicators(opponentPlayer);
    }

    /**
     * Sets up interaction behaviors for a board zone, including highlighting,
     * animation, and user actions based on the current game phase.
     *
     * @param zone         The circle element representing the board zone.
     * @param shadow       The shadow element for visual feedback.
     * @param ring         The ring element used to highlight a piece.
     * @param playerPiece  The stack pane representing the player's piece.
     * @param enemyPiece   The stack pane representing the opponent's piece.
     * @param position     The position on the board associated with the zone.
     */
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
            // Get the current phase and player information
            int currentPhase = currentGame.getPhase();
            Player currentPlayer = currentGame.getCurrentPlayer();
            Player pieceOwner = currentGame.getBoardPositions()[position];

            // Reset visibility for shadow and ring
            shadow.setVisible(false);
            ring.setVisible(false);

            // Check for deletion phase (-1 or -2)
            if (currentPhase == -1 && pieceOwner == Player.RED) {
                // Red piece deletion phase
                if (!currentGame.formsMill(position, Player.RED)) {
                    ring.setStyle(RED_COLOR);
                    ring.setVisible(true);
                    scaleTransition.playFromStart();
                }
            } else if (currentPhase == -2 && pieceOwner == Player.BLUE) {
                // Blue piece deletion phase
                if (!currentGame.formsMill(position, Player.BLUE)) {
                    ring.setStyle(BLUE_COLOR);
                    ring.setVisible(true);
                    scaleTransition.playFromStart();
                }
            } else if (currentPhase >= 0 && pieceOwner == null) {
                // Show shadow for empty zones in non-deletion phases
                shadow.setVisible(true);
            }

            // Stop the ring animation if it's not visible
            if (!ring.isVisible()) {
                scaleTransition.stop();
                ring.setScaleX(1.0);
                ring.setScaleY(1.0);
            }
        });

    /**
     * Sets up the mouse exited event for a game board zone.
     * Resets all visual effects such as shadows and rings when the mouse leaves the zone.
     */
    zone.setOnMouseExited(event -> {
        shadow.setVisible(false);
        ring.setVisible(false);
        scaleTransition.stop(); // Stop the animation
        ring.setScaleX(1.0); // Reset scale to normal size
        ring.setScaleY(1.0); // Reset scale to normal size
        ring.setStyle("-fx-stroke: white;"); // Reset the stroke color
    });

    /**
     * Ensures the animation is stopped when the ring is hidden.
     * Resets the scale and appearance of the ring to its default state.
     */
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
            ring.setVisible(false);zone.setOnMouseEntered(event -> {
                // Get the current phase and player information
                int currentPhase = currentGame.getPhase();
                Player currentPlayer = currentGame.getCurrentPlayer();
                Player pieceOwner = currentGame.getBoardPositions()[position];

                // Reset visibility for shadow and ring
                shadow.setVisible(false);
                ring.setVisible(false);

                // Check for deletion phase (-1 or -2)
                if (currentPhase == -1 && pieceOwner == Player.RED) {
                    // Red piece deletion phase
                    if (!currentGame.formsMill(position, Player.RED)) {
                        ring.setStyle(RED_COLOR);
                        ring.setVisible(true);
                        scaleTransition.playFromStart();
                    }
                } else if (currentPhase == -2 && pieceOwner == Player.BLUE) {
                    // Blue piece deletion phase
                    if (!currentGame.formsMill(position, Player.BLUE)) {
                        ring.setStyle(BLUE_COLOR);
                        ring.setVisible(true);
                        scaleTransition.playFromStart();
                    }
                } else if (currentPhase >= 0 && pieceOwner == null) {
                    // Show shadow for empty zones in non-deletion phases
                    shadow.setVisible(true);
                }

                // Stop the ring animation if it's not visible
                if (!ring.isVisible()) {
                    scaleTransition.stop();
                    ring.setScaleX(1.0);
                    ring.setScaleY(1.0);
                }
            });
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

        /**
         * Handles mouse click events for game board zones.
         * Performs actions based on the current game phase, such as placing, moving, or deleting pieces.
         */
        zone.setOnMouseClicked(event -> {
            currentGame.getStrategy().handleMouseClickEvent(position);
            currentGame.isOver();
            updateBoardUI();
        });
    }

    /**
     * Displays indicators for valid move positions on the board.
     * Hides all existing indicators first, then shows indicators at valid move positions.
     *
     * @param validMoves A list of valid positions for a move.
     */
    private void showValidMoveIndicators(List<Integer> validMoves) {
        // Hide all indicators first
        indicators.forEach(indicator -> indicator.setVisible(false));

        // Show indicators at valid move positions
        for (Integer position : validMoves) {
            indicators.get(position).setVisible(true);
        }
    }

    /**
     * Displays indicators for pieces that can be deleted during the deletion phase.
     * Highlights opponent pieces that are not part of a mill.
     *
     * @param opponentPlayer The opponent player whose pieces are being checked.
     */
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

        showValidMovesIndicators();

        // Update the game number text based on the current game index
        gameNumText.setText("GAME" + (gameManager.getCurrentGameIndex() + 1));
        pieceNumText.setText((currentGame.isIn12MenMorrisVersion() ? "12 " : "9 ") + "PIECES");
        if (currentGame.isIn12MenMorrisVersion()) {
            tinyCircle1.setLayoutX(137);
        } else {
            tinyCircle1.setLayoutX(121);
        }

        updateLineColors();
        updateOnHandPieces();
        toggleTurnIndicator();
        updateUndoRedoButtons();
    }


    private void showValidMovesIndicators() {
        List<Integer> validMoves = currentGame.getValidMoves();
        for (Integer move : validMoves) {
            indicators.get(move).setVisible(true);  // Assume each move indicator corresponds to a board position
        }
    }

    /**
     * Updates the colors of the lines on the board based on the game state.
     * Checks for completed lines (mills) and updates their color to match the player.
     */
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

    /**
     * Updates the style of a line to reflect the color of a mill if all positions are occupied by the same player.
     *
     * @param line      The line to be updated.
     * @param positions The positions in the line to check.
     */
    private void checkLine(Line line, Player... positions) {
        if (positions[0] == Player.BLUE && positions[1] == Player.BLUE && positions[2] == Player.BLUE) {
            line.setStyle(BLUE_COLOR);
        } else if (positions[0] == Player.RED && positions[1] == Player.RED && positions[2] == Player.RED) {
            line.setStyle(RED_COLOR);
        } else {
            line.setStyle(BLACK_COLOR);
        }
    }

    /**
     * Updates the visibility of on-hand pieces based on the current game version
     * and the number of pieces each player has placed on the board.
     */
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
        int moveCountBlue = currentGame.getPlacedPiecesBlue();
        int moveCountRed = currentGame.getPlacedPiecesRed();

        // Update the visibility of blue pieces based on move count
        for (int i = 0; i < onHandBluePieces.length; i++) {
            onHandBluePieces[i].setVisible(i >= moveCountBlue);
        }

        // Update the visibility of red pieces based on move count
        for (int i = 0; i < onHandRedPieces.length; i++) {
            onHandRedPieces[i].setVisible(i >= moveCountRed);
        }
    }

    /**
     * Sets up mouse interaction for on-hand pieces to create a scaling effect when hovered.
     *
     * @param onHandPiece The StackPane representing the on-hand piece.
     */
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

    /**
     * Sets up rotation animations for indicators when they are made visible.
     * Resets all animations and rotations when indicators become visible.
     */
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

    /**
     * Resets animations for all indicators by stopping and clearing their rotation.
     * Ensures all indicators are in a consistent state before starting new animations.
     */
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

    /**
     * Configures arrow button interactions to switch between games in the game manager.
     * Updates the board UI after switching games.
     */
    private void setupMenuArrows() {
        MenuBigLeftArrow.setOnMouseClicked(event -> {
            hidePopups();
            gameManager.switchToPreviousGame();
            updateBoardUI();
        });
        MenuBigRightArrow.setOnMouseClicked(event -> {
            hidePopups();
            gameManager.switchToNextGame();
            updateBoardUI();
        });
    }

    private void hidePopups() {
        WinPopUpRed.setVisible(false);
        WinPopUpBlue.setVisible(false);
        DrawPopUp.setVisible(false);
    }

    /**
     * Updates the bot based on the current game mode. If the game mode is "PLAYER VS BOT",
     * it initializes the bot with the corresponding difficulty level based on the currentBotMode.
     * Otherwise, it disables the bot.
     */
    private void updateBotBasedOnGameMode() { // Adding new bots will happen here (use currentBotMode=0 for easy 1 for mid and 2 for hard)
        if (currentGame.gameMode.equals("PLAYER VS BOT")) {
            if (currentGame.red instanceof MeatBot) {
                currentGame.red = new EasyBot(); // Change bot here only when Human VS Bot
            }
        }
    }

    /**
     * Updates the game mode and the UI components based on the current game mode index.
     * This method handles the display of the game mode and the updates to UI elements such as
     * the game mode text and layout positions.
     */
    private void updateGameMode() {
        switch (currentGameModeIndex) {
            case 0: // LOCAL 2 PLAYER
                currentGame.gameMode = "LOCAL 2 PLAYER";
                easyBotText.setVisible(false);
                local2pText.setVisible(true);
                tinyCircle0.setLayoutX(105);
                break;
            case 1: // PLAYER VS BOT - Easy
                currentGame.gameMode = "PLAYER VS BOT";
                currentBotMode = 0; // Easy Bot
                easyBotText.setText("Easy Bot");
                easyBotText.setVisible(true);
                local2pText.setVisible(false);
                tinyCircle0.setLayoutX(120);
                break;
            case 2: // PLAYER VS BOT - Mid
                currentGame.gameMode = "PLAYER VS BOT";
                currentBotMode = 1; // Mid Bot
                easyBotText.setText("Mid Bot");
                easyBotText.setVisible(true);
                local2pText.setVisible(false);
                tinyCircle0.setLayoutX(135);
                break;
            case 3: // PLAYER VS BOT - Hard
                currentGame.gameMode = "PLAYER VS BOT";
                currentBotMode = 2; // Hard Bot
                easyBotText.setText("Hard Bot");
                easyBotText.setVisible(true);
                local2pText.setVisible(false);
                tinyCircle0.setLayoutX(150);
                break;
        }
        updateBotBasedOnGameMode(); // Update the bot behavior if applicable
    }

    /**
     * Sets up the small arrow animations for cycling through the game modes. The left and right arrows
     * allow the user to cycle through the available game modes, and hover animations are added to the arrows.
     */
    private void setupSmallArrowAnimations() {
        // Small left arrow 0 behavior (cycles backward through all game modes)
        MenuSmallLeftArrow0.setOnMouseClicked(event -> {
            currentGameModeIndex = (currentGameModeIndex - 1 + 4) % 4; // Circular decrement for game modes
            updateGameMode();
        });

        // Small right arrow 0 behavior (cycles forward through all game modes)
        MenuSmallRightArrow0.setOnMouseClicked(event -> {
            currentGameModeIndex = (currentGameModeIndex + 1) % 4; // Circular increment for game modes
            updateGameMode();
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

    /**
     * Handles the mouse click event for switching the version. It simulates the ActionEvent
     * by calling the switchVersionAction method.
     * 
     * @param event The mouse event that triggered the method call.
     */
    private void handleMouseClickForSwitchVersion(MouseEvent event) {
        // Call the switchVersionAction, ignoring the MouseEvent but simulating the ActionEvent
        switchVersionAction(new ActionEvent());
    }

    /**
     * Sets up hover animations for a given arrow (scale up when mouse enters, scale down when mouse exits).
     * This method provides a smooth animation for the user interface to enhance interactivity.
     * 
     * @param arrow The arrow to which hover animations will be applied.
     */
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

    /**
     * Sets up the hover interaction for a ToggleButton. The button scales up when the mouse enters and
     * scales down when the mouse exits, providing a smooth interactive effect.
     * 
     * @param toggleButton The ToggleButton to which the hover interaction is applied.
     */
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

    /**
     * Sets up the hover interaction for a Button. The button scales up when the mouse enters and
     * scales down when the mouse exits, creating a smooth transition effect for the user.
     * 
     * @param button The Button to which the hover interaction is applied.
     */
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

    /**
     * Sets up the hover interaction for a Pane. The pane scales up when the mouse enters and
     * scales down when the mouse exits, offering a smooth, interactive transition effect.
     * 
     * @param menuObject The Pane to which the hover interaction is applied.
     */
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

    /**
     * Sets up the hover interaction for a TitledPane. The TitledPane scales up when the mouse enters and
     * scales down when the mouse exits, offering a visually appealing transition effect.
     * 
     * @param titledPane The TitledPane to which the hover interaction is applied.
     */
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

    /**
     * Sets up the hover interaction for a Circle (used as an arrow). The circle scales up when the mouse enters
     * and scales down when the mouse exits, providing an interactive user interface element.
     * 
     * @param menuObject The Circle (arrow) to which the hover interaction is applied.
     */
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

    /**
     * This method is triggered when the version switch button is clicked. It toggles the version 
     * of the current game between 9-Men Morris and 12-Men Morris, resets the game logic, 
     * and updates the UI to reflect the new game state.
     * 
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void switchVersionAction(ActionEvent event) {
        // Toggle the version in the current game instance
        boolean newVersion = !currentGame.isIn12MenMorrisVersion();
        currentGame.setIn12MenMorrisVersion(newVersion);

        resetGameUI("SWITCH VERSION");
    }

    private void resetGameUI(String message) {
        hidePopups();

        System.out.println(message);

        // Reset the game but keep the version field intact
        currentGame.resetGame();

        // Reset the UI to reflect the new game state
        updateBoardUI();
    }

    /**
     * This method simulates a series of moves in the game for testing purposes.
     * It makes a series of moves on the game board to test the game flow.
     * 
     * @param event The ActionEvent triggered by the button click.
     */
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

    /**
     * This method is triggered when the index visibility toggle button is clicked.
     * It toggles the visibility of the index labels on the UI.
     * 
     * @param event The ActionEvent triggered by the button click.
     */
    @FXML
    void showIndexAction(ActionEvent event) {
        toggleIndex();
    }

    /**
     * This method is triggered when the "New Game" button is clicked. It hides any win or draw popups,
     * resets the game logic, and updates the board UI for a fresh start.
     * 
     * @param event The ActionEvent triggered by the "New Game" button click.
     */
    @FXML
    void newGameClicked(ActionEvent event) {
        resetGameUI("NEW GAME");
    }

    /**
     * Toggles the visibility of the index labels based on the current visibility state of the index23 label.
     */
    private void toggleIndex() {
        Arrays.stream(indicies).forEach(text -> text.setVisible(!index23.isVisible()));
    }

    //SCALING

    /**
     * Sets the primary stage (window) of the application, used for resizing the window during zoom actions.
     * 
     * @param stage The primary stage to be set for the application.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * This method is triggered when the zoom-in button is clicked. It scales up the UI elements 
     * and increases the window size by a predefined scale factor.
     * 
     * @param event The ActionEvent triggered by the zoom-in button click.
     */
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

    /**
     * This method is triggered when the zoom-out button is clicked. It scales down the UI elements
     * and decreases the window size by a predefined scale factor.
     * 
     * @param event The ActionEvent triggered by the zoom-out button click.
     */
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
