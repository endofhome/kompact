package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import uk.co.endofhome.javoice.customer.CustomerStore;
import uk.co.endofhome.javoice.ledger.AnnualReport;

import java.time.Month;

public class Controller {
    private CustomerStore customerStore;

    public Controller(CustomerStore customerStore) {
        this.customerStore = customerStore;
    }

    public String nextInvoiceNumber(AnnualReport annualReport, Month month) {
        HSSFSheet sheetForThisMonth = annualReport.sheetAt(month.getValue());
        int lastInvoiceRowNum = sheetForThisMonth.getLastRowNum() -2;
        HSSFRow lastInvoiceRow = sheetForThisMonth.getRow(lastInvoiceRowNum);
        int invoiceNumber = Integer.parseInt(lastInvoiceRow.getCell(1).getStringCellValue());
        return String.valueOf(++invoiceNumber);
    }
}
