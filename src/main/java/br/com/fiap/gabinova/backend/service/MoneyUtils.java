package br.com.fiap.gabinova.backend.service;

/** Parser para valores monetarios formatados como texto (ex: "R$ 120.000"), igual ao app Android. */
public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static double parse(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        String cleaned = value
                .replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static double parsePercentage(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        String cleaned = value.replace("%", "").trim();
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String format(double value) {
        return "R$ " + String.format("%,.0f", value).replace(",", ".");
    }
}
