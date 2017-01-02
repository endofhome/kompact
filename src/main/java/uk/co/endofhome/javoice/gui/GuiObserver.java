package uk.co.endofhome.javoice.gui;

import javafx.scene.layout.StackPane;

public interface GuiObserver {
    void switchScene(StackPane stackPane);

    void updateInvoiceDetails();
}
