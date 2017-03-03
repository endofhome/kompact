package uk.co.endofhome.javoice.gui

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text

abstract class JavoiceScreen {

    lateinit var title: Text

    internal fun basicGridSetup(gridPane: GridPane, screenTitle: String, rowIndexForTitle: Int) {
        gridPane.alignment = Pos.CENTER
        gridPane.hgap = 10.0
        gridPane.vgap = 10.0
        gridPane.padding = Insets(25.0, 25.0, 25.0, 25.0)

        title = Text(screenTitle)
        title.font = Font.font("Tahoma", FontWeight.NORMAL, 20.0)
        title.fill = BLACKBOARD
        gridPane.add(title, 0, 1, 2, rowIndexForTitle)
    }

    internal fun initLabel(grid: GridPane, text: String, columnIndex: Int, rowIndex: Int): Label {
        val label = Label(text)
        grid.add(label, columnIndex, rowIndex)
        return label
    }

    internal fun initLabelWithColumnSpanAndHAlignment(grid: GridPane, text: String, columnIndex: Int, rowIndex: Int, columnSpan: Int, hpos: HPos): Label {
        val label = initLabel(grid, text, columnIndex, rowIndex)
        // TODO: not sure about this, was calling grid.setColumnSpan and setHalignment
        GridPane.setColumnSpan(label, columnSpan)
        GridPane.setHalignment(label, hpos)
        return label
    }

    internal fun initTextField(grid: GridPane, columnSpan: Int, fieldText: String, columnIndex: Int, rowIndex: Int): TextField {
        val textField = TextField()
        GridPane.setColumnSpan(textField, columnSpan)
        textField.text = fieldText
        grid.add(textField, columnIndex, rowIndex)
        return textField
    }

    private fun initExistingButton(button: Button, grid: GridPane, buttonText: String, actionEventEventHandler: EventHandler<ActionEvent>, columnIndex: Int, rowIndex: Int): Button {
        button.text = buttonText
        button.onAction = actionEventEventHandler
        grid.add(button, columnIndex, rowIndex)
        return button
    }

    internal fun initButton(grid: GridPane, buttonText: String, actionEventEventHandler: EventHandler<ActionEvent>, columnIndex: Int, rowIndex: Int): Button {
        val button = Button()
        initExistingButton(button, grid, buttonText, actionEventEventHandler, columnIndex, rowIndex)
        return button
    }

    internal fun initButtonWithMinWidth(grid: GridPane, buttonText: String, actionEventEventHandler: EventHandler<ActionEvent>, columnIndex: Int, rowIndex: Int, minWidth: Int): Button {
        val button = initButton(grid, buttonText, actionEventEventHandler, columnIndex, rowIndex)
        button.minWidth = minWidth.toDouble()
        return button
    }

    internal fun initButtonWithColumnSpanAndHAlignment(grid: GridPane, buttonText: String, actionEventEventHandler: EventHandler<ActionEvent>, columnIndex: Int, rowIndex: Int, columnSpan: Int, hpos: HPos): Button {
        val button = initButton(grid, buttonText, actionEventEventHandler, columnIndex, rowIndex)
        // TODO: not sure about this, was calling grid.setColumnSpan and setHalignment
        GridPane.setColumnSpan(button, columnSpan)
        GridPane.setHalignment(button, hpos)
        return button
    }

    companion object {
        internal val BLACKBOARD = Color.valueOf("565656")
        internal val OXBLOOD = Color.valueOf("76323F")
    }
}
