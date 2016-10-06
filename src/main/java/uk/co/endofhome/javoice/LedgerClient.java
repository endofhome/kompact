package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class LedgerClient {
    private final String rootPath;
    private final HSSFWorkbook workBook;

    public LedgerClient(HSSFWorkbook workBook) {
        this.rootPath = "data/";
        this.workBook = workBook;
    }
}
