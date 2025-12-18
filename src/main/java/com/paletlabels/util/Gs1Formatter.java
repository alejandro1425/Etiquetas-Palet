package com.paletlabels.util;

import com.paletlabels.model.Product;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class Gs1Formatter {
    private static final DateTimeFormatter INPUT_DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    private Gs1Formatter() {
    }

    public static String buildGs1Data(Product product, double netWeightKg, String lot, String bestBefore) {
        LocalDate date = parseDate(bestBefore);
        String gtin = toGtin14(product.getEan13());

        int weight = (int) Math.round(netWeightKg * 1000); // kg -> gramos (3 dec)
        String weightFormatted = String.format("%07d", weight);

        StringBuilder builder = new StringBuilder();
        builder.append("(").append(product.getGtinAi()).append(")").append(gtin)
           .append("(3103)").append(weightFormatted);

        if (date != null) {
            builder.append("(17)").append(date.format(YYMMDD));
        }
        if (lot != null && !lot.isBlank()) {
            builder.append("(10)").append(lot.trim());
        }
        return builder.toString();
    }


    private static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(date, INPUT_DATE);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String toGtin14(String ean13) {
        String digits = ean13 == null ? "" : ean13.replaceAll("\\D", "");
        return String.format("%014d", Long.parseLong(digits.isEmpty() ? "0" : digits));
    }
}
