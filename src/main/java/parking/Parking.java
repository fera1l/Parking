package parking;

import java.util.ArrayList;
import java.util.List;

//парковка
public class Parking {

    public static final int SPOTS_COUNT = 20;

    private final List<ParkingSpot> spots = new ArrayList<>();

    public Parking() {
        for (int i = 1; i <= SPOTS_COUNT; i++) {
            spots.add(new ParkingSpot(i));
        }
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }

    public ParkingSpot getSpot(int number) {
        return spots.get(number - 1);
    }

    public int freeCount() {
        int n = 0;
        for (ParkingSpot s : spots) {
            if (s.isFree()) n++;
        }
        return n;
    }

    public int occupiedCount() {
        return SPOTS_COUNT - freeCount();
    }
}
