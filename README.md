# Solveur du second degré : tests JUnit 5 (INF563, chapitres 1 à 3)

Fil rouge A du cours de E. Fomekong : on teste `Solveur.racines(a, b, c)` (racines de a·x² + b·x + c = 0)
avec **JUnit 5** (tests paramétrés) et **AssertJ**, en suivant le modèle du chapitre 3 :

```
DE (a, b, c)  →  g = Solveur  →  y  →  oracle O  →  verdict PASS / FAIL / ERROR / INCONCLUSIF
oracle par référence :  N(y, RA) ≤ Tol      avec  N = |y − RA| / |RA|  et  Tol = c·κ·ε
```

## 1. Contenu du projet

```
Projet_test_polynome_chapitre3/
├── pom.xml                         Java 21, JUnit 5.13, AssertJ 3.27
├── mvnw.cmd, mvnw, .mvn/           Maven Wrapper : pas besoin d'installer Maven
├── .vscode/                        réglages VS Code
├── RAPPORT.md                      rapport d'une page (tolérances + tableau détecte / ne détecte pas)
├── doc/                            Fonctionnement_detaille_du_projet.pdf : explication complète du projet (56 p.)
└── src/
    ├── main/java/inf563/solveur/
    │   ├── Solveur.java            naif() = formule du lycée ; stable() = formule stable + mise à l'échelle
    │   └── Demo.java               programme : tableau naïf / stable / attendu (TP séance 2)
    └── test/java/inf563/solveur/
        ├── SuiteOraclesSolveur.java  LA suite de tests (écrite une seule fois)
        ├── SolveurStableTest.java    exécute la suite contre la version stable : tout VERT
        └── SolveurNaifTest.java      exécute la suite contre la version naïve : 18 ROUGES attendus
```

Ce que contient `SuiteOraclesSolveur` (84 tests) :

| Bloc | Test | Ce qu'il illustre (chapitre 3) |
|---|---|---|
| Oracle par référence | `parReference` (8 cas DE / RA / Tol) | N = erreur relative, Tol = c·κ·ε justifiée |
| Contrat | `aNul`, `discriminantNegatif` | exception attendue = PASS ; Δ < 0 → aucune racine |
| Propriété | `residu` | erreur inverse : \|a·r² + b·r + c\| ≤ 8·ε·(\|a\|r² + \|b·r\| + \|c\|) |
| Propriété | `viete` | r₁ + r₂ = −b/a et r₁·r₂ = c/a |
| Métamorphique | `echelle` | racines(1024a, 1024b, 1024c) = racines(a, b, c), égalité stricte |
| Métamorphique | `oppose` | racines(a, −b, c) = −racines(a, b, c) |
| Construction | `construction` | on choisit r₁, r₂ puis on fabrique (x − r₁)(x − r₂) |
| Combinaisons | `combinaisons` (`@MethodSource`) | un tableau de valeurs **par paramètre** (a, b, c) : JUnit exécute les 3 × 4 × 3 = 36 combinaisons ; oracle sans RA (signe exact de Δ + résidu) |
| Tests dynamiques | `constructionGeneree` (`@TestFactory`) | 20 cas **générés à l'exécution** (graine fixe), oracle par construction |
| Différentiel (bonus) | `differentiel` | référence BigDecimal à 50 chiffres |

## 2. Prérequis (déjà vérifiés sur ce PC)

| Outil | État |
|---|---|
| JDK 21 (Temurin) | installé : `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot`, `JAVA_HOME` défini |
| Maven | **pas nécessaire** : le wrapper `mvnw.cmd` télécharge Maven 3.9.11 tout seul (déjà fait une fois) |
| VS Code + « Extension Pack for Java » | installé (`vscjava.vscode-java-pack`) |
| Internet | seulement au premier lancement (dépendances JUnit / AssertJ, déjà en cache dans `~\.m2`) |

Vérification rapide dans un terminal :

```powershell
java -version        # doit afficher : openjdk version "21..."
```

## 3. Lancer les tests dans VS Code, étape par étape

1. **Ouvrir le dossier** : VS Code → *Fichier → Ouvrir le dossier…* → choisir `Projet_test_polynome_chapitre3`.
   (ou dans un terminal : `code "C:\Users\SmartCamp PC1\Desktop\ProjetM2\Projet_test_polynome_chapitre3"`)
2. Si VS Code demande *« Faites-vous confiance aux auteurs ? »* → **Oui**.
3. **Attendre que Java soit prêt** : en bas à droite, l'icône `{ }` / « Java » doit passer à **Ready** (👍).
   La première fois, VS Code importe le projet Maven : 30 s à 2 min.
4. **Ouvrir le Test Explorer** : cliquer sur l'icône **fiole (Testing)** dans la barre de gauche.
   Vous voyez : `solveur-second-degre → inf563.solveur → Solveur STABLE… / Solveur NAÏF…`
   (les tests sont écrits une seule fois dans `SuiteOraclesSolveur` et *hérités* par ces deux classes ;
   si une classe n'affiche pas encore ses tests, lancez-la avec ▶ : ils apparaissent après l'exécution).
5. **Version stable** : survoler *Solveur STABLE* → cliquer ▶ **Run Test**.
   Résultat attendu : **84 tests verts** ✔.
6. **Version naïve** : cliquer ▶ sur *Solveur NAÏF*.
   Résultat attendu : **66 verts, 18 rouges** ✘. C'est **voulu** : chaque rouge est un oracle qui *détecte* la formule naïve.
   Cliquer sur un test rouge pour lire le verdict, par exemple :
   `racine 1 : y = -7.450580596923828E-9, RA = -1.0E-8, N = 0.2549 > Tol = 5.0E-15`
   → la formule naïve se trompe de 25 % sur la petite racine de x² + 10⁸x + 1 (élimination catastrophique).
