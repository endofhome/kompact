package uk.co.endofhome.javoice

import com.googlecode.totallylazy.Option
import org.junit.Before
import org.junit.Test
import uk.co.endofhome.javoice.customer.Customer
import uk.co.endofhome.javoice.customer.CustomerStore
import uk.co.endofhome.javoice.invoice.Invoice
import uk.co.endofhome.javoice.invoice.ItemLine
import uk.co.endofhome.javoice.ledger.AnnualReport

import java.io.IOException
import java.nio.file.Path
import java.time.LocalDate

import com.googlecode.totallylazy.Option.none
import com.googlecode.totallylazy.Sequences.sequence
import java.nio.file.Paths.get
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import uk.co.endofhome.javoice.invoice.ItemLine.Companion.itemLine
import uk.co.endofhome.javoice.ledger.AnnualReport.Companion.annualReportCustomConfig
import uk.co.endofhome.javoice.ledger.LedgerEntry.Companion.ledgerEntry

class ControllerTest {
    private lateinit var controller: Controller
    private lateinit var annualReport: AnnualReport
    private lateinit var pathForTestOutput: Path
    private lateinit var customerStore: CustomerStore
    private lateinit var customer: Customer

    @Before
    @Throws(IOException::class)
    fun set_up() {
        pathForTestOutput = get("src/test/resources/functional/controller")
        cleanDirectory(pathForTestOutput)
        Config.setSalesLedgerFileOutputPath(pathForTestOutput)
        Config.setInvoiceOutputPath(pathForTestOutput)
        Config.setCustomerDataFileOutputPath(get(pathForTestOutput.toString(), "/Customers.xls"))
        customerStore = CustomerStore()
        customer = Customer("some customer", "don't care", "don't care", "P05T C0D3", "don't care", "798")
        customerStore.addCustomer(customer)
        controller = Controller(customerStore)
        annualReport = annualReportCustomConfig(2016, pathForTestOutput)
        annualReport.writeFile(pathForTestOutput)
    }

