# Solveur du second degré : tests JUnit 5, pairwise, réflexion, JaCoCo et PIT (INF563, chapitres 1 à 5)

Fil rouge A du cours de E. Fomekong : on teste `racines(a, b, c)` de l'interface `SolveurSecondDegre`
(racines de a·x² + b·x + c = 0), l'implémentation étant chargée **par réflexion** à partir de son nom,
avec **JUnit 5** (tests paramétrés) et **AssertJ**, en suivant le modèle du chapitre 3 ; les cas combinés viennent
de la stratégie **pairwise** sur 9 catégories de réels (chapitre 4) ; la qualité de la suite est mesurée par
**JaCoCo** (couverture) et **PIT** (mutation) (chapitre 5). Résultats et analyse : [`RAPPORT.md`](RAPPORT.md).

```
DE (a, b, c)  →  g = SolveurSecondDegre (chargée par réflexion)  →  y  →  oracle O  →  verdict PASS / FAIL / ERROR / INCONCLUSIF
oracle par référence :  N(y, RA) ≤ Tol      avec  N = |y − RA| / |RA|  et  Tol = c·κ·ε
```

## 1. Contenu du projet

```
Projet_test_polynome_chapitre3/
├── pom.xml                         Java 21, JUnit 5.13, AssertJ 3.27, JaCoCo 0.8.12, PIT 1.15.3
├── mvnw.cmd, mvnw, .mvn/           Maven Wrapper : pas besoin d'installer Maven
├── .vscode/                        réglages VS Code
├── RAPPORT.md                      le rapport : réflexion, tolérances, pairwise, défaut trouvé, JaCoCo, PIT
├── doc/
│   ├── Fonctionnement_detaille_du_projet.pdf      explication complète, chapitres I à III (56 p., 2 nov.)
│   ├── Fonctionnement_detaille_chapitres_4_5.pdf  le complément à jour : réflexion, pairwise, JaCoCo, PIT
│   └── rapports/jacoco/, rapports/pit/            copies des rapports HTML (ouvrir index.html)
└── src/
    ├── main/java/inf563/solveur/
    │   ├── SolveurSecondDegre.java interface : le contrat double[] racines(a, b, c)
    │   ├── SolveurNaif.java        formule du lycée (diapo 51)
    │   ├── SolveurStable.java      formule stable + mise à l'échelle (diapo 54), corrigée grâce au pairwise
    │   └── Demo.java               programme : tableau naïf / stable / attendu (TP séance 2)
    └── test/java/inf563/solveur/
        ├── ChargeurSolveur.java      réflexion : Class.forName(nom) puis getConstructor().newInstance()
        ├── ChargeurSolveurTest.java  tests du chargeur (11 tests : classes acceptées / refusées)
        ├── CategorieReel.java        les 9 catégories de réels (diapo 93) et leur représentant
        ├── Pairwise.java             génère les 81 cas pairwise (tableau orthogonal) et les 729 exhaustifs
        ├── PairwiseTest.java         vérifie que les 81 cas couvrent bien toutes les paires (5 tests)
        ├── ReferenceExacte.java      oracle des cas générés : RA (Δ exact, BigDecimal) et Tol = 10·κ·ε
        ├── SuiteOraclesSolveur.java  LA suite de tests (écrite une seule fois, ne connaît que l'interface)
        ├── SolveurStableTest.java    la suite contre "inf563.solveur.SolveurStable" : tout VERT
        ├── SolveurNaifTest.java      la suite contre "inf563.solveur.SolveurNaif" : 28 ROUGES attendus
        ├── SolveurConfigurableTest.java  la suite contre la classe donnée par -Dsolveur.impl=...
        └── SolveurStableDiapo54.java la version stable d'origine (diapo 54) : 5 ROUGES (4 pairwise + 1 cas PIT)
```

