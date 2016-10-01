package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class InvoiceFileActions {
    private final Invoice invoice;
    public final String rootPath;
    private final NPOIFSFileSystem fs;
    public final HSSFWorkbook workBook;
    private final HSSFSheet sheet;

    public InvoiceFileActions(Invoice invoice, File filePath) throws IOException {
        this.invoice = invoice;
        this.rootPath = "data/";
        this.fs = new NPOIFSFileSystem(filePath);
        this.workBook = new HSSFWorkbook(fs.getRoot(), true);
        this.sheet = workBook.getSheetAt(2);
    }

    public Invoice writeFile(String filePath) throws IOException {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            workBook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("There was a problem writing your file.");
        }
        fs.close();
        return this.invoice;
    }
}
