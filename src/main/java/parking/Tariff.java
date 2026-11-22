package parking;

import java.time.Duration;
import java.time.LocalDateTime;

// расчёт оплаты за стоянку
public class Tariff {

    public static final int FREE_MINUTES = 20;
    public static final int HOUR_PRICE = 200;
    public static final int DAY_PRICE = 200;
    public static int calculate(LocalDateTime from, LocalDateTime to) {
        long seconds = Duration.between(from, to).getSeconds();

        if (seconds <= FREE_MINUTES * 60L) {
            return 0;
        }

        if (seconds <= 12 * 3600L) {
            long hours = (seconds + 3600 - 1) / 3600; // округление вверх
            return (int) hours * HOUR_PRICE;
        }

        long days = (seconds + 86400 - 1) / 86400;
        return (int) days * DAY_PRICE;
    }
}
