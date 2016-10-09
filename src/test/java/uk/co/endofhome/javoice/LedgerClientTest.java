package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFRow;
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
        LedgerEntry ledgerEntry = new LedgerEntry(some("Carla Azar"), some("INV-808"), some(10.0), none(), none(), option(LocalDate.now()), none());
        HSSFSheet updatedLedgerMonthlySheet = ledgerClient.setNewEntry(ledgerMonthlySheet, ledgerEntry);

        HSSFSheet updatedLedgerMonthlySheetNoFooter = ledgerClient.removeFooter(updatedLedgerMonthlySheet);
        LedgerMonthly updatedLedgerMonthly = ledgerClient.getLedgerMonthlyFrom(updatedLedgerMonthlySheetNoFooter);

        assertThat(updatedLedgerMonthly.entries.get(79).customerName.getOrElse(""), is(ledgerEntry.customerName.get()));
        assertThat(updatedLedgerMonthly.entries.get(79).invoiceNumber.getOrElse(""), is(ledgerEntry.invoiceNumber.get()));
        assertThat(updatedLedgerMonthly.entries.get(79).valueNett.getOrElse(0.0), is(ledgerEntry.valueNett.get()));
        assertThat(updatedLedgerMonthly.entries.get(79).date.get(), is(LocalDate.now()));
    }

    @Test
    public void can_set_footer() throws IOException {
        HSSFSheet ledgerMonthlySheet = ledgerClient.getSheetFromPath("data/SGMSales2015.xls", 4);
        LedgerEntry firstLedgerEntry = new LedgerEntry(some("Carla Azar"), some("INV-808"), some(10.0), none(), none(), option(LocalDate.now()), none());
        LedgerEntry secondLedgerEntry = new LedgerEntry(some("Chris Corsano"), some("INV-909"), some(11.0), none(), none(), option(LocalDate.now()), none());
        LedgerEntry thirdLedgerEntry = new LedgerEntry(some("Drumbo Drums"), some("INV-101"), some(1.0), none(), none(), option(LocalDate.now()), none());

        HSSFSheet sheetWithOneAdded = ledgerClient.setNewEntry(ledgerMonthlySheet, firstLedgerEntry);
        HSSFSheet sheetWithTwoAdded = ledgerClient.setNewEntry(sheetWithOneAdded, secondLedgerEntry);
        HSSFSheet sheetWithThreeAdded = ledgerClient.setNewEntry(sheetWithTwoAdded, thirdLedgerEntry);
        HSSFRow secondToLastRow = sheetWithThreeAdded.getRow(sheetWithThreeAdded.getLastRowNum() - 1);
        HSSFRow lastRow = sheetWithThreeAdded.getRow(sheetWithThreeAdded.getLastRowNum());

        assertThat(secondToLastRow.getCell(2).getStringCellValue(), is("Nett"));
        assertThat(secondToLastRow.getCell(3).getStringCellValue(), is("VAT"));
        assertThat(secondToLastRow.getCell(4).getStringCellValue(), is("Gross"));
        assertThat(lastRow.getCell(2).getCellFormula(), is("SUM(C5:C85)"));
        assertThat(lastRow.getCell(3).getCellFormula(), is("SUM(D5:D85)"));
        assertThat(lastRow.getCell(4).getCellFormula(), is("SUM(E5:E85)"));
    }
}