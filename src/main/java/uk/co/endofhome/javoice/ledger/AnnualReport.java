package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.capitalise;
import static java.time.format.TextStyle.SHORT;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static uk.co.endofhome.javoice.ledger.MonthlyReport.LEDGER_ENTRIES_START_AT;
import static uk.co.endofhome.javoice.ledger.MonthlyReport.TOTAL_FOOTER_ROWS;

public class AnnualReport extends SpreadsheetReport {
    public final HSSFWorkbook workbook;
    private Year year;
    private Sequence<MonthlyReport> monthlyReports;

    public AnnualReport(int isoReportYear) {
        workbook = new HSSFWorkbook();
        year = Year.of(isoReportYear);
        monthlyReports = sequence();
        createDefaultReports();
        createDefaultSheets();
        setMonthlyReportHeaders();
        setMonthlyReportFooters();
    }

    public void write(FileOutputStream fileOut) throws IOException {
        try {
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new IOException("Sorry, there was a problem writing your spreadsheet.");
        }
    }

    public MonthlyReport getMonthlyReportFrom(HSSFSheet monthlyReportSheet) {
        MonthlyReport monthlyReport = new MonthlyReport(yearFrom(monthlyReportSheet), monthFrom(monthlyReportSheet));
        Sequence<LedgerEntry> entries = sequence();
        for (int i = LEDGER_ENTRIES_START_AT; i < monthlyReportSheet.getLastRowNum() - 1; i++) {
            HSSFRow rowToExtract = monthlyReportSheet.getRow(i);
            LedgerEntry ledgerEntry = new LedgerEntry(
                    getStringCellValueFor(rowToExtract.getCell(0, CREATE_NULL_AS_BLANK)),
                    getStringCellValueFor(rowToExtract.getCell(1, CREATE_NULL_AS_BLANK)),
                    getNumericCellValueFor(rowToExtract.getCell(2, CREATE_NULL_AS_BLANK)),
                    getStringCellValueFor(rowToExtract.getCell(5, CREATE_NULL_AS_BLANK)),
                    getStringCellValueFor(rowToExtract.getCell(6, CREATE_NULL_AS_BLANK)),
                    getDateCellValueFor(rowToExtract.getCell(7, CREATE_NULL_AS_BLANK)),
                    getStringCellValueFor(rowToExtract.getCell(8, CREATE_NULL_AS_BLANK))
            );
            entries = entries.append(ledgerEntry);
        }
        monthlyReport.entries = entries;
        return monthlyReport;
    }

