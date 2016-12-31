package uk.co.endofhome.javoice;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.gui.UiController;

import java.io.IOException;
import java.nio.file.Files;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        CustomerStore customerStore;
        if (Files.exists(Config.customerDataFilePath())) {
            try {
                customerStore = CustomerStore.readFile(Config.customerDataFilePath(), 1);
            } catch (IOException e) {
                throw new IOException("There was a problem reading existing customer store" + e);
            }
        } else {
            customerStore = new CustomerStore();
        }
        Controller controller = new Controller(customerStore);
        UiController uiController = new UiController(controller);
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