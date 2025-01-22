package groupXX.bot;

/**
* The EvaluationResult class represents the result of evaluating a game situation.
* It contains the evaluation score, the associated game situation, and details about the action taken,
* including the positions involved in the action.
*/
public class EvaluationResult {
    private GameSituation gameSituation;
    private double evaluation;
    private int action;
    public int position1;
    public int position2;

    /**
     * Constructs an EvaluationResult with the specified game situation, evaluation score, action, and positions.
     * 
     * @param gameSituation the game situation associated with this evaluation
     * @param evaluation the evaluation score for the game situation
     * @param action the action taken (e.g., placing or moving a piece)
     * @param position1 the first position involved in the action
     * @param position2 the second position involved in the action
     */
    public EvaluationResult(GameSituation gameSituation, double evaluation,
                            int action, int position1, int position2) {
        this.gameSituation = gameSituation;
        this.evaluation = evaluation;
        this.action = action;
        this.position1 = position1;
        this.position2 = position2;

    }

    /**
     * Returns the game situation associated with this evaluation result.
     * 
     * @return the game situation
     */
    public GameSituation getGameStatus() {
        return gameSituation;
    }

    /**
     * Returns the evaluation score for the game situation.
     * 
     * @return the evaluation score
     */
    public double getEvaluation() {
        return evaluation;
    }
}
