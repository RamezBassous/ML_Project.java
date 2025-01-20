package group15.bot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import group15.GameOri;

/**
 * The GameSituation class represents the state of the game during different phases, including placing, moving,
 * flying, and deleting pieces. It extends the `GameOri` class and includes additional logic for handling game actions,
 * tracking deleted pieces, and logging game events.
 */
public class GameSituation extends GameOri {

    // Array to store inputs for the neural network.
    // The size of 25 indicates the number of input nodes the network expects.
    // Each element in this array represents an input value for a corresponding input node in the network.
    public int[] inputForNet = new int[25];

    //deleted pieces count
    public int deletedCountBlue = 0;
    public int deletedCountRed = 0;

    private BufferedWriter writer; // Writer to output logs to log.txt

    /**
     * Constructor for the GameSituation class, calling the parent class constructor.
     * Initializes the game situation state.
     */
    public GameSituation() {
        super(); // Calls the parent class constructor

    }

    /**
     * Logs a message to a log file ("log.txt") for debugging or record-keeping purposes.
     * 
     * @param message the message to log
     */
    private void log(String message) {
        try {
            // Initialize the BufferedWriter to write to log.txt in the current directory
            writer = new BufferedWriter(new FileWriter("log.txt", true)); // 'true' for appending to the file
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.write(message); // Write message to the file
            writer.newLine(); // Add a new line after each message
            writer.flush(); // Ensure the message is written immediately
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy constructor for the GameSituation class, creating a new instance from an existing `GameOri` instance.
     * 
     * @param gameSituation the GameOri instance to copy
     */
    public GameSituation(GameOri gameSituation) {
        super(); // Calling the parent class constructor
        this.phase = gameSituation.phase;
        this.currentPlayer = gameSituation.currentPlayer;
        this.selectedPiece = gameSituation.selectedPiece;
        this.moveCountBlue = gameSituation.moveCountBlue;
        this.moveCountRed = gameSituation.moveCountRed;
     /* this.deletedCountBlue = gameSituation.deletedCountBlue;
        this.deletedCountRed = gameSituation.deletedCountRed; */
        this.moveWithoutCapture = gameSituation.moveWithoutCapture;
        this.boardPositions = Arrays.copyOf(gameSituation.boardPositions, gameSituation.boardPositions.length);
        this.listener = gameSituation.listener;

    }

    /**
     * Returns a list of valid moves and flying positions for a given piece.
     * 
     * @param position the position of the piece
     * @return a list of valid moves
     */
    public List<Integer> getValidFlyAndMoves(int position){
        return super.getValidMoves(position);
    }

/*    public boolean isFormsMill(int position, int player) {
        return super.formsMill(position,player);
    }*/

    /**
     * Executes a placement action where the current player places a piece on the board.
     * 
     * @param position the position where the piece should be placed
     * @return true if the action was successful, false otherwise
     */
    public boolean doActionPlace(int position) {

        if (position < 0 || position >= 24) {
            //System.out.println("Invalid position: " + position);  // Print for invalid position
            return false; // Invalid move
        }

        if (super.phase == 0) {
            // Placing phase logic
            if (boardPositions[position] != 0) {
                //System.out.println("Position " + position + " is already occupied.");  // Print for occupied position
                return false; // Position is already occupied
            }
            boardPositions[position] = currentPlayer;

            //System.out.println("Player " + currentPlayer + " placed piece at position " + position);

            // Increment move count during the placing phase
            if (currentPlayer == 1) {
                moveCountBlue++;
            } else {
                moveCountRed++;
            }

            // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
            int requiredPieces = getBoardGraph().getRequiredPieces();

            if (moveCountBlue+ deletedCountBlue == requiredPieces &&
                    moveCountRed + deletedCountRed == requiredPieces) {
                phase = 1; // Transition to the moving phase
                //System.out.println("Transitioning to the moving phase!");
                //log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Transitioning to the moving phase!");
            }

            // Check if a mill is formed
            if (formsMill(position, currentPlayer)) {
                //System.out.println("Mill formed by Player " + currentPlayer + " at position " + position);
                phase = -currentPlayer; // Enter delete phase for right player
            }

            switchPlayer();
            return true;

        }
        return false;
    }

    /**
     * Executes a flying or moving action for the current player.
     * 
     * @param position the current position of the piece
     * @param positionTo the target position to move or fly to
     * @return true if the action was successful, false otherwise
     */
    public boolean doActionFlyMove(int position,int positionTo) {

        // Moving or flying phase logic

        // Check if current player needs to enter the flying phase
        if (getPieceCount(currentPlayer) <= 3) {
            phase = 2; // Enter the flying phase
            //System.out.println("Player " + currentPlayer + " is in the flying phase!");
        }

        if (selectedPiece == -1) {
            // No piece selected, select a piece
            if (boardPositions[position] != currentPlayer) {
                //System.out.println("Invalid selection. Player " + currentPlayer + " cannot select opponent's piece at position " + position);
                return false;
            }

            selectedPiece = position;
            //System.out.println("Player " + currentPlayer + " selected piece at position " + position);
            return true;

        } else {
            // If the player selects the same player's piece again, allow reselection
/*            if (boardPositions[position] == currentPlayer) {
                selectedPiece = position;
                //System.out.println("Player " + currentPlayer + " reselected piece at position " + position);
                return true;
            }*/

            selectedPiece = position;

            // Move the selected piece to a new position
            List<Integer> validMoves = getValidMoves(selectedPiece);


            if (!validMoves.contains(positionTo)) {
                //System.out.println("Invalid move. Position " + positionTo + " is not a valid move for piece at position " + selectedPiece);
                return false;
            }else{
                System.out.println("Valid moves for piece " + selectedPiece + ": " + validMoves);
            }

            // Move piece logic


            System.out.println("Player " + currentPlayer + " moved piece from " + selectedPiece + " to " + positionTo);
            boardPositions[selectedPiece] = 0;
            boardPositions[positionTo] = currentPlayer;

            // Check if a mill is formed
            if (formsMill(positionTo, currentPlayer)) {
                //System.out.println("Mill formed by Player " + currentPlayer + " at position " + position);

                // Check if Game is won by the move
                if (getPieceCount(1) <= 3 && currentPlayer == 2) {
                    if (listener != null) listener.onGameWon("Red"); // Notify the controller

                } else if (getPieceCount(2) <= 3 && currentPlayer == 1) {
                    if (listener != null) listener.onGameWon("Blue"); // Notify the controller
                }
                checkDrawConditionsPlus();

                phase = -currentPlayer; // Enter delete phase for right player
            }else{
                incrementMoveWithoutCapture();
            }


            selectedPiece = -1;  // Reset selection after move

            switchPlayer();

            // Only check win/loss and draw conditions in the moving and flying phases
            checkWinLossPlus();
            checkDrawConditionsPlus();
            return true;
        }
    }

    /**
     * Executes a deletion action where the current player removes an opponent's piece.
     * 
     * @param position the position of the opponent's piece to be deleted
     * @return true if the action was successful, false otherwise
     */
    public boolean doActionDelete(int position) {

        // Delete phase logic
        if (phase == -2) {
            // Delete phase logic
            if (boardPositions[position] == 1) {
                boardPositions[position] = 0;

                // Increment move count during the Delete phase
                moveCountBlue--;
                deletedCountBlue++;

                //System.out.println("Deleted piece " + position + " by Player 1");

                phase = 0;

                // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
                int requiredPieces = getBoardGraph().getRequiredPieces();
                if (moveCountBlue + deletedCountBlue == requiredPieces && moveCountRed + deletedCountRed == requiredPieces) {
                    phase = 1; // Transition to the moving phase
                    //System.out.println("Transitioning to the moving phase!");
                }

                return true;
            }

            return false;

        } else if (phase == -1) {
            // Delete phase logic
            if (boardPositions[position] == 2) {
                boardPositions[position] = 0;
                //System.out.println("Deleted piece " + position + " by Player 2");

                moveCountRed--;
                deletedCountRed++;
                phase = 0;

                // Check if both players have placed all pieces (9 for 9 Men’s Morris, 12 for 12 Men’s Morris)
                int requiredPieces = getBoardGraph().getRequiredPieces();
                if (moveCountBlue + deletedCountBlue == requiredPieces &&
                        moveCountRed + deletedCountRed == requiredPieces) {
                    phase = 1; // Transition to the moving phase
                    //System.out.println("Transitioning to the moving phase!");
                }
                return true;
            }

            return false;
        }

        return false;
    }


    /**
     * Checks if the current game state satisfies any win/loss conditions.
     * 
     * @return true if a win/loss condition is met, false otherwise
     */
    public boolean checkWinLossPlus() {

        if (this.phase != 1 && this.phase != 2) {
            return false;
        }

        int bluePieceCount = getPieceCount(1); // Blue player is 1
        int redPieceCount = getPieceCount(2);  // Red player is 2

        // Condition 1: A player has less than 3 pieces (loss condition)
        int requiredPieces = getBoardGraph().getRequiredPieces();
        //add moveCountBlue/moveCountRed == requiredPieces check
        if (moveCountBlue + deletedCountBlue == requiredPieces && bluePieceCount < 3) {
            System.out.println("Red wins! Blue has less than 3 pieces.");
            if (listener != null) listener.onGameWon("Red"); // Notify the controller
            return true;
        } else if (moveCountRed + deletedCountRed == requiredPieces && redPieceCount < 3) {
            System.out.println("Blue wins! Red has less than 3 pieces.");
            if (listener != null) listener.onGameWon("Blue"); // Notify the controller
            return true;
        }

        // Condition 2: A player cannot make any valid moves
        if (moveCountBlue + deletedCountBlue == requiredPieces) {
            boolean blueHasValidMoves = super.hasValidMoves(1); // Check if Blue can move
            if (!blueHasValidMoves) {
                System.out.println("Red wins! Blue cannot make any valid moves.");
                if (listener != null) listener.onGameWon("Red"); // Notify the controller
                return true;
            }
        } else if (moveCountRed + deletedCountRed == requiredPieces) {
            boolean redHasValidMoves = super.hasValidMoves(2);  // Check if Red can move
            if (!redHasValidMoves) {
                System.out.println("Blue wins! Red cannot make any valid moves.");
                if (listener != null) listener.onGameWon("Blue"); // Notify the controller
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current game state satisfies any draw conditions.
     * 
     * @return true if a draw condition is met, false otherwise
     */
    public boolean checkDrawConditionsPlus() {

        if (this.phase != 1 && this.phase != 2) {
            return false;
        }

        // 1. Threefold Repetition
        String currentBoard = Arrays.toString(boardPositions);
        boardHistory.put(currentBoard, boardHistory.getOrDefault(currentBoard, 0) + 1);
        if (boardHistory.get(currentBoard) >= 3) {
            System.out.println("Draw by Threefold Repetition!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }

        // 2. Insufficient Material
        int bluePieces = getPieceCount(1);
        int redPieces = getPieceCount(2);
        System.out.println("BLUE "+ bluePieces + "RED " + redPieces);

        int requiredPieces = getBoardGraph().getRequiredPieces();
        //add moveCountBlue/moveCountRed == requiredPieces check
        if (moveCountBlue + deletedCountBlue == requiredPieces &&  bluePieces == 3 &&
                moveCountRed +deletedCountRed == requiredPieces &&  redPieces == 3) {
            System.out.println("Draw by Insufficient Material!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }

        // 3. 50-Move Rule (optional)
        //System.out.println("moveWithoutCapture: " + super.moveWithoutCapture);
        if (super.moveWithoutCapture >= 50) {
            System.out.println("Draw by 50-Move Rule!");
            //if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }

        // 4. No Legal Moves
        boolean blueHasValidMoves = hasValidMoves(1);
        boolean redHasValidMoves = hasValidMoves(2);
        if (!blueHasValidMoves && !redHasValidMoves) {
            System.out.println("Draw by No Legal Moves!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }

        // 5. Agreement by Both Players
        if (drawAgreed) {
            System.out.println("Draw by Agreement of Both Players!");
            if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            return true;
        }

        // 6. Repetition in Endgame with Limited Pieces
        if (bluePieces <= 3 && redPieces <= 3) {
            if (boardHistory.getOrDefault(currentBoard, 0) >= 3) {
                System.out.println("Draw by Repetition in Endgame with Limited Pieces!");
                if (listener != null) listener.onGameDraw();  // Notify the controller on a draw
            }
        }

        return false;
    }

}
