package uk.co.endofhome.javoice.gui;

import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import static uk.co.endofhome.javoice.gui.UiController.invoiceDetailsStackPane;
import static uk.co.endofhome.javoice.gui.UiController.mainMenuStackPane;
import static uk.co.endofhome.javoice.gui.UiController.newCustomerStackPane;

public class NewInvoice extends JavoiceScreen implements GuiObservable {

    StackPane newInvoiceStackPane;
    private GuiObserver guiObserver;

    public NewInvoice() {
        initialise();
    }

    private void initialise() {
        GridPane newInvoiceGrid = new GridPane();
        basicGridSetup(newInvoiceGrid, "New invoice", 1);

        Label customerSearchLabel = initLabel(newInvoiceGrid, "Search for existing customer:", 0, 11);
        TextField customerSearchField = initTextField(newInvoiceGrid, 1, "Search", 1, 11);
        Button customerSearchButton = initButton(newInvoiceGrid, "Search", event -> notifyGuiObserver(invoiceDetailsStackPane), 2, 11);

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
}
