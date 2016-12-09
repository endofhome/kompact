package uk.co.endofhome.javoice;

import org.junit.Before;
import org.junit.Test;
import uk.co.endofhome.javoice.customer.Customer;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.invoice.Invoice;
import uk.co.endofhome.javoice.invoice.ItemLine;
import uk.co.endofhome.javoice.ledger.AnnualReport;
import uk.co.endofhome.javoice.ledger.LedgerEntry;
import uk.co.endofhome.javoice.ledger.MonthlyReport;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.nio.file.Paths.get;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.endofhome.javoice.ledger.AnnualReport.annualReportCustomConfig;
import static uk.co.endofhome.javoice.ledger.LedgerEntry.ledgerEntry;

public class ControllerTest {
    private Controller controller;
    private AnnualReport annualReport;
    private Path pathForTestOutput;
    private CustomerStore customerStore;

    @Before
    public void set_up() throws IOException {
        pathForTestOutput = get("src/test/resources/functional/controller");
        Config.setSalesLedgerFileOutputPath(pathForTestOutput);
        Config.setInvoiceOutputPath(pathForTestOutput);
        Config.setCustomerDataFileOutputPath(pathForTestOutput);
        customerStore = new CustomerStore();
        Customer customer = new Customer("some customer", null, null, null, null, null);
        controller = new Controller(customerStore);
        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.now(), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        annualReport = annualReportCustomConfig(LocalDate.now().getYear(), pathForTestOutput);
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(pathForTestOutput);
    }

    @Test
    public void can_get_next_invoice_number() {
        assertThat(controller.nextInvoiceNumber(annualReport, LocalDate.now().getMonth()), is("2"));
    }

    @Test
    public void can_create_a_new_invoice_and_write_out_all_files() throws Exception {
        Customer anotherCustomer = new Customer("another customer", null, null, null, null, null);
        ItemLine itemLine = new ItemLine(3.0, "Baked beans", 0.75);
        controller.newInvoice(anotherCustomer, "another ref", sequence(itemLine));

        AnnualReport annualReportFromFS = AnnualReport.readFile(get(pathForTestOutput.toString(), "sales" + String.valueOf(LocalDate.now().getYear()) + ".xls"));
        int thisMonth = LocalDate.now().getMonthValue() -1;
        MonthlyReport monthlyReport = annualReportFromFS.monthlyReports().get(thisMonth);
        LedgerEntry lastEntry = monthlyReport.entries.last();
        assertThat(lastEntry.invoiceNumber.get(), is("2"));
        assertThat(lastEntry.date.get(), is(LocalDate.now()));
        assertThat(lastEntry.customerName.get(), is("another customer"));
        assertThat(lastEntry.valueNett.get(), is(2.25));
    }

    @Test
    public void can_add_new_customer_to_existing_customer_DB() throws Exception {
        controller.newCustomer("Friendly Customer", "first bit of address", "second bit of address", "a postcode", "45632", "ACC-9876");

        CustomerStore updatedCustomerStoreFromFS = CustomerStore.readFile(get(pathForTestOutput + "/Customers.xls"), 0);
        Customer customerUnderTest = updatedCustomerStoreFromFS.customers().last();
        assertThat(customerUnderTest.name, is("Friendly Customer"));
        assertThat(customerUnderTest.postcode, is("a postcode"));
        assertThat(customerUnderTest.accountCode, is("ACC-9876"));
    }
}