package uk.co.endofhome.javoice.gui

import com.googlecode.totallylazy.Option
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import uk.co.endofhome.javoice.Observable
import uk.co.endofhome.javoice.Observer
import uk.co.endofhome.javoice.customer.Customer

import uk.co.endofhome.javoice.gui.UiController.Companion.invoiceDetailsStackPane
import uk.co.endofhome.javoice.gui.UiController.Companion.mainMenuStackPane
import uk.co.endofhome.javoice.gui.UiController.Companion.newCustomerStackPane

class NewInvoice : JavoiceScreen(), GuiObservable, Observable {

    lateinit var newInvoiceStackPane: StackPane
    private lateinit var guiObserver: GuiObserver
    private lateinit var observer: Observer

    init {
        initialise()
    }

    private fun initialise() {
        val newInvoiceGrid = GridPane()
        basicGridSetup(newInvoiceGrid, "New invoice", 1)

        val customerSearchLabel = initLabel(newInvoiceGrid, "Search for existing customer:", 0, 11)
        val customerSearchField = initTextField(newInvoiceGrid, 1, "", 1, 11)
        val customerSearchButton = initButton(newInvoiceGrid, "Search", EventHandler { event -> searchForCustomer(customerSearchField.text) }, 2, 11)

        val or = initLabelWithColumnSpanAndHAlignment(newInvoiceGrid, "- OR -", 0, 13, 3, HPos.CENTER)

        val addCustomer = initButtonWithColumnSpanAndHAlignment(newInvoiceGrid, "Add new customer", EventHandler { event -> notifyGuiObserver(newCustomerStackPane) }, 0, 15, 3, HPos.CENTER)

        val mainMenu = initButton(newInvoiceGrid, "Main menu", EventHandler { event -> notifyGuiObserver(mainMenuStackPane) }, 0, 26)

        newInvoiceStackPane = StackPane(newInvoiceGrid)
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

    override fun searchForCustomer(name: String) {
        val customer = observer!!.findCustomer(name)
        if (customer.isDefined) {
            observer.updateCurrentCustomer(customer)
            guiObserver.updateInvoiceDetails()
            notifyGuiObserver(invoiceDetailsStackPane)
        } else {
            notifyGuiObserver(newInvoiceStackPane)
        }
    }

    // TODO: method/s not required, side-effect of the fact that the observer pattern stuff isn't quite the right tool for the job?
    override fun newCustomer(name: String, addressOne: String, addressTwo: String, postcode: String, phoneNumber: String) {
    }
}
