package inf563.solveur;

import java.util.Arrays;

/**
 * Affiche le tableau du TP : résultat naïf / stable / attendu pour chaque cas.
 * (Sortie en ASCII pour rester lisible dans la console Windows.)
 */
public class Demo {

    public static void main(String[] args) {
        // a, b, c, diagnostic, résultat attendu
        Object[][] cas = {
                {1, -3, 2, "cas nominal", "[1.0, 2.0]"},
                {1, 1e8, 1, "elimination catastrophique", "[-1.0E8, -1.0E-8]"},
                {1, 1e200, 1, "b^2 deborde en +Inf", "[-1.0E200, -1.0E-200]"},
                {1e200, -3e200, 2e200, "Inf - Inf = NaN", "[1.0, 2.0]"},
                {1e-200, 1, 1e-200, "absorption", "[-1.0E200, -1.0E-200]"},
                {1, -2, 1 - 1.1e-16, "Delta ~ 0 (mal conditionne)", "[0.99999998946..., 1.00000001054...]"},
        };
        String format = "%-28s | %-34s | %-42s | %-42s | %s%n";
        System.out.printf(format, "Diagnostic", "(a, b, c)", "Naif", "Stable", "Attendu");
        System.out.println("-".repeat(190));
        for (Object[] l : cas) {
            double a = ((Number) l[0]).doubleValue(), b = ((Number) l[1]).doubleValue(), c = ((Number) l[2]).doubleValue();
            System.out.printf(format, l[3], "(" + a + ", " + b + ", " + c + ")",
                    Arrays.toString(Solveur.racinesNaif(a, b, c)), Arrays.toString(Solveur.racines(a, b, c)), l[4]);
        }
    }
}
