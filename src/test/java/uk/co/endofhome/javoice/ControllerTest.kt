package uk.co.endofhome.javoice

import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.endofhome.javoice.customer.Customer
import uk.co.endofhome.javoice.customer.CustomerStore
import uk.co.endofhome.javoice.invoice.Invoice
import uk.co.endofhome.javoice.invoice.ItemLine
import uk.co.endofhome.javoice.ledger.AnnualReport
import uk.co.endofhome.javoice.ledger.AnnualReport.annualReportCustomConfig
import uk.co.endofhome.javoice.ledger.LedgerEntry
import uk.co.endofhome.javoice.pdf.PdfConvertor
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths.get
import java.time.LocalDate
import java.util.Arrays.asList

class ControllerTest {
    private lateinit var controller: Controller
    private lateinit var annualReport: AnnualReport
    private lateinit var pathForTestOutput: Path
    private lateinit var customer: Customer

    @Before
    @Throws(IOException::class)
    fun set_up() {
        pathForTestOutput = get("src/test/resources/functional/controller")
        val pathForTemplateFile = get("src/test/resources/functional/templates/test-template.xls")
        Config.setInvoiceFileTemplatePath(pathForTemplateFile)
        if (Files.notExists(pathForTestOutput)) {
            Files.createDirectories(pathForTestOutput)
        } else {
            cleanDirectory(pathForTestOutput)
        }
        Config.setSalesLedgerFileOutputPath(pathForTestOutput)
        Config.setInvoiceXlsOutputPath(pathForTestOutput)
        Config.setCustomerDataFileOutputPath(get(pathForTestOutput.toString(), "/Customers.xls"))
        val customerStore = CustomerStore()
        val pdfConvertor = PdfConvertor()
        customer = Customer("some customer", null, null, "P05T C0D3", null, "798")
        customerStore.addCustomer(customer)
        controller = Controller(customerStore, pdfConvertor)
        annualReport = annualReportCustomConfig(2016, pathForTestOutput)
        annualReport.writeFile(pathForTestOutput)
    }

