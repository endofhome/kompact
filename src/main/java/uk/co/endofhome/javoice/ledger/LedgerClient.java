package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;

import static com.googlecode.totallylazy.Sequences.sequence;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static uk.co.endofhome.javoice.ledger.MonthlyReport.LEDGER_ENTRIES_START_AT;
import static uk.co.endofhome.javoice.ledger.MonthlyReport.TOTAL_FOOTER_ROWS;

public class LedgerClient extends SpreadsheetReport {

    public LedgerClient() {
    }

    // MonthlyReport
    public HSSFSheet getSheetFromPath(String filePath, int sheetNumber) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        HSSFWorkbook readInWorkBook = new HSSFWorkbook(new POIFSFileSystem(inputStream));
        return readInWorkBook.getSheetAt(sheetNumber);
    }

    // MonthlyReport
    public MonthlyReport getLedgerMonthlyFrom(HSSFSheet ledgerMonthlySheet) {
        MonthlyReport monthlyReport = new MonthlyReport(getYearFrom(ledgerMonthlySheet), getMonthFrom(ledgerMonthlySheet));
        Sequence<LedgerEntry> entries = sequence();
        for (int i = LEDGER_ENTRIES_START_AT; i <= ledgerMonthlySheet.getLastRowNum(); i++) {
            HSSFRow rowToExtract = ledgerMonthlySheet.getRow(i);
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

    // MonthlyReport
    public HSSFSheet removeFooter(HSSFSheet ledgerMonthlySheet) {
        //TODO: Don't use MonthlyReport constants.

        for (int i = 0; i < TOTAL_FOOTER_ROWS; i++) {
            HSSFRow rowToRemove = ledgerMonthlySheet.getRow(ledgerMonthlySheet.getLastRowNum());
            ledgerMonthlySheet.removeRow(rowToRemove);
        }
        return ledgerMonthlySheet;
    }

    // MonthlyReport
    public HSSFSheet setNewEntry(HSSFSheet ledgerMonthlySheet, LedgerEntry ledgerEntry) {
        HSSFSheet ledgerMonthlySheetNoFooter = removeFooter(ledgerMonthlySheet);
        HSSFRow rowToSet = getNextRow(ledgerMonthlySheetNoFooter);
        rowToSet.createCell(0).setCellValue(ledgerEntry.customerName.getOrElse(""));
        rowToSet.createCell(1).setCellValue(ledgerEntry.invoiceNumber.getOrElse(""));
        rowToSet.createCell(2).setCellValue(ledgerEntry.valueNett.getOrElse(0.0));
        rowToSet.createCell(5).setCellValue(ledgerEntry.crReq.getOrElse(""));
        rowToSet.createCell(6).setCellValue(ledgerEntry.allocation.getOrElse(""));
        rowToSet.createCell(8).setCellValue(ledgerEntry.notes.getOrElse(""));
        HSSFCell dateCell = rowToSet.createCell(7);
        if (ledgerEntry.date.isDefined()) {
            dateCell.setCellValue(dateFromLocalDate(ledgerEntry.date.get()));
        } else {
            dateCell.setCellValue(CELL_TYPE_BLANK);
        }
        return setFooter(ledgerMonthlySheetNoFooter);
    }

    // MonthlyReport
    public HSSFSheet setFooter(HSSFSheet ledgerMonthlySheet) {
        MonthlyReport monthlyReport = getLedgerMonthlyFrom(ledgerMonthlySheet);

        HSSFRow footerRowOne = getNextRow(ledgerMonthlySheet);
        for (int i = 0; i < monthlyReport.footer.get("rowOne").size(); i++) {
            footerRowOne.createCell(i).setCellValue(monthlyReport.footer.get("rowOne").get(i).getOrElse(""));
        }

        HSSFRow footerRowTwo = getNextRow(ledgerMonthlySheet);
        for (int j = 0; j < monthlyReport.footer.get("rowTwo").size(); j++) {
            String cellContents = monthlyReport.footer.get("rowTwo").get(j).getOrElse("");
            if (j >= 2 && j <= 4) {
                cellContents = footerFormulas(ledgerMonthlySheet, cellContents);
                footerRowTwo.createCell(j).setCellFormula(cellContents);
            } else {
                footerRowTwo.createCell(j).setCellValue(monthlyReport.footer.get("rowTwo").get(j).getOrElse(""));
            }
        }
        return ledgerMonthlySheet;
    }

    // MonthlyReport
    private String footerFormulas(HSSFSheet ledgerMonthlySheet, String cellContents) {
        String sumStringBeginning = cellContents.substring(0, 8);
        Integer lastRowToSum = ledgerMonthlySheet.getLastRowNum() - 1;
        String sumStringEnding = cellContents.substring(8);
        cellContents = sumStringBeginning + lastRowToSum.toString() + sumStringEnding;
        return cellContents;
    }

    // MonthlyReport
    private HSSFRow getNextRow(HSSFSheet ledgerMonthlySheet) {
        return ledgerMonthlySheet.createRow(ledgerMonthlySheet.getLastRowNum() + 1);
    }

    // MonthlyReport
    private Date dateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // MonthlyReport
    private Year getYearFrom(HSSFSheet ledgerMonthlySheet) {
        int year = (int) ledgerMonthlySheet.getRow(1).getCell(1).getNumericCellValue();
        return Year.of(year);
    }

    // MonthlyReport
    private Month getMonthFrom(HSSFSheet ledgerMonthlySheet) {
        String monthString = ledgerMonthlySheet.getRow(1).getCell(0).getStringCellValue().toUpperCase();
        return Month.valueOf(monthString);
    }
}