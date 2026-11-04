package inf563.solveur;

/**
 * La version stable TELLE QUE dans la diapo 54 (+ la règle « coefficient non fini »), conservée comme pièce
 * à conviction : le test pairwise la met en défaut sur 4 cas sur 81 (voir SolveurStable pour la correction).
 *
 * <p>Défaut : la mise à l'échelle divise les trois coefficients par la MÊME puissance de 2 (celle du plus
 * grand). Les petits coefficients passent alors sous MIN_VALUE : b² et 4ac valent 0, donc Δ = 0 au lieu de
 * Δ &gt; 0, et une seule racine est renvoyée au lieu de deux. Ex. : (1e-300, 2.5, 1e300) ou (3, 1e-300, -0.0).
 *
 * <p>Classe de test (src/test) : elle n'est pas livrée et n'entre pas dans les mesures JaCoCo / PIT.
 * À lancer avec : mvnw test "-Dsolveur.impl=inf563.solveur.SolveurStableDiapo54"
 */
public class SolveurStableDiapo54 implements SolveurSecondDegre {

    @Override
    public double[] racines(double a, double b, double c) {
        if (!Double.isFinite(a) || !Double.isFinite(b) || !Double.isFinite(c))
            throw new IllegalArgumentException("coefficient non fini (NaN ou infini)");
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
