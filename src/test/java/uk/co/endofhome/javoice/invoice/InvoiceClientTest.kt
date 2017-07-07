package uk.co.endofhome.javoice.invoice

import com.googlecode.totallylazy.matchers.Matchers.`is`
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.endofhome.javoice.customer.Customer
import java.io.IOException
import java.nio.file.Paths.get
import java.time.LocalDate
import java.util.Arrays.asList

class InvoiceClientTest {
    private val customer = Customer("Milford", "Herbalist St.", "New York", "NY-1010", "12345", "ACC-1967")
    private val itemLine = ItemLine(3.0, "Green bottles", 10.0)
    private var invoiceClient: InvoiceClient? = null

    @Before
    @Throws(IOException::class)
    fun set_up() {
        writeInvoiceToFileSystem()
    }

    @Test
    @Throws(IOException::class)
    fun `can write out and read in a file`() {
        val invoiceFromFileSystem = invoiceClient!!.readFile("src/test/resources/INV-001.xls", 0)

        assertThat(invoiceFromFileSystem.date, `is`(LocalDate.now()))
        assertThat(invoiceFromFileSystem.number, `is`("INV-001"))
        assertThat(invoiceFromFileSystem.customer.name, `is`("Milford"))
        assertThat(invoiceFromFileSystem.orderNumber, `is`("cust ref"))
        assertThat<String>(invoiceFromFileSystem.itemLines[0].description, `is`("Green bottles"))
    }

    @Test
    @Throws(IOException::class)
    fun `can set customer section`() {
        val invoiceSheet = invoiceClient!!.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val invoice = Invoice("some invoice number", LocalDate.now(), customer, "anything", asList(itemLine))
        val updatedSheet = invoiceClient!!.setCustomerSection(invoiceSheet, invoice)
        val customerDetails = invoiceClient!!.getCustomerSectionFrom(updatedSheet)

        assertThat(customerDetails[0], `is`("Milford"))
        assertThat(customerDetails[1], `is`("Herbalist St."))
        assertThat(customerDetails[2], `is`("New York"))
        assertThat(customerDetails[3], `is`("NY-1010"))
        assertThat(customerDetails[4], `is`("12345"))
    }

    @Test
    @Throws(IOException::class)
    fun `can set order refs section`() {
        val invoiceSheet = invoiceClient!!.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val invoice = Invoice("some invoice number", LocalDate.of(2017, 3, 19), customer, "Bob", asList(itemLine))
        val updatedSheet = invoiceClient!!.setOrderRefsSection(invoiceSheet, invoice)
        val orderRefsDetails = invoiceClient!!.getOrderRefsSectionFrom(updatedSheet)

        assertThat(orderRefsDetails[0], `is`("19/03/2017"))
        assertThat(orderRefsDetails[1], `is`("Bob"))
        assertThat(orderRefsDetails[2], `is`("ACC-1967"))
    }

    @Test
    @Throws(IOException::class)
    fun `can set invoice number`() {
        val invoiceSheet = invoiceClient!!.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val invoice = Invoice("INV-999", LocalDate.now(), customer, "some customer ref", asList(itemLine))
        val updatedSheet = invoiceClient!!.setInvoiceNumber(invoiceSheet, invoice)
        val invoiceNumber = invoiceClient!!.getInvoiceNumberFrom(updatedSheet)

        assertThat(invoiceNumber, `is`("INV-999"))
    }

    @Test
    @Throws(IOException::class)
    fun `can set one item line`() {
        val invoiceSheet = invoiceClient!!.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val singleItemLine = ItemLine(3.0, "Magic beans", 3.0)
        val invoice = Invoice("some invoice number", LocalDate.now(), customer, "some customer ref", asList(singleItemLine))
        val updatedSheet = invoiceClient!!.setItemLine(invoiceSheet, invoice, 0)
        val newFirstLine = invoiceClient!!.getItemLinesFrom(updatedSheet)[0]

        assertThat(newFirstLine, `is`(singleItemLine))
    }

    @Test
    @Throws(IOException::class)
    fun `can set multiple item lines`() {
        val invoiceSheet = invoiceClient!!.getSingleSheetFromPath(get("src/test/resources/INV-001.xls"), 0)
        val firstItemLine = ItemLine(3.0, "Magic beans", 3.0)
        val secondItemLine = ItemLine(1.5, "Golden tickets", 100.0)
        val thirdItemLine = ItemLine(0.5, "Super foods", 19.0)
        val fourthItemLine = ItemLine(3000.0, "Frozen Calamari", 0.15)
        val actualItemLines = asList(firstItemLine, secondItemLine, thirdItemLine, fourthItemLine)
        val invoice = Invoice(
            "some invoice number",
            LocalDate.now(),
            customer,
            "some customer ref",
            actualItemLines
        )
        val updatedItemLines = invoiceClient!!.setItemLines(invoiceSheet, invoice)

        assertThat(updatedItemLines, `is`(actualItemLines))
    }

    @Throws(IOException::class)
    private fun writeInvoiceToFileSystem() {
        val workbook = HSSFWorkbook()
        val testSheet = workbook.createSheet("test invoice")
        createTestRows(testSheet)
        invoiceClient = InvoiceClient.invoiceClientCustomConfig(workbook, get("data/"), get("src/test/resources"))
        val invoice = Invoice("INV-001", LocalDate.now(), customer, "cust ref", asList(itemLine))
        invoiceClient!!.setCustomerSection(testSheet, invoice)
        invoiceClient!!.setInvoiceNumber(testSheet, invoice)
        invoiceClient!!.setOrderRefsSection(testSheet, invoice)
        invoiceClient!!.setItemLines(testSheet, invoice)
        invoiceClient!!.writeFile(invoiceClient!!.fileOutputPath(), invoice)
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