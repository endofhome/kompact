package uk.co.endofhome.javoice;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InvoiceFileActionsTest {

    @Test
    public void can_write_out_a_file() throws IOException {
        Customer customer = new Customer("My Customer", "Some street", "Some town", "S0M3 T0W9", "1234567890", "ACC-01");
        Invoice invoice = new Invoice(LocalDate.now(), customer, "ORD-01", "Bob", sequence());
        InvoiceFileActions invoiceFileActions = new InvoiceFileActions(invoice, new File("data/test.xls"));
        Invoice writtenInvoice = invoiceFileActions.writeFile(invoiceFileActions.rootPath + "testOutput.xls");
        // TODO: read the written file and test some of the fields.

        assertThat(writtenInvoice.orderNumber, is("ORD-01"));
    }
}