package parking;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// точка входа в программу
public class Main extends Application {

    private MainView view;

    @Override
    public void start(Stage stage) {
        view = new MainView();
        Scene scene = new Scene(view.getRoot(), 1000, 780);
        view.installSceneHandlers(scene);

        stage.setTitle("Парковка аэропорта");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        view.getClock().stop();
        view.getJournal().close(view.getClock().getTime());
    }
}
