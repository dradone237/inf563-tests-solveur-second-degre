package inf563.solveur;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** On teste aussi les données de test : le tableau pairwise a-t-il bien la propriété annoncée ? */
@DisplayName("Générateur pairwise : 81 cas couvrent toutes les paires")
class PairwiseTest {

    static final int N = CategorieReel.values().length;    // 9

    @Test
    @DisplayName("81 cas pairwise contre 9³ = 729 cas exhaustifs")
    void taille() {
        assertEquals(9, N);
        assertEquals(81, Pairwise.tableauOrthogonal().size());
        assertEquals(729, Pairwise.exhaustif().size());
    }

    /** Pour deux paramètres p et q (0 = a, 1 = b, 2 = c), chacune des 81 paires apparaît exactement une fois. */
    @ParameterizedTest(name = "paires ({1}, {2}) : les 81 paires, chacune une fois")
    @CsvSource({"0, a, b, 1", "0, a, c, 2", "1, b, c, 2"})
    @DisplayName("Chaque paire de valeurs de deux paramètres est couverte")
    void toutesLesPaires(int p, String nomP, String nomQ, int q) {
        Map<String, Integer> occurrences = new HashMap<>();
        for (CategorieReel[] cas : Pairwise.tableauOrthogonal()) {
            occurrences.merge(cas[p] + " / " + cas[q], 1, Integer::sum);
        }
        assertEquals(N * N, occurrences.size(), "nombre de paires distinctes");
        occurrences.forEach((paire, n) -> assertEquals(1, n, "paire " + paire));
    }

    /** Each-choice (1-wise) en découle : chaque catégorie est prise 9 fois par chaque paramètre. */
    @Test
    @DisplayName("Chaque catégorie apparaît 9 fois pour a, pour b et pour c")
    void equilibre() {
        List<CategorieReel[]> cas = Pairwise.tableauOrthogonal();
        for (int p = 0; p < 3; p++) {
            for (CategorieReel valeur : CategorieReel.values()) {
                final int param = p;
                long n = cas.stream().filter(t -> t[param] == valeur).count();
                assertEquals(N, n, "paramètre " + p + ", " + valeur);
            }
        }
    }
}