Les tests n'importent **aucune** classe concrète : chaque classe de test ne donne qu'un **nom** (une chaîne),
et `ChargeurSolveur` charge et instancie la classe à l'exécution (Java Reflection API), puis la manipule
uniquement à travers l'interface `SolveurSecondDegre`.

Ce que contient `SuiteOraclesSolveur` (142 tests) :

| Bloc | Test | Ce qu'il illustre |
|---|---|---|
| Oracle par référence | `parReference` (8 cas DE / RA / Tol) | N = erreur relative, Tol = c·κ·ε justifiée |
| Contrat | `aNul`, `discriminantNegatif` | exception attendue = PASS ; Δ < 0 → aucune racine |
| Propriété | `residu` | erreur inverse : \|a·r² + b·r + c\| ≤ 8·ε·(\|a\|r² + \|b·r\| + \|c\|) |
| Propriété | `viete` | r₁ + r₂ = −b/a et r₁·r₂ = c/a |
| Métamorphique | `echelle` | racines(1024a, 1024b, 1024c) = racines(a, b, c), égalité stricte |
| Métamorphique | `oppose` | racines(a, −b, c) = −racines(a, b, c) |
| Construction | `construction` | on choisit r₁, r₂ puis on fabrique (x − r₁)(x − r₂) |
| Pairwise (chap. 4) | `pairwise` (`@MethodSource`) | a, b, c prennent leurs valeurs dans les **9 catégories de réels** (`CategorieReel`, diapo 93) : 81 cas pairwise (`Pairwise`, tableau orthogonal) au lieu de 9³ = 729 ; RA et Tol = 10·κ·ε calculés par `ReferenceExacte` (Δ exact en BigDecimal) |
| Mutation (chap. 5) | `casMutation` | 2 cas ajoutés après PIT : chacun tue un mutant survivant (voir la colonne « mutant visé ») |
| Robustesse (chap. 4) | `classeInvalide` | NaN, ±∞ sur a, b, c puis a = ±0 : **une seule classe invalide par cas** (diapo 90) → `IllegalArgumentException` |
| Tests dynamiques | `constructionGeneree` (`@TestFactory`) | 20 cas **générés à l'exécution** (graine fixe), oracle par construction |
| Différentiel (bonus) | `differentiel` | référence BigDecimal à 50 chiffres |

## 2. Prérequis (déjà vérifiés sur ce PC)

