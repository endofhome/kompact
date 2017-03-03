package uk.co.endofhome.javoice.gui

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser

import java.io.File

import uk.co.endofhome.javoice.gui.UiController.Companion.mainMenuStackPane

class Settings : JavoiceScreen(), GuiObservable {

    private lateinit var guiObserver: GuiObserver
    lateinit var settingsStackPane: StackPane
    private val fakeInvoiceTemplateConfig = File(String.format("%s/Javoice/Templates/invoice-template.xls", System.getProperty("user.home")))
    private var fakeInvoiceOutputPathConfig: File = File(String.format("%s/Javoice/Invoices", System.getProperty("user.home")))
    private val fakeSalesLedgerOutputPathConfig = File(String.format("%s/Javoice/Sales Ledger", System.getProperty("user.home")))
    private val fakeCustomerDataOutputPathConfig = File(String.format("%s/Javoice/Customer Data/Customers.xls", System.getProperty("user.home")))
    private lateinit var updateInvoiceTemplatePath: Button
    private lateinit var updateInvoiceFileOutputPath: Button
    private lateinit var updateSalesLedgerOutputPath: Button
    private lateinit var updateCustomerLedgerOutputPath: Button

    init {
        initialise()
    }

    private fun initialise() {
        val settingsGrid = GridPane()
        basicGridSetup(settingsGrid, "Settings", 1)

        val invoiceFileTemplateLabel = initLabel(settingsGrid, "Invoice template file:", 0, 2)
        val invoiceTemplatePath = xlsFileChooser(fakeInvoiceTemplateConfig)
        updateInvoiceTemplatePath = initButton(settingsGrid, fakeInvoiceTemplateConfig.toString(), EventHandler { event -> newFileChoice(invoiceTemplatePath, updateInvoiceTemplatePath) }, 1, 2)

        val invoiceFileOutputLabel = initLabel(settingsGrid, "Invoice output folder:", 0, 3)
        val invoiceFileOutputPath = directoryChooser(fakeInvoiceOutputPathConfig)
        val initialDirectory = invoiceFileOutputPath.initialDirectory
        updateInvoiceFileOutputPath = initButton(settingsGrid, initialDirectory.toString(), EventHandler { event -> newDirectoryChoice(invoiceFileOutputPath, updateInvoiceFileOutputPath) }, 1, 3)

        val salesLedgerOutputLabel = initLabel(settingsGrid, "Sales ledger output folder:", 0, 4)
        val salesLedgerOutputPath = directoryChooser(fakeSalesLedgerOutputPathConfig)
        val initialSalesLedgerDirectory = salesLedgerOutputPath.initialDirectory
        updateSalesLedgerOutputPath = initButton(settingsGrid, initialSalesLedgerDirectory.toString(), EventHandler { event -> newDirectoryChoice(salesLedgerOutputPath, this.updateSalesLedgerOutputPath) }, 1, 4)

        val customerDataLabel = initLabel(settingsGrid, "Customer data file:", 0, 5)
        val customerDataOutputPath = xlsFileChooser(fakeCustomerDataOutputPathConfig)
        updateCustomerLedgerOutputPath = initButton(settingsGrid, fakeCustomerDataOutputPathConfig.toString(), EventHandler { event -> newFileChoice(customerDataOutputPath, updateCustomerLedgerOutputPath) }, 1, 5)

        val updateSettings = initButton(settingsGrid, "Update", EventHandler { event -> println("settings updated...") }, 0, 7)

        val mainMenu = initButton(settingsGrid, "Main menu", EventHandler { event -> notifyGuiObserver(mainMenuStackPane) }, 0, 9)

        settingsStackPane = StackPane(settingsGrid)
    }

    private fun directoryChooser(file: File): DirectoryChooser {
        val invoiceFileOutputPath = DirectoryChooser()
        invoiceFileOutputPath.initialDirectory = file
        return invoiceFileOutputPath
    }

    private fun xlsFileChooser(file: File): FileChooser {
        val invoiceTemplatePath = FileChooser()
        val dataDirectory = File(file.parent)
        invoiceTemplatePath.initialDirectory = dataDirectory
        invoiceTemplatePath.extensionFilters.add(FileChooser.ExtensionFilter("Excel '97-2003 spreadsheet", "*.xls"))
        return invoiceTemplatePath
    }

    private fun newFileChoice(fileChooser: FileChooser, buttonToUpdate: Button) {
        val fileConfig = fileChooser.showOpenDialog(UiController.fixedScene.window)
        if (fileConfig != null) {
            fileChooser.initialDirectory = File(fileConfig.parent)
            buttonToUpdate.text = fileConfig.toString()
        }
    }

    private fun newDirectoryChoice(directoryChooser: DirectoryChooser, buttonToUpdate: Button) {
        fakeInvoiceOutputPathConfig = directoryChooser.showDialog(UiController.fixedScene.window)
        if (fakeInvoiceOutputPathConfig != null) {
            directoryChooser.initialDirectory = fakeInvoiceOutputPathConfig
            buttonToUpdate.text = fakeInvoiceOutputPathConfig!!.toString()
        }
    }

    override fun registerGuiObserver(guiObserver: GuiObserver) {
        this.guiObserver = guiObserver
    }

    override fun notifyGuiObserver(stackPane: StackPane) {
        guiObserver!!.switchScene(stackPane)
    }
}
