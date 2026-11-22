package parking;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// экран
public class MainView {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final Parking parking = new Parking();
    private final SimClock clock = new SimClock();
    private final Journal journal = new Journal();

    private final StackPane root = new StackPane();
    private final Pane dragLayer = new Pane(); // слой поверх всего, тут летает машина пока тащим

    private final HBox carField = new HBox(6);
    private final GridPane parkingGrid = new GridPane();
    private final List<StackPane> spotPanes = new ArrayList<>();

    private final Label clockLabel = new Label();
    private final Label freeLabel = new Label();
    private final Label occupiedLabel = new Label();
    private final Label dayRevenueLabel = new Label();
    private final Label totalRevenueLabel = new Label();
    private final Label infoLabel = new Label("Выберите занятое место, чтобы посмотреть информацию.");
    private final ListView<String> messages = new ListView<>();

    private int totalRevenue = 0;
    private int selectedSpot = -1; // номер выбранного места, -1 если ничего не выбрано

    private CarView dragged = null;
    private ParkingSpot draggedFrom = null; // null - тащим из поля новых машин
    private double grabOffsetX;
    private double grabOffsetY;

    public MainView() {
        buildLayout();
        fillCarField();
        updateCounters();
        updateClockLabel(clock.getTime());
        journal.newDay(clock.getTime()); // первая строка журнала
        addMessage("Парковка открылась. " + clock.getTime().format(DATE_TIME));

        clock.addListener(this::onClockTick);
        clock.start();
    }

    public StackPane getRoot() {
        return root;
    }

    public SimClock getClock() {
        return clock;
    }

    public Journal getJournal() {
        return journal;
    }
    // интерфейс
    private void buildLayout() {
        // верхняя строка: счётчики, выручка и часы
        freeLabel.setFont(new Font(14));
        occupiedLabel.setFont(new Font(14));
        dayRevenueLabel.setFont(new Font(14));
        totalRevenueLabel.setFont(new Font(14));
        clockLabel.setFont(new Font(16));

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox stats = new HBox(15, freeLabel, occupiedLabel, spacer1, dayRevenueLabel, totalRevenueLabel, spacer2, clockLabel);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(8));

        Label fieldTitle = new Label("Доступные автомобили (перетащите на свободное место):");
        carField.setAlignment(Pos.CENTER);
        carField.setPadding(new Insets(6));
        carField.setMinHeight(CarView.CAR_HEIGHT + 20);
        carField.setStyle("-fx-border-color: #888; -fx-border-width: 1.5; -fx-background-color: #f4f4f4;");
        VBox fieldBox = new VBox(4, fieldTitle, carField);
        fieldBox.setPadding(new Insets(4, 8, 4, 8));

        VBox top = new VBox(stats, fieldBox);

        parkingGrid.setHgap(8);
        parkingGrid.setVgap(8);
        parkingGrid.setPadding(new Insets(10));
        parkingGrid.setAlignment(Pos.CENTER);
        for (ParkingSpot spot : parking.getSpots()) {
            StackPane cell = makeSpotCell(spot);
            spotPanes.add(cell);
            int col = (spot.getNumber() - 1) % 10;
            int row = (spot.getNumber() - 1) / 10;
            parkingGrid.add(cell, col, row);
        }
        Label parkingTitle = new Label("ПАРКОВКА (20 мест)");
        parkingTitle.setFont(new Font(14));
        VBox parkingBox = new VBox(4, parkingTitle, parkingGrid);
        parkingBox.setAlignment(Pos.CENTER);
        parkingBox.setPadding(new Insets(8));
        parkingBox.setStyle("-fx-border-color: #333; -fx-border-width: 2; -fx-background-color: #e8f0e8;");
        parkingBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        infoLabel.setPadding(new Insets(4));
        messages.setPrefHeight(160);
        Label msgTitle = new Label("Сообщения:");
        VBox bottom = new VBox(4, infoLabel, msgTitle, messages);
        bottom.setPadding(new Insets(6));

        BorderPane content = new BorderPane();
        content.setTop(top);
        content.setCenter(parkingBox);
        content.setBottom(bottom);
        BorderPane.setAlignment(parkingBox, Pos.CENTER);

