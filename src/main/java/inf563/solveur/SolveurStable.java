package inf563.solveur;

/**
 * Version stable : formule stable + mise à l'échelle par puissances de 2 (diapo 54), corrigée.
 *
 * <p>q additionne deux termes de même signe (plus de soustraction de nombres proches),
 * la seconde racine vient de Viète (r1·r2 = c/a). Multiplier par 2^k est exact en binaire.
 *
 * <p>Correction trouvée grâce au test pairwise : la diapo 54 divise les trois coefficients par la même
 * puissance de 2 ; les petits coefficients passent alors sous MIN_VALUE et Δ devient 0 à tort
 * (ex. (1e-300, 2.5, 1e300) : 1 racine au lieu de 2). Ici on écrit chaque coefficient x = m·2^e
 * (mantisse m dans [1, 2[) et on calcule Δ = b² − 4ac = 2^e·d en mettant à l'échelle b² et 4ac
 * SÉPARÉMENT : ni l'un ni l'autre ne peut déborder (+∞) ou s'annuler (sous MIN_VALUE).
 * Les racines sont ensuite obtenues par une seule division chacune (q/a et c/q) : si la vraie racine
 * dépasse MAX_VALUE ou passe sous MIN_VALUE, l'arrondi donne ±∞ ou 0, comme il se doit.
 */
public class SolveurStable implements SolveurSecondDegre {

    @Override
    public double[] racines(double a, double b, double c) {
        if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c))
            throw new IllegalArgumentException("coefficient non fini (NaN ou infini)");
        if (a == 0) throw new IllegalArgumentException("a = 0");
        if (c == 0) {                                   // x·(a·x + b) = 0 : racines 0 et −b/a
            if (b == 0) return new double[]{0.0};       // Δ = 0 : racine double 0
            double r = -b / a;
            return new double[]{Math.min(r, 0.0), Math.max(r, 0.0)};
        }
        // x = m·2^e, mantisse m dans [1, 2[ : multiplier par une puissance de 2 est exact
        int ea = Math.getExponent(a), eb = Math.getExponent(b), ec = Math.getExponent(c);
        double ma = Math.scalb(a, -ea), mb = Math.scalb(b, -eb), mc = Math.scalb(c, -ec);
        // Δ = 2^e·d : e = exposant du plus grand des deux termes b² et 4ac, rendu pair pour la racine carrée
        // (b = 0 : getExponent(0) = −1023, donc 2·eb ≤ ea + ec et le max choisit bien 4ac)
        int e = Math.max(2 * eb, ea + ec);
        e += e & 1;                                     // pair supérieur (le pair inférieur marcherait aussi)
        double d = Math.scalb(mb * mb, 2 * eb - e) - Math.scalb(4 * ma * mc, ea + ec - e);
        if (d < 0) return new double[0];
        if (d == 0) return new double[]{-(b / a) / 2};
        double s = Math.scalb(Math.sqrt(d), e / 2);     // √Δ = √d · 2^(e/2)
        double q = -0.5 * (b + Math.copySign(s, b));
        double r1 = q / a, r2 = c / q;                  // Viète : r1 * r2 = c / a
        return new double[]{Math.min(r1, r2), Math.max(r1, r2)};
    }
}
