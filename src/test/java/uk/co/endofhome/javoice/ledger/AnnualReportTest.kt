package uk.co.endofhome.javoice.ledger

import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.junit.Before
import org.junit.Test

import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Year

import com.googlecode.totallylazy.Option.none
import com.googlecode.totallylazy.Option.some
import java.time.Month.JANUARY
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import uk.co.endofhome.javoice.ledger.LedgerEntry.Companion.ledgerEntry
import java.time.Month

class AnnualReportTest {
    private lateinit var annualReport: AnnualReport
    private lateinit var testOutputPath: Path

    @Before
    @Throws(IOException::class)
    fun set_up() {
        testOutputPath = Paths.get("src/test/resources/functional/annual-report/")
        annualReport = AnnualReport.annualReportCustomConfig(1983, testOutputPath)
        annualReport.writeFile(testOutputPath)
    }

    @Test
    @Throws(IOException::class)
    fun can_set_a_new_entry_in_ledger() {
        val monthlyReportSheet = annualReport.sheetAt(1)
        val monthlyReport = annualReport.monthlyReports().get(0)
        val ledgerEntry = ledgerEntry(some("Carla Azar"), some("INV-808"), some(10.0), none(), none(), some(LocalDate.now()), none())
        val updatedLedgerMonthlySheet = annualReport.setNewEntry(monthlyReportSheet, monthlyReport, ledgerEntry)
        val updatedMonthlyReport = AnnualReport.getMonthlyReportFrom(updatedLedgerMonthlySheet)

        assertThat(updatedMonthlyReport.entries.get(0).customerName.getOrElse(""), `is`<String>(ledgerEntry.customerName.get()))
        assertThat(updatedMonthlyReport.entries.get(0).invoiceNumber.getOrElse(""), `is`<String>(ledgerEntry.invoiceNumber.get()))
        assertThat(updatedMonthlyReport.entries.get(0).valueNett.getOrElse(0.0), `is`<Double>(ledgerEntry.valueNett.get()))
        assertThat(updatedMonthlyReport.entries.get(0).date.get(), `is`(LocalDate.now()))
    }

    @Test
    @Throws(IOException::class)
    fun can_set_a_new_entry_in_ledger_from_LedgerEntry_only() {
        val ledgerEntry = ledgerEntry(some("Carla Azar"), some("INV-808"), some(10.0), none(), none(), some(LocalDate.of(1983, 11, 10)), none())
        annualReport.setNewEntry(ledgerEntry)
        val novemberReport = annualReport.monthlyReports().get(10)

        assertThat(novemberReport.entries.get(0).customerName.getOrElse(""), `is`<String>(ledgerEntry.customerName.get()))
        assertThat(novemberReport.entries.get(0).invoiceNumber.getOrElse(""), `is`<String>(ledgerEntry.invoiceNumber.get()))
        assertThat(novemberReport.entries.get(0).valueNett.getOrElse(0.0), `is`<Double>(ledgerEntry.valueNett.get()))
        assertThat(novemberReport.entries.get(0).date.get(), `is`(LocalDate.of(1983, 11, 10)))
    }

    @Test(expected = RuntimeException::class)
    @Throws(IOException::class)
    fun blows_up_if_ledger_entry_added_to_workbook_for_different_year() {
        val ledgerEntry = ledgerEntry(some("Carla Azar"), some("INV-808"), some(10.0), none(), none(), some(LocalDate.of(1978, 11, 10)), none())
        annualReport.setNewEntry(ledgerEntry)
    }

    @Test
    @Throws(IOException::class)
    fun can_set_footer() {
        val monthlyReportSheet = annualReport.sheetAt(1)
        val monthlyReport = annualReport.monthlyReports().get(0)
        val firstLedgerEntry = ledgerEntry(some("Carla Azar"), some("INV-808"), some(10.0), none(), none(), some(LocalDate.now()), none())
        val secondLedgerEntry = ledgerEntry(some("Chris Corsano"), some("INV-909"), some(11.0), none(), none(), some(LocalDate.now()), none())
        val thirdLedgerEntry = ledgerEntry(some("Drumbo Drums"), some("INV-101"), some(1.0), none(), none(), some(LocalDate.now()), none())

        val sheetWithOneAdded = annualReport.setNewEntry(monthlyReportSheet, monthlyReport, firstLedgerEntry)
        val sheetWithTwoAdded = annualReport.setNewEntry(sheetWithOneAdded, monthlyReport, secondLedgerEntry)
        val sheetWithThreeAdded = annualReport.setNewEntry(sheetWithTwoAdded, monthlyReport, thirdLedgerEntry)
        val secondToLastRow = sheetWithThreeAdded.getRow(sheetWithThreeAdded.lastRowNum - 1)
        val lastRow = sheetWithThreeAdded.getRow(sheetWithThreeAdded.lastRowNum)

        assertThat(secondToLastRow.getCell(2).stringCellValue, `is`("Nett"))
        assertThat(secondToLastRow.getCell(3).stringCellValue, `is`("VAT"))
        assertThat(secondToLastRow.getCell(4).stringCellValue, `is`("Gross"))
        assertThat(lastRow.getCell(2).cellFormula, `is`("SUM(C5:C7)"))
        assertThat(lastRow.getCell(3).cellFormula, `is`("SUM(D5:D7)"))
        assertThat(lastRow.getCell(4).cellFormula, `is`("SUM(E5:E7)"))
    }

    @Test
    @Throws(IOException::class)
    fun can_read_in_an_annual_report_file() {
        annualReport = AnnualReport.annualReportCustomConfig(1984, Paths.get("src/test/resources"))
        val januaryReportSheet = annualReport.sheetAt(1)
        val monthlyReport = annualReport.monthlyReports().get(0)
        val newLedgerEntry = ledgerEntry(some("Buddy Rich"), some("INV-909"), some(3.0), none(), none(), some(LocalDate.now()), none())
        annualReport.setNewEntry(januaryReportSheet, monthlyReport, newLedgerEntry)
        annualReport.writeFile(testOutputPath)

        val annualReport = AnnualReport.readFile(Paths.get(testOutputPath.toString() + "/sales1984.xls"))
        val reportToTest = annualReport.monthlyReports().get(0)

        assertThat<Month>(reportToTest.month, `is`<Month>(JANUARY))
        assertThat(reportToTest.year, `is`(Year.of(1984)))
        assertThat(reportToTest.entries.get(0).customerName.get(), `is`("Buddy Rich"))
        assertThat(reportToTest.entries.get(0).invoiceNumber.get(), `is`("INV-909"))
    }
}