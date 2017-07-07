package uk.co.endofhome.javoice.gui;

import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import uk.co.endofhome.javoice.Observable;
import uk.co.endofhome.javoice.Observer;
import uk.co.endofhome.javoice.customer.Customer;

import static uk.co.endofhome.javoice.gui.UiController.invoiceDetailsStackPane;
import static uk.co.endofhome.javoice.gui.UiController.mainMenuStackPane;
import static uk.co.endofhome.javoice.gui.UiController.newCustomerStackPane;

public class NewInvoice extends JavoiceScreen implements GuiObservable, Observable {

    StackPane newInvoiceStackPane;
    private GuiObserver guiObserver;
    private Observer observer;

    public NewInvoice() {
        initialise();
    }

    private void initialise() {
        GridPane newInvoiceGrid = new GridPane();
        basicGridSetup(newInvoiceGrid, "New invoice", 1);

        Label customerSearchLabel = initLabel(newInvoiceGrid, "Search for existing customer:", 0, 11);
        TextField customerSearchField = initTextField(newInvoiceGrid, 1, "", 1, 11);
        Button customerSearchButton = initButton(newInvoiceGrid, "Search", event -> searchForCustomer(customerSearchField.getText()), 2, 11);

        Label or = initLabelWithColumnSpanAndHAlignment(newInvoiceGrid, "- OR -", 0, 13, 3, HPos.CENTER);

        Button addCustomer = initButtonWithColumnSpanAndHAlignment(newInvoiceGrid, "Add new customer", event -> notifyGuiObserver(newCustomerStackPane), 0, 15, 3, HPos.CENTER);

        Button mainMenu = initButton(newInvoiceGrid, "Main menu", event -> notifyGuiObserver(mainMenuStackPane), 0, 26);

        newInvoiceStackPane = new StackPane(newInvoiceGrid);
    }

    @Override
    public void registerGuiObserver(GuiObserver guiObserver) {
        this.guiObserver = guiObserver;
    }

    @Override
    public void notifyGuiObserver(StackPane stackPane) {
        guiObserver.switchScene(stackPane);
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observer = observer;
    }

    @Override
    public void searchForCustomer(String name) {
        Customer customer = observer.findCustomer(name);
        if (customer != null) {
            observer.setCurrentCustomer(customer);
            guiObserver.updateInvoiceDetails();
            notifyGuiObserver(invoiceDetailsStackPane);
        } else {
            notifyGuiObserver(newInvoiceStackPane);
        }
    }

    // TODO: method/s not required, side-effect of the fact that the observer pattern stuff isn't quite the right tool for the job?
    @Override
    public void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) {}
}
