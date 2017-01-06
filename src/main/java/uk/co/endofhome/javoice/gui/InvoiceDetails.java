package uk.co.endofhome.javoice.gui;

import com.googlecode.totallylazy.Option;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import uk.co.endofhome.javoice.Observable;
import uk.co.endofhome.javoice.Observer;
import uk.co.endofhome.javoice.customer.Customer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.totallylazy.Sequences.sequence;
import static uk.co.endofhome.javoice.gui.UiController.mainMenuStackPane;

public class InvoiceDetails extends JavoiceScreen implements GuiObservable, Observable {

    StackPane invoiceDetailsStackPane;
    private GuiObserver guiObserver;
    private Observer observer;
    private Customer customer;
    private TextField orderNumberField;

    public InvoiceDetails(Option<Customer> customer) {
        this.customer = ensureCustomer(customer);
        initialise();
    }

    private Customer ensureCustomer(Option<Customer> customer) {
        if (customer.isDefined()) {
            return this.customer = customer.get();
        }
        return this.customer = new FakeCustomer();
    }

    private void initialise() {
        GridPane invoiceDetailsGrid = new GridPane();
        basicGridSetup(invoiceDetailsGrid, "Invoice details:", 1);

        Label nameLabel = initLabel(invoiceDetailsGrid, "Name:", 0, 3);
        TextField nameField = initTextField(invoiceDetailsGrid, 3, customer.name, 0,4);

        Label addressOne = initLabel(invoiceDetailsGrid, "Address (1):", 0, 5);
        TextField addressField = initTextField(invoiceDetailsGrid, 4, customer.addressOne, 0,6);

        Label addressTwo = initLabel(invoiceDetailsGrid, "Address (2):", 0, 7);
        TextField addressTwoField = initTextField(invoiceDetailsGrid, 3, customer.addressTwo, 0,8);

        Label postcodeLabel = initLabel(invoiceDetailsGrid, "Postcode:", 3, 7);
        TextField postcodeField = initTextField(invoiceDetailsGrid, 1, customer.postcode, 3, 8);

        Label dateLabel = initLabel(invoiceDetailsGrid, "Date:", 5, 3);
        TextField dateField = initTextField(invoiceDetailsGrid, 1, todaysDate(), 5, 4);

        Label orderNumberLabel = initLabel(invoiceDetailsGrid, "Order Number:", 5, 5);
        orderNumberField = initTextField(invoiceDetailsGrid, 1, "", 5, 6);

        Label accountCodeLabel = initLabel(invoiceDetailsGrid, "Account code:", 5, 7);
        TextField accountCodeField = initTextField(invoiceDetailsGrid, 1, customer.accountCode, 5, 8);

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

        Button mainMenu = initButton(invoiceDetailsGrid, "Main menu", event -> notifyGuiObserver(mainMenuStackPane), 0, 31);

        Button submitInvoice = initButton(invoiceDetailsGrid, "Submit", event -> {
            try {
                newInvoice();
            } catch (IOException e) {
                // TODO: fix this mess too. should be throwing this exception somewhere, not swallowing it.
            }
        }, 2, 31);

        ScrollPane invoiceDetailsScroll = new ScrollPane(invoiceDetailsGrid);
        invoiceDetailsScroll.setFitToWidth(true);
        invoiceDetailsStackPane = new StackPane(invoiceDetailsScroll);
        // TODO: this doesn't work, for some reason:
        quantitiyFieldList.get(0).requestFocus();
    }

    private void newInvoice() throws IOException {
        // TODO: get the item lines by looping through? Empty sequence for now.
        observer.newInvoice(customer, orderNumberField.getText(), sequence());
    }

    private String todaysDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter ukFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return now.format(ukFormat);
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

    // TODO: method/s not required, side-effect of the fact that the observer pattern stuff isn't quite the right tool for the job?
    @Override
    public void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) throws IOException {}

    @Override
    public void searchForCustomer(String name) throws Exception {}
}
