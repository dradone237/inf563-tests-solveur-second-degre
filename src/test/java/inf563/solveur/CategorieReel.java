package inf563.solveur;

/**
 * Les neuf catégories de valeurs d'un réel (double), d'après la diapo 93 (« classes usuelles : réels »),
 * avec un représentant par catégorie. Chaque coefficient a, b, c prend ses valeurs dans ces catégories.
 *
 * <p>Classes valides et invalides : NaN est invalide pour a, b et c ; zéro est invalide pour a seulement
 * (a = 0 : ce n'est plus une équation du second degré). Les autres sont valides partout.
 */
enum CategorieReel {

    NEGATIF("négatif", -3.7),
    POSITIF("positif", 2.5),
    ZERO_SIGNE("zéro signé", -0.0),                         // le piège : -0.0 == 0.0 est vrai
    TRES_PETIT("très petit", 1e-300),
    TRES_GRAND("très grand", 1e300),
    SPECIALE("valeur spéciale", Double.NaN),                // ±∞ : voir le test « classes invalides »
    NON_REPRESENTABLE("non représentable", 0.1),            // 0.1 n'existe pas en binaire
    ENTIER_AU_DELA_2_53("entier > 2^53", (double) 9_007_199_254_740_993L), // devient 2^53
    VALEUR_ENTIERE("valeur entière", 3.0);

    final String libelle;
    final double valeur;

    CategorieReel(String libelle, double valeur) {
        this.libelle = libelle;
        this.valeur = valeur;
    }

    @Override
    public String toString() {
        return libelle + " (" + valeur + ")";
    }
}
