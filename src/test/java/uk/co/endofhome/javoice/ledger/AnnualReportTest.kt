package uk.co.endofhome.javoice.ledger

import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Files.notExists
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Month.JANUARY
import java.time.Year

class AnnualReportTest {
    private var testOutputPath: Path = Paths.get("src/test/resources/functional/annual-report/")
    private var annualReport: AnnualReport = AnnualReport.annualReportCustomConfig(1983, testOutputPath)

    @Before
    @Throws(IOException::class)
    fun setUp() {
        val testOutputPath = Paths.get("src/test/resources/functional/annual-report/")
        if (notExists(testOutputPath)) {
            Files.createDirectories(testOutputPath)
        }
        val annualReport = AnnualReport.annualReportCustomConfig(1983, testOutputPath)
        annualReport.writeFile(testOutputPath)
    }

    @Test
    @Throws(IOException::class)
    fun `can set a new entry in ledger`() {
        val monthlyReportSheet = annualReport.sheetAt(1)
        val monthlyReport = annualReport.monthlyReports().get(0)
        val ledgerEntry = LedgerEntry.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.now(), null)
        val updatedLedgerMonthlySheet = annualReport.setNewEntry(monthlyReportSheet, monthlyReport, ledgerEntry)
        val updatedMonthlyReport = AnnualReport.getMonthlyReportFrom(updatedLedgerMonthlySheet)

        assertThat(updatedMonthlyReport.entries[0].customerName, `is`(ledgerEntry.customerName))
        assertThat(updatedMonthlyReport.entries[0].invoiceNumber, `is`(ledgerEntry.invoiceNumber))
        assertThat(updatedMonthlyReport.entries[0].valueNett, `is`(ledgerEntry.valueNett))
        assertThat(updatedMonthlyReport.entries[0].date, `is`(LocalDate.now()))
    }

    @Test
    @Throws(IOException::class)
    fun `can set a new entry in ledger from ledger entry only`() {
        val ledgerEntry = LedgerEntry.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.of(1983, 11, 10), null)
        annualReport.setNewEntry(ledgerEntry)
        val novemberReport = annualReport.monthlyReports().get(10)

        assertThat(novemberReport.entries[0].customerName, `is`(ledgerEntry.customerName))
        assertThat(novemberReport.entries[0].invoiceNumber, `is`(ledgerEntry.invoiceNumber))
        assertThat(novemberReport.entries[0].valueNett, `is`(ledgerEntry.valueNett))
        assertThat(novemberReport.entries[0].date, `is`(LocalDate.of(1983, 11, 10)))
    }

    @Test(expected = RuntimeException::class)
    @Throws(IOException::class)
    fun `blows up if ledger entry added to workbook for different year`() {
        val ledgerEntry = LedgerEntry.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.of(1978, 11, 10), null)
        annualReport.setNewEntry(ledgerEntry)
    }

    @Test
    @Throws(IOException::class)
    fun `can set footer`() {
        val monthlyReportSheet = annualReport.sheetAt(1)
        val monthlyReport = annualReport.monthlyReports().get(0)
        val firstLedgerEntry = LedgerEntry.ledgerEntry("Carla Azar", "INV-808", 10.0, null, null, LocalDate.now(), null)
        val secondLedgerEntry = LedgerEntry.ledgerEntry("Chris Corsano", "INV-909", 11.0, null, null, LocalDate.now(), null)
        val thirdLedgerEntry = LedgerEntry.ledgerEntry("Drumbo Drums", "INV-101", 1.0, null, null, LocalDate.now(), null)

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
    fun `can read in an annual report file`() {
        annualReport = AnnualReport.annualReportCustomConfig(1984, Paths.get("src/test/resources"))
        val januaryReportSheet = annualReport.sheetAt(1)
        val monthlyReport = annualReport.monthlyReports().get(0)
        val newLedgerEntry = LedgerEntry.ledgerEntry("Buddy Rich", "INV-909", 3.0, null, null, LocalDate.now(), null)
        annualReport.setNewEntry(januaryReportSheet, monthlyReport, newLedgerEntry)
        annualReport.writeFile(testOutputPath)

        val annualReport = AnnualReport.readFile(Paths.get(testOutputPath.toString() + "/sales1984.xls"))
        val reportToTest = annualReport.monthlyReports().get(0)

        assertThat(reportToTest.month, `is`(JANUARY))
        assertThat(reportToTest.year, `is`(Year.of(1984)))
        assertThat<String>(reportToTest.entries[0].customerName, `is`("Buddy Rich"))
        assertThat<String>(reportToTest.entries[0].invoiceNumber, `is`("INV-909"))
    }
}