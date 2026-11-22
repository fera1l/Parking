package parking;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Journal {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Path file;
    private PrintWriter writer;
    private int dayEntries = 0;
    private int dayExits = 0;
    private int dayRevenue = 0;

    public Journal() {
        try {
            Path dir = Path.of("logs");
            Files.createDirectories(dir);
            String name = "journal_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".txt";
            file = dir.resolve(name);
            writer = new PrintWriter(Files.newBufferedWriter(file), true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path getFile() {
        return file;
    }

    public void logEntry(String plate, int spotNumber, LocalDateTime time) {
        dayEntries++;
        write(time.format(DATE_TIME) + ". Автомобиль " + plate + " заехал на место №" + spotNumber + ".");
    }

    public void logExit(String plate, LocalDateTime entry, LocalDateTime exit, int payment) {
        dayExits++;
        dayRevenue += payment;
        write(exit.format(DATE_TIME) + ". Автомобиль " + plate + " выехал. Въезд: "
                + entry.format(DATE_TIME) + ". Оплачено: " + payment + " ₽.");
    }

    public void daySummary(LocalDateTime time) {
        write(time.format(DATE_TIME) + ". Итог дня: въехало " + dayEntries + ", выехало " + dayExits
                + ". Выручка: " + dayRevenue + " ₽.");
    }

    public void newDay(LocalDateTime time) {
        write(time.format(DATE) + ". Начало записей нового дня.");
        dayEntries = 0;
        dayExits = 0;
        dayRevenue = 0;
    }

    public int getDayRevenue() {
        return dayRevenue;
    }

    // при обычном закрытии пишем промежуточный итог
    public void close(LocalDateTime time) {
        write("--- Программа закрыта. Промежуточный итог дня: въехало " + dayEntries
                + ", выехало " + dayExits + ". Выручка: " + dayRevenue + " ₽. ---");
        writer.close();
    }

    private void write(String line) {
        writer.println(line);
        writer.flush();
    }
}