| Outil | État |
|---|---|
| JDK 21 (Temurin) | installé : `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot`, `JAVA_HOME` défini |
| Maven | **pas nécessaire** : le wrapper `mvnw.cmd` télécharge Maven 3.9.11 tout seul (déjà fait une fois) |
| VS Code + « Extension Pack for Java » | installé (`vscjava.vscode-java-pack`) |
| Internet | seulement au premier lancement (JUnit, AssertJ, JaCoCo, PIT : déjà en cache dans `~\.m2`) |

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
   Vous voyez : `solveur-second-degre → inf563.solveur →` *Solveur STABLE…*, *Solveur NAÏF…*,
   *Solveur choisi au lancement…* (désactivée sans `-Dsolveur.impl` : normal), *Chargement de l'implémentation
   par réflexion*, *Générateur pairwise…* (les tests des trois classes « Solveur » sont écrits une seule fois dans
   `SuiteOraclesSolveur` et *hérités* ; si une classe n'affiche pas encore ses tests, lancez-la avec ▶).
5. **Version stable** : survoler *Solveur STABLE* → cliquer ▶ **Run Test**.
   Résultat attendu : **142 tests verts** ✔.
6. **Version naïve** : cliquer ▶ sur *Solveur NAÏF*.
   Résultat attendu : **114 verts, 28 rouges** ✘. C'est **voulu** : chaque rouge est un oracle qui *détecte* la formule naïve.
   Cliquer sur un test rouge pour lire le verdict, par exemple :
   `racine 1 : y = -7.450580596923828E-9, RA = -1.0E-8, N = 0.2549 > Tol = 5.0E-15`
   → la formule naïve se trompe de 25 % sur la petite racine de x² + 10⁸x + 1 (élimination catastrophique).
7. **Déplier un test paramétré** (ex. `Oracle par référence`) : chaque ligne du `@CsvSource` est un cas
   `[1] cas nominal`, `[2] élimination catastrophique`, … qu'on peut relancer seul avec ▶.
8. **Déboguer un cas** : mettre un point d'arrêt (clic dans la marge) dans `SolveurNaif.racines`, puis clic droit sur
   le cas rouge → **Debug Test**. Observer `d`, `r1`, `r2` dans le panneau *Variables*.
9. **Lancer le programme** : ouvrir `Demo.java` → cliquer **Run** au-dessus de `main`.
   Le terminal affiche le tableau naïf / stable / attendu (le même que la diapo 52 du cours).
   Attention : `Demo` **n'est pas un test**, il ne fait qu'afficher. Le verdict PASS / FAIL vient uniquement des
   assertions JUnit (l'enseignant l'a rappelé en cours : afficher des résultats, ce n'est pas tester).

## 4. Lancer les tests en ligne de commande (terminal VS Code : *Ctrl + ù*)

```powershell
.\mvnw.cmd test            # stable + chargeur + pairwise → Tests run: 158 (142 + 11 + 5), Failures: 0 → BUILD SUCCESS
.\mvnw.cmd test -Pnaif     # version naïve     → Tests run: 142, Failures: 28 → BUILD FAILURE (attendu)
# n'importe quelle implémentation, choisie au lancement (réflexion), sans recompiler les tests :
.\mvnw.cmd test "-Dsolveur.impl=inf563.solveur.SolveurNaif"
```

(Sous PowerShell, mettre l'option `-D...` entre guillemets.)

Les rapports détaillés sont dans `target\surefire-reports\`.

**Couverture de code (JaCoCo, chapitre 5)** :

```powershell
.\mvnw.cmd clean verify                       # tests + rapport JaCoCo + seuil (100 % des branches)
start target\site\jacoco\index.html           # ouvrir le rapport dans le navigateur
```

Résultat attendu : `All coverage checks have been met` et, pour `SolveurStable`, 100 % des instructions,
des branches (16/16) et des lignes. Dans le rapport, cliquer sur `inf563.solveur` → `SolveurStable` →
`SolveurStable.java` : chaque ligne est colorée (vert = couverte, jaune = branche manquante, rouge = jamais exécutée).
`Demo` et `SolveurNaif` sont exclus de la mesure (voir `pom.xml`).

**Test de mutation (PIT, chapitre 5)** :

```powershell
.\mvnw.cmd test-compile org.pitest:pitest-maven:mutationCoverage
start target\pit-reports\index.html          # ouvrir le rapport dans le navigateur
```

Résultat attendu : `Generated 39 mutations Killed 38 (97%)` puis `BUILD SUCCESS` (seuil : 85 %).
Le seul survivant (ligne `e += e & 1`) est un **mutant équivalent** : arrondir l'exposant e au pair
inférieur au lieu du pair supérieur donne exactement la même √Δ. Hors mutant équivalent : 38/38 = 100 %.
PIT ne mute que `SolveurStable` et n'exécute pas la suite naïve (il exige une suite verte avant de muter).

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
Relancer *Solveur NAÏF* : **27 échecs au lieu de 28**. Le cas « absorption » devient **vert** alors que la
version naïve renvoie 0.0 au lieu de −10⁻²⁰⁰ (100 % d'erreur). Mettez en plus Tol = `1e-6` sur la ligne
« élimination catastrophique » (la valeur de la diapo 70) : elle devient verte elle aussi, avec 25 % d'erreur.
Pour |RA| < 1, `max(1, |RA|)` transforme la tolérance relative en tolérance absolue.
Annuler ensuite (*Ctrl + Z*).

**d) Une donnée trop particulière laisse survivre un mutant (PIT).** Dans `SuiteOraclesSolveur.casMutation`,
supprimer la ligne `4 | -4 | 1 | …`. Relancer PIT : **37 mutants tués sur 39** au lieu de 38. Le mutant
« `b / a` → `b * a` » survit : le seul test de racine double restant est (1, −2, 1), et avec a = 1, multiplier
ou diviser par a donne le même résultat. Remettre la ligne ensuite.

**e) La couverture comme garde-fou (JaCoCo).** Lancer
`.\mvnw.cmd verify "-Dtest=SolveurStableTest#parReference+aNul+discriminantNegatif" "-Dsurefire.failIfNoSpecifiedTests=false"` :
la couverture des branches descend sous 100 % et le build échoue (`Rule violated`). Le rapport JaCoCo montre en
jaune et rouge les lignes que seuls le pairwise et les classes invalides exécutent (coefficient non fini, c = 0).

## 6. Tester la fonction d'un camarade (sans connaître son code)

Les tests ne dépendent que de l'**interface** `SolveurSecondDegre` (racines triées, tableau vide si Δ < 0,
`IllegalArgumentException` si a = 0). Grâce à la réflexion, on teste une autre implémentation **sans écrire
ni recompiler un seul test** :
1. copier sa classe dans `src/main/java/` (ex. `inf563/solveur/SolveurCamarade.java`). Elle doit
   `implements SolveurSecondDegre` et avoir un constructeur public sans argument ;
2. lancer : `.\mvnw.cmd test "-Dsolveur.impl=inf563.solveur.SolveurCamarade"`
   → les 142 tests de la suite s'exécutent contre son code (classe `SolveurConfigurableTest`).

Si le nom est faux ou la classe inutilisable, chaque test se termine en **ERROR** avec un message explicite,
par exemple `classe introuvable : inf563.solveur.SolveurCamarade` ou
`... n'a pas de constructeur public sans argument`.

## 7. Rendu sous Git (comme demandé pour les TP)

Le dépôt existe déjà (branche `main`). Après une modification :

```powershell
git add .
git commit -m "message"
git log --oneline          # historique des étapes
```

(`.gitignore` exclut `target/`, l'enregistrement audio et les PDF du cours, mais garde `doc/*.pdf` ;
les rapports JaCoCo et PIT sont versionnés dans `doc/rapports/` puisque `target/` ne l'est pas.)

## 8. Dépannage

| Problème | Solution |
|---|---|
| Le Test Explorer est vide | *Ctrl + Maj + P* → **Java: Clean Java Language Server Workspace** → *Restart and delete* |
| `java` n'est pas reconnu | fermer et rouvrir VS Code (le PATH contient déjà le JDK 21) |
| Mauvaise version de Java | *Ctrl + Maj + P* → **Java: Configure Java Runtime** → choisir JDK 21 |
| `mvnw.cmd` : erreur de téléchargement | vérifier la connexion Internet (premier lancement seulement) |
| Les tests NAÏF sont rouges | normal : c'est la démonstration de ce que les oracles détectent (voir `RAPPORT.md`) |
| `-Dsolveur.impl=...` ignoré ou erreur « Unknown lifecycle phase » | sous PowerShell, mettre l'option entre guillemets : `"-Dsolveur.impl=..."` |
| `Rule violated for bundle … branches covered ratio` | un test a été retiré ou du code ajouté sans test : ouvrir `target\site\jacoco\index.html`, chercher les lignes jaunes / rouges |
| PIT : `Coverage generation minion exited abnormally` | relancer avec `"-Dverbose=true"` pour voir la cause ; avec JUnit 5.13, `pitest-junit5-plugin` doit être en 1.2.3 (la 1.2.1 de la diapo 138 échoue : « OutputDirectoryProvider not available ») |
| PIT : `did not pass without mutation` | PIT exige une suite verte : corriger le test rouge d'abord (la suite naïve est déjà exclue dans le `pom.xml`) |
