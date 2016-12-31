package uk.co.endofhome.javoice.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static uk.co.endofhome.javoice.ui.UiController.mainMenuStackPane;

public class InvoiceDetails extends JavoiceScreen implements Observable {

    private Observer observer;
    StackPane invoiceDetailsStackPane;
    private String fakeOrderNumber = "1053";

    public InvoiceDetails() {
        initialise();
    }

    private void initialise() {
        // Fake customer for testing purposes...
        FakeCustomer fakeCustomer = new FakeCustomer();

        GridPane invoiceDetailsGrid = new GridPane();
        basicGridSetup(invoiceDetailsGrid, "Invoice details:", 1);

        Label nameLabel = initLabel(invoiceDetailsGrid, "Name:", 0, 3);
        TextField nameField = initTextField(invoiceDetailsGrid, 3, fakeCustomer.name, 0,4);

        Label addressOne = initLabel(invoiceDetailsGrid, "Address (1):", 0, 5);
        TextField addressField = initTextField(invoiceDetailsGrid, 4, fakeCustomer.addressOne, 0,6);

        Label addressTwo = initLabel(invoiceDetailsGrid, "Address (2):", 0, 7);
        TextField addressTwoField = initTextField(invoiceDetailsGrid, 3, fakeCustomer.addressTwo, 0,8);

        Label postcodeLabel = initLabel(invoiceDetailsGrid, "Postcode:", 3, 7);
        TextField postcodeField = initTextField(invoiceDetailsGrid, 1, fakeCustomer.postcode, 3, 8);

        Label dateLabel = initLabel(invoiceDetailsGrid, "Date:", 5, 3);
        TextField dateField = initTextField(invoiceDetailsGrid, 1, todaysDate(), 5, 4);

        Label orderNumberLabel = initLabel(invoiceDetailsGrid, "Order Number:", 5, 5);
        TextField orderNumberField = initTextField(invoiceDetailsGrid, 1, fakeOrderNumber, 5, 6);

        Label accountCodeLabel = initLabel(invoiceDetailsGrid, "Account code:", 5, 7);
        TextField accountCodeField = initTextField(invoiceDetailsGrid, 1, fakeCustomer.accountCode, 5, 8);

        Label quantity = initLabel(invoiceDetailsGrid, "Quantity", 0, 13);
        Label description = initLabel(invoiceDetailsGrid, "Description", 1, 13);
        Label unitPrice = initLabel(invoiceDetailsGrid, "Unit price", 4, 13);
        Label total = initLabel(invoiceDetailsGrid, "Total", 5, 13);

        List<TextField> quantitiyFieldList = new ArrayList<>();
        for (int i = 0; i <= 16; i++) {
            quantitiyFieldList.add(new TextField());
            quantitiyFieldList.get(i).setMaxWidth(75);
        }

        List<TextField> descriptionFieldList = new ArrayList<>();
        for (int i = 0; i <= 16; i++) {
            descriptionFieldList.add(new TextField());
            descriptionFieldList.get(i).setMinWidth(200);
            GridPane.setColumnSpan(descriptionFieldList.get(i), 3);
        }

        List<TextField> unitPriceList = new ArrayList<>();
        for (int i = 0; i <= 16; i++) {
            unitPriceList.add(new TextField());
            unitPriceList.get(i).setMaxWidth(75);
        }

        List<TextField> totalList = new ArrayList<>();
        for (int i = 0; i <= 16; i++) {
            totalList.add(new TextField());
        }

        for (int i = 0; i <= 16; i++) {
            invoiceDetailsGrid.add(quantitiyFieldList.get(i), 0, 14 + i);
            invoiceDetailsGrid.add(descriptionFieldList.get(i), 1, 14 + i);
            invoiceDetailsGrid.add(unitPriceList.get(i), 4, 14 + i);
            invoiceDetailsGrid.add(totalList.get(i), 5, 14 + i);
        }

        Button mainMenu = initButton(invoiceDetailsGrid, "Main menu", event -> notifyObserver(mainMenuStackPane), 0, 31);

        Button submitInvoice = initButton(invoiceDetailsGrid, "Submit", event -> System.out.println("INVOICE SUBMITTED!"), 2, 31);

        ScrollPane invoiceDetailsScroll = new ScrollPane(invoiceDetailsGrid);
        invoiceDetailsScroll.setFitToWidth(true);
        invoiceDetailsStackPane = new StackPane(invoiceDetailsScroll);
        // TODO: this doesn't work, for some reason:
        quantitiyFieldList.get(0).requestFocus();
    }

    private String todaysDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter ukFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return now.format(ukFormat);
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
