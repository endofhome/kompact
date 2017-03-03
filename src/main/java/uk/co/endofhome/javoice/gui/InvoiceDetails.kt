package uk.co.endofhome.javoice.gui

import com.googlecode.totallylazy.Option
import com.googlecode.totallylazy.Sequence
import javafx.beans.binding.NumberBinding
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import uk.co.endofhome.javoice.Observable
import uk.co.endofhome.javoice.Observer
import uk.co.endofhome.javoice.customer.Customer
import uk.co.endofhome.javoice.invoice.ItemLine

import java.io.IOException
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.ArrayList

import com.googlecode.totallylazy.Option.none
import com.googlecode.totallylazy.Option.some
import com.googlecode.totallylazy.Sequences.sequence
import javafx.event.EventHandler
import uk.co.endofhome.javoice.gui.UiController.Companion.mainMenuStackPane
import uk.co.endofhome.javoice.invoice.Invoice.Companion.MAX_ITEM_LINES

class InvoiceDetails(customer: Option<Customer>) : JavoiceScreen(), GuiObservable, Observable {
    lateinit var invoiceDetailsStackPane: StackPane
    private var guiObserver: GuiObserver? = null
    private var observer: Observer? = null
    private var customer: Customer? = null
    private var nameField: TextField? = null
    private var orderNumberField: TextField? = null
    private var addressOneField: TextField? = null
    private var addressTwoField: TextField? = null
    private var postcodeField: TextField? = null
    private var quantityFieldList: MutableList<TextField>? = null
    private var quantityPropertyList: MutableList<SimpleDoubleProperty>? = null
    private var descriptionFieldList: MutableList<TextField>? = null
    private var unitPriceFieldList: MutableList<TextField>? = null
    private var unitPricePropertyList: MutableList<SimpleDoubleProperty>? = null
    private var totalLabelList: MutableList<Label>? = null
    private var decimalFormatter: DecimalFormat? = null

    init {
        this.customer = ensureCustomer(customer)
        initialise()
    }

    private fun ensureCustomer(customer: Option<Customer>): Customer {
        if (customer.isDefined) {
            return customer.get()
        }
        return FakeCustomer()
    }

    private fun initialise() {
        decimalFormatter = DecimalFormat("#.00")

        val invoiceDetailsGrid = GridPane()
        basicGridSetup(invoiceDetailsGrid, "Invoice details:", 1)
        addInvoiceHeader(invoiceDetailsGrid)
        addItemLines(invoiceDetailsGrid)
        addButtons(invoiceDetailsGrid)

        val invoiceDetailsScroll = ScrollPane(invoiceDetailsGrid)
        invoiceDetailsScroll.isFitToWidth = true
        invoiceDetailsStackPane = StackPane(invoiceDetailsScroll)
        // TODO: this doesn't work, for some reason:
        quantityFieldList!![0].requestFocus()
    }

    private fun addButtons(invoiceDetailsGrid: GridPane) {
        val mainMenu = initButton(invoiceDetailsGrid, "Main menu", EventHandler { event -> notifyGuiObserver(mainMenuStackPane) }, 0, 31)

        val submitInvoice = initButton(invoiceDetailsGrid, "Submit", EventHandler { event ->
            try {
                newInvoice()
            } catch (e: IOException) {
                // TODO: fix this mess too. should be throwing this exception somewhere, not swallowing it.
            }
        }, 2, 31)
    }

    private fun addItemLines(invoiceDetailsGrid: GridPane) {
        val quantity = initLabel(invoiceDetailsGrid, "Quantity", 0, 13)
        val description = initLabel(invoiceDetailsGrid, "Description", 1, 13)
        val unitPrice = initLabel(invoiceDetailsGrid, "Unit price", 4, 13)
        val total = initLabel(invoiceDetailsGrid, "Total", 5, 13)

        quantityFieldList = ArrayList<TextField>()
        quantityPropertyList = ArrayList<SimpleDoubleProperty>()
        initPropAndFieldListsFor(quantityPropertyList as ArrayList<SimpleDoubleProperty>, quantityFieldList as ArrayList<TextField>)

        descriptionFieldList = ArrayList<TextField>()
        initDescriptionFieldList()

        unitPriceFieldList = ArrayList<TextField>()
        unitPricePropertyList = ArrayList<SimpleDoubleProperty>()
        initPropAndFieldListsFor(unitPricePropertyList as ArrayList<SimpleDoubleProperty>, unitPriceFieldList as ArrayList<TextField>)

        totalLabelList = ArrayList<Label>()
        initTotalLabelLists()

        addItemLineElementsToGrid(invoiceDetailsGrid)
    }

