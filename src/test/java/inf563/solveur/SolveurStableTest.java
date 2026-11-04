package inf563.solveur;

import org.junit.jupiter.api.DisplayName;

/** Toute la suite contre la version stable : tout doit être VERT. */
@DisplayName("Solveur STABLE (formule stable + mise à l'échelle)")
class SolveurStableTest extends SuiteOraclesSolveur {

    SolveurStableTest() {
        super("inf563.solveur.SolveurStable");     // un nom (texte), pas une dépendance à la classe
    }
}
