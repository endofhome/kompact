package uk.co.endofhome.javoice

import javafx.application.Application
import javafx.stage.Stage
import uk.co.endofhome.javoice.customer.CustomerStore
import uk.co.endofhome.javoice.gui.UiController

import java.io.IOException
import java.nio.file.Files

class App : Application() {

    @Throws(IOException::class)
    override fun start(primaryStage: Stage) {
        val customerStore: CustomerStore
        if (Files.exists(Config.customerDataFilePath())) {
            try {
                customerStore = CustomerStore.readFile(Config.customerDataFilePath(), 1)
            } catch (e: IOException) {
                throw IOException("There was a problem reading existing customer store" + e)
            }

        } else {
            customerStore = CustomerStore()
        }
        val controller = Controller(customerStore)
        val uiController = UiController(controller)
        uiController.setTheStage(primaryStage)
    }

    fun runGui() {
        Application.launch()
    }

    fun runCli() {
        println("I'm running!")
        println("I'm exiting!")
        System.exit(0)
    }
}