7. **Déplier un test paramétré** (ex. `Oracle par référence`) : chaque ligne du `@CsvSource` est un cas
   `[1] cas nominal`, `[2] élimination catastrophique`, … qu'on peut relancer seul avec ▶.
8. **Déboguer un cas** : mettre un point d'arrêt (clic dans la marge) dans `Solveur.naif`, puis clic droit sur
   le cas rouge → **Debug Test**. Observer `d`, `r1`, `r2` dans le panneau *Variables*.
9. **Lancer le programme** : ouvrir `Demo.java` → cliquer **Run** au-dessus de `main`.
   Le terminal affiche le tableau naïf / stable / attendu (le même que la diapo 52 du cours).
   Attention : `Demo` **n'est pas un test**, il ne fait qu'afficher. Le verdict PASS / FAIL vient uniquement des
   assertions JUnit (l'enseignant l'a rappelé en cours : afficher des résultats, ce n'est pas tester).

## 4. Lancer les tests en ligne de commande (terminal VS Code : *Ctrl + ù*)

```powershell
.\mvnw.cmd test            # version stable  → Tests run: 84, Failures: 0   → BUILD SUCCESS
.\mvnw.cmd test -Pnaif     # version naïve   → Tests run: 84, Failures: 18  → BUILD FAILURE (attendu)
```

Les rapports détaillés sont dans `target\surefire-reports\`.

Correspondance des **quatre verdicts** du cours avec JUnit :

| Verdict (cours) | Dans JUnit / Maven |
|---|---|
| PASS | test vert ✔ (y compris une exception attendue avec `assertThrows`) |
| FAIL | `Failures` : une assertion (l'oracle) a rejeté la sortie |
| ERROR | `Errors` : exception imprévue pendant le test |
| INCONCLUSIF | `Skipped` : hypothèse non remplie (`Assumptions.assumeTrue`) |

## 5. Exercices pour s'approprier le modèle (5 min chacun)

**a) Ajouter un cas = ajouter une ligne.** Dans `SuiteOraclesSolveur.parReference`, ajouter à la fin du `textBlock` :

```
  1,      1e5,    1,                  -99999.99999,       -1.0000000001e-5,   5e-15, b = 1e5
  1,      1e6,    1,                  -999999.999999,     -1.000000000001e-6, 5e-15, b = 1e6
```

Relancer : la version stable reste verte, la version naïve échoue sur les deux
(erreur relative 3,4·10⁻⁷ et 7,6·10⁻⁶).

**b) Une tolérance trop large cache les bugs.** Remplacer `5e-15` par `1e-6` (la Tol de la diapo 69) sur ces deux lignes :
la version naïve **passe** pour b = 10⁵ (3,4·10⁻⁷ ≤ 10⁻⁶) et n'échoue plus que pour b = 10⁶.
C'est la leçon « Tol trop large : le test laisse passer les bugs ». Remettre `5e-15` ensuite.

**c) Une tolérance absolue masque 25 % d'erreur (diapo 70).** Temporairement :
dans `erreurRelative`, remplacer `/ Math.abs(ra)` par `/ Math.max(1, Math.abs(ra))`.
Relancer *Solveur NAÏF* : **17 échecs au lieu de 18**. Le cas « absorption » devient **vert** alors que la
version naïve renvoie 0.0 au lieu de −10⁻²⁰⁰ (100 % d'erreur). Mettez en plus Tol = `1e-6` sur la ligne
« élimination catastrophique » (la valeur de la diapo 70) : elle devient verte elle aussi, avec 25 % d'erreur.
Pour |RA| < 1, `max(1, |RA|)` transforme la tolérance relative en tolérance absolue.
Annuler ensuite (*Ctrl + Z*).

## 6. Tester la fonction d'un camarade (sans connaître son code)

Les tests ne dépendent que du **prototype** `double[] racines(double a, double b, double c)` (racines triées,
tableau vide si Δ < 0, `IllegalArgumentException` si a = 0). Pour tester une autre implémentation :
1. copier sa classe dans `src/main/java/inf563/solveur/` (ex. `SolveurCamarade.java`) ;
2. créer `src/test/java/inf563/solveur/SolveurCamaradeTest.java` :

```java
package inf563.solveur;

class SolveurCamaradeTest extends SuiteOraclesSolveur {
    @Override
    double[] g(double a, double b, double c) {
        return SolveurCamarade.racines(a, b, c);
    }
}
```

3. lancer cette classe dans le Test Explorer : les 84 tests s'exécutent contre son code.

## 7. Rendu sous Git (comme demandé pour les TP)

```powershell
git init
git add .
git commit -m "TP séance 3 : oracles du solveur du second degré"
```

(`.gitignore` exclut `target/`, l'enregistrement audio et les PDF du cours.)

## 8. Dépannage

| Problème | Solution |
|---|---|
| Le Test Explorer est vide | *Ctrl + Maj + P* → **Java: Clean Java Language Server Workspace** → *Restart and delete* |
| `java` n'est pas reconnu | fermer et rouvrir VS Code (le PATH contient déjà le JDK 21) |
| Mauvaise version de Java | *Ctrl + Maj + P* → **Java: Configure Java Runtime** → choisir JDK 21 |
| `mvnw.cmd` : erreur de téléchargement | vérifier la connexion Internet (premier lancement seulement) |
| Les tests NAÏF sont rouges | normal : c'est la démonstration de ce que les oracles détectent (voir `RAPPORT.md`) |
