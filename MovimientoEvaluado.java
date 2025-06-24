public class MovimientoEvaluado {
    private final String movimiento;
    private final double score;

    public MovimientoEvaluado(String movimiento, double score) {
        this.movimiento = movimiento;
        this.score = score;
    }

    public String getMovimiento() {
        return movimiento;
    }

    public double getScore() {
        return score;
    }
}
