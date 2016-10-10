package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InvoiceClientTest {
    private Customer customer = new Customer("Milford", "Herbalist St.", "New York", "NY-1010", "12345", "ACC-1967");
    ItemLine itemLine = new ItemLine(3.0, "Green bottles", 10.0);
    private InvoiceClient invoiceClient;

    @Before
    public void set_up() throws IOException {
        writeInvoiceToFileSystem();
    }

    @Test
    public void can_write_out_and_read_in_a_file() throws IOException {
        Invoice invoiceFromFileSystem = invoiceClient.readFile("src/test/resources/INV-001.xls", 0);

        assertThat(invoiceFromFileSystem.date, is(LocalDate.now()));
        assertThat(invoiceFromFileSystem.number, is("INV-001"));
        assertThat(invoiceFromFileSystem.customer.name, is("Milford"));
        assertThat(invoiceFromFileSystem.orderNumber, is("cust ref"));
        assertThat(invoiceFromFileSystem.itemLines.get(0).description, is("Green bottles"));
    }

    @Test
    public void can_set_customer_section() throws IOException {
        HSSFSheet invoiceSheet = invoiceClient.getSingleSheetFromPath("src/test/resources/INV-001.xls", 0);
        Invoice invoice = new Invoice("some invoice number", LocalDate.now(), customer, "anything", sequence(itemLine));
        HSSFSheet updatedSheet = invoiceClient.setCustomerSection(invoiceSheet, invoice);
        Sequence<String> customerDetails = invoiceClient.getCustomerSectionFrom(updatedSheet);

        assertThat(customerDetails.get(0), is("Milford"));
        assertThat(customerDetails.get(1), is("Herbalist St."));
        assertThat(customerDetails.get(2), is("New York"));
        assertThat(customerDetails.get(3), is("NY-1010"));
        assertThat(customerDetails.get(4), is("12345"));
    }

    @Test
    public void can_set_order_refs_section() throws IOException {
        HSSFSheet invoiceSheet = invoiceClient.getSingleSheetFromPath("src/test/resources/INV-001.xls", 0);
        Invoice invoice = new Invoice("some invoice number", LocalDate.of(2017, 3, 19), customer, "Bob", sequence(itemLine));
        HSSFSheet updatedSheet = invoiceClient.setOrderRefsSection(invoiceSheet, invoice);
        Sequence<String> orderRefsDetails = invoiceClient.getOrderRefsSectionFrom(updatedSheet);

        assertThat(orderRefsDetails.get(0), is("19/03/2017"));
        assertThat(orderRefsDetails.get(1), is("Bob"));
        assertThat(orderRefsDetails.get(2), is("ACC-1967"));
    }

    @Test
    public void can_set_invoice_number() throws IOException {
        HSSFSheet invoiceSheet = invoiceClient.getSingleSheetFromPath("src/test/resources/INV-001.xls", 0);
        Invoice invoice = new Invoice("INV-999", LocalDate.now(), customer, "some customer ref", sequence(itemLine));
        HSSFSheet updatedSheet = invoiceClient.setInvoiceNumber(invoiceSheet, invoice);
        String invoiceNumber = invoiceClient.getInvoiceNumberFrom(updatedSheet);

        assertThat(invoiceNumber, is("INV-999"));
    }

    @Test
    public void can_set_one_item_line() throws IOException {
        HSSFSheet invoiceSheet = invoiceClient.getSingleSheetFromPath("src/test/resources/INV-001.xls", 0);
        ItemLine singleItemLine = new ItemLine(3.0, "Magic beans", 3.0);
        Invoice invoice = new Invoice("some invoice number", LocalDate.now(), customer, "some customer ref", sequence(singleItemLine));
        HSSFSheet updatedSheet = invoiceClient.setItemLine(invoiceSheet, invoice, 0);
        ItemLine newFirstLine = invoiceClient.getItemLinesFrom(updatedSheet).get(0);

        assertThat(newFirstLine, is(singleItemLine));
    }

    @Test
    public void can_set_multiple_item_lines() throws IOException {
        HSSFSheet invoiceSheet = invoiceClient.getSingleSheetFromPath("src/test/resources/INV-001.xls", 0);
        ItemLine firstItemLine = new ItemLine(3.0, "Magic beans", 3.0);
        ItemLine secondItemLine = new ItemLine(1.5, "Golden tickets", 100.0);
        ItemLine thirdItemLine = new ItemLine(0.5, "Super foods", 19.0);
        ItemLine fourthItemLine = new ItemLine(3000.0, "Frozen Calamari", 0.15);
        Sequence<ItemLine> actualItemLines = sequence(firstItemLine, secondItemLine, thirdItemLine, fourthItemLine);
        Invoice invoice = new Invoice(
                "some invoice number",
                LocalDate.now(),
                customer,
                "some customer ref",
                actualItemLines
        );
        Sequence<ItemLine> updatedItemLines = invoiceClient.setItemLines(invoiceSheet, invoice);

        assertThat(updatedItemLines, is(actualItemLines));
    }

    private void writeInvoiceToFileSystem() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet testSheet = workbook.createSheet("test invoice");
        createTestRows(testSheet);
        invoiceClient = new InvoiceClient(workbook, Paths.get("src/test/resources"));
        Invoice invoice = new Invoice("INV-001", LocalDate.now(), customer, "cust ref", sequence(itemLine));
        invoiceClient.setCustomerSection(testSheet, invoice);
        invoiceClient.setInvoiceNumber(testSheet, invoice);
        invoiceClient.setOrderRefsSection(testSheet, invoice);
        invoiceClient.setItemLines(testSheet, invoice);
        invoiceClient.writeFile(invoiceClient.fileOutputPath, invoice);
    }

    private void createTestRows(HSSFSheet testSheet) {
        for (int i = 0; i <= 33; i++) {
            testSheet.createRow(i);
            for (int j = 0; j <= 12; j++) {
                testSheet.getRow(i).createCell(j);
            }
        }
    }
}