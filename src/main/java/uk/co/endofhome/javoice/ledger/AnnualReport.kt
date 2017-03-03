package uk.co.endofhome.javoice.ledger

import com.googlecode.totallylazy.Option
import com.googlecode.totallylazy.Sequence
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.Sheet
import uk.co.endofhome.javoice.Config

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.ZoneId
import java.util.Date
import java.util.Locale

import com.googlecode.totallylazy.Option.none
import com.googlecode.totallylazy.Option.option
import com.googlecode.totallylazy.Sequences.sequence
import com.googlecode.totallylazy.Strings.capitalise
import java.lang.Integer.parseInt
import java.lang.String.format
import java.time.format.TextStyle.SHORT
import org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK
import org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK
import uk.co.endofhome.javoice.CellStyler.excelBoldBorderBottomCellStyleFor
import uk.co.endofhome.javoice.CellStyler.excelBoldCellStyleFor
import uk.co.endofhome.javoice.CellStyler.excelDateCellStyleFor
import uk.co.endofhome.javoice.CellStyler.excelGeneralCellStyleFor
import uk.co.endofhome.javoice.CellStyler.excelSterlingCellStyleFor
import uk.co.endofhome.javoice.ledger.LedgerEntry.Companion.ledgerEntry
import uk.co.endofhome.javoice.ledger.MonthlyReport.Companion.LEDGER_ENTRIES_START_AT
import uk.co.endofhome.javoice.ledger.MonthlyReport.Companion.TOTAL_FOOTER_ROWS

class AnnualReport private constructor(val workbook: HSSFWorkbook, val year: Year, private var monthlyReports: Sequence<MonthlyReport>, private val fileOutputPath: Path) {

    @Throws(IOException::class)
    fun writeFile(filePath: Path) {
        try {
            val fileOut = FileOutputStream(format("%s/sales%s.xls", filePath, year.value))
            resizeColumns()
            workbook.write(fileOut)
            fileOut.close()
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException(String.format("There was a problem writing your file: ", e))
        }

    }

