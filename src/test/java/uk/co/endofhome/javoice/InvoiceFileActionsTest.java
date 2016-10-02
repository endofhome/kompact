package uk.co.endofhome.javoice;

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
import static org.hamcrest.MatcherAssert.assertThat;

public class InvoiceFileActionsTest {
    private Customer customer = new Customer("My Customer", "Some street", "Some town", "S0M3 T0W9", "1234567890", "ACC-01");
    private Invoice invoice = new Invoice(LocalDate.now(), customer, "ORD-01", "Bob", sequence());
    private InvoiceFileActions invoiceFileActions;
    private File file;
    private NPOIFSFileSystem fs;
    private HSSFWorkbook workbook;

    @Before
    public void set_up() throws IOException {
        file = new File("data/test.xls");
        fs = new NPOIFSFileSystem(file);
        workbook = new HSSFWorkbook(fs.getRoot(), true);
        invoiceFileActions = new InvoiceFileActions(invoice, workbook);
    }

    @After
    public void clean_up() throws IOException {
        fs.close();
    }

    @Test
    public void can_write_out_a_file() throws IOException {
        Invoice writtenInvoice = invoiceFileActions.writeFile(invoiceFileActions.rootPath);
        // TODO: read the written file and test some of the fields.

        assertThat(writtenInvoice.orderNumber, is("ORD-01"));
    }
}