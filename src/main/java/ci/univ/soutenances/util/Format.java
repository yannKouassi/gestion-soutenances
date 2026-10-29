package ci.univ.soutenances.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Format {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Format() {
    }

    public static String date(LocalDate value) {
        return value == null ? null : value.format(DATE);
    }

    public static String dateHeure(LocalDateTime value) {
        return value == null ? null : value.format(DATE_HEURE);
    }
}
