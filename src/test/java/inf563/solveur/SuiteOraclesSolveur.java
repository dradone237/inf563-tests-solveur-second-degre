package inf563.solveur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Séance 3 : la même suite de tests, exécutée contre les deux versions du solveur.
 *
 * <p>Modèle d'un test : DE (données d'entrée) → g (système sous test) → y,
 * puis l'oracle O décide PASS / FAIL. Pour l'oracle par référence : N(y, RA) ≤ Tol.
 *
 * <p>Les sous-classes ne font qu'une chose : dire quel g on teste.
 */
abstract class SuiteOraclesSolveur {

    /** ε = ulp(1) = 2^-52 ≈ 2,2·10^-16 en double. */
    static final double EPS = Math.ulp(1.0);

    /** Le système sous test g : renvoie les racines triées de a·x² + b·x + c = 0. */
    abstract double[] g(double a, double b, double c);

    /** Mesure d'erreur N : erreur relative |y − RA| / |RA| (aucun RA n'est nul dans nos cas). */
    static double erreurRelative(double y, double ra) {
        return Math.abs(y - ra) / Math.abs(ra);
    }

    // ------------------------------------------------------------------
    // 1. Oracle par référence : DE / RA / Tol (8 cas)
    //    Tol = c·κ·ε avec c = 10 (marge), κ = conditionnement de la racine.
    //    RA calculés à part avec 1 200 chiffres (Python Decimal) à partir
    //    des valeurs exactes des double.
    // ------------------------------------------------------------------
    @ParameterizedTest(name = "[{index}] {6} : ({0}, {1}, {2}) → [{3}, {4}], Tol = {5}")
    //   κ et 10·κ·ε par cas :  nominal κ=6 → 1,3e-14 ; élimination κ=2 → 4,4e-15 ;
    //   débordement κ=2 → 4,4e-15 ; NaN κ=6 → 1,3e-14 ; absorption κ=2 → 4,4e-15 ;
    //   a négatif κ=1,43 → 3,2e-15 ; racine double : Δ = 0 calculé sans arrondi → Tol = 0 ;
    //   Δ ≈ 0 : κ=1,9e8 → 4,2e-7 (on ne garde que ~√ε : le PROBLÈME est mal conditionné).
    @CsvSource(textBlock = """
            # a,      b,      c,                  RA r1,              RA r2,              Tol,   diagnostic
              1,      -3,     2,                  1.0,                2.0,                2e-14, cas nominal
              1,      1e8,    1,                  -99999999.99999999, -1e-8,              5e-15, élimination catastrophique
              1,      1e200,  1,                  -1e200,             -1e-200,            5e-15, b² déborde en +∞
              1e200,  -3e200, 2e200,              1.0,                2.0,                2e-14, ∞ − ∞ = NaN
              1e-200, 1,      1e-200,             -1e200,             -1e-200,            5e-15, absorption
              -2,     3,      5,                  -1.0,               2.5,                4e-15, a négatif
              1,      -2,     1,                  1.0,                1.0,                0,     racine double exacte
              1,      -2,     0.9999999999999999, 0.9999999894632878, 1.0000000105367122, 5e-7,  Δ ≈ 0 mal conditionné
            """)
    @DisplayName("Oracle par référence : N(y, RA) ≤ Tol")
    void parReference(double a, double b, double c, double r1, double r2, double tol, String diagnostic) {
        double[] y = g(a, b, c);                        // y = g(DE)
        double[] ra = (r1 == r2) ? new double[]{r1} : new double[]{r1, r2};
        assertEquals(ra.length, y.length, "nombre de racines");
        for (int i = 0; i < ra.length; i++) {
            double n = erreurRelative(y[i], ra[i]);     // N
            assertTrue(n <= tol,                        // verdict : N ≤ Tol (faux si y = NaN)
                    "racine " + i + " : y = " + y[i] + ", RA = " + ra[i] + ", N = " + n + " > Tol = " + tol);
        }
    }

    // ------------------------------------------------------------------
    // 2. Cas particuliers du contrat (exception attendue = PASS)
    // ------------------------------------------------------------------
    @Test
    @DisplayName("a = 0 : IllegalArgumentException attendue")
    void aNul() {
        assertThrows(IllegalArgumentException.class, () -> g(0, 2, 1));
    }

