package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import uk.co.endofhome.javoice.Config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.capitalise;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.time.format.TextStyle.SHORT;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static uk.co.endofhome.javoice.CellStyler.*;
import static uk.co.endofhome.javoice.ledger.LedgerEntry.ledgerEntry;
import static uk.co.endofhome.javoice.ledger.MonthlyReport.LEDGER_ENTRIES_START_AT;
import static uk.co.endofhome.javoice.ledger.MonthlyReport.TOTAL_FOOTER_ROWS;

public class AnnualReport {
    public final HSSFWorkbook workbook;
    private Year year;
    private Sequence<MonthlyReport> monthlyReports;
    private Path fileOutputPath;

    private AnnualReport(HSSFWorkbook reportWorkbook, Year reportYear, Sequence<MonthlyReport> monthReports, Path outputPath) {
        workbook = reportWorkbook;
        year = reportYear;
        monthlyReports = monthReports;
        fileOutputPath = outputPath;
    }

    public static AnnualReport annualReportCustomConfig(int isoReportYear, Path outputPath) {
        AnnualReport annualReport = new AnnualReport(new HSSFWorkbook(), Year.of(isoReportYear), sequence(), outputPath);
        annualReport.createDefaultReports();
        annualReport.createDefaultSheets();
        annualReport.setMonthlyReportHeaders();
        annualReport.setMonthlyReportFooters();
        return annualReport;
    }

    public static AnnualReport annualReport(int isoReportYear) {
        AnnualReport annualReport = new AnnualReport(new HSSFWorkbook(), Year.of(isoReportYear), sequence(), Config.salesLedgerFileOutputPath());
        annualReport.createDefaultReports();
        annualReport.createDefaultSheets();
        annualReport.setMonthlyReportHeaders();
        annualReport.setMonthlyReportFooters();
        return annualReport;
    }

    public static AnnualReport readFile(String filePath) throws IOException {
        HSSFWorkbook workbook;
        try {
            workbook = workbookFromPath(filePath);
        } catch (IOException e) {
            throw new IOException(String.format("Sorry, could not read file %s", filePath));
        }
        Year year = Year.of(isoYearFromFileName(filePath));
        Sequence<MonthlyReport> monthlyReports = sequence();
        for (int i = 1; i < workbook.getNumberOfSheets(); i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            monthlyReports = monthlyReports.append(getMonthlyReportFrom(sheet));
        }
        Path outputPath = Config.salesLedgerFileOutputPath();
        return new AnnualReport(workbook, year, monthlyReports, outputPath);
    }

    public static MonthlyReport getMonthlyReportFrom(HSSFSheet monthlyReportSheet) {
        MonthlyReport monthlyReport = new MonthlyReport(yearFrom(monthlyReportSheet), monthFrom(monthlyReportSheet));
        Sequence<LedgerEntry> entries = sequence();
        for (int i = LEDGER_ENTRIES_START_AT; i < monthlyReportSheet.getLastRowNum() - 1; i++) {
            HSSFRow rowToExtract = monthlyReportSheet.getRow(i);
            LedgerEntry ledgerEntry = ledgerEntry(
                    stringOptionCellValueFor(rowToExtract.getCell(0, CREATE_NULL_AS_BLANK)),
                    stringOptionCellValueFor(rowToExtract.getCell(1, CREATE_NULL_AS_BLANK)),
                    numericOptionCellValueFor(rowToExtract.getCell(2, CREATE_NULL_AS_BLANK)),
                    stringOptionCellValueFor(rowToExtract.getCell(5, CREATE_NULL_AS_BLANK)),
                    stringOptionCellValueFor(rowToExtract.getCell(6, CREATE_NULL_AS_BLANK)),
                    dateOptionCellValueFor(rowToExtract.getCell(7, CREATE_NULL_AS_BLANK)),
                    stringOptionCellValueFor(rowToExtract.getCell(8, CREATE_NULL_AS_BLANK))
            );
            entries = entries.append(ledgerEntry);
        }
        monthlyReport.entries = entries;
        return monthlyReport;
    }

    private static Year yearFrom(HSSFSheet monthlyReportSheet) {
        int year = (int) monthlyReportSheet.getRow(1).getCell(1).getNumericCellValue();
        return Year.of(year);
    }

    private static Month monthFrom(HSSFSheet monthlyReportSheet) {
        String monthString = monthlyReportSheet.getRow(1).getCell(0).getStringCellValue().toUpperCase();
        return Month.valueOf(monthString);
    }

    private static int isoYearFromFileName(String filePath) {
        int length = filePath.length();
        if (length > 8) {
            return parseInt(filePath.substring(length - 8, length - 4));
        } else {
            throw new IllegalArgumentException("Non-standard filename.");
        }
    }

    static Option<String> stringOptionCellValueFor(HSSFCell cell) {
        Option<String> optionString;
        if (cell != null) {
            optionString = option(cell.getStringCellValue());
        } else {
            optionString = none();
        }
        return optionString;
    }

    static Option<Double> numericOptionCellValueFor(HSSFCell cell) {
        Option<Double> optionDouble;
        if (cell != null) {
            optionDouble = option(cell.getNumericCellValue());
        } else {
            optionDouble = none();
        }
        return optionDouble;
    }

