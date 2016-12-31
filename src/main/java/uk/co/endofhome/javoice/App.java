package uk.co.endofhome.javoice;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.co.endofhome.javoice.ui.UiController;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        UiController uiController = new UiController();
        uiController.setTheStage(primaryStage);
    }

    public void runGui() {
        launch();
    }

    public void runCli() {
        System.out.println("I'm running!");
        System.out.println("I'm exiting!");
        System.exit(0);
    }
}