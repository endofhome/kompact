package uk.co.endofhome.javoice;

import org.junit.Before;
import org.junit.Test;
import uk.co.endofhome.javoice.customer.Customer;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.invoice.Invoice;
import uk.co.endofhome.javoice.invoice.ItemLine;
import uk.co.endofhome.javoice.ledger.AnnualReport;
import uk.co.endofhome.javoice.ledger.LedgerEntry;

import java.nio.file.Paths;
import java.time.LocalDate;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.time.Month.NOVEMBER;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.endofhome.javoice.ledger.AnnualReport.annualReportCustomConfig;
import static uk.co.endofhome.javoice.ledger.LedgerEntry.ledgerEntry;

public class ControllerTest {
    private Controller controller;
    private AnnualReport annualReport;

    @Before
    public void set_up() {
        CustomerStore customerStore = new CustomerStore();
        Customer customer = new Customer("some customer", null, null, null, null, null);
        controller = new Controller(customerStore);
        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice1 = new Invoice("1", LocalDate.of(2016, 11, 10), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice1, none(), none(), none());
        annualReport = annualReportCustomConfig(2016, Paths.get("src/test/resources/functional"));
        annualReport.setNewEntry(ledgerEntry);
    }

    @Test
    public void can_get_next_invoice_number() {
        assertThat(controller.nextInvoiceNumber(annualReport, NOVEMBER), is("2"));
    }
}