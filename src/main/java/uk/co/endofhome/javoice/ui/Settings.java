package uk.co.endofhome.javoice.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;

import static uk.co.endofhome.javoice.ui.UiController.mainMenuStackPane;

public class Settings extends JavoiceScreen implements Observable {

    private Observer observer;
    StackPane settingsStackPane;
    private File fakeInvoiceTemplateConfig = new File(String.format("%s/Javoice/Templates/invoice-template.xls", System.getProperty("user.home")));;
    private File fakeInvoiceOutputPathConfig = new File(String.format("%s/Javoice/Invoices", System.getProperty("user.home")));;
    private File fakeSalesLedgerOutputPathConfig = new File(String.format("%s/Javoice/Sales Ledger", System.getProperty("user.home")));;
    private File fakeCustomerDataOutputPathConfig = new File(String.format("%s/Javoice/Customer Data/Customers.xls", System.getProperty("user.home")));;
    private Button updateInvoiceTemplatePath;
    private Button updateInvoiceFileOutputPath;
    private Button updateSalesLedgerOutputPath;
    private Button updateCustomerLedgerOutputPath;

    public Settings() {
        initialise();
    }

    private void initialise() {
        GridPane settingsGrid = new GridPane();
        basicGridSetup(settingsGrid, "Settings", 1);

        Label invoiceFileTemplateLabel = initLabel(settingsGrid, "Invoice template file:", 0, 2);
        FileChooser invoiceTemplatePath = xlsFileChooser(fakeInvoiceTemplateConfig);
        updateInvoiceTemplatePath = initButton(settingsGrid, fakeInvoiceTemplateConfig.toString(), event -> newFileChoice(invoiceTemplatePath, updateInvoiceTemplatePath), 1, 2);

        Label invoiceFileOutputLabel = initLabel(settingsGrid, "Invoice output folder:", 0, 3);
        DirectoryChooser invoiceFileOutputPath = directoryChooser(fakeInvoiceOutputPathConfig);
        File initialDirectory = invoiceFileOutputPath.getInitialDirectory();
        updateInvoiceFileOutputPath = initButton(settingsGrid, initialDirectory.toString(), event -> newDirectoryChoice(invoiceFileOutputPath, updateInvoiceFileOutputPath), 1, 3);

        Label salesLedgerOutputLabel = initLabel(settingsGrid, "Sales ledger output folder:", 0, 4);
        DirectoryChooser salesLedgerOutputPath = directoryChooser(fakeSalesLedgerOutputPathConfig);
        File initialSalesLedgerDirectory = salesLedgerOutputPath.getInitialDirectory();
        updateSalesLedgerOutputPath = initButton(settingsGrid, initialSalesLedgerDirectory.toString(), event -> newDirectoryChoice(salesLedgerOutputPath, this.updateSalesLedgerOutputPath), 1, 4);

        Label customerDataLabel = initLabel(settingsGrid, "Customer data file:", 0, 5);
        FileChooser customerDataOutputPath = xlsFileChooser(fakeCustomerDataOutputPathConfig);
        updateCustomerLedgerOutputPath = initButton(settingsGrid, fakeCustomerDataOutputPathConfig.toString(), event -> newFileChoice(customerDataOutputPath, updateCustomerLedgerOutputPath), 1, 5);

        Button updateSettings = initButton(settingsGrid, "Update", event -> System.out.println("settings updated..."), 0, 7);

        Button mainMenu = initButton(settingsGrid, "Main menu", event -> notifyObserver(mainMenuStackPane), 0, 9);

        settingsStackPane = new StackPane(settingsGrid);
    }

    private DirectoryChooser directoryChooser(File file) {
        DirectoryChooser invoiceFileOutputPath = new DirectoryChooser();
        invoiceFileOutputPath.setInitialDirectory(file);
        return invoiceFileOutputPath;
    }

    private FileChooser xlsFileChooser(File file) {
        FileChooser invoiceTemplatePath = new FileChooser();
        File dataDirectory = new File(file.getParent());
        invoiceTemplatePath.setInitialDirectory(dataDirectory);
        invoiceTemplatePath.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel '97-2003 spreadsheet", "*.xls"));
        return invoiceTemplatePath;
    }

    private void newFileChoice(FileChooser fileChooser, Button buttonToUpdate) {
        File fileConfig = fileChooser.showOpenDialog(UiController.fixedScene.getWindow());
        if (fileConfig != null) {
            fileChooser.setInitialDirectory(new File(fileConfig.getParent()));
            buttonToUpdate.setText(fileConfig.toString());
        }
    }

    private void newDirectoryChoice(DirectoryChooser directoryChooser, Button buttonToUpdate) {
        fakeInvoiceOutputPathConfig = directoryChooser.showDialog(UiController.fixedScene.getWindow());
        if (fakeInvoiceOutputPathConfig != null) {
            directoryChooser.setInitialDirectory(fakeInvoiceOutputPathConfig);
            buttonToUpdate.setText(fakeInvoiceOutputPathConfig.toString());
        }
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
