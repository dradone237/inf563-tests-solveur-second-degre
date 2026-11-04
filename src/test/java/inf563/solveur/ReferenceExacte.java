package inf563.solveur;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Résultat attendu (RA) et tolérance (Tol) calculés pour N'IMPORTE QUELS coefficients finis, a ≠ 0.
 * Sert d'oracle au test pairwise, où les cas sont générés : on ne peut pas écrire les RA à la main.
 *
 * <ul>
 *   <li>Δ = b² − 4ac est calculé EXACTEMENT (produits BigDecimal sans arrondi) : le nombre de racines est sûr ;</li>
 *   <li>les racines viennent de la formule stable, en BigDecimal à 40 chiffres (pas d'élimination),
 *       puis sont arrondies au double le plus proche (±∞ si la vraie racine dépasse MAX_VALUE, 0 si elle est
 *       sous MIN_VALUE) ;</li>
 *   <li>Tol = 10·κ·ε pour chaque racine (RAPPORT § 2), avec κ = (|a|r² + |b||r| + |c|) / (|r|·√Δ).</li>
 * </ul>
 *
 * @param racines    RA : racines attendues, triées
 * @param tolerances Tol de chaque racine (erreur relative maximale acceptée)
 */
record ReferenceExacte(double[] racines, double[] tolerances) {

    private static final MathContext MC = new MathContext(40);
    private static final BigDecimal DEUX = BigDecimal.valueOf(2), QUATRE = BigDecimal.valueOf(4);
    private static final double EPS = Math.ulp(1.0);

    static ReferenceExacte de(double a, double b, double c) {
        BigDecimal A = new BigDecimal(a), B = new BigDecimal(b), C = new BigDecimal(c);  // valeurs exactes des double
        BigDecimal delta = B.multiply(B).subtract(QUATRE.multiply(A).multiply(C));        // exact
        if (delta.signum() < 0) {
            return new ReferenceExacte(new double[0], new double[0]);
        }
        if (delta.signum() == 0) {                     // racine double −b/2a : κ infini, on ne garde que ≈ √ε
            double r = B.negate().divide(DEUX.multiply(A), MC).doubleValue();
            return new ReferenceExacte(new double[]{r}, new double[]{10 * Math.sqrt(EPS)});
        }
        BigDecimal s = delta.sqrt(MC);
        BigDecimal q = (B.signum() >= 0 ? B.add(s) : B.subtract(s)).divide(DEUX.negate());  // −(b + signe(b)·√Δ)/2
        BigDecimal r1 = q.divide(A, MC), r2 = C.divide(q, MC);
        if (r1.compareTo(r2) > 0) {
            BigDecimal t = r1;
            r1 = r2;
            r2 = t;
        }
        return new ReferenceExacte(new double[]{r1.doubleValue(), r2.doubleValue()},
                new double[]{10 * kappa(A, B, C, r1, s) * EPS, 10 * kappa(A, B, C, r2, s) * EPS});
    }

    /** Conditionnement d'une racine simple r : κ = (|a|r² + |b||r| + |c|) / (|r|·|2ar + b|), et |2ar + b| = √Δ. */
    private static double kappa(BigDecimal a, BigDecimal b, BigDecimal c, BigDecimal r, BigDecimal racineDelta) {
        if (r.signum() == 0) {
            return 1;                                  // racine nulle (c = 0) : vérifiée en absolu, voir accepte()
        }
        BigDecimal num = a.abs().multiply(r.pow(2)).add(b.abs().multiply(r.abs())).add(c.abs());
        return num.divide(r.abs().multiply(racineDelta), MC).doubleValue();
    }

    /**
     * Verdict de l'oracle pour une racine : N = |y − RA| / |RA| ≤ Tol.
     * <ul>
     *   <li>RA = ±∞ (la vraie racine dépasse MAX_VALUE) : on exige y = RA ;</li>
     *   <li>sous MIN_NORMAL ≈ 2,2·10⁻³⁰⁸ (sous-normaux, diapo 93), le double n'a plus de précision relative :
     *       l'écart est rapporté à MIN_NORMAL au lieu de |RA|. Ce plancher est 10³⁰⁸ fois plus petit que le
     *       max(1, |RA|) de la diapo 70 : il ne masque aucune erreur sur une racine représentable.</li>
     * </ul>
     * Faux si y est NaN (toute comparaison avec NaN est fausse).
     */
    static boolean accepte(double y, double ra, double tol) {
        if (Double.isInfinite(ra)) {
            return y == ra;
        }
        return Math.abs(y - ra) <= tol * Math.max(Math.abs(ra), Double.MIN_NORMAL);
    }
}
