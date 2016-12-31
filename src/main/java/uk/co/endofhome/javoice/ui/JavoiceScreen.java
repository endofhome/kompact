package uk.co.endofhome.javoice.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public abstract class JavoiceScreen {

    public Text title;
    static final Color BLACKBOARD = Color.valueOf("565656");
    static final Color OXBLOOD = Color.valueOf("76323F");

    void basicGridSetup(GridPane gridPane, String screenTitle, int rowIndexForTitle) {
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        title = new Text(screenTitle);
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setFill(BLACKBOARD);
        gridPane.add(title, 0, 1, 2, rowIndexForTitle);
    }

    Label initLabel(GridPane grid, String text, int columnIndex, int rowIndex) {
        Label label = new Label(text);
        grid.add(label, columnIndex, rowIndex);
        return label;
    }

    Label initLabelWithColumnSpanAndHAlignment(GridPane grid, String text, int columnIndex, int rowIndex, int columnSpan, HPos hpos) {
        Label label = initLabel(grid, text, columnIndex, rowIndex);
        grid.setColumnSpan(label, columnSpan);
        grid.setHalignment(label, hpos);
        return label;
    }

    TextField initTextField(GridPane grid, int columnSpan, String labelText, int columnIndex, int rowIndex) {
        TextField textField = new TextField();
        GridPane.setColumnSpan(textField, columnSpan);
        textField.setText(labelText);
        grid.add(textField, columnIndex, rowIndex);
        return textField;
    }

    private Button initExistingButton(Button button, GridPane grid, String buttonText, EventHandler<ActionEvent> actionEventEventHandler, int columnIndex, int rowIndex) {
        button.setText(buttonText);
        button.setOnAction(actionEventEventHandler);
        grid.add(button, columnIndex, rowIndex);
        return button;
    }

    Button initButton(GridPane grid, String buttonText, EventHandler<ActionEvent> actionEventEventHandler, int columnIndex, int rowIndex) {
        Button button = new Button();
        initExistingButton(button, grid, buttonText, actionEventEventHandler, columnIndex, rowIndex);
        return button;
    }

    Button initButtonWithMinWidth(GridPane grid, String buttonText, EventHandler<ActionEvent> actionEventEventHandler, int columnIndex, int rowIndex, int minWidth) {
        Button button = initButton(grid, buttonText, actionEventEventHandler, columnIndex, rowIndex);
        button.setMinWidth(minWidth);
        return button;
    }

    Button initButtonWithColumnSpanAndHAlignment(GridPane grid, String buttonText, EventHandler<ActionEvent> actionEventEventHandler, int columnIndex, int rowIndex, int columnSpan, HPos hpos) {
        Button button = initButton(grid, buttonText, actionEventEventHandler, columnIndex, rowIndex);
        grid.setColumnSpan(button, columnSpan);
        grid.setHalignment(button, hpos);
        return button;
    }
}
