package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import uk.co.endofhome.javoice.customer.Customer;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.invoice.Invoice;
import uk.co.endofhome.javoice.invoice.InvoiceClient;
import uk.co.endofhome.javoice.invoice.ItemLine;
import uk.co.endofhome.javoice.ledger.AnnualReport;
import uk.co.endofhome.javoice.ledger.LedgerEntry;
import uk.co.endofhome.javoice.ledger.MonthlyReport;
import uk.co.endofhome.javoice.pdf.PdfConvertor;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.notExists;
import static java.nio.file.Paths.get;
import static java.time.Month.DECEMBER;
import static java.time.temporal.ChronoUnit.YEARS;
import static uk.co.endofhome.javoice.Config.invoicePdfFileOutputPath;
import static uk.co.endofhome.javoice.Config.invoiceXlsFileOutputPath;
import static uk.co.endofhome.javoice.Config.salesLedgerFileOutputPath;
import static uk.co.endofhome.javoice.invoice.InvoiceClient.invoiceClient;
import static uk.co.endofhome.javoice.ledger.AnnualReport.annualReport;
import static uk.co.endofhome.javoice.ledger.AnnualReport.readFile;
import static uk.co.endofhome.javoice.ledger.LedgerEntry.ledgerEntry;

public class Controller implements Observer {
    private final CustomerStore customerStore;
    public Option<Customer> currentCustomer = none();

    Controller(CustomerStore customerStore) {
        this.customerStore = customerStore;
    }

    public void newInvoice(Customer customer, String orderNumber, Sequence<ItemLine> itemLines) throws IOException {
        int year = LocalDate.now().getYear();
        ensureAnnualReportForThisYear(year, annualReportFor(year));
        AnnualReport annualReport = AnnualReport.readFile(annualReportFor(year));
        Invoice invoice = new Invoice(nextInvoiceNumber(annualReport), LocalDate.now(), customer, orderNumber, itemLines);
        InvoiceClient invoiceClient = invoiceClient(new HSSFWorkbook());
        HSSFSheet invoiceTemplateSheet = getSheetForInvoiceTemplate(invoiceClient);
        createInvoiceOnFS(invoice, invoiceClient, invoiceTemplateSheet);
        updateAnnualReportOnFS(annualReport, invoice);
        try {
            PdfConvertor.convert(invoiceClient.invoiceFilePath(invoiceXlsFileOutputPath(), invoice));
        } catch (Exception e) {
            if (exists(Config.libreOfficePath())) {
                throw new RuntimeException("Couldn't write PDF for invoice " + invoice.number + " and path " + invoicePdfFileOutputPath() + e);
            }
        }
    }

    public void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNum) throws IOException {
        Customer newCustomer = new Customer(name, addressOne, addressTwo, postcode, phoneNum, nextAccountNumber());
        customerStore.addCustomer(newCustomer);
        customerStore.writeFile(Config.customerDataFilePath());
    }

    public Option<Customer> findCustomer(String customerName) {
        return customerStore.search(customerName);
    }

    @Override
    public void setCurrentCustomer(Option<Customer> customer) {
        currentCustomer = customer;
    }

    public String nextAccountNumber() {
        return customerStore.nextAccountNumber();
    }

    public String nextInvoiceNumber(AnnualReport currentAnnualReport) throws IOException {
        String result = "1";
        for (int i = 0; i <= 6; i++) {
            Year currentYear = currentAnnualReport.year.minus(i, YEARS);
            if (exists(annualReportFor(currentYear.getValue()))) {
                currentAnnualReport = readFile(annualReportFor(currentYear.getValue()));
                Option<String> invoiceNumberValue = findNextInvoiceNumberIn(currentAnnualReport, DECEMBER);
                if (invoiceNumberValue.isDefined()) {
                    result = invoiceNumberValue.get();
                    break;
                }
            }
        }
        return result;
    }

    private Option<String> findNextInvoiceNumberIn(AnnualReport annualReport, Month month) {
            for (int i = month.getValue(); i >= 1; i--) {
                MonthlyReport monthlyReport = annualReport.monthlyReports().get(i - 1);
                Sequence<LedgerEntry> reversedEntries = monthlyReport.entries.reverse();
                Option<LedgerEntry> lastInvoiceEntryOption = reversedEntries.find(entry -> entry.invoiceNumber.isDefined());
                if (lastInvoiceEntryOption.isDefined()) {
                    Option<Integer> invoiceNumberOption = parseIntOption(lastInvoiceEntryOption.get().invoiceNumber.get());
                    if (invoiceNumberOption.isDefined()) {
                        Integer invoiceNumber = invoiceNumberOption.get();
                        return option(String.valueOf(++invoiceNumber));
                    }
                }
        }
        return none();
    }

    private void updateAnnualReportOnFS(AnnualReport annualReport, Invoice invoice) throws IOException {
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(salesLedgerFileOutputPath());
    }

    private Path annualReportFor(int year) {
        return get(salesLedgerFileOutputPath().toString(), String.format("sales%s.xls", year));
    }

    private void createInvoiceOnFS(Invoice invoice, InvoiceClient invoiceClient, HSSFSheet invoiceSheet) throws IOException {
        invoiceClient.setSections(invoiceSheet, invoice);
        try {
            invoiceClient.writeFile(invoiceXlsFileOutputPath(), invoice);
        } catch (IOException e) {
            throw new IOException("Controller could not write new invoice file.");
        }
    }

    private HSSFSheet getSheetForInvoiceTemplate(InvoiceClient invoiceClient) throws IOException {
        HSSFSheet invoiceSheet;
        try {
            invoiceSheet = invoiceClient.getSingleSheetFromPath(Config.invoiceFileTemplatePath(), 2);
        } catch (IOException e) {
            throw new IOException("Controller could not get invoice template.");
        }
        return invoiceSheet;
    }

    private void ensureAnnualReportForThisYear(int year, Path annualReportForThisYear) throws IOException {
        if (notExists(annualReportForThisYear)) {
            AnnualReport newAnnualReport = annualReport(year);
            newAnnualReport.writeFile(salesLedgerFileOutputPath());
        }
    }

    private Option<Integer> parseIntOption(String lastInvoiceNumber) {
        try {
            return option(Integer.parseInt(lastInvoiceNumber));
        } catch (NumberFormatException e) {
            return none();
        }
    }
}
