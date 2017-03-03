package uk.co.endofhome.javoice.gui

import javafx.scene.layout.StackPane

interface GuiObserver {
    fun switchScene(stackPane: StackPane)

    fun updateInvoiceDetails()
}
