package ci.univ.soutenances.service.impl;

import java.util.Locale;

final class ReferentielValidation {
    private ReferentielValidation() {
    }

    static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " est obligatoire.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(field + " ne peut pas dépasser " + maxLength + " caractères.");
        }
        return trimmed;
    }

    static String code(String value) {
        return required(value, "Le code", 30).toUpperCase(Locale.ROOT);
    }
}
