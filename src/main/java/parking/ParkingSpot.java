package parking;

import java.time.LocalDateTime;

//место на парковки
public class ParkingSpot {

    private final int number;
    private Car car;
    private LocalDateTime entryTime;

    public ParkingSpot(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public boolean isFree() {
        return car == null;
    }

    public Car getCar() {
        return car;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void park(Car car, LocalDateTime entryTime) {
        this.car = car;
        this.entryTime = entryTime;
    }

    public void clear() {
        this.car = null;
        this.entryTime = null;
    }
}
