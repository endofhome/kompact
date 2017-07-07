package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import uk.co.endofhome.javoice.CellStyler;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.capitalise;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.time.format.TextStyle.SHORT;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

public class AnnualReport {
    private static final int LEDGER_ENTRIES_START_AT = MonthlyReport.Companion.getLEDGER_ENTRIES_START_AT();
    private static final int TOTAL_FOOTER_ROWS = MonthlyReport.Companion.getTOTAL_FOOTER_ROWS();
    private final HSSFWorkbook workbook;
    public final Year year;
    private final Path fileOutputPath;
    private Sequence<MonthlyReport> monthlyReports;

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

    public static AnnualReport readFile(Path filePath) throws IOException {
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
        List<LedgerEntry> entries = new ArrayList<>();
        for (int i = LEDGER_ENTRIES_START_AT ; i < monthlyReportSheet.getLastRowNum() - 1; i++) {
            HSSFRow rowToExtract = monthlyReportSheet.getRow(i);
            LedgerEntry ledgerEntry = LedgerEntry.Companion.ledgerEntry(
                stringOrNullCellValueFor(rowToExtract.getCell(0, CREATE_NULL_AS_BLANK)),
                // TODO: below blows up if manually entered as it's not a string.
                // TODO: Enforcing string cell type in stringOptionCellValueFor, as a fix (may be temporary):
                stringOrNullCellValueFor(rowToExtract.getCell(1, CREATE_NULL_AS_BLANK)),
                rowToExtract.getCell(2, CREATE_NULL_AS_BLANK).getNumericCellValue(),
                stringOrNullCellValueFor(rowToExtract.getCell(5, CREATE_NULL_AS_BLANK)),
                stringOrNullCellValueFor(rowToExtract.getCell(6, CREATE_NULL_AS_BLANK)),
                dateOrNullCellValueFor(rowToExtract.getCell(7, CREATE_NULL_AS_BLANK)),
                stringOrNullCellValueFor(rowToExtract.getCell(8, CREATE_NULL_AS_BLANK))
            );
            entries.add(ledgerEntry);
        }
        monthlyReport.setEntries(entries);
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

    private static int isoYearFromFileName(Path filePath) {
        String pathString = filePath.toString();
        int length = pathString.length();
        if (length > 8) {
            return parseInt(pathString.substring(length - 8, length - 4));
        } else {
            throw new IllegalArgumentException("Non-standard filename.");
        }
    }

    private static String stringOrNullCellValueFor(HSSFCell cell) {
        if (cell != null) {
            // TODO: Write a test for enforcing string cell type:
            if (cell.getCellType() != CELL_TYPE_STRING) {
                cell.setCellType(CELL_TYPE_STRING);
            }
            return cell.getStringCellValue();
        } else {
            return null;
        }
    }

    private static LocalDate dateOrNullCellValueFor(HSSFCell cell) {
        if (cell != null && cell.getCellType() == 0) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    private static HSSFWorkbook workbookFromPath(Path filePath) throws IOException {
        InputStream inputStream = new FileInputStream(filePath.toString());
        return new HSSFWorkbook(new POIFSFileSystem(inputStream));
    }

    public void writeFile(Path filePath) throws IOException {
        try {
            FileOutputStream fileOut = new FileOutputStream(format("%s/sales%s.xls", filePath, year.getValue()));
            resizeColumns();
            workbook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(String.format("There was a problem writing your file: ", e));
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
        rowToSet.createCell(0).setCellValue(emptyStringIfNull(ledgerEntry.getCustomerName()));
        rowToSet.createCell(1).setCellValue(emptyStringIfNull(ledgerEntry.getInvoiceNumber()));
        rowToSet.createCell(2).setCellValue(zeroDoubleIfNull(ledgerEntry.getValueNett()));
        rowToSet.getCell(2).setCellStyle(CellStyler.Companion.excelSterlingCellStyleFor(workbook));
        rowToSet.createCell(3).setCellFormula(String.format("SUM(C%s*0.2)", rowToSet.getRowNum() + 1));
        rowToSet.getCell(3).setCellStyle(CellStyler.Companion.excelSterlingCellStyleFor(workbook));
        rowToSet.createCell(4).setCellFormula((String.format("SUM(C%1$s:D%1$s)", rowToSet.getRowNum() + 1)));
        rowToSet.getCell(4).setCellStyle(CellStyler.Companion.excelSterlingCellStyleFor(workbook));
        rowToSet.createCell(5).setCellValue(emptyStringIfNull(ledgerEntry.getCrReq()));
        rowToSet.createCell(6).setCellValue(emptyStringIfNull(ledgerEntry.getAllocation()));
        rowToSet.createCell(8).setCellValue(emptyStringIfNull(ledgerEntry.getNotes()));
        HSSFCell dateCell = rowToSet.createCell(7);
        if (ledgerEntry.getDate() != null) {
            dateCell.setCellValue(dateFrom(ledgerEntry.getDate()));
            dateCell.setCellStyle(CellStyler.Companion.excelDateCellStyleFor(workbook, dateCell));
        } else {
            dateCell.setCellValue(CELL_TYPE_BLANK);
        }
        return setFooter(monthlyReportSheetNoFooter, monthlyReport.getFooter());
    }

    public void setNewEntry(LedgerEntry ledgerEntry) {
        LocalDate dateOfEntry = ledgerEntry.getDate();
        if (dateOfEntry == null) {
            throw new DateTimeException("Ledger entry has no date");
        }
        if (dateOfEntry.getYear() != year.getValue()) {
            throw new RuntimeException("Wrong ledger for entry.");
        }
        int entryMonthValue = dateOfEntry.getMonthValue();
        MonthlyReport monthlyReport = monthlyReports.get(entryMonthValue - 1);
        HSSFSheet reportSheet = workbook.getSheetAt(entryMonthValue);

        List<LedgerEntry> entries = new ArrayList<>(monthlyReport.getEntries());
        entries.add(ledgerEntry);
        monthlyReport.setEntries(entries);
        setNewEntry(reportSheet, monthlyReport, ledgerEntry);
    }

    private HSSFSheet removeFooterFrom(HSSFSheet monthlyReportSheet) {
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
            workbook.createSheet(monthlyReport.getMonth().getDisplayName(SHORT, Locale.getDefault()));
        }
    }

    private void setMonthlyReportHeaders() {
        for (int i = 0; i < workbook.getNumberOfSheets() - 1; i++) {
            HSSFSheet sheet = workbook.getSheetAt(i + 1);
            MonthlyReport monthlyReport = monthlyReports.get(i);
            createRowsForMonthlyReportHeaders(sheet);
            HSSFRow titleRow = sheet.getRow(1);
            titleRow.createCell(0).setCellValue(capitalise(monthlyReport.getMonth().toString().toLowerCase()));
            titleRow.getCell(0).setCellStyle(CellStyler.Companion.excelBoldCellStyleFor(workbook));
            HSSFCell dateCell = titleRow.createCell(1);
            dateCell.setCellValue(year.getValue());
            dateCell.setCellStyle(CellStyler.Companion.excelGeneralCellStyleFor(workbook));
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
            tableHeadersRow.getCell(j).setCellStyle(CellStyler.Companion.excelBoldBorderBottomCellStyleFor(workbook));
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
            Map<String, List<String>> footerMap = monthlyReports.get(i).getFooter();

            setFooter(sheet, footerMap);
        }
    }

    private HSSFSheet setFooter(HSSFSheet sheet, Map<String, List<String>> footerMap) {
        HSSFRow footerRowOne = getNextRow(sheet);
        for (int j = 0; j < footerMap.get("rowOne").size(); j++) {
            footerRowOne.createCell(j).setCellValue(emptyStringIfNull(footerMap.get("rowOne").get(j)));
        }

        HSSFRow footerRowTwo = getNextRow(sheet);
        for (int k = 0; k < footerMap.get("rowTwo").size(); k++) {
            String cellContents = emptyStringIfNull(footerMap.get("rowTwo").get(k));
            if (k >= 2 && k <= 4) {
                cellContents = footerFormulas(sheet, cellContents);
                footerRowTwo.createCell(k).setCellFormula(cellContents);
            } else {
                footerRowTwo.createCell(k).setCellValue(emptyStringIfNull(footerMap.get("rowTwo").get(k)));
            }
        }
        footerRowTwo.getCell(2).setCellStyle(CellStyler.Companion.excelSterlingCellStyleFor(workbook));
        footerRowTwo.getCell(3).setCellStyle(CellStyler.Companion.excelSterlingCellStyleFor(workbook));
        footerRowTwo.getCell(4).setCellStyle(CellStyler.Companion.excelSterlingCellStyleFor(workbook));
        return sheet;
    }

    private String emptyStringIfNull(String string) {
        if (string == null) {
            return "";
        } else {
            return string;
        }
    }

    private Double zeroDoubleIfNull(Double number) {
        if (number == null) {
            return 0.0;
        } else {
            return number;
        }
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
