package uk.co.endofhome.javoice.invoice

import com.googlecode.totallylazy.Sequence
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.junit.Before
import org.junit.Test
import uk.co.endofhome.javoice.customer.Customer

import java.io.IOException
import java.time.LocalDate

import com.googlecode.totallylazy.Sequences.sequence
import com.googlecode.totallylazy.matchers.Matchers.`is`
import java.nio.file.Paths.get
import org.hamcrest.MatcherAssert.assertThat
import uk.co.endofhome.javoice.invoice.InvoiceClient.Companion.invoiceClientCustomConfig
import uk.co.endofhome.javoice.invoice.ItemLine.Companion.itemLine

class InvoiceClientTest {
    private val customer = Customer("Milford", "Herbalist St.", "New York", "NY-1010", "12345", "ACC-1967")
    internal var itemLine = itemLine(3.0, "Green bottles", 10.0)
    private lateinit var invoiceClient: InvoiceClient

    @Before
    @Throws(IOException::class)
    fun set_up() {
        writeInvoiceToFileSystem()
    }

    @Test
    @Throws(IOException::class)
    fun can_write_out_and_read_in_a_file() {
        val invoiceFromFileSystem = invoiceClient.readFile("src/test/resources/INV-001.xls", 0)

        assertThat(invoiceFromFileSystem.date, `is`(LocalDate.now()))
        assertThat(invoiceFromFileSystem.number, `is`("INV-001"))
        assertThat(invoiceFromFileSystem.customer.name, `is`("Milford"))
        assertThat(invoiceFromFileSystem.orderNumber, `is`("cust ref"))
        assertThat(invoiceFromFileSystem.itemLines.get(0).description.get(), `is`("Green bottles"))
    }

    @Test
    @Throws(IOException::class)
    fun can_set_customer_section() {
        val invoiceSheet = invoiceClient.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val invoice = Invoice("some invoice number", LocalDate.now(), customer, "anything", sequence<ItemLine>(itemLine))
        val updatedSheet = invoiceClient.setCustomerSection(invoiceSheet, invoice)
        val customerDetails = invoiceClient.getCustomerSectionFrom(updatedSheet)

        assertThat(customerDetails.get(0), `is`("Milford"))
        assertThat(customerDetails.get(1), `is`("Herbalist St."))
        assertThat(customerDetails.get(2), `is`("New York"))
        assertThat(customerDetails.get(3), `is`("NY-1010"))
        assertThat(customerDetails.get(4), `is`("12345"))
    }

    @Test
    @Throws(IOException::class)
    fun can_set_order_refs_section() {
        val invoiceSheet = invoiceClient.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val invoice = Invoice("some invoice number", LocalDate.of(2017, 3, 19), customer, "Bob", sequence<ItemLine>(itemLine))
        val updatedSheet = invoiceClient.setOrderRefsSection(invoiceSheet, invoice)
        val orderRefsDetails = invoiceClient.getOrderRefsSectionFrom(updatedSheet)

        assertThat(orderRefsDetails.get(0), `is`("19/03/2017"))
        assertThat(orderRefsDetails.get(1), `is`("Bob"))
        assertThat(orderRefsDetails.get(2), `is`("ACC-1967"))
    }

    @Test
    @Throws(IOException::class)
    fun can_set_invoice_number() {
        val invoiceSheet = invoiceClient.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val invoice = Invoice("INV-999", LocalDate.now(), customer, "some customer ref", sequence<ItemLine>(itemLine))
        val updatedSheet = invoiceClient.setInvoiceNumber(invoiceSheet, invoice)
        val invoiceNumber = invoiceClient.getInvoiceNumberFrom(updatedSheet)

        assertThat(invoiceNumber, `is`("INV-999"))
    }

    @Test
    @Throws(IOException::class)
    fun can_set_one_item_line() {
        val invoiceSheet = invoiceClient.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val singleItemLine = itemLine(3.0, "Magic beans", 3.0)
        val invoice = Invoice("some invoice number", LocalDate.now(), customer, "some customer ref", sequence<ItemLine>(singleItemLine))
        val updatedSheet = invoiceClient.setItemLine(invoiceSheet, invoice, 0)
        val newFirstLine = invoiceClient.getItemLinesFrom(updatedSheet).get(0)

        assertThat(newFirstLine, `is`<ItemLine>(singleItemLine))
    }

    @Test
    @Throws(IOException::class)
    fun can_set_multiple_item_lines() {
        val invoiceSheet = invoiceClient.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val firstItemLine = itemLine(3.0, "Magic beans", 3.0)
        val secondItemLine = itemLine(1.5, "Golden tickets", 100.0)
        val thirdItemLine = itemLine(0.5, "Super foods", 19.0)
        val fourthItemLine = itemLine(3000.0, "Frozen Calamari", 0.15)
        val actualItemLines = sequence<ItemLine>(firstItemLine, secondItemLine, thirdItemLine, fourthItemLine)
        val invoice = Invoice(
            "some invoice number",
            LocalDate.now(),
            customer,
            "some customer ref",
            actualItemLines
        )
        val updatedItemLines = invoiceClient.setItemLines(invoiceSheet, invoice)

        assertThat(updatedItemLines, `is`(actualItemLines))
    }

    @Throws(IOException::class)
    private fun writeInvoiceToFileSystem() {
        val workbook = HSSFWorkbook()
        val testSheet = workbook.createSheet("test invoice")
        createTestRows(testSheet)
        invoiceClient = invoiceClientCustomConfig(workbook, get("data/"), get("src/test/resources"))
        val invoice = Invoice("INV-001", LocalDate.now(), customer, "cust ref", sequence<ItemLine>(itemLine))
        invoiceClient.setCustomerSection(testSheet, invoice)
        invoiceClient.setInvoiceNumber(testSheet, invoice)
        invoiceClient.setOrderRefsSection(testSheet, invoice)
        invoiceClient.setItemLines(testSheet, invoice)
        invoiceClient.writeFile(invoiceClient.fileOutputPath(), invoice)
    }

    private fun createTestRows(testSheet: HSSFSheet) {
        for (i in 0..33) {
            testSheet.createRow(i)
            for (j in 0..12) {
                testSheet.getRow(i).createCell(j)
            }
        }
    }
}