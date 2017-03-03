package uk.co.endofhome.javoice.gui

import javafx.scene.layout.StackPane

interface GuiObservable {
    fun registerGuiObserver(guiObserver: GuiObserver)
    fun notifyGuiObserver(stackPane: StackPane)
}