    public HSSFSheet getSheetFromPath(String filePath, int sheetNumber) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        HSSFWorkbook readInWorkBook = new HSSFWorkbook(new POIFSFileSystem(inputStream));
        return readInWorkBook.getSheetAt(sheetNumber);
    }

    public HSSFSheet setNewEntry(HSSFSheet monthlyReportSheet, MonthlyReport monthlyReport, LedgerEntry ledgerEntry) {
        HSSFSheet monthlyReportSheetNoFooter = removeFooterFrom(monthlyReportSheet);
        HSSFRow rowToSet = getNextRow(monthlyReportSheetNoFooter);
        rowToSet.createCell(0).setCellValue(ledgerEntry.customerName.getOrElse(""));
        rowToSet.createCell(1).setCellValue(ledgerEntry.invoiceNumber.getOrElse(""));
        rowToSet.createCell(2).setCellValue(ledgerEntry.valueNett.getOrElse(0.0));
        rowToSet.createCell(5).setCellValue(ledgerEntry.crReq.getOrElse(""));
        rowToSet.createCell(6).setCellValue(ledgerEntry.allocation.getOrElse(""));
        rowToSet.createCell(8).setCellValue(ledgerEntry.notes.getOrElse(""));
        HSSFCell dateCell = rowToSet.createCell(7);
        if (ledgerEntry.date.isDefined()) {
            dateCell.setCellValue(dateFrom(ledgerEntry.date.get()));
        } else {
            dateCell.setCellValue(CELL_TYPE_BLANK);
        }
        return setFooter(monthlyReportSheetNoFooter, monthlyReport.footer);
    }

    public HSSFSheet removeFooterFrom(HSSFSheet monthlyReportSheet) {
        //TODO: Don't use MonthlyReport constants.

        for (int i = 0; i < TOTAL_FOOTER_ROWS; i++) {
            HSSFRow rowToRemove = monthlyReportSheet.getRow(monthlyReportSheet.getLastRowNum());
            monthlyReportSheet.removeRow(rowToRemove);
        }
        return monthlyReportSheet;
    }

    public HSSFSheet sheetAt(int i) {
        return workbook.getSheetAt(i);
    }

    public Sequence<MonthlyReport> monthlyReports() {
        return monthlyReports;
    }

    @SuppressWarnings("unchecked")
    private void createDefaultReports() {
        for (Month month : Month.values()) {
            monthlyReports = monthlyReports.append(new MonthlyReport(year, month));
        }
    }

    private void createDefaultSheets() {
        workbook.createSheet("Overview");
        for (MonthlyReport monthlyReport : monthlyReports) {
            workbook.createSheet(monthlyReport.month.getDisplayName(SHORT, Locale.getDefault()));
        }
    }

    private void setMonthlyReportHeaders() {
        for (int i = 0; i < workbook.getNumberOfSheets() - 1; i++) {
            HSSFSheet sheet = workbook.getSheetAt(i + 1);
            MonthlyReport monthlyReport = monthlyReports.get(i);
            sheet.createRow(0);
            sheet.createRow(1);
            sheet.getRow(1).createCell(0).setCellValue(capitalise(monthlyReport.month.toString().toLowerCase()));
            sheet.getRow(1).createCell(1).setCellValue(year.getValue());
            sheet.createRow(2);
            sheet.createRow(3);
        }
    }

    private void setMonthlyReportFooters() {
        for (int i = 0; i < workbook.getNumberOfSheets() - 1; i++) {
            HSSFSheet sheet = workbook.getSheetAt(i + 1);
            Map<String, Sequence<Option<String>>> footerMap = monthlyReports.get(i).footer;

            setFooter(sheet, footerMap);
        }
    }

    private HSSFSheet setFooter(HSSFSheet sheet, Map<String, Sequence<Option<String>>> footerMap) {
        HSSFRow footerRowOne = getNextRow(sheet);
        for (int j = 0; j < footerMap.get("rowOne").size(); j++) {
            footerRowOne.createCell(j).setCellValue(footerMap.get("rowOne").get(j).getOrElse(""));
        }

        HSSFRow footerRowTwo = getNextRow(sheet);
        for (int k = 0; k < footerMap.get("rowTwo").size(); k++) {
            String cellContents = footerMap.get("rowTwo").get(k).getOrElse("");
            if (k >= 2 && k <= 4) {
                cellContents = footerFormulas(sheet, cellContents);
                footerRowTwo.createCell(k).setCellFormula(cellContents);
            } else {
                footerRowTwo.createCell(k).setCellValue(footerMap.get("rowTwo").get(k).getOrElse(""));
            }
        }
        return sheet;
    }

    private HSSFRow getNextRow(HSSFSheet monthlyReportSheet) {
        return monthlyReportSheet.createRow(monthlyReportSheet.getLastRowNum() + 1);
    }

    private String footerFormulas(HSSFSheet monthlyReportSheet, String cellContents) {
        String sumStringBeginning = cellContents.substring(0, 8);
        Integer lastRowToSum = monthlyReportSheet.getLastRowNum() - 1;
        String sumStringEnding = cellContents.substring(8);
        cellContents = sumStringBeginning + lastRowToSum.toString() + sumStringEnding;
        return cellContents;
    }

    private Date dateFrom(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Year yearFrom(HSSFSheet monthlyReportSheet) {
        int year = (int) monthlyReportSheet.getRow(1).getCell(1).getNumericCellValue();
        return Year.of(year);
    }

    private Month monthFrom(HSSFSheet monthlyReportSheet) {
        String monthString = monthlyReportSheet.getRow(1).getCell(0).getStringCellValue().toUpperCase();
        return Month.valueOf(monthString);
    }
}