    @Test
    @Throws(IOException::class)
    fun can_get_next_invoice_number_when_invoices_already_exist_for_this_month() {
        val itemLine = ItemLine(5.0, "Slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2016, 12, 31), customer, "some ref", asList(itemLine))
        val ledgerEntry = LedgerEntry.ledgerEntry(invoice, null, null, null)
        annualReport.setNewEntry(ledgerEntry)
        annualReport.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class)
    fun can_get_next_invoice_number_when_no_invoices_exist_for_this_month_but_do_exist_for_this_year() {
        val itemLine = ItemLine(5.0, "Slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2016, 3, 1), customer, "some ref", asList(itemLine))
        val ledgerEntry = LedgerEntry.ledgerEntry(invoice, null, null, null)
        annualReport.setNewEntry(ledgerEntry)
        annualReport.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun can_get_next_invoice_number_when_last_invoice_exists_for_previous_year() {
        val itemLine = ItemLine(5.0, "Old slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2015, 6, 10), customer, "some ref", asList(itemLine))
        val ledgerEntry = LedgerEntry.ledgerEntry(invoice, null, null, null)
        val annualReportPreviousYear = AnnualReport.annualReport(2015)
        annualReportPreviousYear.setNewEntry(ledgerEntry)
        annualReportPreviousYear.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun can_get_next_invoice_number_when_last_invoice_exists_six_years_ago() {
        val itemLine = ItemLine(5.0, "Really old slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2010, 3, 7), customer, "some ref", asList(itemLine))
        val ledgerEntry = LedgerEntry.ledgerEntry(invoice, null, null, null)
        val annualReport2010 = AnnualReport.annualReport(2010)
        annualReport2010.setNewEntry(ledgerEntry)
        annualReport2010.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun can_get_correct_invoice_number_when_invoices_exists_for_this_year_and_previous_years() {
        val oldItemLine = ItemLine(5.0, "Really old slices of toast", 0.5)
        val oldInvoice = Invoice("1", LocalDate.of(2015, 3, 7), customer, "some ref", asList(oldItemLine))
        val oldLedgerEntry = LedgerEntry.ledgerEntry(oldInvoice, null, null, null)
        val annualReport2015 = AnnualReport.annualReport(2015)
        annualReport2015.setNewEntry(oldLedgerEntry)
        annualReport2015.writeFile(pathForTestOutput)

        val itemLine = ItemLine(5.0, "Slices of toast", 0.5)
        val invoice = Invoice("2", LocalDate.of(2016, 12, 31), customer, "some ref", asList(itemLine))
        val ledgerEntry = LedgerEntry.ledgerEntry(invoice, null, null, null)
        annualReport.setNewEntry(ledgerEntry)
        annualReport.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("3"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun ignores_invoice_number_when_last_invoice_exists_in_future_year() {
        val itemLine = ItemLine(5.0, "Super futuristic toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2017, 6, 30), customer, "some ref", asList(itemLine))
        val ledgerEntry = LedgerEntry.ledgerEntry(invoice, null, null, null)
        val annualReport2017 = AnnualReport.annualReport(2017)
        annualReport2017.setNewEntry(ledgerEntry)
        annualReport2017.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("1"))
    }

    @Test
    @Throws(IOException::class)
    fun can_get_next_invoice_number_when_no_invoices_exist_at_all() {
        assertThat(controller.nextInvoiceNumber(annualReport), `is`("1"))
    }

    @Test
    @Throws(Exception::class)
    fun can_get_next_account_number() {
        assertThat(controller.nextAccountNumber(), `is`("799"))
    }

    @Test
    @Throws(Exception::class)
    fun can_create_a_new_invoice_and_write_out_all_files() {
        val firstCustomer = Customer("firstCustomer", null, null, null, null, "ACC-01")
        val someItemLine = ItemLine(3.0, "Baked beans", 0.75)
        controller.newInvoice(firstCustomer, "some ref", asList(someItemLine))

        val secondCustomer = Customer("secondCustomer", null, null, null, null, "ACC-02")
        val secondItemLine = ItemLine(1.0, "Baked beans", 10.20)
        controller.newInvoice(secondCustomer, "another ref", asList(secondItemLine))

        val annualReportFromFS = AnnualReport.readFile(get(pathForTestOutput.toString(), "sales" + LocalDate.now().year.toString() + ".xls"))
        val thisMonth = LocalDate.now().monthValue - 1
        val monthlyReport = annualReportFromFS.monthlyReports().get(thisMonth)
        val lastEntry = monthlyReport.entries[monthlyReport.entries.size - 1]

        assertThat(lastEntry.invoiceNumber, `is`("2"))
        assertThat(lastEntry.date, `is`(LocalDate.now()))
        assertThat(lastEntry.customerName, `is`("secondCustomer"))
        assertThat(lastEntry.valueNett, `is`(10.20))
    }

    @Test
    @Throws(Exception::class)
    fun can_add_new_customer_to_existing_customer_DB() {
        controller.newCustomer("Friendly Customer", "first bit of address", "second bit of address", "a postcode", "45632")

        val updatedCustomerStoreFromFS = CustomerStore.readFile(get(pathForTestOutput.toString() + "/Customers.xls"), 0)
        val customers = updatedCustomerStoreFromFS.customers
        val (name, _, _, postcode, _, accountCode) = customers[customers.size - 1]

        assertThat(name, `is`("Friendly Customer"))
        assertThat(postcode, `is`("a postcode"))
        assertThat(accountCode, `is`("799"))
    }

    @Test
    @Throws(Exception::class)
    fun can_find_customer_using_exact_name() {
        val foundCustomer = controller.findCustomer("some customer")

        assertThat(foundCustomer!!.name, `is`("some customer"))
        assertThat(foundCustomer.postcode, `is`("P05T C0D3"))
        assertThat(foundCustomer.accountCode, `is`("798"))
    }

    @Test
    @Throws(Exception::class)
    fun cannot_find_any_customers_if_customer_name_does_not_match_exactly() {
        val missingCustomer = controller.findCustomer("I don't exist")

        assertNull(missingCustomer)
    }

    private fun cleanDirectory(pathForTestOutput: Path) {
        val testDir = pathForTestOutput.toFile().listFiles()
        for (file in testDir) {
            if (!file.isDirectory) {
                file.delete()
            }
        }
    }
}