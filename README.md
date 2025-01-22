# Project 2-1

## Game Description
Nine Men's Morris is a classic two-player strategy board game that involves moving pieces on the board to form
"mills" — rows/columns/diagonals (depending on the game version and board) of three pieces, which allows a
player to remove an opponent's piece. The goal is to leave your opponent with three pieces or
block them such that they cannot make any more moves.

### Phases of the Game:
#### Phase 1: Placing Pieces
1. Players take turns placing one piece on any empty intersection on the board.
2. The goal in this phase is to form a "mill" (a row of three pieces along any straight line).
3. **Forming a Mill**: When a player forms a mill, they can remove one of the opponent's pieces from the board.
    - You can only remove pieces that are not in a mill unless no other pieces are available.
4. Continue this phase until all the pieces from both players are placed on the board.

#### Phase 2: Moving Pieces
1. After all pieces are placed, players take turns moving one piece along a line to an adjacent empty spot.
2. The objective remains to form mills and capture the opponent's pieces.
3. Each time a player forms a mill, they can remove one of the opponent's pieces from the board.
4. This phase continues until one player has only three pieces left.

#### Phase 3: Flying (When a Player Has 3 Pieces)
1. Once a player is down to three pieces, they can "fly" or "jump" their pieces to any empty intersection on the board.
2. This phase continues until one player either cannot move or has only two pieces left, ending the game.

### Game Endings:
1. **A player has only two pieces left**: They can no longer form mills and are defeated.
2. **A player cannot move**: If a player’s pieces are blocked, and they cannot make any legal move, they lose.

### Strategy Tips
- Form mills strategically to control the board and limit your opponent’s options.
- Break and reform mills by moving a piece in and out, allowing you to capture more pieces.
- During the "flying" phase, place your pieces to maximize your mobility and threaten mills in multiple locations.


### Types of Agents:
#### MeatBot
- The Human Player

#### RandomBot
- Uses a **Random Number Generator**
- **Placement phase:** Identifies empty positions Randomly selects one position to place the piece 
- **Selection phase:** Identifies movable pieces Randomly selects one to move 
- **Movement Phase:** Randomly chooses one of the valid moves for the selected piece 
- **Deletion phase:** Randomly chooses one of the valid opponents pieces to remove

#### EasyBot (Feedforward Neural Network)
- **Core Functionality:** Implements a simple AI using a neural network to evaluate and make decisions in the game. 
- **Placing Pieces:** Evaluates board positions to place pieces strategically. 
- **Moving Pieces:** Selects the best moves for optimal game progression. 
- **Deleting Opponent's Pieces:** Identifies the most impactful piece to remove. 
- **Neural Network:** Trained model loaded from a file that evaluates game states to maximize chances of winning.
- **Game Interaction:** Uses game state data and valid moves to simulate and evaluate outcomes.

#### MonteCarloBot
- Uses the **Monte Carlo Tree Search (MCTS)** algorithm
- **Selection:** choosing the most promising node within the game tree 
- **Expansion stage:** expand the game tree with new possible moves
- **Simulation stage:** clone the games state and perform random rollouts 
- **Backpropagation stage:** update the game tree with simulation outcomes


#### AlphaBetaBot
- **Alpha-Beta Pruning algorithm:**
   - **Minimax Basics:** checks all possible moves and selects the one that maximizes the bot’s chances of winning and minimizes the opponent’s chances
   - **Alpha-Beta Pruning:** “pruning” branches of the game tree that can’t possibly be better than the already explored options
   - **Evaluation function:** evaluate the state of the board, such as the number of pieces, whether a mill can be formed, whether there is a chance to   prevent the opponent from forming a mill, etc

#### HybridBot
- **Combines rule-based decision-making (αβ Bot) with AI-based approaches (Monte-Carlo Bot)** to balance structured and adaptive gameplay strategies
- **Placing Phase:** uses αβ Bot for precise, rule-based piece placement
- **Moving & Flying Phase:** uses Monte-Carlo Bot for adaptive decision-making in selecting and moving pieces
- **Deletion Phase:** prioritizes αβ Bot for reliable choices, with Monte-Carlo Bot as fallback for edge cases
- **Key-Feature:** switches between rule-based and AI-driven strategies to optimize performance, in both predictable early-game scenarios and complex mid/late-game situations



## Building and Running
**Note:** you need to have the following installed on your machine in order to run the game:
1. java development kit (JDK) 21
2. maven 3.6 or later
    * command-line tools, such as mvn - for running Maven commands
    * maven-compiler-plugin 3.8.1: This plugin compiles Java source code
    * javafx-maven-plugin 0.0.8: This plugin helps run JavaFX applications
3. javafx 21.0.1

Go to project directory and run:
```shell
mvn compiler:compile resources:resources javafx:run
```

**If you get error** `Error: Command execution failed. Cannot run program "java"`, then in IDE Setting's
`Build, Execution, Deployment -> Build Tools -> Maven -> Runner`:
`[X] Delegate IDE build/run actions to Maven` and add to `Environment variables:`: `JAVA_HOME=...path to to your java home`
