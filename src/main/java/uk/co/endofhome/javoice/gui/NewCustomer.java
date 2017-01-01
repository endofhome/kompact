package uk.co.endofhome.javoice.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import uk.co.endofhome.javoice.Observable;
import uk.co.endofhome.javoice.Observer;

import java.io.IOException;

import static uk.co.endofhome.javoice.gui.UiController.mainMenuStackPane;

public class NewCustomer extends JavoiceScreen implements GuiObservable, Observable {

    StackPane newCustomerStackPane;
    private GuiObserver guiObserver;
    private Observer observer;

    public NewCustomer() {
        initialise();
    }

    private void initialise() {
        GridPane addCustomerGrid = new GridPane();
        basicGridSetup(addCustomerGrid, "New customer", 1);

        // TODO: Add appropriate character limits for textfields.

        Label customerName = new Label("Name");
        addCustomerGrid.add(customerName, 0, 2);

        TextField customerNameField = new TextField();
        addCustomerGrid.add(customerNameField, 1, 2);

        Label customerAddressOne = new Label("Address (1)");
        addCustomerGrid.add(customerAddressOne, 0, 3);

        TextField customerAddressOneField = new TextField();
        addCustomerGrid.add(customerAddressOneField, 1, 3);

        Label customerAddressTwo = new Label("Address (2)");
        addCustomerGrid.add(customerAddressTwo, 0, 4);

        TextField customerAddressTwoField = new TextField();
        addCustomerGrid.add(customerAddressTwoField, 1, 4);

        Label customerPostcode = new Label("Postcode");
        addCustomerGrid.add(customerPostcode, 0, 5);

        TextField customerPostcodeField = new TextField();
        addCustomerGrid.add(customerPostcodeField, 1, 5);

        Label customerPhoneNum = new Label("Phone number:");
        addCustomerGrid.add(customerPhoneNum, 0, 6);

        TextField customerPhoneField = new TextField();
        addCustomerGrid.add(customerPhoneField, 1, 6);

        Button mainMenu = initButton(addCustomerGrid, "Main menu", event -> notifyGuiObserver(mainMenuStackPane), 0, 7);

        Button addCustomer = initButton(addCustomerGrid, "Add customer", event -> {
            try {
                newCustomer(
                        customerNameField.getText(),
                        customerAddressOneField.getText(),
                        customerAddressTwoField.getText(),
                        customerPostcodeField.getText(),
                        customerPhoneField.getText()
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 5, 7);

        newCustomerStackPane = new StackPane(addCustomerGrid);
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
    public void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) throws IOException {
        /* TODO: adding a customer should take you either to the invoice details screen, or back to the new customer screen.
           TODO: ...depending on where you came from.
        */
        observer.newCustomer(name, addressOne, addressTwo, postcode, phoneNumber);
    }

    // TODO: method/s not required, side-effect of the fact that the observer pattern stuff isn't quite the right tool for the job?
    @Override
    public void searchForCustomer(String name) {}
}
