package inf563.solveur;

/**
 * Version naïve : formule du lycée (-b ± √Δ) / 2a (diapo 51).
 * Mathématiquement juste, numériquement fragile : élimination catastrophique quand b² ≫ 4ac,
 * débordement de b² en +∞, ∞ − ∞ = NaN, absorption.
 * Sert à montrer ce que les oracles détectent.
 */
public class SolveurNaif implements SolveurSecondDegre {

    @Override
    public double[] racines(double a, double b, double c) {
        if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c))
            throw new IllegalArgumentException("coefficient non fini (NaN ou infini)");
        if (a == 0) throw new IllegalArgumentException("a = 0");
        double d = b * b - 4 * a * c;
        if (d < 0) return new double[0];
        if (d == 0) return new double[]{-b / (2 * a)};
        double r1 = (-b - Math.sqrt(d)) / (2 * a);
        double r2 = (-b + Math.sqrt(d)) / (2 * a);
        return new double[]{Math.min(r1, r2), Math.max(r1, r2)};
    }
}
