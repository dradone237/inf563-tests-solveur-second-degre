package inf563.solveur;

import org.junit.jupiter.api.DisplayName;

/** Toute la suite contre la version stable : tout doit être VERT. */
@DisplayName("Solveur STABLE (formule stable + mise à l'échelle)")
class SolveurStableTest extends SuiteOraclesSolveur {

    @Override
    double[] g(double a, double b, double c) {
        return Solveur.racines(a, b, c);
    }
}
