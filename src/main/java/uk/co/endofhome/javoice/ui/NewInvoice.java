package uk.co.endofhome.javoice.ui;

import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import static uk.co.endofhome.javoice.ui.UiController.invoiceDetailsStackPane;
import static uk.co.endofhome.javoice.ui.UiController.mainMenuStackPane;
import static uk.co.endofhome.javoice.ui.UiController.newCustomerStackPane;

public class NewInvoice extends JavoiceScreen implements Observable {

    StackPane newInvoiceStackPane;
    private Observer observer;

    public NewInvoice() {
        initialise();
    }

    private void initialise() {
        GridPane newInvoiceGrid = new GridPane();
        basicGridSetup(newInvoiceGrid, "New invoice", 1);

        Label customerSearchLabel = initLabel(newInvoiceGrid, "Search for existing customer:", 0, 11);
        TextField customerSearchField = initTextField(newInvoiceGrid, 1, "Search", 1, 11);
        Button customerSearchButton = initButton(newInvoiceGrid, "Search", event -> notifyObserver(invoiceDetailsStackPane), 2, 11);

        Label or = initLabelWithColumnSpanAndHAlignment(newInvoiceGrid, "- OR -", 0, 13, 3, HPos.CENTER);

        Button addCustomer = initButtonWithColumnSpanAndHAlignment(newInvoiceGrid, "Add new customer", event -> notifyObserver(newCustomerStackPane), 0, 15, 3, HPos.CENTER);

        Button mainMenu = initButton(newInvoiceGrid, "Main menu", event -> notifyObserver(mainMenuStackPane), 0, 26);

        newInvoiceStackPane = new StackPane(newInvoiceGrid);
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observer = observer;
    }

    @Override
    public void notifyObserver(StackPane stackPane) {
        observer.switchScene(stackPane);
    }
}
