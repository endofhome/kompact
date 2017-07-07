package uk.co.endofhome.javoice.gui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import uk.co.endofhome.javoice.Controller;

public class UiController implements GuiObserver {

    static Scene fixedScene;
    static StackPane mainMenuStackPane;
    static StackPane newInvoiceStackPane;
    static StackPane invoiceDetailsStackPane;
    static StackPane newCustomerStackPane;
    static StackPane settingsStackPane;
    public Controller controller;

    public UiController(Controller controller) {
        this.controller = controller;
        initialise();
    }

    private void initialise() {
        MainMenu mainMenu = new MainMenu();
        mainMenu.registerGuiObserver(this);
        mainMenuStackPane = mainMenu.mainMenuStackPane;

        NewInvoice newInvoice = new NewInvoice();
        newInvoice.registerGuiObserver(this);
        newInvoice.registerObserver(controller);
        newInvoiceStackPane = newInvoice.newInvoiceStackPane;

        updateInvoiceDetails();

        NewCustomer newCustomer = new NewCustomer();
        newCustomer.registerObserver(controller);
        newCustomer.registerGuiObserver(this);
        newCustomerStackPane = newCustomer.newCustomerStackPane;

        Settings settings = new Settings();
        settings.registerGuiObserver(this);
        settingsStackPane = settings.settingsStackPane;
    }

    public void setTheStage(Stage primaryStage) {
        fixedScene = new Scene(mainMenuStackPane);
        mainMenuStackPane.requestFocus();
        primaryStage.setTitle("Javoice");
        primaryStage.setScene(fixedScene);
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(new Image("file:resources/icons/javoice_icon.png"));
        primaryStage.show();
    }

    public void switchScene(StackPane layout) {
        fixedScene.setRoot(layout);
        layout.requestFocus();
    }

    @Override
    public void updateInvoiceDetails() {
        InvoiceDetails invoiceDetails = new InvoiceDetails(controller.getCurrentCustomer());
        invoiceDetails.registerGuiObserver(this);
        invoiceDetails.registerObserver(controller);
        invoiceDetailsStackPane = invoiceDetails.invoiceDetailsStackPane;
    }
}
