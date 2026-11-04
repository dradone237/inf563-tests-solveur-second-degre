package inf563.solveur;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Toute la suite contre N'IMPORTE QUELLE implémentation, choisie au lancement, sans recompiler les tests :
 * <pre>  mvnw test -Dsolveur.impl=nom.complet.DeLaClasse</pre>
 * La classe doit implémenter SolveurSecondDegre et avoir un constructeur public sans argument.
 *
 * <p>Sans la propriété, cette classe est désactivée (et exclue de « mvnw test » par le tag).
 */
@Tag("configurable")
@EnabledIfSystemProperty(named = ChargeurSolveur.PROPRIETE, matches = ".+")
@DisplayName("Solveur choisi au lancement (-Dsolveur.impl=...)")
class SolveurConfigurableTest extends SuiteOraclesSolveur {

    SolveurConfigurableTest() {
        super(System.getProperty(ChargeurSolveur.PROPRIETE));
    }
}
