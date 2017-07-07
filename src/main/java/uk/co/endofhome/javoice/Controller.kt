package uk.co.endofhome.javoice

import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import uk.co.endofhome.javoice.Config.Companion.invoicePdfFileOutputPath
import uk.co.endofhome.javoice.Config.Companion.invoiceXlsFileOutputPath
import uk.co.endofhome.javoice.Config.Companion.salesLedgerFileOutputPath
import uk.co.endofhome.javoice.customer.Customer
import uk.co.endofhome.javoice.customer.CustomerStore
import uk.co.endofhome.javoice.invoice.*
import uk.co.endofhome.javoice.ledger.AnnualReport
import uk.co.endofhome.javoice.ledger.AnnualReport.annualReport
import uk.co.endofhome.javoice.ledger.AnnualReport.readFile
import uk.co.endofhome.javoice.ledger.LedgerEntry
import uk.co.endofhome.javoice.pdf.PdfConvertor
import java.io.IOException
import java.nio.file.Files.exists
import java.nio.file.Files.notExists
import java.nio.file.Path
import java.nio.file.Paths.get
import java.time.LocalDate
import java.time.Month
import java.time.Month.DECEMBER
import java.time.temporal.ChronoUnit.YEARS

class Controller(private val customerStore: CustomerStore, private val pdfConvertor: PdfConvertor) : Observer {
    var currentCustomer: Customer? = null

    @Throws(IOException::class)
    override fun newInvoice(customer: Customer, orderNumber: String, itemLines: List<ItemLine>) {
        val year = LocalDate.now().year
        ensureAnnualReportForThisYear(year, annualReportFor(year))
        val annualReport = AnnualReport.readFile(annualReportFor(year))
        val invoice = Invoice(nextInvoiceNumber(annualReport), LocalDate.now(), customer, orderNumber, itemLines)
        val invoiceClient = InvoiceClient.invoiceClient(HSSFWorkbook())
        val invoiceTemplateSheet = getSheetForInvoiceTemplate(invoiceClient)
        createInvoiceOnFS(invoice, invoiceClient, invoiceTemplateSheet)
        updateAnnualReportOnFS(annualReport, invoice)
        try {
            pdfConvertor.convert(invoiceClient.invoiceFilePath(invoiceXlsFileOutputPath(), invoice))
        } catch (e: Exception) {
            if (exists(Config.libreOfficePath())) {
                throw RuntimeException("Couldn't write PDF for invoice " + invoice.number + " and path " + invoicePdfFileOutputPath() + e)
            }
        }

    }

    @Throws(IOException::class)
    override fun newCustomer(name: String, addressOne: String, addressTwo: String, postcode: String, phoneNum: String) {
        val newCustomer = Customer(name, addressOne, addressTwo, postcode, phoneNum, nextAccountNumber())
        customerStore.addCustomer(newCustomer)
        customerStore.writeFile(Config.customerDataFilePath())
    }

    override fun findCustomer(name: String): Customer? {
        return customerStore.search(name)
    }

    override fun updateCurrentCustomer(customer: Customer) {
        this.currentCustomer = customer
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
                if (invoiceNumberValue != null) {
                    result = invoiceNumberValue
                    break
                }
            }
        }
        return result
    }

    private fun findNextInvoiceNumberIn(annualReport: AnnualReport, month: Month): String? {
        (month.value downTo 1).map {
            val monthlyReport = annualReport.monthlyReports()[it - 1]
            val lastInvoiceEntry: LedgerEntry? = monthlyReport.entries.reversed().find { it.invoiceNumber != null }
            lastInvoiceEntry?.let {
                return parseIntOrNull(it.invoiceNumber ?: "0")?.inc().toString()
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun updateAnnualReportOnFS(annualReport: AnnualReport, invoice: Invoice) {
        val ledgerEntry = LedgerEntry.ledgerEntry(invoice, null, null, null)
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
            invoiceClient.writeFile(invoiceXlsFileOutputPath(), invoice)
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

    private fun parseIntOrNull(lastInvoiceNumber: String): Int? {
        try {
            return Integer.parseInt(lastInvoiceNumber)
        } catch (e: NumberFormatException) {
            return null
        }
    }
}
