package ci.univ.soutenances.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    public static String note(BigDecimal value) {
        return value == null ? null : String.format(Locale.FRANCE, "%.2f", value);
    }
}
