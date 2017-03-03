package uk.co.endofhome.javoice.gui

import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text

import java.time.LocalDate

import uk.co.endofhome.javoice.gui.UiController.Companion.newCustomerStackPane
import uk.co.endofhome.javoice.gui.UiController.Companion.newInvoiceStackPane
import uk.co.endofhome.javoice.gui.UiController.Companion.settingsStackPane

class MainMenu : JavoiceScreen(), GuiObservable {
    lateinit var mainMenuStackPane: StackPane
    private var guiObserver: GuiObserver? = null

    init {
        initialise()
    }

    private fun initialise() {
        val mainMenuGrid = GridPane()
        basicGridSetup(mainMenuGrid, "Main menu", 7)

        val bannerTitle = Text("J A V O I C E")
        bannerTitle.font = Font.font("Tahoma", FontWeight.NORMAL, 20.0)
        bannerTitle.fill = JavoiceScreen.OXBLOOD
        mainMenuGrid.add(bannerTitle, 0, 0, 2, 1)

        val invoice = initButtonWithMinWidth(mainMenuGrid, "New invoice", EventHandler { event -> notifyGuiObserver(newInvoiceStackPane) }, 0, 7, 200)

        val customer = initButtonWithMinWidth(mainMenuGrid, "New customer", EventHandler { event -> notifyGuiObserver(newCustomerStackPane) }, 0, 8, 200)

        val settings = initButtonWithMinWidth(mainMenuGrid, "Settings", EventHandler { event -> notifyGuiObserver(settingsStackPane) }, 0, 9, 200)

        val copyright = Label(String.format("© %s  Tom Barnes", copyrightYears()))
        copyright.textFill = JavoiceScreen.OXBLOOD
        mainMenuGrid.add(copyright, 0, 16)

        mainMenuStackPane = StackPane(mainMenuGrid)
    }

    private fun copyrightYears(): String {
        if (LocalDate.now().year == 2016) {
            return "2016"
        }
        return String.format("2016-%s", LocalDate.now().year)
    }

    override fun registerGuiObserver(guiObserver: GuiObserver) {
        this.guiObserver = guiObserver
    }

    override fun notifyGuiObserver(stackPane: StackPane) {
        guiObserver!!.switchScene(stackPane)
    }
}
