package uk.co.endofhome.javoice.gui

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import uk.co.endofhome.javoice.Observable
import uk.co.endofhome.javoice.Observer

import java.io.IOException

import uk.co.endofhome.javoice.gui.UiController.Companion.mainMenuStackPane

class NewCustomer : JavoiceScreen(), GuiObservable, Observable {

    lateinit var newCustomerStackPane: StackPane
    private var guiObserver: GuiObserver? = null
    private var observer: Observer? = null

    init {
        initialise()
    }

    private fun initialise() {
        val addCustomerGrid = GridPane()
        basicGridSetup(addCustomerGrid, "New customer", 1)

        // TODO: Add appropriate character limits for textfields.

        val customerName = Label("Name")
        addCustomerGrid.add(customerName, 0, 2)

        val customerNameField = TextField()
        addCustomerGrid.add(customerNameField, 1, 2)

        val customerAddressOne = Label("Address (1)")
        addCustomerGrid.add(customerAddressOne, 0, 3)

        val customerAddressOneField = TextField()
        addCustomerGrid.add(customerAddressOneField, 1, 3)

        val customerAddressTwo = Label("Address (2)")
        addCustomerGrid.add(customerAddressTwo, 0, 4)

        val customerAddressTwoField = TextField()
        addCustomerGrid.add(customerAddressTwoField, 1, 4)

        val customerPostcode = Label("Postcode")
        addCustomerGrid.add(customerPostcode, 0, 5)

        val customerPostcodeField = TextField()
        addCustomerGrid.add(customerPostcodeField, 1, 5)

        val customerPhoneNum = Label("Phone number:")
        addCustomerGrid.add(customerPhoneNum, 0, 6)

        val customerPhoneField = TextField()
        addCustomerGrid.add(customerPhoneField, 1, 6)

        val mainMenu = initButton(addCustomerGrid, "Main menu", EventHandler { event -> notifyGuiObserver(mainMenuStackPane) }, 0, 7)

        val addCustomer = initButton(addCustomerGrid, "Add customer", EventHandler { event ->
            try {
                newCustomer(
                    customerNameField.text,
                    customerAddressOneField.text,
                    customerAddressTwoField.text,
                    customerPostcodeField.text,
                    customerPhoneField.text
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }, 5, 7)

        newCustomerStackPane = StackPane(addCustomerGrid)
    }

    override fun registerGuiObserver(guiObserver: GuiObserver) {
        this.guiObserver = guiObserver
    }

    override fun notifyGuiObserver(stackPane: StackPane) {
        guiObserver!!.switchScene(stackPane)
    }

    override fun registerObserver(observer: Observer) {
        this.observer = observer
    }

    @Throws(IOException::class)
    override fun newCustomer(name: String, addressOne: String, addressTwo: String, postcode: String, phoneNumber: String) {
        /* TODO: adding a customer should take you either to the invoice details screen, or back to the new customer screen.
           TODO: ...depending on where you came from.
        */
        observer!!.newCustomer(name, addressOne, addressTwo, postcode, phoneNumber)
    }

    // TODO: method/s not required, side-effect of the fact that the observer pattern stuff isn't quite the right tool for the job?
    override fun searchForCustomer(name: String) {
    }
}