    private fun resizeColumns() {
        HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook)
        for (sheet in workbook) {
            for (i in 0..7) {
                sheet.autoSizeColumn(i)
            }
        }
    }

    fun setNewEntry(monthlyReportSheet: HSSFSheet, monthlyReport: MonthlyReport, ledgerEntry: LedgerEntry): HSSFSheet {
        val monthlyReportSheetNoFooter = removeFooterFrom(monthlyReportSheet)
        val rowToSet = getNextRow(monthlyReportSheetNoFooter)
        rowToSet.createCell(0).setCellValue(ledgerEntry.customerName.getOrElse(""))
        rowToSet.createCell(1).setCellValue(ledgerEntry.invoiceNumber.getOrElse(""))
        rowToSet.createCell(2).setCellValue(ledgerEntry.valueNett.getOrElse(0.0))
        rowToSet.getCell(2).setCellStyle(excelSterlingCellStyleFor(workbook))
        rowToSet.createCell(3).cellFormula = String.format("SUM(C%s*0.2)", rowToSet.rowNum + 1)
        rowToSet.getCell(3).setCellStyle(excelSterlingCellStyleFor(workbook))
        rowToSet.createCell(4).cellFormula = String.format("SUM(C%1\$s:D%1\$s)", rowToSet.rowNum + 1)
        rowToSet.getCell(4).setCellStyle(excelSterlingCellStyleFor(workbook))
        rowToSet.createCell(5).setCellValue(ledgerEntry.crReq.getOrElse(""))
        rowToSet.createCell(6).setCellValue(ledgerEntry.allocation.getOrElse(""))
        rowToSet.createCell(8).setCellValue(ledgerEntry.notes.getOrElse(""))
        val dateCell = rowToSet.createCell(7)
        if (ledgerEntry.date.isDefined) {
            dateCell.setCellValue(dateFrom(ledgerEntry.date.get()))
            dateCell.setCellStyle(excelDateCellStyleFor(workbook, dateCell))
        } else {
            dateCell.setCellValue(CELL_TYPE_BLANK.toDouble())
        }
        return setFooter(monthlyReportSheetNoFooter, monthlyReport.footer)
    }

    fun setNewEntry(ledgerEntry: LedgerEntry) {
        val dateOfEntry = ledgerEntry.date.getOrThrow(DateTimeException("Ledger entry has no date"))
        if (dateOfEntry.year != year.value) {
            throw RuntimeException("Wrong ledger for entry.")
        }
        val entryMonthValue = dateOfEntry.monthValue
        val monthlyReport = this.monthlyReports!!.get(entryMonthValue - 1)
        val reportSheet = workbook.getSheetAt(entryMonthValue)

        monthlyReport.entries = monthlyReport.entries.append(ledgerEntry)
        setNewEntry(reportSheet, monthlyReport, ledgerEntry)
    }

    fun removeFooterFrom(monthlyReportSheet: HSSFSheet): HSSFSheet {
        //TODO: Don't use MonthlyReport constants.

        for (i in 0..TOTAL_FOOTER_ROWS - 1) {
            val rowToRemove = monthlyReportSheet.getRow(monthlyReportSheet.lastRowNum)
            monthlyReportSheet.removeRow(rowToRemove)
        }
        return monthlyReportSheet
    }

    fun sheetAt(i: Int): HSSFSheet {
        return workbook.getSheetAt(i)
    }

    fun monthlyReports(): Sequence<MonthlyReport> {
        return monthlyReports
    }

    fun fileOutputPath(): Path {
        return fileOutputPath
    }

    private fun createDefaultReports() {
        for (month in Month.values()) {
            monthlyReports = monthlyReports!!.append(MonthlyReport(year, month))
        }
    }

    private fun createDefaultSheets() {
        workbook.createSheet("Overview")
        for (monthlyReport in monthlyReports!!) {
            workbook.createSheet(monthlyReport.month.getDisplayName(SHORT, Locale.getDefault()))
        }
    }

    private fun setMonthlyReportHeaders() {
        for (i in 0..workbook.numberOfSheets - 1 - 1) {
            val sheet = workbook.getSheetAt(i + 1)
            val monthlyReport = monthlyReports!!.get(i)
            createRowsForMonthlyReportHeaders(sheet)
            val titleRow = sheet.getRow(1)
            titleRow.createCell(0).setCellValue(capitalise(monthlyReport.month.toString().toLowerCase()))
            titleRow.getCell(0).setCellStyle(excelBoldCellStyleFor(workbook))
            val dateCell = titleRow.createCell(1)
            dateCell.setCellValue(year.value.toDouble())
            dateCell.setCellStyle(excelGeneralCellStyleFor(workbook))
            val tableHeadersRow = sheet.getRow(3)
            setTableHeaders(tableHeadersRow)
            setCellStyleForTableHeaders(tableHeadersRow)
        }
    }

    private fun createRowsForMonthlyReportHeaders(sheet: HSSFSheet) {
        sheet.createRow(0)
        sheet.createRow(1)
        sheet.createRow(2)
        sheet.createRow(3)
    }

    private fun setCellStyleForTableHeaders(tableHeadersRow: HSSFRow) {
        for (j in 0..8) {
            tableHeadersRow.getCell(j).setCellStyle(excelBoldBorderBottomCellStyleFor(workbook))
        }
    }

    private fun setTableHeaders(tableHeadersRow: HSSFRow) {
        tableHeadersRow.createCell(0).setCellValue("Customer")
        tableHeadersRow.createCell(1).setCellValue("Invoice")
        tableHeadersRow.createCell(2).setCellValue("Value nett")
        tableHeadersRow.createCell(3).setCellValue("Vat")
        tableHeadersRow.createCell(4).setCellValue("Gross")
        tableHeadersRow.createCell(5).setCellValue("Cr req")
        tableHeadersRow.createCell(6).setCellValue("Allocation")
        tableHeadersRow.createCell(7).setCellValue("Date")
        tableHeadersRow.createCell(8).setCellValue("Notes")
    }

    private fun setMonthlyReportFooters() {
        for (i in 0..workbook.numberOfSheets - 1 - 1) {
            val sheet = workbook.getSheetAt(i + 1)
            val footerMap = monthlyReports!!.get(i).footer

            setFooter(sheet, footerMap)
        }
    }

    private fun setFooter(sheet: HSSFSheet, footerMap: Map<String, Sequence<Option<String>>>): HSSFSheet {
        val footerRowOne = getNextRow(sheet)
        for (j in 0..footerMap["rowOne"]!!.size - 1) {
            footerRowOne.createCell(j).setCellValue(footerMap["rowOne"]!!.get(j).getOrElse(""))
        }

        val footerRowTwo = getNextRow(sheet)
        for (k in 0..footerMap["rowTwo"]!!.size - 1) {
            var cellContents = footerMap["rowTwo"]!!.get(k).getOrElse("")
            if (k >= 2 && k <= 4) {
                cellContents = footerFormulas(sheet, cellContents)
                footerRowTwo.createCell(k).cellFormula = cellContents
            } else {
                footerRowTwo.createCell(k).setCellValue(footerMap["rowTwo"]!!.get(k).getOrElse(""))
            }
        }
        footerRowTwo.getCell(2).setCellStyle(excelSterlingCellStyleFor(workbook))
        footerRowTwo.getCell(3).setCellStyle(excelSterlingCellStyleFor(workbook))
        footerRowTwo.getCell(4).setCellStyle(excelSterlingCellStyleFor(workbook))
        return sheet
    }

    private fun getNextRow(monthlyReportSheet: HSSFSheet): HSSFRow {
        return monthlyReportSheet.createRow(monthlyReportSheet.lastRowNum + 1)
    }

    private fun footerFormulas(monthlyReportSheet: HSSFSheet, cellContents: String): String {
        var cellContents = cellContents
        val sumStringBeginning = cellContents.substring(0, 8)
        val lastRowToSum = monthlyReportSheet.lastRowNum - 1
        val sumStringEnding = cellContents.substring(8)
        cellContents = sumStringBeginning + lastRowToSum.toString() + sumStringEnding
        return cellContents
    }

    private fun dateFrom(localDate: LocalDate): Date {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    companion object {

        fun annualReportCustomConfig(isoReportYear: Int, outputPath: Path): AnnualReport {
            val annualReport = AnnualReport(HSSFWorkbook(), Year.of(isoReportYear), sequence<MonthlyReport>(), outputPath)
            annualReport.createDefaultReports()
            annualReport.createDefaultSheets()
            annualReport.setMonthlyReportHeaders()
            annualReport.setMonthlyReportFooters()
            return annualReport
        }

        fun annualReport(isoReportYear: Int): AnnualReport {
            val annualReport = AnnualReport(HSSFWorkbook(), Year.of(isoReportYear), sequence<MonthlyReport>(), Config.salesLedgerFileOutputPath())
            annualReport.createDefaultReports()
            annualReport.createDefaultSheets()
            annualReport.setMonthlyReportHeaders()
            annualReport.setMonthlyReportFooters()
            return annualReport
        }

        @Throws(IOException::class)
        fun readFile(filePath: Path): AnnualReport {
            val workbook: HSSFWorkbook
            try {
                workbook = workbookFromPath(filePath)
            } catch (e: IOException) {
                throw IOException(String.format("Sorry, could not read file %s", filePath))
            }

            val year = Year.of(isoYearFromFileName(filePath))
            var monthlyReports = sequence<MonthlyReport>()
            for (i in 1..workbook.numberOfSheets - 1) {
                val sheet = workbook.getSheetAt(i)
                monthlyReports = monthlyReports.append(getMonthlyReportFrom(sheet))
            }
            val outputPath = Config.salesLedgerFileOutputPath()
            return AnnualReport(workbook, year, monthlyReports, outputPath)
        }

        fun getMonthlyReportFrom(monthlyReportSheet: HSSFSheet): MonthlyReport {
            val monthlyReport = MonthlyReport(yearFrom(monthlyReportSheet), monthFrom(monthlyReportSheet))
            var entries = sequence<LedgerEntry>()
            for (i in LEDGER_ENTRIES_START_AT..monthlyReportSheet.lastRowNum - 1 - 1) {
                val rowToExtract = monthlyReportSheet.getRow(i)
                val ledgerEntry = ledgerEntry(
                    stringOptionCellValueFor(rowToExtract.getCell(0, CREATE_NULL_AS_BLANK)),
                    // TODO: below blows up if manually entered as it's not a string.
                    // TODO: Enforcing string cell type in stringOptionCellValueFor, as a fix (may be temporary):
                    stringOptionCellValueFor(rowToExtract.getCell(1, CREATE_NULL_AS_BLANK)),
                    numericOptionCellValueFor(rowToExtract.getCell(2, CREATE_NULL_AS_BLANK)),
                    stringOptionCellValueFor(rowToExtract.getCell(5, CREATE_NULL_AS_BLANK)),
                    stringOptionCellValueFor(rowToExtract.getCell(6, CREATE_NULL_AS_BLANK)),
                    dateOptionCellValueFor(rowToExtract.getCell(7, CREATE_NULL_AS_BLANK)),
                    stringOptionCellValueFor(rowToExtract.getCell(8, CREATE_NULL_AS_BLANK))
                )
                entries = entries.append(ledgerEntry)
            }
            monthlyReport.entries = entries
            return monthlyReport
        }

        private fun yearFrom(monthlyReportSheet: HSSFSheet): Year {
            val year = monthlyReportSheet.getRow(1).getCell(1).numericCellValue.toInt()
            return Year.of(year)
        }

        private fun monthFrom(monthlyReportSheet: HSSFSheet): Month {
            val monthString = monthlyReportSheet.getRow(1).getCell(0).stringCellValue.toUpperCase()
            return Month.valueOf(monthString)
        }

        private fun isoYearFromFileName(filePath: Path): Int {
            val pathString = filePath.toString()
            val length = pathString.length
            if (length > 8) {
                return parseInt(pathString.substring(length - 8, length - 4))
            } else {
                throw IllegalArgumentException("Non-standard filename.")
            }
        }

        internal fun stringOptionCellValueFor(cell: HSSFCell?): Option<String> {
            val optionString: Option<String>
            if (cell != null) {
                // TODO: Write a test for enforcing string cell type:
                if (cell.cellType != CELL_TYPE_STRING) {
                    cell.cellType = CELL_TYPE_STRING
                }
                optionString = option(cell.stringCellValue)
            } else {
                optionString = none<String>()
            }
            return optionString
        }

        internal fun numericOptionCellValueFor(cell: HSSFCell?): Option<Double> {
            val optionDouble: Option<Double>
            if (cell != null) {
                optionDouble = option(cell.numericCellValue)
            } else {
                optionDouble = none<Double>()
            }
            return optionDouble
        }

        internal fun dateOptionCellValueFor(cell: HSSFCell?): Option<LocalDate> {
            val optionLocalDate: Option<LocalDate>
            if (cell != null && cell.cellType == 0) {
                optionLocalDate = option(cell.dateCellValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
            } else {
                optionLocalDate = none<LocalDate>()
            }
            return optionLocalDate
        }

        @Throws(IOException::class)
        private fun workbookFromPath(filePath: Path): HSSFWorkbook {
            val inputStream = FileInputStream(filePath.toString())
            return HSSFWorkbook(POIFSFileSystem(inputStream))
        }
    }
}
