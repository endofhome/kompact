package uk.co.endofhome.javoice.ui;

import javafx.scene.layout.StackPane;

public interface Observable {
    void registerObserver(Observer observer);
    void notifyObserver(StackPane stackPane);
}
