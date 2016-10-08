package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Option;
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

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Sequences.sequence;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static uk.co.endofhome.javoice.LedgerMonthly.*;

public class LedgerClient {
    private final String rootPath;
    private final HSSFWorkbook workBook;

    public LedgerClient(HSSFWorkbook workBook) {
        this.rootPath = "data/";
        this.workBook = workBook;
    }

    public HSSFSheet getSheetFromPath(String filePath, int sheetNumber) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        HSSFWorkbook readInWorkBook = new HSSFWorkbook(new POIFSFileSystem(inputStream));
        return readInWorkBook.getSheetAt(sheetNumber);
    }

    public HSSFSheet setNewEntry(HSSFSheet ledgerMonthlySheet, LedgerEntry ledgerEntry) {
        HSSFRow rowToSet = getNextRow(ledgerMonthlySheet);
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
        return ledgerMonthlySheet;
    }

    public LedgerMonthly getLedgerMonthlyFrom(HSSFSheet ledgerMonthlySheet) {
        LedgerMonthly ledgerMonthly = new LedgerMonthly(getYearFrom(ledgerMonthlySheet), getMonthFrom(ledgerMonthlySheet));
        Sequence<LedgerEntry> entries = sequence();
        int totalEntries = ledgerMonthlySheet.getLastRowNum() - TOTAL_WHITESPACE_ROWS - TOTAL_FOOTER_ROWS;
        int lastEntry = LEDGER_ENTRIES_START_AT + totalEntries;
        for (int i = LEDGER_ENTRIES_START_AT; i < lastEntry; i++) {
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
        ledgerMonthly.entries = entries;
        return ledgerMonthly;
    }

    private HSSFRow getNextRow(HSSFSheet ledgerMonthlySheet) {
        int nextRowNum = ledgerMonthlySheet.getLastRowNum() - TOTAL_FOOTER_ROWS + 1;
        return ledgerMonthlySheet.getRow(nextRowNum);
    }

    private Date dateFromLocalDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Year getYearFrom(HSSFSheet ledgerMonthlySheet) {
        int year = (int) ledgerMonthlySheet.getRow(1).getCell(1).getNumericCellValue();
        return Year.of(year);
    }

    private Month getMonthFrom(HSSFSheet ledgerMonthlySheet) {
        String monthString = ledgerMonthlySheet.getRow(1).getCell(0).getStringCellValue().toUpperCase();
        return Month.valueOf(monthString);
    }

    private Option<String> getStringCellValueFor(HSSFCell cell) {
        Option<String> optionString;
        if (cell != null) {
            optionString = option(cell.getStringCellValue());
        } else {
            optionString = none();
        }
        return optionString;
    }

    private Option<Double> getNumericCellValueFor(HSSFCell cell) {
        Option<Double> optionDouble;
        if (cell != null) {
            optionDouble = option(cell.getNumericCellValue());
        } else {
            optionDouble = none();
        }
        return optionDouble;
    }

    private Option<LocalDate> getDateCellValueFor(HSSFCell cell) {
        Option<LocalDate> optionLocalDate;
        if (cell != null && cell.getCellType() == 0) {
            optionLocalDate = option(cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            optionLocalDate = none();
        } return optionLocalDate;
    }
}