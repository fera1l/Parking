package parking;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 1 сек = 1 мин
public class SimClock {

    // старт
    public static final LocalDateTime START_TIME = LocalDateTime.of(2027, 1, 1, 8, 0);

    private LocalDateTime time = START_TIME;
    private final Timeline timeline;
    private final List<ClockListener> listeners = new ArrayList<>();

    public interface ClockListener {
        void onTick(LocalDateTime time);
    }

    public SimClock() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void addListener(ClockListener listener) {
        listeners.add(listener);
    }

    private void tick() {
        time = time.plusMinutes(1);
        for (ClockListener l : listeners) {
            l.onTick(time);
        }
    }
}
