package inf563.solveur;

/**
 * Fil rouge A : solveur de l'équation du second degré a·x² + b·x + c = 0.
 *
 * <p>Contrat commun aux deux versions :
 * <ul>
 *   <li>a = 0 : IllegalArgumentException (ce n'est plus une équation du second degré) ;</li>
 *   <li>Δ &lt; 0 : tableau vide (pas de racine réelle) ;</li>
 *   <li>Δ = 0 : une racine double ;</li>
 *   <li>Δ &gt; 0 : deux racines triées dans l'ordre croissant.</li>
 * </ul>
 */
public final class Solveur {

    private Solveur() {
    }

    /** Système sous test g utilisé par défaut : la version stable. */
    public static double[] racines(double a, double b, double c) {
        return stable(a, b, c);
    }

    /** Système sous test g, version naïve (pour montrer ce que les oracles détectent). */
    public static double[] racinesNaif(double a, double b, double c) {
        return naif(a, b, c);
    }

    /**
     * Formule du lycée : (-b ± √Δ) / 2a.
     * Mathématiquement juste, numériquement fragile : élimination catastrophique quand b² ≫ 4ac,
     * débordement de b² en +∞, ∞ − ∞ = NaN, absorption.
     */
    static double[] naif(double a, double b, double c) {
        if (a == 0) throw new IllegalArgumentException("a = 0");
        double d = b * b - 4 * a * c;
        if (d < 0) return new double[0];
        if (d == 0) return new double[]{-b / (2 * a)};
        double r1 = (-b - Math.sqrt(d)) / (2 * a);
        double r2 = (-b + Math.sqrt(d)) / (2 * a);
        return new double[]{Math.min(r1, r2), Math.max(r1, r2)};
    }

    /**
     * Formule stable + mise à l'échelle.
     * q additionne deux termes de même signe (plus de soustraction de nombres proches),
     * la seconde racine vient de Viète (r1·r2 = c/a). Multiplier par 2^k est exact en binaire :
     * les racines ne changent pas et b² ne déborde plus.
     */
    static double[] stable(double a, double b, double c) {
        if (a == 0) throw new IllegalArgumentException("a = 0");
        // mise à l'échelle exacte par une puissance de 2
        double m = Math.max(Math.abs(a), Math.max(Math.abs(b), Math.abs(c)));
        int e = Math.getExponent(m);
        a = Math.scalb(a, -e);
        b = Math.scalb(b, -e);
        c = Math.scalb(c, -e);
        double d = b * b - 4 * a * c;
        if (d < 0) return new double[0];
        if (d == 0) return new double[]{-b / (2 * a)};
        double q = -0.5 * (b + Math.copySign(Math.sqrt(d), b));
        double r1 = q / a, r2 = c / q;          // Viète : r1 * r2 = c / a
        return new double[]{Math.min(r1, r2), Math.max(r1, r2)};
    }
}
