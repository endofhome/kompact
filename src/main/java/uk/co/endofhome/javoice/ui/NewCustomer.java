package uk.co.endofhome.javoice.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import static uk.co.endofhome.javoice.ui.UiController.mainMenuStackPane;

public class NewCustomer extends JavoiceScreen implements Observable {

    StackPane newCustomerStackPane;
    private Observer observer;

    public NewCustomer() {
        initialise();
    }

    private void initialise() {
        GridPane addCustomerGrid = new GridPane();
        basicGridSetup(addCustomerGrid, "New customer", 1);

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

        Button mainMenu = initButton(addCustomerGrid, "Main menu", event -> notifyObserver(mainMenuStackPane), 0, 7);

        Button addCustomer = initButton(addCustomerGrid, "Add customer", event -> printCustomerDetails(
                customerNameField,
                customerAddressOneField,
                customerAddressTwoField,
                customerPostcodeField,
                customerPhoneField
        ), 5, 7);

        newCustomerStackPane = new StackPane(addCustomerGrid);
    }

    private void printCustomerDetails(TextField nameField, TextField customerAddressOneField, TextField customerAddressTwoField, TextField customerPostcodeField, TextField customerPhoneField) {
        System.out.println(String.format("...adding customer %s, from %s %s %s, ph. %s",
                nameField.getText(),
                customerAddressOneField.getText(),
                customerAddressTwoField.getText(),
                customerPostcodeField.getText(),
                customerPhoneField.getText()));
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
