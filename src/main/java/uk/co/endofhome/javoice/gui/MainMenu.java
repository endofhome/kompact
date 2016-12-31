package uk.co.endofhome.javoice.gui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.LocalDate;

import static uk.co.endofhome.javoice.gui.UiController.newCustomerStackPane;
import static uk.co.endofhome.javoice.gui.UiController.newInvoiceStackPane;
import static uk.co.endofhome.javoice.gui.UiController.settingsStackPane;

public class MainMenu extends JavoiceScreen implements GuiObservable {
    public StackPane mainMenuStackPane;
    private GuiObserver guiObserver;

    public MainMenu() {
        initialise();
    }

    private void initialise() {
        GridPane mainMenuGrid = new GridPane();
        basicGridSetup(mainMenuGrid, "Main menu", 7);

        Text bannerTitle = new Text("J A V O I C E");
        bannerTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        bannerTitle.setFill(OXBLOOD);
        mainMenuGrid.add(bannerTitle, 0, 0, 2, 1);

        Button invoice = initButtonWithMinWidth(mainMenuGrid, "New invoice", event -> notifyGuiObserver(newInvoiceStackPane), 0, 7, 200);

        Button customer = initButtonWithMinWidth(mainMenuGrid, "New customer", event -> notifyGuiObserver(newCustomerStackPane), 0, 8, 200);

        Button settings = initButtonWithMinWidth(mainMenuGrid, "Settings", event -> notifyGuiObserver(settingsStackPane), 0, 9, 200);

        Label copyright = new Label(String.format("© %s  Tom Barnes", copyrightYears()));
        copyright.setTextFill(OXBLOOD);
        mainMenuGrid.add(copyright, 0, 16);

        mainMenuStackPane = new StackPane(mainMenuGrid);
    }

    private String copyrightYears() {
        if (LocalDate.now().getYear() == 2016) {
            return "2016";
        }
        return String.format("2016-%s", LocalDate.now().getYear());
    }

    @Override
    public void registerGuiObserver(GuiObserver guiObserver) {
        this.guiObserver = guiObserver;
    }

    @Override
    public void notifyGuiObserver(StackPane stackPane) {
        guiObserver.switchScene(stackPane);
    }
}