    @Test
    @Throws(IOException::class)
    fun can_get_next_invoice_number_when_invoices_already_exist_for_this_month() {
        val itemLine = itemLine(5.0, "Slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2016, 12, 31), customer, "some ref", sequence<ItemLine>(itemLine))
        val ledgerEntry = ledgerEntry(invoice, none(), none(), none())
        annualReport.setNewEntry(ledgerEntry)
        annualReport.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class)
    fun can_get_next_invoice_number_when_no_invoices_exist_for_this_month_but_do_exist_for_this_year() {
        val itemLine = itemLine(5.0, "Slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2016, 3, 1), customer, "some ref", sequence<ItemLine>(itemLine))
        val ledgerEntry = ledgerEntry(invoice, none(), none(), none())
        annualReport.setNewEntry(ledgerEntry)
        annualReport.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun can_get_next_invoice_number_when_last_invoice_exists_for_previous_year() {
        val itemLine = itemLine(5.0, "Old slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2015, 6, 10), customer, "some ref", sequence<ItemLine>(itemLine))
        val ledgerEntry = ledgerEntry(invoice, none(), none(), none())
        val annualReportPreviousYear = AnnualReport.annualReport(2015)
        annualReportPreviousYear.setNewEntry(ledgerEntry)
        annualReportPreviousYear.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun can_get_next_invoice_number_when_last_invoice_exists_six_years_ago() {
        val itemLine = itemLine(5.0, "Really old slices of toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2010, 3, 7), customer, "some ref", sequence<ItemLine>(itemLine))
        val ledgerEntry = ledgerEntry(invoice, none(), none(), none())
        val annualReport2010 = AnnualReport.annualReport(2010)
        annualReport2010.setNewEntry(ledgerEntry)
        annualReport2010.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("2"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun can_get_correct_invoice_number_when_invoices_exists_for_this_year_and_previous_years() {
        val oldItemLine = itemLine(5.0, "Really old slices of toast", 0.5)
        val oldInvoice = Invoice("1", LocalDate.of(2015, 3, 7), customer, "some ref", sequence<ItemLine>(oldItemLine))
        val oldLedgerEntry = ledgerEntry(oldInvoice, none(), none(), none())
        val annualReport2015 = AnnualReport.annualReport(2015)
        annualReport2015.setNewEntry(oldLedgerEntry)
        annualReport2015.writeFile(pathForTestOutput)

        val itemLine = itemLine(5.0, "Slices of toast", 0.5)
        val invoice = Invoice("2", LocalDate.of(2016, 12, 31), customer, "some ref", sequence<ItemLine>(itemLine))
        val ledgerEntry = ledgerEntry(invoice, none(), none(), none())
        annualReport.setNewEntry(ledgerEntry)
        annualReport.writeFile(pathForTestOutput)

        assertThat(controller.nextInvoiceNumber(annualReport), `is`("3"))
    }

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun ignores_invoice_number_when_last_invoice_exists_in_future_year() {
        val itemLine = itemLine(5.0, "Super futuristic toast", 0.5)
        val invoice = Invoice("1", LocalDate.of(2017, 6, 30), customer, "some ref", sequence<ItemLine>(itemLine))
        val ledgerEntry = ledgerEntry(invoice, none(), none(), none())
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
        val firstCustomer = Customer("firstCustomer", "don't care", "don't care", "don't care", "don't care", "don't care")
        val someItemLine = itemLine(3.0, "Baked beans", 0.75)
        controller.newInvoice(firstCustomer, "some ref", sequence<ItemLine>(someItemLine))

        val secondCustomer = Customer("secondCustomer", "don't care", "don't care", "don't care", "don't care", "don't care")
        val secondItemLine = itemLine(1.0, "Baked beans", 10.20)
        controller.newInvoice(secondCustomer, "another ref", sequence<ItemLine>(secondItemLine))

        val annualReportFromFS = AnnualReport.readFile(get(pathForTestOutput.toString(), "sales" + LocalDate.now().year.toString() + ".xls"))
        val thisMonth = LocalDate.now().monthValue - 1
        val monthlyReport = annualReportFromFS.monthlyReports().get(thisMonth)
        val lastEntry = monthlyReport.entries.last()

        assertThat(lastEntry.invoiceNumber.get(), `is`("2"))
        assertThat(lastEntry.date.get(), `is`(LocalDate.now()))
        assertThat(lastEntry.customerName.get(), `is`("secondCustomer"))
        assertThat(lastEntry.valueNett.get(), `is`(10.20))
    }

    @Test
    @Throws(Exception::class)
    fun can_add_new_customer_to_existing_customer_DB() {
        controller.newCustomer("Friendly Customer", "first bit of address", "second bit of address", "a postcode", "45632")

        val updatedCustomerStoreFromFS = CustomerStore.readFile(get(pathForTestOutput.toString() + "/Customers.xls"), 0)
        val customerUnderTest = updatedCustomerStoreFromFS.customers()!!.last()

        assertThat(customerUnderTest.name, `is`("Friendly Customer"))
        assertThat(customerUnderTest.postcode, `is`("a postcode"))
        assertThat(customerUnderTest.accountCode, `is`("799"))
    }

    @Test
    @Throws(Exception::class)
    fun can_find_customer_using_exact_name() {
        val foundCustomer = controller.findCustomer("some customer")

        assertThat(foundCustomer.get().name, `is`("some customer"))
        assertThat(foundCustomer.get().postcode, `is`("P05T C0D3"))
        assertThat(foundCustomer.get().accountCode, `is`("798"))
    }

    @Test
    @Throws(Exception::class)
    fun cannot_find_any_customers_if_customer_name_does_not_match_exactly() {
        val missingCustomer = controller.findCustomer("I don't exist")

        assertThat(missingCustomer, `is`<Option<out Any>>(none<Any>()))
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