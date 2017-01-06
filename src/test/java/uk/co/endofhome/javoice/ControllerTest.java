package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Option;
import org.junit.Before;
import org.junit.Test;
import uk.co.endofhome.javoice.customer.Customer;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.invoice.Invoice;
import uk.co.endofhome.javoice.invoice.ItemLine;
import uk.co.endofhome.javoice.ledger.AnnualReport;
import uk.co.endofhome.javoice.ledger.LedgerEntry;
import uk.co.endofhome.javoice.ledger.MonthlyReport;

import java.io.File;
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
    private Customer customer;

    @Before
    public void set_up() throws IOException {
        pathForTestOutput = get("src/test/resources/functional/controller");
        cleanDirectory(pathForTestOutput);
        Config.setSalesLedgerFileOutputPath(pathForTestOutput);
        Config.setInvoiceOutputPath(pathForTestOutput);
        Config.setCustomerDataFileOutputPath(get(pathForTestOutput.toString(), "/Customers.xls"));
        customerStore = new CustomerStore();
        customer = new Customer("some customer", null, null, "P05T C0D3", null, "798");
        customerStore.addCustomer(customer);
        controller = new Controller(customerStore);
        annualReport = annualReportCustomConfig(2016, pathForTestOutput);
        annualReport.writeFile(pathForTestOutput);
    }

    @Test
    public void can_get_next_invoice_number_when_invoices_already_exist_for_this_month() throws IOException {
        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2016, 12, 31), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_next_invoice_number_when_no_invoices_exist_for_this_month_but_do_exist_for_this_year() throws IOException {
        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2016, 3, 1), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_next_invoice_number_when_last_invoice_exists_for_previous_year() throws IOException, InterruptedException {
        ItemLine itemLine = new ItemLine(5.0, "Old slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2015, 6, 10), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        AnnualReport annualReportPreviousYear = AnnualReport.annualReport(2015);
        annualReportPreviousYear.setNewEntry(ledgerEntry);
        annualReportPreviousYear.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_next_invoice_number_when_last_invoice_exists_six_years_ago() throws IOException, InterruptedException {
        ItemLine itemLine = new ItemLine(5.0, "Really old slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2010, 3, 7), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        AnnualReport annualReport2010 = AnnualReport.annualReport(2010);
        annualReport2010.setNewEntry(ledgerEntry);
        annualReport2010.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_correct_invoice_number_when_invoices_exists_for_this_year_and_previous_years() throws IOException, InterruptedException {
        ItemLine oldItemLine = new ItemLine(5.0, "Really old slices of toast", 0.5);
        Invoice oldInvoice = new Invoice("1", LocalDate.of(2015, 3, 7), customer, "some ref", sequence(oldItemLine));
        LedgerEntry oldLedgerEntry = ledgerEntry(oldInvoice, none(), none(), none());
        AnnualReport annualReport2015 = AnnualReport.annualReport(2015);
        annualReport2015.setNewEntry(oldLedgerEntry);
        annualReport2015.writeFile(pathForTestOutput);

        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice = new Invoice("2", LocalDate.of(2016, 12, 31), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("3"));
    }

    @Test
    public void ignores_invoice_number_when_last_invoice_exists_in_future_year() throws IOException, InterruptedException {
        ItemLine itemLine = new ItemLine(5.0, "Super futuristic toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2017, 6, 30), customer, "some ref", sequence(itemLine));
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        AnnualReport annualReport2017 = AnnualReport.annualReport(2017);
        annualReport2017.setNewEntry(ledgerEntry);
        annualReport2017.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("1"));
    }

    @Test
    public void can_get_next_invoice_number_when_no_invoices_exist_at_all() throws IOException {
        assertThat(controller.nextInvoiceNumber(annualReport), is("1"));
    }

    @Test
    public void can_get_next_account_number() throws Exception {
        assertThat(controller.nextAccountNumber(), is("799"));
    }

    @Test
    public void can_create_a_new_invoice_and_write_out_all_files() throws Exception {
        Customer firstCustomer = new Customer("firstCustomer", null, null, null, null, null);
        ItemLine someItemLine = new ItemLine(3.0, "Baked beans", 0.75);
        controller.newInvoice(firstCustomer, "some ref", sequence(someItemLine));

        Customer secondCustomer = new Customer("secondCustomer", null, null, null, null, null);
        ItemLine secondItemLine = new ItemLine(1.0, "Baked beans", 10.20);
        controller.newInvoice(secondCustomer, "another ref", sequence(secondItemLine));

        AnnualReport annualReportFromFS = AnnualReport.readFile(get(pathForTestOutput.toString(), "sales" + String.valueOf(LocalDate.now().getYear()) + ".xls"));
        int thisMonth = LocalDate.now().getMonthValue() -1;
        MonthlyReport monthlyReport = annualReportFromFS.monthlyReports().get(thisMonth);
        LedgerEntry lastEntry = monthlyReport.entries.last();

        assertThat(lastEntry.invoiceNumber.get(), is("2"));
        assertThat(lastEntry.date.get(), is(LocalDate.now()));
        assertThat(lastEntry.customerName.get(), is("secondCustomer"));
        assertThat(lastEntry.valueNett.get(), is(10.20));
    }

    @Test
    public void can_add_new_customer_to_existing_customer_DB() throws Exception {
        controller.newCustomer("Friendly Customer", "first bit of address", "second bit of address", "a postcode", "45632");

        CustomerStore updatedCustomerStoreFromFS = CustomerStore.readFile(get(pathForTestOutput + "/Customers.xls"), 0);
        Customer customerUnderTest = updatedCustomerStoreFromFS.customers().last();

        assertThat(customerUnderTest.name, is("Friendly Customer"));
        assertThat(customerUnderTest.postcode, is("a postcode"));
        assertThat(customerUnderTest.accountCode, is("799"));
    }

    @Test
    public void can_find_customer_using_exact_name() throws Exception {
        Option<Customer> foundCustomer = controller.findCustomer("some customer");

        assertThat(foundCustomer.get().name, is("some customer"));
        assertThat(foundCustomer.get().postcode, is("P05T C0D3"));
        assertThat(foundCustomer.get().accountCode, is("798"));
    }

    @Test
    public void cannot_find_any_customers_if_customer_name_does_not_match_exactly() throws Exception {
        Option<Customer> missingCustomer = controller.findCustomer("I don't exist");

        assertThat(missingCustomer, is(none()));
    }

    private void cleanDirectory(Path pathForTestOutput) {
        File[] testDir = pathForTestOutput.toFile().listFiles();
        for (File file : testDir) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }
}