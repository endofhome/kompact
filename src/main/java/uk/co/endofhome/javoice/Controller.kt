package uk.co.endofhome.javoice

import com.googlecode.totallylazy.Option
import com.googlecode.totallylazy.Sequence
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import uk.co.endofhome.javoice.customer.Customer
import uk.co.endofhome.javoice.customer.CustomerStore
import uk.co.endofhome.javoice.invoice.Invoice
import uk.co.endofhome.javoice.invoice.InvoiceClient
import uk.co.endofhome.javoice.invoice.ItemLine
import uk.co.endofhome.javoice.ledger.AnnualReport

import java.io.IOException
import java.nio.file.Path
import java.time.LocalDate
import java.time.Month

import com.googlecode.totallylazy.Option.none
import com.googlecode.totallylazy.Option.some
import java.nio.file.Files.exists
import java.nio.file.Files.notExists
import java.nio.file.Paths.get
import java.time.Month.DECEMBER
import java.time.temporal.ChronoUnit.YEARS
import uk.co.endofhome.javoice.Config.invoiceFileOutputPath
import uk.co.endofhome.javoice.Config.salesLedgerFileOutputPath
import uk.co.endofhome.javoice.invoice.InvoiceClient.Companion.invoiceClient
import uk.co.endofhome.javoice.ledger.AnnualReport.Companion.annualReport
import uk.co.endofhome.javoice.ledger.AnnualReport.Companion.readFile
import uk.co.endofhome.javoice.ledger.LedgerEntry.Companion.ledgerEntry

class Controller (private val customerStore: CustomerStore) : Observer {
    var currentCustomer: Option<Customer> = none()

    @Throws(IOException::class)
    override fun newInvoice(customer: Customer, orderNumber: String, itemLines: Sequence<ItemLine>) {
        val year = LocalDate.now().year
        ensureAnnualReportForThisYear(year, annualReportFor(year))
        val annualReport = AnnualReport.readFile(annualReportFor(year))
        val invoice = Invoice(nextInvoiceNumber(annualReport), LocalDate.now(), customer, orderNumber, itemLines)
        val invoiceClient = invoiceClient(HSSFWorkbook())
        val invoiceTemplateSheet = getSheetForInvoiceTemplate(invoiceClient)
        createInvoiceOnFS(invoice, invoiceClient, invoiceTemplateSheet)
        updateAnnualReportOnFS(annualReport, invoice)
    }

    @Throws(IOException::class)
    override fun newCustomer(name: String, addressOne: String, addressTwo: String, postcode: String, phoneNum: String) {
        val newCustomer = Customer(name, addressOne, addressTwo, postcode, phoneNum, nextAccountNumber())
        customerStore.addCustomer(newCustomer)
        customerStore.writeFile(Config.customerDataFilePath())
    }

    override fun findCustomer(name: String): Option<Customer> {
        return customerStore.search(name)
    }

    fun nextAccountNumber(): String {
        return customerStore.nextAccountNumber()
    }

    @Throws(IOException::class)
    fun nextInvoiceNumber(currentAnnualReport: AnnualReport): String {
        var currentAnnualReport = currentAnnualReport
        var result = "1"
        for (i in 0..6) {
            val currentYear = currentAnnualReport.year.minus(i.toLong(), YEARS)
            if (exists(annualReportFor(currentYear.value))) {
                currentAnnualReport = readFile(annualReportFor(currentYear.value))
                val invoiceNumberValue = findNextInvoiceNumberIn(currentAnnualReport, DECEMBER)
                if (invoiceNumberValue.isDefined) {
                    result = invoiceNumberValue.get()
                    break
                }
            }
        }
        return result
    }

    override fun updateCurrentCustomer(customer: Option<Customer>) {
        currentCustomer = customer
    }

    private fun findNextInvoiceNumberIn(annualReport: AnnualReport, month: Month): Option<String> {
        for (i in month.value downTo 1) {
            val monthlyReport = annualReport.monthlyReports().get(i - 1)
            val reversedEntries = monthlyReport.entries.reverse()
            val lastInvoiceEntryOption = reversedEntries.find { entry -> entry.invoiceNumber.isDefined }
            if (lastInvoiceEntryOption.isDefined) {
                val invoiceNumberOption = parseIntOption(lastInvoiceEntryOption.get().invoiceNumber.get())
                if (invoiceNumberOption.isDefined) {
                    var invoiceNumber: Int = invoiceNumberOption.get()
                    return some((++invoiceNumber).toString())
                }
            }
        }
        return none()
    }

    @Throws(IOException::class)
    private fun updateAnnualReportOnFS(annualReport: AnnualReport, invoice: Invoice) {
        val ledgerEntry = ledgerEntry(invoice, none<String>(), none<String>(), none<String>())
        annualReport.setNewEntry(ledgerEntry)
        annualReport.writeFile(salesLedgerFileOutputPath())
    }

    private fun annualReportFor(year: Int): Path {
        return get(salesLedgerFileOutputPath().toString(), String.format("sales%s.xls", year))
    }

    @Throws(IOException::class)
    private fun createInvoiceOnFS(invoice: Invoice, invoiceClient: InvoiceClient, invoiceSheet: HSSFSheet) {
        invoiceClient.setSections(invoiceSheet, invoice)
        try {
            invoiceClient.writeFile(invoiceFileOutputPath(), invoice)
        } catch (e: IOException) {
            throw IOException("Controller could not write new invoice file.")
        }

    }

    @Throws(IOException::class)
    private fun getSheetForInvoiceTemplate(invoiceClient: InvoiceClient): HSSFSheet {
        val invoiceSheet: HSSFSheet
        try {
            invoiceSheet = invoiceClient.getSingleSheetFromPath(Config.invoiceFileTemplatePath(), 2)
        } catch (e: IOException) {
            throw IOException("Controller could not get invoice template.")
        }

        return invoiceSheet
    }

    @Throws(IOException::class)
    private fun ensureAnnualReportForThisYear(year: Int, annualReportForThisYear: Path) {
        if (notExists(annualReportForThisYear)) {
            val newAnnualReport = annualReport(year)
            newAnnualReport.writeFile(salesLedgerFileOutputPath())
        }
    }

    private fun parseIntOption(lastInvoiceNumber: String): Option<Int> {
        try {
            return some(Integer.parseInt(lastInvoiceNumber))
        } catch (e: NumberFormatException) {
            return none()
        }

    }
}
