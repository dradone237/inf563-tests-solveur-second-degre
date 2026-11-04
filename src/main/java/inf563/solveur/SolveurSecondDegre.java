package inf563.solveur;

/**
 * Fil rouge A : contrat d'un solveur de l'équation du second degré a·x² + b·x + c = 0.
 *
 * <p>Contrat commun à toutes les implémentations :
 * <ul>
 *   <li>a, b ou c vaut NaN, +∞ ou −∞ : IllegalArgumentException (coefficient non fini) ;</li>
 *   <li>a = 0 : IllegalArgumentException (ce n'est plus une équation du second degré) ;</li>
 *   <li>Δ &lt; 0 : tableau vide (pas de racine réelle) ;</li>
 *   <li>Δ = 0 : une racine double ;</li>
 *   <li>Δ &gt; 0 : deux racines triées dans l'ordre croissant.</li>
 * </ul>
 *
 * <p>Les tests ne dépendent que de cette interface : l'implémentation à tester est chargée
 * par réflexion à partir de son nom (voir ChargeurSolveur dans src/test). Une implémentation
 * doit donc être une classe publique avec un constructeur public sans argument.
 */
public interface SolveurSecondDegre {

    /** Racines réelles triées de a·x² + b·x + c = 0. */
    double[] racines(double a, double b, double c);
}
