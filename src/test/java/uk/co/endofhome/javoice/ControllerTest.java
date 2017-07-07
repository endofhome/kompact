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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static uk.co.endofhome.javoice.ledger.AnnualReport.annualReportCustomConfig;

public class ControllerTest {
    private Controller controller;
    private AnnualReport annualReport;
    private Path pathForTestOutput;
    private Customer customer;

    @Before
    public void set_up() throws IOException {
        pathForTestOutput = get("src/test/resources/functional/controller");
        Path pathForTemplateFile = get("src/test/resources/functional/templates/test-template.xls");
        Config.setInvoiceFileTemplatePath(pathForTemplateFile);
        if (Files.notExists(pathForTestOutput)) {
            Files.createDirectories(pathForTestOutput);
        } else {
            cleanDirectory(pathForTestOutput);
        }
        Config.setSalesLedgerFileOutputPath(pathForTestOutput);
        Config.setInvoiceXlsOutputPath(pathForTestOutput);
        Config.setCustomerDataFileOutputPath(get(pathForTestOutput.toString(), "/Customers.xls"));
        CustomerStore customerStore = new CustomerStore();
        customer = new Customer("some customer", null, null, "P05T C0D3", null, "798");
        customerStore.addCustomer(customer);
        controller = new Controller(customerStore);
        annualReport = annualReportCustomConfig(2016, pathForTestOutput);
        annualReport.writeFile(pathForTestOutput);
    }

    @Test
    public void can_get_next_invoice_number_when_invoices_already_exist_for_this_month() throws IOException {
        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2016, 12, 31), customer, "some ref", asList(itemLine));
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry(invoice, null, null, null);
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_next_invoice_number_when_no_invoices_exist_for_this_month_but_do_exist_for_this_year() throws IOException {
        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2016, 3, 1), customer, "some ref", asList(itemLine));
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry(invoice, null, null, null);
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_next_invoice_number_when_last_invoice_exists_for_previous_year() throws IOException, InterruptedException {
        ItemLine itemLine = new ItemLine(5.0, "Old slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2015, 6, 10), customer, "some ref", asList(itemLine));
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry(invoice, null, null, null);
        AnnualReport annualReportPreviousYear = AnnualReport.annualReport(2015);
        annualReportPreviousYear.setNewEntry(ledgerEntry);
        annualReportPreviousYear.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_next_invoice_number_when_last_invoice_exists_six_years_ago() throws IOException, InterruptedException {
        ItemLine itemLine = new ItemLine(5.0, "Really old slices of toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2010, 3, 7), customer, "some ref", asList(itemLine));
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry(invoice, null, null, null);
        AnnualReport annualReport2010 = AnnualReport.annualReport(2010);
        annualReport2010.setNewEntry(ledgerEntry);
        annualReport2010.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("2"));
    }

    @Test
    public void can_get_correct_invoice_number_when_invoices_exists_for_this_year_and_previous_years() throws IOException, InterruptedException {
        ItemLine oldItemLine = new ItemLine(5.0, "Really old slices of toast", 0.5);
        Invoice oldInvoice = new Invoice("1", LocalDate.of(2015, 3, 7), customer, "some ref", asList(oldItemLine));
        LedgerEntry oldLedgerEntry = LedgerEntry.Companion.ledgerEntry(oldInvoice, null, null, null);
        AnnualReport annualReport2015 = AnnualReport.annualReport(2015);
        annualReport2015.setNewEntry(oldLedgerEntry);
        annualReport2015.writeFile(pathForTestOutput);

        ItemLine itemLine = new ItemLine(5.0, "Slices of toast", 0.5);
        Invoice invoice = new Invoice("2", LocalDate.of(2016, 12, 31), customer, "some ref", asList(itemLine));
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry(invoice, null, null, null);
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(pathForTestOutput);

        assertThat(controller.nextInvoiceNumber(annualReport), is("3"));
    }

    @Test
    public void ignores_invoice_number_when_last_invoice_exists_in_future_year() throws IOException, InterruptedException {
        ItemLine itemLine = new ItemLine(5.0, "Super futuristic toast", 0.5);
        Invoice invoice = new Invoice("1", LocalDate.of(2017, 6, 30), customer, "some ref", asList(itemLine));
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry(invoice, null, null, null);
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
        Customer firstCustomer = new Customer("firstCustomer", null, null, null, null, "ACC-01");
        ItemLine someItemLine = new ItemLine(3.0, "Baked beans", 0.75);
        controller.newInvoice(firstCustomer, "some ref", asList(someItemLine));

        Customer secondCustomer = new Customer("secondCustomer", null, null, null, null, "ACC-02");
        ItemLine secondItemLine = new ItemLine(1.0, "Baked beans", 10.20);
        controller.newInvoice(secondCustomer, "another ref", asList(secondItemLine));

        AnnualReport annualReportFromFS = AnnualReport.readFile(get(pathForTestOutput.toString(), "sales" + String.valueOf(LocalDate.now().getYear()) + ".xls"));
        int thisMonth = LocalDate.now().getMonthValue() -1;
        MonthlyReport monthlyReport = annualReportFromFS.monthlyReports().get(thisMonth);
        LedgerEntry lastEntry = monthlyReport.getEntries().get(monthlyReport.getEntries().size() -1);

        assertThat(lastEntry.getInvoiceNumber(), is("2"));
        assertThat(lastEntry.getDate(), is(LocalDate.now()));
        assertThat(lastEntry.getCustomerName(), is("secondCustomer"));
        assertThat(lastEntry.getValueNett(), is(10.20));
    }

    @Test
    public void can_add_new_customer_to_existing_customer_DB() throws Exception {
        controller.newCustomer("Friendly Customer", "first bit of address", "second bit of address", "a postcode", "45632");

        CustomerStore updatedCustomerStoreFromFS = CustomerStore.Companion.readFile(get(pathForTestOutput + "/Customers.xls"), 0);
        List<Customer> customers = updatedCustomerStoreFromFS.getCustomers();
        Customer customerUnderTest = customers.get(customers.size() -1);

        assertThat(customerUnderTest.getName(), is("Friendly Customer"));
        assertThat(customerUnderTest.getPostcode(), is("a postcode"));
        assertThat(customerUnderTest.getAccountCode(), is("799"));
    }

    @Test
    public void can_find_customer_using_exact_name() throws Exception {
        Customer foundCustomer = controller.findCustomer("some customer");

        assertThat(foundCustomer.getName(), is("some customer"));
        assertThat(foundCustomer.getPostcode(), is("P05T C0D3"));
        assertThat(foundCustomer.getAccountCode(), is("798"));
    }

    @Test
    public void cannot_find_any_customers_if_customer_name_does_not_match_exactly() throws Exception {
        Customer missingCustomer = controller.findCustomer("I don't exist");

        assertNull(missingCustomer);
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