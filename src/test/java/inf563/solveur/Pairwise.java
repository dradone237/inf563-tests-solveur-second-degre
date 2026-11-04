package inf563.solveur;

import java.util.ArrayList;
import java.util.List;

/**
 * Stratégie pairwise (2-wise, diapo 109) pour 3 paramètres (a, b, c) à 9 valeurs chacun.
 *
 * <p>Exhaustif : 9³ = 729 cas. Pairwise : chaque PAIRE de valeurs de deux paramètres apparaît au moins
 * une fois. Il faut au moins 9 × 9 = 81 cas (toutes les paires (a, b)), et 81 suffisent : c'est un
 * tableau orthogonal L81(9³), construit par un carré latin, comme les tableaux L4 et L9 des diapos 109-110.
 *
 * <p>Construction : cas (i, j) = (V[i], V[j], V[(i + j) mod 9]) pour i, j = 0..8.
 * <ul>
 *   <li>(a, b) : tous les couples (i, j) sont parcourus ;</li>
 *   <li>(a, c) : à i fixé, (i + j) mod 9 prend les 9 valeurs quand j varie ;</li>
 *   <li>(b, c) : à j fixé, (i + j) mod 9 prend les 9 valeurs quand i varie.</li>
 * </ul>
 * Chaque paire apparaît donc exactement une fois (vérifié par PairwiseTest).
 */
final class Pairwise {

    private Pairwise() {
    }

    /** Les 81 cas pairwise : chaque cas est un triplet {catégorie de a, catégorie de b, catégorie de c}. */
    static List<CategorieReel[]> tableauOrthogonal() {
        CategorieReel[] v = CategorieReel.values();
        int n = v.length;
        List<CategorieReel[]> cas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cas.add(new CategorieReel[]{v[i], v[j], v[(i + j) % n]});
            }
        }
        return cas;
    }

    /** Les 729 cas exhaustifs (produit cartésien), pour comparer. */
    static List<CategorieReel[]> exhaustif() {
        List<CategorieReel[]> cas = new ArrayList<>();
        for (CategorieReel a : CategorieReel.values()) {
            for (CategorieReel b : CategorieReel.values()) {
                for (CategorieReel c : CategorieReel.values()) {
                    cas.add(new CategorieReel[]{a, b, c});
                }
            }
        }
        return cas;
    }
}
