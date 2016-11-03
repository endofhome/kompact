package uk.co.endofhome.javoice.invoice;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import uk.co.endofhome.javoice.Customer;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class Invoice {
    public final String number;
    public final LocalDate date;
    public final Customer customer;
    public final String orderNumber;
    public final Sequence<ItemLine> itemLines;

    public static final int ITEM_LINES_START_AT = 17;
    public static final int MAX_ITEM_LINES = 17;

    public Invoice(String invoiceNumber, LocalDate date, Customer customer, String orderNumber, Sequence<ItemLine> itemLines) {
        this.number = invoiceNumber;
        this.date = date;
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.itemLines = itemLines;
    }

    public double nettValue() {
        return itemLines.map((item -> (item.quantity * item.unitPrice)))
                .reduce((x, y) -> ((double) x) + y);
    }

    public static class InvoiceClient {
        public final HSSFWorkbook workBook;
        public final Path fileTemplatePath;
        public final Path fileOutputPath;

        public InvoiceClient(HSSFWorkbook workBook, Path templatePath, Path outputPath) {
            this.fileTemplatePath = templatePath;
            this.fileOutputPath = outputPath;
            this.workBook = workBook;
        }

        public Invoice writeFile(Path filePath, Invoice invoice) throws IOException {
            try {
                FileOutputStream fileOut = new FileOutputStream(format("%s/%s.xls", filePath, invoice.number));
                workBook.write(fileOut);
                fileOut.close();
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("There was a problem writing your file.");
            }
            return invoice;
        }

        public Invoice readFile(String filePath, int... sheetsToGet) throws IOException {
            if (sheetsToGet.length == 1) {
                HSSFSheet invoiceSheet = getSingleSheetFromPath(filePath, sheetsToGet[0]);
                Sequence<String> customerDetails = getCustomerSectionFrom(invoiceSheet);
                Sequence<String> orderRefs = getOrderRefsSectionFrom(invoiceSheet);
                String invoiceNumber = getInvoiceNumberFrom(invoiceSheet);
                Sequence<ItemLine> itemLines = getItemLinesFrom(invoiceSheet);
                return buildInvoice(invoiceNumber, customerDetails, orderRefs, itemLines);
            }
            throw new RuntimeException("Sorry, simultaneously reading in multiple sheets from one file is not yet supported.");
        }

        public HSSFSheet getSingleSheetFromPath(String filePath, int sheetNum) throws IOException {
            InputStream inputStream = new FileInputStream(filePath);
            HSSFWorkbook readInWorkBook = new HSSFWorkbook(new POIFSFileSystem(inputStream));
             return readInWorkBook.getSheetAt(sheetNum);
        }

        public HSSFSheet setCustomerSection(HSSFSheet invoiceSheet, Invoice invoice) {
            Customer customer = invoice.customer;
            invoiceSheet.getRow(11).createCell(4).setCellValue(customer.name);
            invoiceSheet.getRow(12).createCell(4).setCellValue(customer.addressOne);
            invoiceSheet.getRow(13).createCell(4).setCellValue(customer.addressTwo);
            invoiceSheet.getRow(13).createCell(8).setCellValue(customer.postcode);
            invoiceSheet.getRow(14).createCell(4).setCellValue(customer.phoneNumber);
            return invoiceSheet;
        }

        public HSSFSheet setOrderRefsSection(HSSFSheet invoiceSheet, Invoice invoice) {
            LocalDate invoiceDate = invoice.date;
            Date date = Date.from(invoiceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Customer customer = invoice.customer;
            invoiceSheet.getRow(11).createCell(11).setCellValue(date);
            invoiceSheet.getRow(12).createCell(11).setCellValue(invoice.orderNumber);
            invoiceSheet.getRow(13).createCell(11).setCellValue(customer.accountCode);
            return invoiceSheet;
        }

        public HSSFSheet setInvoiceNumber(HSSFSheet invoiceSheet, Invoice invoice) {
            invoiceSheet.getRow(3).createCell(11).setCellValue(invoice.number);
            return invoiceSheet;
        }

        public HSSFSheet setItemLine(HSSFSheet invoiceSheet, Invoice invoice, int itemLineNumber) {
            ItemLine itemLineDetails = invoice.itemLines.get(itemLineNumber);
            HSSFRow row = invoiceSheet.getRow(itemLineNumber + ITEM_LINES_START_AT);
            row.createCell(3).setCellValue(itemLineDetails.quantity);
            row.createCell(4).setCellValue(itemLineDetails.description);
            row.createCell(10).setCellValue(itemLineDetails.unitPrice);
            return invoiceSheet;
        }

        public Sequence<ItemLine> setItemLines(HSSFSheet invoiceSheet, Invoice invoice) {
            Sequence<ItemLine> itemLines = sequence();
            int lastItemLine = ITEM_LINES_START_AT + invoice.itemLines.size() - 1;
            if (invoice.itemLines.size() <= MAX_ITEM_LINES) {
                for (int i = ITEM_LINES_START_AT; i <= lastItemLine; i++) {
                    int itemLineNumber = i - ITEM_LINES_START_AT;
                    setItemLine(invoiceSheet, invoice, itemLineNumber);
                    itemLines = itemLines.append(invoice.itemLines.get(itemLineNumber));
                }
            } else {
                throw new RuntimeException(format("Too many lines for invoice number %s", invoice.number));
            }
            return itemLines;
        }

        public Sequence<String> getCustomerSectionFrom(HSSFSheet invoiceSheet) {
            String customerName = invoiceSheet.getRow(11).getCell(4).getStringCellValue();
            String addressOne = invoiceSheet.getRow(12).getCell(4).getStringCellValue();
            String addressTwo = invoiceSheet.getRow(13).getCell(4).getStringCellValue();
            String postcode = invoiceSheet.getRow(13).getCell(8).getStringCellValue();
            String phoneNumber = invoiceSheet.getRow(14).getCell(4).getStringCellValue();
            return sequence(customerName, addressOne, addressTwo, postcode, phoneNumber);
        }

        public Sequence<String> getOrderRefsSectionFrom(HSSFSheet invoiceSheet) {
            Date date = invoiceSheet.getRow(11).getCell(11).getDateCellValue();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String invoiceDate = formatter.format(date);
            String orderNumber = invoiceSheet.getRow(12).getCell(11).getStringCellValue();
            String accountCode = invoiceSheet.getRow(13).getCell(11).getStringCellValue();
            return sequence(invoiceDate, orderNumber, accountCode);
        }

        public String getInvoiceNumberFrom(HSSFSheet invoiceSheet) {
            return invoiceSheet.getRow(3).getCell(11).getStringCellValue();
        }

        public Sequence<ItemLine> getItemLinesFrom(HSSFSheet invoiceSheet) {
            Sequence<ItemLine> itemLines = sequence();
            int lastPossibleItemLine = ITEM_LINES_START_AT + MAX_ITEM_LINES - 1;
            for (int i = ITEM_LINES_START_AT; i <= lastPossibleItemLine; i++) {
                double quantity = invoiceSheet.getRow(i).getCell(3).getNumericCellValue();
                String description = invoiceSheet.getRow(i).getCell(4).getStringCellValue();
                double unitPrice = invoiceSheet.getRow(i).getCell(10).getNumericCellValue();
                ItemLine itemLine = new ItemLine(quantity, description, unitPrice);
                itemLines = itemLines.append(itemLine);
            }
            return itemLines;
        }

        private Invoice buildInvoice(String invoiceNumber, Sequence<String> customerDetails, Sequence<String> orderRefs, Sequence<ItemLine> itemLines) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.getDefault());
            LocalDate invoiceDate = LocalDate.parse(orderRefs.get(0), formatter);
            Customer customer = new Customer(customerDetails.get(0), customerDetails.get(1), customerDetails.get(2), customerDetails.get(3), customerDetails.get(4), "");
            return new Invoice(invoiceNumber, invoiceDate, customer, orderRefs.get(1), itemLines);
        }
    }
}
