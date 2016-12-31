package uk.co.endofhome.javoice.gui;

import javafx.scene.layout.StackPane;

public interface GuiObservable {
    void registerGuiObserver(GuiObserver guiObserver);
    void notifyGuiObserver(StackPane stackPane);
}
