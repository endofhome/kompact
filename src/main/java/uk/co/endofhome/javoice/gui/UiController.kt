package uk.co.endofhome.javoice.gui

import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import uk.co.endofhome.javoice.Controller

class UiController(var controller: Controller) : GuiObserver {

    init {
        initialise()
    }

    private fun initialise() {
        val mainMenu = MainMenu()
        mainMenu.registerGuiObserver(this)
        mainMenuStackPane = mainMenu.mainMenuStackPane

        val newInvoice = NewInvoice()
        newInvoice.registerGuiObserver(this)
        newInvoice.registerObserver(controller)
        newInvoiceStackPane = newInvoice.newInvoiceStackPane

        updateInvoiceDetails()

        val newCustomer = NewCustomer()
        newCustomer.registerObserver(controller)
        newCustomer.registerGuiObserver(this)
        newCustomerStackPane = newCustomer.newCustomerStackPane

        val settings = Settings()
        settings.registerGuiObserver(this)
        settingsStackPane = settings.settingsStackPane
    }

    fun setTheStage(primaryStage: Stage) {
        fixedScene = Scene(mainMenuStackPane)
        mainMenuStackPane.requestFocus()
        primaryStage.title = "Javoice"
        primaryStage.scene = fixedScene
        primaryStage.isMaximized = true
        primaryStage.icons.add(Image("file:resources/icons/javoice_icon.png"))
        primaryStage.show()
    }

    override fun switchScene(layout: StackPane) {
        fixedScene.root = layout
        layout.requestFocus()
    }

    override fun updateInvoiceDetails() {
        val invoiceDetails = InvoiceDetails(controller.currentCustomer)
        invoiceDetails.registerGuiObserver(this)
        invoiceDetails.registerObserver(controller)
        invoiceDetailsStackPane = invoiceDetails.invoiceDetailsStackPane
    }

    companion object {
        lateinit var fixedScene: Scene
        lateinit var mainMenuStackPane: StackPane
        lateinit var newInvoiceStackPane: StackPane
        lateinit var invoiceDetailsStackPane: StackPane
        lateinit var newCustomerStackPane: StackPane
        lateinit var settingsStackPane: StackPane
    }
}
