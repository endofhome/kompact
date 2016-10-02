package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InvoiceFileActions {
    private final Invoice invoice;
    public final String rootPath;
    public final HSSFWorkbook workBook;
    private final HSSFSheet sheet;

    public InvoiceFileActions(Invoice invoice, HSSFWorkbook workBook) {
        this.invoice = invoice;
        this.rootPath = "data/";
        this.workBook = workBook;
        this.sheet = workBook.getSheetAt(2);
    }

    public Invoice writeFile(String filePath) throws IOException {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath + invoice.orderNumber + ".xls");
            workBook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("There was a problem writing your file.");
        }
        return this.invoice;
    }
}
