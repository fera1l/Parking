package parking;

import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Car {

    private static final Random rnd = new Random();
    private static final Set<String> usedPlates = new HashSet<>();

    private final String plate;
    private final Color color;

    public Car(String plate, Color color) {
        this.plate = plate;
        this.color = color;
    }

    public String getPlate() {
        return plate;
    }

    public Color getColor() {
        return color;
    }

    //рандом тачка
    public static Car randomCar() {
        return new Car(randomPlate(), randomColor());
    }

    private static Color randomColor() {
        Color[] colors = {
                Color.web("#d9534f"), Color.web("#5bc0de"), Color.web("#5cb85c"),
                Color.web("#f0ad4e"), Color.web("#9370db"), Color.web("#e8e8e8"),
                Color.web("#4a6fa5"), Color.web("#c0c0c0"), Color.web("#ff7f50")
        };
        return colors[rnd.nextInt(colors.length)];
    }
    //нейронка делала
    private static String randomPlate() {
        String letters = "АВЕКМНОРСТУХ";
        String plate;
        do {
            StringBuilder sb = new StringBuilder();
            sb.append(letters.charAt(rnd.nextInt(letters.length())));
            sb.append(rnd.nextInt(10)).append(rnd.nextInt(10)).append(rnd.nextInt(10));
            sb.append(letters.charAt(rnd.nextInt(letters.length())));
            sb.append(letters.charAt(rnd.nextInt(letters.length())));
            int region = rnd.nextInt(1000);
            sb.append(String.format("%02d", region % 100 == 0 ? 1 + rnd.nextInt(99) : region % 100));
            plate = sb.toString();
        } while (usedPlates.contains(plate));
        usedPlates.add(plate);
        return plate;
    }
}
