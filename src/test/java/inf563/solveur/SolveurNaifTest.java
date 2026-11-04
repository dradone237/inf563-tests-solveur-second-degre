package inf563.solveur;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

/**
 * Toute la suite contre la version naïve : certains tests DOIVENT échouer.
 * C'est le but du TP : remplir le tableau « l'oracle détecte / ne détecte pas ».
 *
 * <p>Exclue de « mvnw test » (tag naif) ; à lancer avec « mvnw test -Pnaif »
 * ou directement depuis le Test Explorer de VS Code.
 */
@Tag("naif")
@DisplayName("Solveur NAÏF (formule du lycée)")
class SolveurNaifTest extends SuiteOraclesSolveur {

    SolveurNaifTest() {
        super("inf563.solveur.SolveurNaif");       // un nom (texte), pas une dépendance à la classe
    }
}
