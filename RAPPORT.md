# Rapport TP séance 3 : des oracles pour le solveur du second degré

**Système sous test** g = `Solveur.racines(a, b, c)` (racines réelles triées de a·x² + b·x + c = 0),
en deux versions : `naif` (formule du lycée) et `stable` (q = −½(b + signe(b)·√Δ), r₁ = q/a, r₂ = c/q, mise à l'échelle par 2ᵏ).
Outils : Java 21, JUnit 5 (`@ParameterizedTest`, `@CsvSource`), AssertJ.

## 1. Mesure d'erreur N

N = |y − RA| / |RA| (**erreur relative**). Justification : les racines testées vont de 10⁻²⁰⁰ à 10²⁰⁰ ; une tolérance
absolue fixe n'a de sens à aucune de ces échelles (diapo 70 : `max(1, |RA|)` masque 25 % d'erreur). Aucun RA n'est nul
dans nos cas, donc l'erreur relative est toujours définie. Les racines sont comparées élément par élément après tri
(mesure « ensembles »), et le nombre de racines est vérifié d'abord.

## 2. Tolérance Tol = c·κ·ε

ε = `Math.ulp(1.0)` = 2⁻⁵² ≈ 2,2·10⁻¹⁶ ; c = 10 (marge pour quelques arrondis) ;
κ = (|a|r² + |b||r| + |c|) / (|r|·|2ar + b|), conditionnement de la racine r, calculé à part.
Les RA sont calculés en Python `Decimal` à 1 200 chiffres à partir des valeurs exactes des `double`.

| # | DE (a, b, c) | Diagnostic | κ | 10·κ·ε | Tol retenue |
|---|---|---|---|---|---|
| 1 | (1, −3, 2) | cas nominal | 6 | 1,3·10⁻¹⁴ | 2·10⁻¹⁴ |
| 2 | (1, 10⁸, 1) | élimination catastrophique | 2 | 4,4·10⁻¹⁵ | 5·10⁻¹⁵ |
| 3 | (1, 10²⁰⁰, 1) | b² déborde en +∞ | 2 | 4,4·10⁻¹⁵ | 5·10⁻¹⁵ |
| 4 | (10²⁰⁰, −3·10²⁰⁰, 2·10²⁰⁰) | ∞ − ∞ = NaN | 6 | 1,3·10⁻¹⁴ | 2·10⁻¹⁴ |
| 5 | (10⁻²⁰⁰, 1, 10⁻²⁰⁰) | absorption | 2 | 4,4·10⁻¹⁵ | 5·10⁻¹⁵ |
| 6 | (−2, 3, 5) | a négatif | 1,43 | 3,2·10⁻¹⁵ | 4·10⁻¹⁵ |
| 7 | (1, −2, 1) | racine double exacte | ∞ | — | 0 (Δ = 0 calculé sans aucun arrondi sur de petits entiers) |
| 8 | (1, −2, 1 − 1,1·10⁻¹⁶) | Δ ≈ 0, **mal conditionné** | 1,9·10⁸ | 4,2·10⁻⁷ | 5·10⁻⁷ |

Cas 8 : c'est le **problème** qui est mal conditionné, pas l'algorithme : on ne peut garder que ≈ √ε de précision
relative, les deux versions sont justes à cette tolérance.
Oracles sans RA : résidu ≤ 8·ε·(|a|r² + |b·r| + |c|) (erreur inverse) ; Viète et construction à 10⁻¹⁰ % et 10⁻⁹ %
(`withinPercentage`) ; métamorphiques en **égalité stricte** car k = 2¹⁰ ne change que l'exposant ; différentiel à 10⁻¹² %.

## 3. Résultats : la même suite contre les deux versions

`mvnw test` → stable : **84/84 PASS**. `mvnw test -Pnaif` → naïf : **66 PASS, 18 FAIL**.

| Oracle | Détecte la version naïve ? | Preuve (sortie naïve) |
|---|---|---|
| Référence DE/RA/Tol | **Oui**, 4 cas sur 8 | cas 2 : N = 0,255 ; cas 3 : −∞ ; cas 4 : NaN ; cas 5 : 0.0 au lieu de −10⁻²⁰⁰ (N = 1) |
| Référence, cas 1, 6, 7, 8 | Non | la formule naïve est juste sur ces entrées |
| Exception a = 0, Δ < 0 | Non | même comportement dans les deux versions |
| Résidu (erreur inverse) | **Oui** | (1, 10⁸, 1) : résidu 0,255 > 3,1·10⁻¹⁵ |
| Viète | **Oui** | (1, 10⁸, 1) : r₁·r₂ = 0,745 au lieu de 1 |
| Métamorphique : échelle ×2¹⁰ | Non | la version naïve est fausse mais **cohérente** |
| Métamorphique : opposé | Non | idem |
| Construction | **Oui** | racines choisies (−10⁸, −10⁻⁸) |
| Combinaisons a × b × c (36 cas, signe exact de Δ + résidu) | **Oui**, 6 cas sur 36 | exactement les 6 combinaisons avec b = 10⁸ et c ≠ 0 (élimination sur la petite racine) |
| Tests dynamiques (20 cas générés, graine 563) | **Oui**, 4 cas sur 20 | cas 9, 12, 17, 19 : racines d'ordres de grandeur très différents (ex. 6·10⁻⁸ calculée 5,9999999996·10⁻⁸) |
| Différentiel BigDecimal | **Oui** | (1, 10⁸, 1) |

**Conclusion.** Un test « (1, −3, 2) → [1, 2] » ne distingue pas les deux versions. Les relations métamorphiques ne
voient pas une version fausse mais cohérente : aucun oracle ne voit tout, il faut **combiner** plusieurs oracles
partiels (référence + propriétés + construction + différentiel), avec une tolérance relative justifiée par κ·ε.
