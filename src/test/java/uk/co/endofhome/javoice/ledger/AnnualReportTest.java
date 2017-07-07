package uk.co.endofhome.javoice.ledger;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Year;

import static java.nio.file.Files.notExists;
import static java.time.Month.JANUARY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AnnualReportTest {
    private AnnualReport annualReport;
    private Path testOutputPath;

    @Before
    public void set_up() throws IOException {
        testOutputPath = Paths.get("src/test/resources/functional/annual-report/");
        if (notExists(testOutputPath)) {
            Files.createDirectories(testOutputPath);
        }
        annualReport = AnnualReport.annualReportCustomConfig(1983, testOutputPath);
        annualReport.writeFile(testOutputPath);
    }

    @Test
    public void can_set_a_new_entry_in_ledger() throws IOException {
        HSSFSheet monthlyReportSheet = annualReport.sheetAt(1);
        MonthlyReport monthlyReport = annualReport.monthlyReports().get(0);
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.now(), null);
        HSSFSheet updatedLedgerMonthlySheet = annualReport.setNewEntry(monthlyReportSheet, monthlyReport, ledgerEntry);
        MonthlyReport updatedMonthlyReport = AnnualReport.getMonthlyReportFrom(updatedLedgerMonthlySheet);

        assertThat(updatedMonthlyReport.entries.get(0).getCustomerName(), is(ledgerEntry.getCustomerName()));
        assertThat(updatedMonthlyReport.entries.get(0).getInvoiceNumber(), is(ledgerEntry.getInvoiceNumber()));
        assertThat(updatedMonthlyReport.entries.get(0).getValueNett(), is(ledgerEntry.getValueNett()));
        assertThat(updatedMonthlyReport.entries.get(0).getDate(), is(LocalDate.now()));
    }

    @Test
    public void can_set_a_new_entry_in_ledger_from_LedgerEntry_only() throws IOException {
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.of(1983,11,10), null);
        annualReport.setNewEntry(ledgerEntry);
        MonthlyReport novemberReport = annualReport.monthlyReports().get(10);

        assertThat(novemberReport.entries.get(0).getCustomerName(), is(ledgerEntry.getCustomerName()));
        assertThat(novemberReport.entries.get(0).getInvoiceNumber(), is(ledgerEntry.getInvoiceNumber()));
        assertThat(novemberReport.entries.get(0).getValueNett(), is(ledgerEntry.getValueNett()));
        assertThat(novemberReport.entries.get(0).getDate(), is(LocalDate.of(1983,11,10)));
    }

    @Test(expected = RuntimeException.class)
    public void blows_up_if_ledger_entry_added_to_workbook_for_different_year() throws IOException {
        LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.of(1978,11,10), null);
        annualReport.setNewEntry(ledgerEntry);
    }

    @Test
    public void can_set_footer() throws IOException {
        HSSFSheet monthlyReportSheet = annualReport.sheetAt(1);
        MonthlyReport monthlyReport = annualReport.monthlyReports().get(0);
        LedgerEntry firstLedgerEntry = LedgerEntry.Companion.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.now(), null);
        LedgerEntry secondLedgerEntry = LedgerEntry.Companion.ledgerEntry("Chris Corsano", "INV-909", 11.0, null, null, LocalDate.now(), null);
        LedgerEntry thirdLedgerEntry = LedgerEntry.Companion.ledgerEntry("Drumbo Drums", "INV-101", 1.0, null, null, LocalDate.now(), null);

        HSSFSheet sheetWithOneAdded = annualReport.setNewEntry(monthlyReportSheet, monthlyReport, firstLedgerEntry);
        HSSFSheet sheetWithTwoAdded = annualReport.setNewEntry(sheetWithOneAdded, monthlyReport, secondLedgerEntry);
        HSSFSheet sheetWithThreeAdded = annualReport.setNewEntry(sheetWithTwoAdded, monthlyReport, thirdLedgerEntry);
        HSSFRow secondToLastRow = sheetWithThreeAdded.getRow(sheetWithThreeAdded.getLastRowNum() - 1);
        HSSFRow lastRow = sheetWithThreeAdded.getRow(sheetWithThreeAdded.getLastRowNum());

        assertThat(secondToLastRow.getCell(2).getStringCellValue(), is("Nett"));
        assertThat(secondToLastRow.getCell(3).getStringCellValue(), is("VAT"));
        assertThat(secondToLastRow.getCell(4).getStringCellValue(), is("Gross"));
        assertThat(lastRow.getCell(2).getCellFormula(), is("SUM(C5:C7)"));
        assertThat(lastRow.getCell(3).getCellFormula(), is("SUM(D5:D7)"));
        assertThat(lastRow.getCell(4).getCellFormula(), is("SUM(E5:E7)"));
    }

    @Test
    public void can_read_in_an_annual_report_file() throws IOException {
        annualReport = AnnualReport.annualReportCustomConfig(1984, Paths.get("src/test/resources"));
        HSSFSheet januaryReportSheet = annualReport.sheetAt(1);
        MonthlyReport monthlyReport = annualReport.monthlyReports().get(0);
        LedgerEntry newLedgerEntry = LedgerEntry.Companion.ledgerEntry("Buddy Rich", "INV-909", 3.0, null, null, LocalDate.now(), null);
        annualReport.setNewEntry(januaryReportSheet, monthlyReport, newLedgerEntry);
        annualReport.writeFile(testOutputPath);

        AnnualReport annualReport = AnnualReport.readFile(Paths.get(testOutputPath + "/sales1984.xls"));
        MonthlyReport reportToTest = annualReport.monthlyReports().get(0);

        assertThat(reportToTest.month, is(JANUARY));
        assertThat(reportToTest.year, is(Year.of(1984)));
        assertThat(reportToTest.entries.get(0).getCustomerName(), is("Buddy Rich"));
        assertThat(reportToTest.entries.get(0).getInvoiceNumber(), is("INV-909"));
    }
}