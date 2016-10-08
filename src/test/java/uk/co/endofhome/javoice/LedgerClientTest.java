package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static com.googlecode.totallylazy.Option.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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

    @Test
    public void can_set_a_new_entry_in_ledger() throws IOException {
        HSSFSheet ledgerMonthlySheet = ledgerClient.getSheetFromPath("data/SGMSales2015.xls", 4);
        LedgerEntry ledgerEntry = new LedgerEntry(some("Carla Azar"), some("INV-666"), some(10.0), none(), none(), option(LocalDate.now()), none());
        HSSFSheet updatedLedgerMonthlySheet = ledgerClient.setNewEntry(ledgerMonthlySheet, ledgerEntry);
        LedgerMonthly updatedLedgerMonthly = ledgerClient.getLedgerMonthlyFrom(updatedLedgerMonthlySheet);

        assertThat(updatedLedgerMonthly.entries.get(6).customerName.getOrElse(""), is(ledgerEntry.customerName.get()));
        assertThat(updatedLedgerMonthly.entries.get(6).invoiceNumber.getOrElse(""), is(ledgerEntry.invoiceNumber.get()));
        assertThat(updatedLedgerMonthly.entries.get(6).valueNett.getOrElse(0.0), is(ledgerEntry.valueNett.get()));
        assertThat(updatedLedgerMonthly.entries.get(6).date.get(), is(LocalDate.now()));
    }
}