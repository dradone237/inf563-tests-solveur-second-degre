package inf563.solveur;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Chargement et instanciation de l'implémentation à tester par réflexion (Java Reflection API).
 *
 * <p>Les tests ne connaissent que l'interface {@link SolveurSecondDegre} et un NOM de classe
 * (une chaîne) : aucune classe concrète n'est importée ni instanciée avec {@code new}.
 * On peut donc tester une autre implémentation sans recompiler ni modifier les tests.
 */
final class ChargeurSolveur {

    /** Propriété système qui choisit l'implémentation au lancement : -Dsolveur.impl=nom.complet.DeLaClasse */
    static final String PROPRIETE = "solveur.impl";

    private ChargeurSolveur() {
    }

    /**
     * @param nomClasse nom complet de la classe, par ex. "inf563.solveur.SolveurStable"
     * @return une nouvelle instance de cette classe, vue uniquement à travers l'interface
     * @throws IllegalArgumentException si la classe est introuvable ou inutilisable (message explicite)
     */
    static SolveurSecondDegre charger(String nomClasse) {
        if (nomClasse == null || nomClasse.isBlank()) {
            throw new IllegalArgumentException("aucun nom de classe fourni (propriété -D" + PROPRIETE + " ?)");
        }
        // 1. Chargement : le nom (texte) devient un objet Class, sans dépendance à la compilation
        Class<?> classe;
        try {
            classe = Class.forName(nomClasse.strip());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("classe introuvable : " + nomClasse, e);
        }
        // 2. Vérification du contrat : la classe doit implémenter l'interface et être instanciable
        if (!SolveurSecondDegre.class.isAssignableFrom(classe)) {
            throw new IllegalArgumentException(classe.getName() + " n'implémente pas " + SolveurSecondDegre.class.getName());
        }
        if (classe.isInterface() || Modifier.isAbstract(classe.getModifiers())) {
            throw new IllegalArgumentException(classe.getName() + " est une interface ou une classe abstraite");
        }
        // 3. Instanciation par le constructeur public sans argument
        try {
            return classe.asSubclass(SolveurSecondDegre.class).getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(classe.getName() + " n'a pas de constructeur public sans argument", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("le constructeur de " + classe.getName() + " a levé une exception", e.getCause());
        } catch (ReflectiveOperationException e) {     // InstantiationException, IllegalAccessException
            throw new IllegalArgumentException("impossible d'instancier " + classe.getName(), e);
        }
    }
}
