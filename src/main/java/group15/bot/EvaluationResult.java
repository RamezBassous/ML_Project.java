package group15.bot;

public class EvaluationResult {
    private GameSituation gameSituation;
    private double evaluation;
    private int action;
    public int position1;
    public int position2;

    public EvaluationResult(GameSituation gameSituation, double evaluation,
                            int action, int position1, int position2) {
        this.gameSituation = gameSituation;
        this.evaluation = evaluation;
        this.action = action;
        this.position1 = position1;
        this.position2 = position2;

    }

    public GameSituation getGameStatus() {
        return gameSituation;
    }

    public double getEvaluation() {
        return evaluation;
    }
}
