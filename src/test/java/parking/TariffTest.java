package parking;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// проверочные расчёты из задания
class TariffTest {

    private final LocalDateTime in = LocalDateTime.of(2027, 1, 1, 8, 0, 0);

    @Test
    void exactly20MinutesIsFree() {
        assertEquals(0, Tariff.calculate(in, in.plusMinutes(20)));
    }

    @Test
    void lessThan20MinutesIsFree() {
        assertEquals(0, Tariff.calculate(in, in.plusMinutes(19).plusSeconds(59)));
    }

    @Test
    void twentyMinutesAndOneSecondCostsOneHour() {
        assertEquals(200, Tariff.calculate(in, in.plusMinutes(20).plusSeconds(1)));
    }

    @Test
    void exactlyOneHourCosts200() {
        assertEquals(200, Tariff.calculate(in, in.plusHours(1)));
    }

    @Test
    void oneHourAndOneSecondCosts400() {
        assertEquals(400, Tariff.calculate(in, in.plusHours(1).plusSeconds(1)));
    }

    @Test
    void exactly12HoursCosts2400() {
        assertEquals(2400, Tariff.calculate(in, in.plusHours(12)));
    }

    @Test
    void after12HoursPriceDropsToDailyRate() {
        assertEquals(200, Tariff.calculate(in, in.plusHours(12).plusSeconds(1)));
    }

    @Test
    void exactly24HoursCostsOneDay() {
        assertEquals(200, Tariff.calculate(in, in.plusDays(1)));
    }

    @Test
    void twentyFourHoursAndOneSecondCostsTwoDays() {
        assertEquals(400, Tariff.calculate(in, in.plusDays(1).plusSeconds(1)));
    }

    // пример 1 из задания: 23 ч 48 мин - одни сутки
    @Test
    void example23Hours48Minutes() {
        LocalDateTime entry = LocalDateTime.of(2027, 12, 3, 10, 2);
        LocalDateTime exit = LocalDateTime.of(2027, 12, 4, 9, 50);
        assertEquals(200, Tariff.calculate(entry, exit));
    }

    // пример 2 из задания: 28 ч 1 мин - двое суток
    @Test
    void example28Hours1Minute() {
        LocalDateTime entry = LocalDateTime.of(2027, 12, 3, 10, 2);
        LocalDateTime exit = LocalDateTime.of(2027, 12, 4, 14, 3);
        assertEquals(400, Tariff.calculate(entry, exit));
    }
}
