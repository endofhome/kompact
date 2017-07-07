package uk.co.endofhome.javoice;

import javafx.application.Application;
import javafx.stage.Stage;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.gui.UiController;
import uk.co.endofhome.javoice.pdf.PdfConvertor;

import java.io.IOException;
import java.nio.file.Files;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        CustomerStore customerStore;
        PdfConvertor pdfConvertor = new PdfConvertor();
        if (Files.exists(Config.Companion.customerDataFilePath())) {
            try {
                customerStore = CustomerStore.Companion.readFile(Config.Companion.customerDataFilePath(), 1);
            } catch (IOException e) {
                throw new IOException("There was a problem reading existing customer store" + e);
            }
        } else {
            customerStore = new CustomerStore();
        }
        Controller controller = new Controller(customerStore, pdfConvertor);
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