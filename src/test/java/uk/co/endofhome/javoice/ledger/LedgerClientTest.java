package uk.co.endofhome.javoice.ledger;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;

import static com.googlecode.totallylazy.Option.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LedgerClientTest {
    private LedgerClient ledgerClient;
    private HSSFWorkbook workbook;

    @Before
    public void set_up() throws IOException {
        workbook = new HSSFWorkbook();
        ledgerClient = new LedgerClient();
        HSSFSheet testSheet = workbook.createSheet("test sheet");
        createHeaderRows(testSheet);
        FileOutputStream fileOut = new FileOutputStream("src/test/resources/test_ledger.xls");
        ledgerClient.setFooter(testSheet);
        workbook.write(fileOut);
        fileOut.close();
    }

    @Test
    public void can_set_a_new_entry_in_ledger() throws IOException {
        HSSFSheet ledgerMonthlySheet = workbook.getSheetAt(0);
        LedgerEntry ledgerEntry = new LedgerEntry(some("Carla Azar"), some("INV-808"), some(10.0), none(), none(), option(LocalDate.now()), none());
        HSSFSheet updatedLedgerMonthlySheet = ledgerClient.setNewEntry(ledgerMonthlySheet, ledgerEntry);
        HSSFSheet updatedLedgerMonthlySheetNoFooter = ledgerClient.removeFooter(updatedLedgerMonthlySheet);
        MonthlyReport updatedMonthlyReport = ledgerClient.getLedgerMonthlyFrom(updatedLedgerMonthlySheetNoFooter);

        assertThat(updatedMonthlyReport.entries.get(0).customerName.getOrElse(""), is(ledgerEntry.customerName.get()));
        assertThat(updatedMonthlyReport.entries.get(0).invoiceNumber.getOrElse(""), is(ledgerEntry.invoiceNumber.get()));
        assertThat(updatedMonthlyReport.entries.get(0).valueNett.getOrElse(0.0), is(ledgerEntry.valueNett.get()));
        assertThat(updatedMonthlyReport.entries.get(0).date.get(), is(LocalDate.now()));
    }

    @Test
    public void can_set_footer() throws IOException {
        HSSFSheet ledgerMonthlySheet = workbook.getSheetAt(0);
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
        assertThat(lastRow.getCell(2).getCellFormula(), is("SUM(C5:C7)"));
        assertThat(lastRow.getCell(3).getCellFormula(), is("SUM(D5:D7)"));
        assertThat(lastRow.getCell(4).getCellFormula(), is("SUM(E5:E7)"));
    }

    private void createHeaderRows(HSSFSheet testSheet) {
        testSheet.createRow(0);
        testSheet.createRow(1);
        testSheet.getRow(1).createCell(0).setCellValue(Month.JANUARY.toString());
        testSheet.getRow(1).createCell(1).setCellValue(2015);
        testSheet.createRow(2);
        testSheet.createRow(3);
    }
}