        dragLayer.setPickOnBounds(false); // клики проходят сквозь пустые места слоя
        root.getChildren().addAll(content, dragLayer);
    }

    private StackPane makeSpotCell(ParkingSpot spot) {
        StackPane cell = new StackPane();
        cell.setMinSize(CarView.CAR_WIDTH + 14, CarView.CAR_HEIGHT + 14);
        cell.setMaxSize(CarView.CAR_WIDTH + 14, CarView.CAR_HEIGHT + 14);
        cell.setStyle("-fx-border-color: #666; -fx-border-style: dashed; -fx-border-width: 1.5; -fx-background-color: white;");
        Label num = new Label(String.valueOf(spot.getNumber()));
        num.setFont(new Font(10));
        num.setStyle("-fx-text-fill: #999;");
        StackPane.setAlignment(num, Pos.TOP_LEFT);
        cell.getChildren().add(num);
        return cell;
    }

    // заполняем поле 12 машинами
    private void fillCarField() {
        while (carField.getChildren().size() < 12) {
            CarView view = new CarView(Car.randomCar());
            makeDraggable(view, null);
            carField.getChildren().add(view);
        }
    }

    //перетаска
    private void makeDraggable(CarView view, ParkingSpot from) {
        view.setOnMousePressed(e -> {
            if (!e.isPrimaryButtonDown()) return;
            dragged = view;
            draggedFrom = from;
            if (from != null) {
                selectSpot(from.getNumber()); // клик по припаркованной машине - показать инфу
            }
            Bounds b = view.localToScene(view.getBoundsInLocal());
            grabOffsetX = e.getSceneX() - b.getMinX();
            grabOffsetY = e.getSceneY() - b.getMinY();
            // перекидываем машину на верхний слой чтобы таскать поверх всего
            if (view.getParent() instanceof Pane p) {
                p.getChildren().remove(view);
            }
            dragLayer.getChildren().add(view);
            view.relocate(b.getMinX(), b.getMinY());
            e.consume();
        });
    }

    public void installSceneHandlers(Scene scene) {
        scene.setOnMouseDragged(e -> {
            if (dragged == null) return;
            dragged.relocate(e.getSceneX() - grabOffsetX, e.getSceneY() - grabOffsetY);
            highlightSpotUnderCar();
        });
        scene.setOnMouseReleased(e -> {
            if (dragged == null) return;
            finishDrag();
        });
    }

    private void finishDrag() {
        clearHighlight();
        if (draggedFrom == null) {
            StackPane cell = findSpotUnderCar();
            if (cell != null) {
                int index = spotPanes.indexOf(cell);
                ParkingSpot spot = parking.getSpots().get(index);
                if (spot.isFree()) {
                    parkCar(dragged, cell, spot);
                } else {
                    addMessage("Место №" + spot.getNumber() + " занято, машина вернулась на поле.");
                    returnToField(dragged);
                }
            } else {
                returnToField(dragged);
            }
        } else {
            // нейронка делала
            Bounds carBounds = dragged.localToScene(dragged.getBoundsInLocal());
            Bounds parkingBounds = parkingGrid.localToScene(parkingGrid.getBoundsInLocal());
            if (!parkingBounds.intersects(carBounds)) {
                exitCar(dragged, draggedFrom);
            } else {
                StackPane cell = spotPanes.get(draggedFrom.getNumber() - 1);
                dragLayer.getChildren().remove(dragged);
                cell.getChildren().add(dragged);
            }
        }
        dragged = null;
        draggedFrom = null;
    }

    private void parkCar(CarView view, StackPane cell, ParkingSpot spot) {
        LocalDateTime time = clock.getTime();
        spot.park(view.getCar(), time);
        dragLayer.getChildren().remove(view);
        cell.getChildren().add(view);
        makeDraggable(view, spot); // теперь таскается уже с места
        journal.logEntry(view.getCar().getPlate(), spot.getNumber(), time);
        addMessage(time.format(DATE_TIME) + ". Автомобиль " + view.getCar().getPlate()
                + " заехал на место №" + spot.getNumber() + ".");
        updateCounters();
        fillCarField(); // на поле снова должно быть 12 машин
    }

    private void exitCar(CarView view, ParkingSpot spot) {
        LocalDateTime exitTime = clock.getTime();
        LocalDateTime entryTime = spot.getEntryTime();
        int payment = Tariff.calculate(entryTime, exitTime);
        String plate = view.getCar().getPlate();

        spot.clear();
        dragLayer.getChildren().remove(view);
        if (selectedSpot == spot.getNumber()) {
            selectedSpot = -1;
        }

        totalRevenue += payment;
        journal.logExit(plate, entryTime, exitTime, payment);
        addMessage(exitTime.format(DATE_TIME) + ". Автомобиль " + plate + " выехал. Въезд: "
                + entryTime.format(DATE_TIME) + ". Оплачено: " + payment + " ₽.");
        infoLabel.setText("Выезд: " + plate + " | въезд " + entryTime.format(DATE_TIME)
                + " | выезд " + exitTime.format(DATE_TIME) + " | оплачено " + payment + " ₽");
        updateCounters();
        updateRevenue();
    }

    private void returnToField(CarView view) {
        dragLayer.getChildren().remove(view);
        makeDraggable(view, null);
        carField.getChildren().add(view);
    }

    private StackPane findSpotUnderCar() {
        Bounds b = dragged.localToScene(dragged.getBoundsInLocal());
        double cx = b.getMinX() + b.getWidth() / 2;
        double cy = b.getMinY() + b.getHeight() / 2;
        Bounds parkingBounds = parkingGrid.localToScene(parkingGrid.getBoundsInLocal());
        if (!parkingBounds.contains(cx, cy)) {
            return null;
        }
        for (StackPane cell : spotPanes) {
            Bounds cb = cell.localToScene(cell.getBoundsInLocal());
            if (cb.contains(cx, cy)) {
                return cell;
            }
        }
        return null;
    }

    private void highlightSpotUnderCar() {
        if (draggedFrom != null) return; // для выезда подсветка не нужна
        clearHighlight();
        StackPane cell = findSpotUnderCar();
        if (cell != null) {
            int index = spotPanes.indexOf(cell);
            boolean free = parking.getSpots().get(index).isFree();
            cell.setStyle("-fx-border-color: " + (free ? "#2e8b22" : "#cc2222")
                    + "; -fx-border-width: 2.5; -fx-background-color: " + (free ? "#d8f0d8" : "#f0d8d8") + ";");
        }
    }

    private void clearHighlight() {
        for (StackPane cell : spotPanes) {
            cell.setStyle("-fx-border-color: #666; -fx-border-style: dashed; -fx-border-width: 1.5; -fx-background-color: white;");
        }
    }

    public void selectSpot(int number) {
        selectedSpot = number;
        refreshInfo();
    }


    private void onClockTick(LocalDateTime time) {
        updateClockLabel(time);

        // конец дня итоги
        if (time.getHour() == 23 && time.getMinute() == 59) {
            journal.daySummary(time);
            addMessage(time.format(DATE_TIME) + ". Итог дня. Выручка: " + journal.getDayRevenue() + " ₽.");
        }
        // новый день
        if (time.getHour() == 0 && time.getMinute() == 0) {
            journal.newDay(time);
            addMessage(time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ". Начало записей нового дня.");
            updateRevenue();
        }
        refreshInfo();
    }

    private void updateClockLabel(LocalDateTime time) {
        clockLabel.setText(time.format(CLOCK_FMT));
    }

    private void updateCounters() {
        freeLabel.setText("Свободно: " + parking.freeCount());
        occupiedLabel.setText("Занято: " + parking.occupiedCount());
    }

    private void updateRevenue() {
        dayRevenueLabel.setText("Выручка за день: " + journal.getDayRevenue() + " ₽");
        totalRevenueLabel.setText("Выручка всего: " + totalRevenue + " ₽");
    }

    // обнова строки с инфой о выбранном месте
    private void refreshInfo() {
        if (selectedSpot < 1) return;
        ParkingSpot spot = parking.getSpot(selectedSpot);
        if (spot.isFree()) {
            selectedSpot = -1;
            return;
        }
        LocalDateTime now = clock.getTime();
        LocalDateTime entry = spot.getEntryTime();
        Duration d = Duration.between(entry, now);
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        int costNow = Tariff.calculate(entry, now);
        infoLabel.setText("Место №" + spot.getNumber() + ": " + spot.getCar().getPlate()
                + " | въезд " + entry.format(DATE_TIME)
                + " | стоит " + hours + " ч " + minutes + " мин"
                + " | к оплате сейчас: " + costNow + " ₽");
    }

    private void addMessage(String text) {
        messages.getItems().add(text);
        messages.scrollTo(messages.getItems().size() - 1);
    }
}
