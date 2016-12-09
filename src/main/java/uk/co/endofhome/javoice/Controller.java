package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import uk.co.endofhome.javoice.customer.Customer;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.invoice.Invoice;
import uk.co.endofhome.javoice.invoice.InvoiceClient;
import uk.co.endofhome.javoice.invoice.ItemLine;
import uk.co.endofhome.javoice.ledger.AnnualReport;
import uk.co.endofhome.javoice.ledger.LedgerEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

import static com.googlecode.totallylazy.Option.none;
import static java.nio.file.Files.notExists;
import static java.nio.file.Paths.get;
import static uk.co.endofhome.javoice.Config.invoiceFileOutputPath;
import static uk.co.endofhome.javoice.Config.salesLedgerFileOutputPath;
import static uk.co.endofhome.javoice.invoice.InvoiceClient.invoiceClient;
import static uk.co.endofhome.javoice.ledger.AnnualReport.annualReport;
import static uk.co.endofhome.javoice.ledger.LedgerEntry.ledgerEntry;

public class Controller {
    private CustomerStore customerStore;

    Controller(CustomerStore customerStore) {
        this.customerStore = customerStore;
    }

    void newInvoice(Customer customer, String orderNumber, Sequence<ItemLine> itemLines) throws IOException {
        int year = LocalDate.now().getYear();
        Month month = LocalDate.now().getMonth();
        Path annualReportForThisYear = get(salesLedgerFileOutputPath().toString(), String.format("sales%s.xls", year));
        if (notExists(annualReportForThisYear)) {
            AnnualReport newAnnualReport = annualReport(year);
            newAnnualReport.writeFile(salesLedgerFileOutputPath());
        }
        AnnualReport annualReport = AnnualReport.readFile(get(salesLedgerFileOutputPath().toString(), String.format("sales%s.xls", year)));
        Invoice invoice = new Invoice(nextInvoiceNumber(annualReport, month), LocalDate.now(), customer, orderNumber, itemLines);
        InvoiceClient invoiceClient = invoiceClient(new HSSFWorkbook());
        HSSFSheet invoiceSheet;
        try {
            invoiceSheet = invoiceClient.getSingleSheetFromPath(Config.invoiceFileTemplatePath(), 2);
        } catch (IOException e) {
            throw new IOException("Controller could not get invoice template.");
        }
        invoiceClient.setSections(invoiceSheet, invoice);
        try {
            invoiceClient.writeFile(invoiceFileOutputPath(), invoice);
        } catch (IOException e) {
            throw new IOException("Controller could not write new invoice file.");
        }
        LedgerEntry ledgerEntry = ledgerEntry(invoice, none(), none(), none());
        annualReport.setNewEntry(ledgerEntry);
        annualReport.writeFile(salesLedgerFileOutputPath());
    }

    String nextInvoiceNumber(AnnualReport annualReport, Month month) {
        HSSFSheet sheetForThisMonth = annualReport.sheetAt(month.getValue());
        int lastInvoiceRowNum = sheetForThisMonth.getLastRowNum() -2;
        HSSFRow lastInvoiceRow = sheetForThisMonth.getRow(lastInvoiceRowNum);
        int invoiceNumber = Integer.parseInt(lastInvoiceRow.getCell(1).getStringCellValue());
        return String.valueOf(++invoiceNumber);
    }
}