    @ParameterizedTest(name = "[{index}] ({0}, {1}, {2}) : Δ < 0 → aucune racine réelle")
    @CsvSource({"1, 0, 1", "1, 1, 1", "2, -4, 2.01"})
    @DisplayName("Δ < 0 : tableau vide")
    void discriminantNegatif(double a, double b, double c) {
        assertEquals(0, g(a, b, c).length);
    }

    // ------------------------------------------------------------------
    // 3. Oracles SANS résultat attendu
    // ------------------------------------------------------------------

    /** Résidu comparé à l'échelle des termes (erreur inverse) : |a·r² + b·r + c| ≤ 8·ε·(|a|r² + |b·r| + |c|). */
    @ParameterizedTest(name = "[{index}] ({0}, {1}, {2})")
    @CsvSource({"1, 1e8, 1", "3, 7, 2", "2, -4, 1.99"})
    @DisplayName("Propriété : résidu (erreur inverse)")
    void residu(double a, double b, double c) {
        for (double r : g(a, b, c)) {
            double res = Math.abs(a * r * r + b * r + c);
            double ech = Math.abs(a) * r * r + Math.abs(b * r) + Math.abs(c);
            assertTrue(res <= 8 * EPS * ech, "r = " + r + " : résidu " + res + " > " + 8 * EPS * ech);
        }
    }

    /** Viète : r1 + r2 = −b/a et r1·r2 = c/a. */
    @ParameterizedTest(name = "[{index}] ({0}, {1}, {2})")
    @CsvSource({"1, 1e8, 1", "3, 7, 2"})
    @DisplayName("Propriété : relations de Viète")
    void viete(double a, double b, double c) {
        double[] r = g(a, b, c);
        assertThat(r[0] + r[1]).isCloseTo(-b / a, withinPercentage(1e-10));
        assertThat(r[0] * r[1]).isCloseTo(c / a, withinPercentage(1e-10));
    }

    /** racines(k·a, k·b, k·c) = racines(a, b, c) avec k = 2^10 : produit exact, donc égalité stricte. */
    @ParameterizedTest(name = "[{index}] ({0}, {1}, {2}) et ×1024")
    @CsvSource({"3, 7.1, 2", "1, 1e8, 1", "-2, 3, 5"})
    @DisplayName("Métamorphique : changement d'échelle")
    void echelle(double a, double b, double c) {
        double k = 1024;                                // 2^10 : produit exact
        assertArrayEquals(g(a, b, c), g(k * a, k * b, k * c)); // strict
    }

    /** racines(a, −b, c) = −racines(a, b, c) ; les racines sont triées : l'opposé inverse l'ordre. */
    @ParameterizedTest(name = "[{index}] ({0}, {1}, {2}) et ({0}, -{1}, {2})")
    @CsvSource({"3, 7.1, 2", "1, 1e8, 1"})
    @DisplayName("Métamorphique : opposé")
    void oppose(double a, double b, double c) {
        double[] r = g(a, b, c);
        double[] s = g(a, -b, c);
        assertEquals(-r[0], s[1], 0.0);
        assertEquals(-r[1], s[0], 0.0);
    }

    /** On choisit les racines, puis on fabrique l'équation (x − r1)(x − r2) = 0. */
    @ParameterizedTest(name = "[{index}] racines choisies {0} et {1}")
    @CsvSource({"-1e8, -1e-8", "1, 2", "-3, 1e-5"})
    @DisplayName("Oracle par construction")
    void construction(double r1, double r2) {
        double[] r = g(1, -(r1 + r2), r1 * r2);
        assertThat(r[0]).isCloseTo(Math.min(r1, r2), withinPercentage(1e-9));
        assertThat(r[1]).isCloseTo(Math.max(r1, r2), withinPercentage(1e-9));
    }

    // ------------------------------------------------------------------
    // 4. Combinaisons : un tableau de valeurs par paramètre, JUnit exécute
    //    TOUS les cas possibles (produit cartésien : 3 × 4 × 3 = 36 cas).
    // ------------------------------------------------------------------
    static final double[] VALEURS_A = {-2, 1, 3};
    static final double[] VALEURS_B = {-5, 0, 1, 1e8};
    static final double[] VALEURS_C = {-4, 0, 2};

