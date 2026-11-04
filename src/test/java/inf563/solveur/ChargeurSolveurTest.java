package inf563.solveur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Le chargement par réflexion lui-même : ce qu'il accepte, ce qu'il refuse et avec quel message. */
@DisplayName("Chargement de l'implémentation par réflexion")
class ChargeurSolveurTest {

    /** Respecte l'interface, mais n'a pas de constructeur public sans argument. */
    static class SansConstructeurVide implements SolveurSecondDegre {
        SansConstructeurVide(int inutile) {
        }

        @Override
        public double[] racines(double a, double b, double c) {
            return new double[0];
        }
    }

    /** Respecte l'interface, mais son constructeur lève une exception. */
    public static class ConstructeurQuiEchoue implements SolveurSecondDegre {
        public ConstructeurQuiEchoue() {
            throw new IllegalStateException("panne au démarrage");
        }

        @Override
        public double[] racines(double a, double b, double c) {
            return new double[0];
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {"inf563.solveur.SolveurStable", "inf563.solveur.SolveurNaif"})
    @DisplayName("Un nom de classe valide donne une instance utilisable à travers l'interface")
    void chargeParLeNom(String nom) {
        SolveurSecondDegre solveur = ChargeurSolveur.charger(nom);
        assertEquals(nom, solveur.getClass().getName());
        assertArrayEquals(new double[]{1.0, 2.0}, solveur.racines(1, -3, 2));
    }

    @Test
    @DisplayName("Chaque appel crée une nouvelle instance")
    void instanceNeuve() {
        assertNotSame(ChargeurSolveur.charger("inf563.solveur.SolveurStable"),
                ChargeurSolveur.charger("inf563.solveur.SolveurStable"));
    }

    @ParameterizedTest(name = "[{index}] {0} → refusée ({1})")
    @CsvSource({
            "inf563.solveur.Inexistant,                                introuvable",
            "java.lang.String,                                         implémente pas",
            "inf563.solveur.SolveurSecondDegre,                        interface ou une classe abstraite",
            "inf563.solveur.ChargeurSolveurTest$SansConstructeurVide,  constructeur public sans argument",
            "inf563.solveur.ChargeurSolveurTest$ConstructeurQuiEchoue, a levé une exception",
    })
    @DisplayName("Une classe inutilisable est refusée avec un message explicite")
    void refuseClasseInutilisable(String nom, String motif) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> ChargeurSolveur.charger(nom));
        assertThat(e).hasMessageContaining(motif);
    }

    @ParameterizedTest(name = "[{index}] nom = \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("Un nom absent est refusé")
    void refuseNomAbsent(String nom) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> ChargeurSolveur.charger(nom));
        assertThat(e).hasMessageContaining("aucun nom de classe");
    }
}
