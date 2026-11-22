package parking;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class CarView extends StackPane {

    public static final double CAR_WIDTH = 40;
    public static final double CAR_HEIGHT = 72;

    private final Car car;

    public CarView(Car car) {
        this.car = car;

        Rectangle body = new Rectangle(CAR_WIDTH, CAR_HEIGHT);
        body.setArcWidth(14);
        body.setArcHeight(14);
        body.setFill(car.getColor());
        body.setStroke(Color.BLACK);
        body.setStrokeWidth(1.5);

        Rectangle roof = new Rectangle(CAR_WIDTH - 8, CAR_HEIGHT / 2.6);
        roof.setArcWidth(8);
        roof.setArcHeight(8);
        roof.setFill(car.getColor().darker());
        roof.setStroke(Color.BLACK);

        Label plate = new Label(car.getPlate());
        plate.setFont(new Font(8));
        plate.setTextFill(Color.WHITE);

        StackPane roofPane = new StackPane(roof, plate);
        roofPane.setAlignment(Pos.CENTER);

        getChildren().addAll(body, roofPane);
        setMaxSize(CAR_WIDTH, CAR_HEIGHT);
    }

    public Car getCar() {
        return car;
    }
}