    private fun addInvoiceHeader(invoiceDetailsGrid: GridPane) {
        val nameLabel = initLabel(invoiceDetailsGrid, "Name:", 0, 3)
        nameField = initTextField(invoiceDetailsGrid, 3, customer!!.name, 0, 4)

        val addressOne = initLabel(invoiceDetailsGrid, "Address (1):", 0, 5)
        addressOneField = initTextField(invoiceDetailsGrid, 4, customer!!.addressOne, 0, 6)

        val addressTwo = initLabel(invoiceDetailsGrid, "Address (2):", 0, 7)
        addressTwoField = initTextField(invoiceDetailsGrid, 3, customer!!.addressTwo, 0, 8)

        val postcodeLabel = initLabel(invoiceDetailsGrid, "Postcode:", 3, 7)
        postcodeField = initTextField(invoiceDetailsGrid, 1, customer!!.postcode, 3, 8)

        val dateLabel = initLabel(invoiceDetailsGrid, "Date:", 5, 3)
        val dateField = initTextField(invoiceDetailsGrid, 1, todaysDate(), 5, 4)
        dateField.isDisable = true

        val orderNumberLabel = initLabel(invoiceDetailsGrid, "Order Number:", 5, 5)
        orderNumberField = initTextField(invoiceDetailsGrid, 1, "", 5, 6)

        val accountCodeLabel = initLabel(invoiceDetailsGrid, "Account code:", 5, 7)
        val accountCodeField = initTextField(invoiceDetailsGrid, 1, customer!!.accountCode, 5, 8)
        accountCodeField.isDisable = true
    }

    private fun addItemLineElementsToGrid(invoiceDetailsGrid: GridPane) {
        for (i in 0..MAX_ITEM_LINES - 1) {
            invoiceDetailsGrid.add(quantityFieldList!![i], 0, 14 + i)
            invoiceDetailsGrid.add(descriptionFieldList!![i], 1, 14 + i)
            invoiceDetailsGrid.add(unitPriceFieldList!![i], 4, 14 + i)
            invoiceDetailsGrid.add(totalLabelList!![i], 5, 14 + i)
        }
    }

    private fun initTotalLabelLists() {
        for (i in 0..MAX_ITEM_LINES - 1) {
            val unitPricePropertyForLine = SimpleDoubleProperty()
            unitPricePropertyForLine.set(10.0)
            val totalForLine = quantityPropertyList!![i].multiply(unitPricePropertyList!![i])
            val totalLabelForLine = Label()
            totalForLine.addListener { observable, oldValue, newValue -> totalOrEmptyString(totalLabelForLine, newValue) }
            totalLabelList!!.add(totalLabelForLine)
        }
    }

    private fun initPropAndFieldListsFor(propertyList: MutableList<SimpleDoubleProperty>, fieldList: MutableList<TextField>) {
        for (i in 0..MAX_ITEM_LINES - 1) {
            val unitPricePropertyForLine = SimpleDoubleProperty()
            // TODO: add decimal (and right-align?) TextFormatter to this field:
            propertyList.add(unitPricePropertyForLine)
            val unitPriceFieldForLine = TextField()
            val i2 = i
            unitPriceFieldForLine.textProperty().addListener { observable, oldValue, newValue ->
                var validNewValue: Double?
                try {
                    validNewValue = newValue.toDouble()
                } catch (e: NumberFormatException) {
                    validNewValue = 0.0
                }

                // TODO: blows up if number too large (over limit for Double?)
                propertyList[i2].setValue(validNewValue)
            }
            fieldList.add(unitPriceFieldForLine)
            fieldList[i].maxWidth = 75.0
        }
    }

    private fun initDescriptionFieldList() {
        for (i in 0..MAX_ITEM_LINES - 1) {
            descriptionFieldList!!.add(TextField())
            descriptionFieldList!![i].minWidth = 200.0
            GridPane.setColumnSpan(descriptionFieldList!![i], 3)
        }
    }

    private fun totalOrEmptyString(totalLabelForLine: Label, newValue: Number) {
        if (newValue.toDouble() != 0.0) {
            // TODO: add right-align formatter to this?
            totalLabelForLine.text = decimalFormatter!!.format(newValue)
        } else {
            totalLabelForLine.text = ""
        }
    }

    @Throws(IOException::class)
    private fun newInvoice() {
        val customerFromUI = updatedCustomer()
        var itemLines = sequence<ItemLine>()
        for (i in 0..MAX_ITEM_LINES - 1) {
            val itemLine = ItemLine(
                doubleOption(quantityFieldList!![i].text),
                some(descriptionFieldList!![i].text),
                doubleOption(unitPriceFieldList!![i].text)
            )
            itemLines = itemLines.append(itemLine)
        }
        observer!!.newInvoice(customerFromUI, orderNumberField!!.text, itemLines)
    }

    private fun doubleOption(text: String): Option<Double> {
        if (text == "") {
            return none()
        }
        return some(java.lang.Double.valueOf(text))
    }

    private fun updatedCustomer(): Customer {
        return Customer(
            nameField!!.text,
            addressOneField!!.text,
            addressTwoField!!.text,
            postcodeField!!.text,
            customer!!.phoneNumber,
            customer!!.accountCode
        )
    }

    private fun todaysDate(): String {
        val now = LocalDate.now()
        val ukFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return now.format(ukFormat)
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

    // TODO: method/s not required, side-effect of the fact that the observer pattern stuff isn't quite the right tool for the job?
    @Throws(IOException::class)
    override fun newCustomer(name: String, addressOne: String, addressTwo: String, postcode: String, phoneNumber: String) {
    }

    @Throws(Exception::class)
    override fun searchForCustomer(name: String) {
    }
}
