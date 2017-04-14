package uk.co.endofhome.javoice.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import uk.co.endofhome.javoice.Config;

import java.io.File;

import static uk.co.endofhome.javoice.gui.UiController.mainMenuStackPane;

public class Settings extends JavoiceScreen implements GuiObservable {

    private GuiObserver guiObserver;
    StackPane settingsStackPane;
    private File invoiceTemplateConfig = new File(Config.invoiceFileTemplatePath().toString());
    private File invoiceXlsOutputPathConfig = new File(Config.invoiceXlsFileOutputPath().toString());
    private File invoicePdfOutputPathConfig = new File(Config.invoicePdfFileOutputPath().toString());
    private File salesLedgerOutputPathConfig = new File(Config.salesLedgerFileOutputPath().toString());
    private File customerDataOutputPathConfig = new File(Config.customerDataFilePath().toString());
    private File libreOfficePathConfig = new File(Config.libreOfficePath().toString());
    private Button updateInvoiceTemplatePath;
    private Button updateInvoiceXlsFileOutputPath;
    private Button updateInvoicePdfFileOutputPath;
    private Button updateSalesLedgerOutputPath;
    private Button updateCustomerLedgerOutputPath;
    private Button updateLibreOfficePath;

    public Settings() {
        initialise();
    }

    private void initialise() {
        GridPane settingsGrid = new GridPane();
        basicGridSetup(settingsGrid, "Settings", 1);

        Label invoiceFileTemplateLabel = initLabel(settingsGrid, "Invoice template file:", 0, 2);
        FileChooser invoiceTemplatePath = xlsFileChooser(invoiceTemplateConfig);
        updateInvoiceTemplatePath = initButton(settingsGrid, invoiceTemplateConfig.toString(), event -> newFileChoice(invoiceTemplatePath, updateInvoiceTemplatePath), 1, 2);

        Label invoiceXlsFileOutputLabel = initLabel(settingsGrid, "Invoice XLS output folder:", 0, 3);
        DirectoryChooser invoiceXlsFileOutputPath = directoryChooser(invoiceXlsOutputPathConfig);
        File initialXlsInvoiceDirectory = invoiceXlsFileOutputPath.getInitialDirectory();
        updateInvoiceXlsFileOutputPath = initButton(settingsGrid, initialXlsInvoiceDirectory.toString(), event -> newDirectoryChoice(invoiceXlsFileOutputPath, updateInvoiceXlsFileOutputPath), 1, 3);

        Label invoicePdfFileOutputLabel = initLabel(settingsGrid, "Invoice PDF output folder:", 0, 4);
        DirectoryChooser invoicePdfFileOutputPath = directoryChooser(invoicePdfOutputPathConfig);
        File initialPdfInvoiceDirectory = invoicePdfFileOutputPath.getInitialDirectory();
        updateInvoicePdfFileOutputPath = initButton(settingsGrid, initialPdfInvoiceDirectory.toString(), event -> newDirectoryChoice(invoicePdfFileOutputPath, updateInvoicePdfFileOutputPath), 1, 4);

        Label salesLedgerOutputLabel = initLabel(settingsGrid, "Sales ledger output folder:", 0, 5);
        DirectoryChooser salesLedgerOutputPath = directoryChooser(salesLedgerOutputPathConfig);
        File initialSalesLedgerDirectory = salesLedgerOutputPath.getInitialDirectory();
        updateSalesLedgerOutputPath = initButton(settingsGrid, initialSalesLedgerDirectory.toString(), event -> newDirectoryChoice(salesLedgerOutputPath, this.updateSalesLedgerOutputPath), 1, 5);

        Label customerDataLabel = initLabel(settingsGrid, "Customer data file:", 0, 6);
        FileChooser customerDataOutputPath = xlsFileChooser(customerDataOutputPathConfig);
        updateCustomerLedgerOutputPath = initButton(settingsGrid, customerDataOutputPathConfig.toString(), event -> newFileChoice(customerDataOutputPath, updateCustomerLedgerOutputPath), 1, 6);

        Label libreOfficeLabel = initLabel(settingsGrid, "LibreOffice ('soffice') installation folder:", 0, 7);
        DirectoryChooser libreOfficePath = directoryChooser(libreOfficePathConfig);
        File initialLIbreOfficeDirectory = libreOfficePath.getInitialDirectory();
        updateLibreOfficePath = initButton(settingsGrid, initialLIbreOfficeDirectory.toString(), event -> newDirectoryChoice(libreOfficePath, this.updateLibreOfficePath), 1, 7);

        Button updateSettings = initButton(settingsGrid, "Update", event -> System.out.println("settings updated..."), 0, 9);

        Button mainMenu = initButton(settingsGrid, "Main menu", event -> notifyGuiObserver(mainMenuStackPane), 0, 11);

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
        invoiceXlsOutputPathConfig = directoryChooser.showDialog(UiController.fixedScene.getWindow());
        if (invoiceXlsOutputPathConfig != null) {
            directoryChooser.setInitialDirectory(invoiceXlsOutputPathConfig);
            buttonToUpdate.setText(invoiceXlsOutputPathConfig.toString());
        }
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