    static Option<LocalDate> dateOptionCellValueFor(HSSFCell cell) {
        Option<LocalDate> optionLocalDate;
        if (cell != null && cell.getCellType() == 0) {
            optionLocalDate = option(cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            optionLocalDate = none();
        }
        return optionLocalDate;
    }

    private static HSSFWorkbook workbookFromPath(String filePath) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        return new HSSFWorkbook(new POIFSFileSystem(inputStream));
    }

    public void writeFile(Path filePath) throws IOException {
        try {
            FileOutputStream fileOut = new FileOutputStream(format("%s/sales%s.xls", filePath, year.getValue()));
            resizeColumns();
            workbook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("There was a problem writing your file.");
        }
    }

    private void resizeColumns() {
        HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        for (Sheet sheet : workbook) {
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    public HSSFSheet setNewEntry(HSSFSheet monthlyReportSheet, MonthlyReport monthlyReport, LedgerEntry ledgerEntry) {
        HSSFSheet monthlyReportSheetNoFooter = removeFooterFrom(monthlyReportSheet);
        HSSFRow rowToSet = getNextRow(monthlyReportSheetNoFooter);
        rowToSet.createCell(0).setCellValue(ledgerEntry.customerName.getOrElse(""));
        rowToSet.createCell(1).setCellValue(ledgerEntry.invoiceNumber.getOrElse(""));
        rowToSet.createCell(2).setCellValue(ledgerEntry.valueNett.getOrElse(0.0));
        rowToSet.getCell(2).setCellStyle(excelSterlingCellStyleFor(workbook));
        rowToSet.createCell(3).setCellFormula(String.format("SUM(C%s*0.2)", rowToSet.getRowNum() + 1));
        rowToSet.getCell(3).setCellStyle(excelSterlingCellStyleFor(workbook));
        rowToSet.createCell(4).setCellFormula((String.format("SUM(C%1$s:D%1$s)", rowToSet.getRowNum() + 1)));
        rowToSet.getCell(4).setCellStyle(excelSterlingCellStyleFor(workbook));
        rowToSet.createCell(5).setCellValue(ledgerEntry.crReq.getOrElse(""));
        rowToSet.createCell(6).setCellValue(ledgerEntry.allocation.getOrElse(""));
        rowToSet.createCell(8).setCellValue(ledgerEntry.notes.getOrElse(""));
        HSSFCell dateCell = rowToSet.createCell(7);
        if (ledgerEntry.date.isDefined()) {
            dateCell.setCellValue(dateFrom(ledgerEntry.date.get()));
            dateCell.setCellStyle(excelDateCellStyleFor(workbook));
        } else {
            dateCell.setCellValue(CELL_TYPE_BLANK);
        }
        return setFooter(monthlyReportSheetNoFooter, monthlyReport.footer);
    }

    public void setNewEntry(LedgerEntry ledgerEntry) {
        LocalDate dateOfEntry = ledgerEntry.date.getOrThrow(new DateTimeException("Ledger entry has no date"));
        if (dateOfEntry.getYear() != year.getValue()) {
            throw new RuntimeException("Wrong ledger for entry.");
        }
        int entryMonthValue = dateOfEntry.getMonthValue();
        MonthlyReport monthlyReport = this.monthlyReports.get(entryMonthValue - 1);
        HSSFSheet reportSheet = workbook.getSheetAt(entryMonthValue);

        monthlyReport.entries = monthlyReport.entries.append(ledgerEntry);
        setNewEntry(reportSheet, monthlyReport, ledgerEntry);
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

    public Path fileOutputPath() {
        return fileOutputPath;
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
            createRowsForMonthlyReportHeaders(sheet);
            HSSFRow titleRow = sheet.getRow(1);
            titleRow.createCell(0).setCellValue(capitalise(monthlyReport.month.toString().toLowerCase()));
            titleRow.getCell(0).setCellStyle(excelBoldCellStyleFor(workbook));
            titleRow.createCell(1).setCellValue(year.getValue());
            HSSFRow tableHeadersRow = sheet.getRow(3);
            setTableHeaders(tableHeadersRow);
            setCellStyleForTableHeaders(tableHeadersRow);
        }
    }

    private void createRowsForMonthlyReportHeaders(HSSFSheet sheet) {
        sheet.createRow(0);
        sheet.createRow(1);
        sheet.createRow(2);
        sheet.createRow(3);
    }

    private void setCellStyleForTableHeaders(HSSFRow tableHeadersRow) {
        for(int j = 0; j < 9; j++) {
            tableHeadersRow.getCell(j).setCellStyle(excelBoldBorderBottomCellStyleFor(workbook));
        }
    }

    private void setTableHeaders(HSSFRow tableHeadersRow) {
        tableHeadersRow.createCell(0).setCellValue("Customer");
        tableHeadersRow.createCell(1).setCellValue("Invoice");
        tableHeadersRow.createCell(2).setCellValue("Value nett");
        tableHeadersRow.createCell(3).setCellValue("Vat");
        tableHeadersRow.createCell(4).setCellValue("Gross");
        tableHeadersRow.createCell(5).setCellValue("Cr req");
        tableHeadersRow.createCell(6).setCellValue("Allocation");
        tableHeadersRow.createCell(7).setCellValue("Date");
        tableHeadersRow.createCell(8).setCellValue("Notes");
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
        footerRowTwo.getCell(2).setCellStyle(excelSterlingCellStyleFor(workbook));
        footerRowTwo.getCell(3).setCellStyle(excelSterlingCellStyleFor(workbook));
        footerRowTwo.getCell(4).setCellStyle(excelSterlingCellStyleFor(workbook));
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
}