    static Stream<Arguments> toutesLesCombinaisons() {
        return Arrays.stream(VALEURS_A).boxed().flatMap(a ->
                Arrays.stream(VALEURS_B).boxed().flatMap(b ->
                        Arrays.stream(VALEURS_C).mapToObj(c -> Arguments.of(a, b, c))));
    }

    /** Aucun RA : nombre de racines donné par le signe EXACT de Δ (BigDecimal), puis résidu (erreur inverse). */
    @ParameterizedTest(name = "[{index}] a = {0}, b = {1}, c = {2}")
    @MethodSource("toutesLesCombinaisons")
    @DisplayName("Combinaisons de paramètres : tous les cas possibles")
    void combinaisons(double a, double b, double c) {
        BigDecimal A = new BigDecimal(a), B = new BigDecimal(b), C = new BigDecimal(c);
        int signeDelta = B.multiply(B).subtract(A.multiply(C).multiply(BigDecimal.valueOf(4))).signum();
        double[] r = g(a, b, c);
        assertEquals(signeDelta + 1, r.length, "nombre de racines");   // Δ<0 → 0, Δ=0 → 1, Δ>0 → 2
        for (double x : r) {
            double res = Math.abs(a * x * x + b * x + c);
            double ech = Math.abs(a) * x * x + Math.abs(b * x) + Math.abs(c);
            assertTrue(res <= 8 * EPS * ech, "x = " + x + " : résidu " + res + " > " + 8 * EPS * ech);
        }
    }

    /**
     * Cas de test GÉNÉRÉS à l'exécution (tests dynamiques JUnit 5) : oracle par construction.
     * Graine fixe : les 20 cas sont toujours les mêmes, un échec se reproduit.
     * Exposants distincts : racines bien séparées, donc κ petit et Tol = 10⁻⁹ % largement justifiée.
     */
    @TestFactory
    @DisplayName("Tests dynamiques : 20 équations générées par construction")
    Stream<DynamicTest> constructionGeneree() {
        Random alea = new Random(563);                  // graine fixe
        return IntStream.rangeClosed(1, 20).mapToObj(i -> {
            int e1 = alea.nextInt(-8, 9);
            int e2 = e1 + 1 + alea.nextInt(0, 8);       // e2 > e1
            double r1 = (alea.nextBoolean() ? 1 : -1) * (1 + alea.nextInt(9)) * Math.pow(10, e1);
            double r2 = (alea.nextBoolean() ? 1 : -1) * (1 + alea.nextInt(9)) * Math.pow(10, e2);
            return dynamicTest("cas " + i + " : racines choisies " + r1 + " et " + r2, () -> {
                double[] r = g(1, -(r1 + r2), r1 * r2);
                assertThat(r[0]).isCloseTo(Math.min(r1, r2), withinPercentage(1e-9));
                assertThat(r[1]).isCloseTo(Math.max(r1, r2), withinPercentage(1e-9));
            });
        });
    }

    /** Bonus : référence BigDecimal à 50 chiffres (la formule naïve n'y souffre plus d'élimination). */
    @ParameterizedTest(name = "[{index}] ({0}, {1}, {2})")
    @CsvSource({"1, 1e8, 1", "3, 7.1, 2", "1e-3, -5, 1e3"})
    @DisplayName("Oracle différentiel (BigDecimal 50 chiffres)")
    void differentiel(double a, double b, double c) {   // a > 0 : (−b + √Δ)/2a est la plus grande racine
        MathContext mc = new MathContext(50);
        BigDecimal A = new BigDecimal(a), B = new BigDecimal(b);
        BigDecimal C = new BigDecimal(c), DEUX = BigDecimal.valueOf(2);
        BigDecimal s = B.multiply(B)
                .subtract(A.multiply(C).multiply(BigDecimal.valueOf(4)))
                .sqrt(mc);
        BigDecimal ref = B.negate().add(s).divide(A.multiply(DEUX), mc);
        assertThat(g(a, b, c)[1]).isCloseTo(ref.doubleValue(), withinPercentage(1e-12));
    }
}
