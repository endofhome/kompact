package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class InvoiceFileActionsTest {
    private Customer customer = new Customer("Milford", "Herbalist St.", "New York", "NY-1010", "12345", "ACC-1967");
    private InvoiceFileActions invoiceFileActions;
    private File file;
    private NPOIFSFileSystem fs;
    private HSSFWorkbook workbook;

    @Before
    public void set_up() throws IOException {
        file = new File("data/test.xls");
        fs = new NPOIFSFileSystem(file);
        workbook = new HSSFWorkbook(fs.getRoot(), true);
        invoiceFileActions = new InvoiceFileActions(workbook);
    }

    @After
    public void clean_up() throws IOException {
        fs.close();
    }

    @Test
    public void can_write_out_a_file() throws IOException {
        Invoice invoice = new Invoice("INV-001", LocalDate.now(), customer, "cust ref", sequence());
        Invoice writtenInvoice = invoiceFileActions.writeFile(invoiceFileActions.rootPath, invoice);

        // TODO: read the written file and test some of the fields.
        assertThat(writtenInvoice.number, is("INV-001"));
    }

    @Test
    public void can_read_in_a_file() throws IOException {
        Invoice invoiceFromFileSystem = invoiceFileActions.readFile("data/INV-001.xls");
        assertThat(invoiceFromFileSystem.number, is("15-000040"));
    }

    @Test
    public void can_set_customer_section() throws IOException {
        HSSFSheet invoiceSheet = invoiceFileActions.getSheetFromPath("data/INV-001.xls");
        Invoice invoice = new Invoice("some invoice number", LocalDate.now(), customer, "anything", sequence());
        HSSFSheet updatedSheet = invoiceFileActions.setCustomerSection(invoiceSheet, invoice);
        Sequence<String> customerDetails = invoiceFileActions.getCustomerSectionFrom(updatedSheet);

        assertThat(customerDetails.get(0), is("Milford"));
        assertThat(customerDetails.get(1), is("Herbalist St."));
        assertThat(customerDetails.get(2), is("New York"));
        assertThat(customerDetails.get(3), is("NY-1010"));
        assertThat(customerDetails.get(4), is("12345"));
    }

    @Test
    public void can_set_order_refs_section() throws IOException {
        HSSFSheet invoiceSheet = invoiceFileActions.getSheetFromPath("data/INV-001.xls");
        Invoice invoice = new Invoice("some invoice number", LocalDate.of(2017, 03, 19), customer, "Bob", sequence());
        HSSFSheet updatedSheet = invoiceFileActions.setOrderRefsSection(invoiceSheet, invoice);
        Sequence<String> orderRefsDetails = invoiceFileActions.getOrderRefsSectionFrom(updatedSheet);

        assertThat(orderRefsDetails.get(0), is("19/03/2017"));
        assertThat(orderRefsDetails.get(1), is("Bob"));
        assertThat(orderRefsDetails.get(2), is("ACC-1967"));
    }

    @Test
    public void can_set_invoice_number() throws IOException {
        HSSFSheet invoiceSheet = invoiceFileActions.getSheetFromPath("data/INV-001.xls");
        Invoice invoice = new Invoice("INV-999", LocalDate.now(), customer, "some customer", sequence());
        HSSFSheet updatedSheet = invoiceFileActions.setInvoiceNumber(invoiceSheet, invoice);
        String invoiceNumber = invoiceFileActions.getInvoiceNumberFrom(updatedSheet);

        assertThat(invoiceNumber, is("INV-999"));
    }

    @Test
    public void can_set_one_item_line() throws IOException {
        HSSFSheet invoiceSheet = invoiceFileActions.getSheetFromPath("data/INV-001.xls");
        ItemLine singleItemLine = new ItemLine(3.0, "Magic beans", 3.0);
        Invoice invoice = new Invoice("some invoice number", LocalDate.now(), customer, "some customer", sequence(singleItemLine));
        ItemLine updatedItemLine = invoiceFileActions.setItemLine(invoiceSheet, invoice, 17);

        assertEquals(updatedItemLine, singleItemLine);
    }
}