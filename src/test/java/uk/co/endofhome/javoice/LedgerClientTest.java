package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

public class LedgerClientTest {
    private LedgerClient ledgerClient;
    private File file;
    private NPOIFSFileSystem fs;

    @Before
    public void set_up() throws IOException {
        file = new File("data/SGMSales2015.xls");
        fs = new NPOIFSFileSystem(file);
        HSSFWorkbook workbook = new HSSFWorkbook(fs.getRoot(), true);
        ledgerClient = new LedgerClient(workbook);
    }

    @After
    public void clean_up() throws IOException {
        fs.close();
    }
}