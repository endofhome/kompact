package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import static com.googlecode.totallylazy.Sequences.sequence;
import static uk.co.endofhome.javoice.Invoice.ITEM_LINES_START_AT;
import static uk.co.endofhome.javoice.Invoice.MAX_ITEM_LINES;

public class InvoiceClient {
    public final String rootPath;
    public final HSSFWorkbook workBook;
    private final HSSFSheet sheet;

    public InvoiceClient(HSSFWorkbook workBook) {
        this.rootPath = "data/";
        this.workBook = workBook;
        this.sheet = workBook.getSheetAt(2);
    }

    public Invoice writeFile(String filePath, Invoice invoice) throws IOException {
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath + invoice.number + ".xls");
            workBook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("There was a problem writing your file.");
        }
        return invoice;
    }

    public Invoice readFile(String filePath) throws IOException {
        HSSFSheet invoiceSheet = getSheetFromPath(filePath);
        Sequence<String> customerDetails = getCustomerSectionFrom(invoiceSheet);
        Sequence<String> orderRefs = getOrderRefsSectionFrom(invoiceSheet);
        String invoiceNumber = getInvoiceNumberFrom(invoiceSheet);
//        getItemLinesFrom(invoiceSheet);
        return buildInvoice(invoiceNumber, customerDetails, orderRefs, sequence());
    }

    public HSSFSheet getSheetFromPath(String filePath) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        HSSFWorkbook readInWorkBook = new HSSFWorkbook(new POIFSFileSystem(inputStream));
        return readInWorkBook.getSheetAt(2);
    }

    public HSSFSheet setCustomerSection(HSSFSheet invoiceSheet, Invoice invoice) {
        Customer customer = invoice.customer;
        invoiceSheet.getRow(11).getCell(4).setCellValue(customer.name);
        invoiceSheet.getRow(12).getCell(4).setCellValue(customer.addressOne);
        invoiceSheet.getRow(13).getCell(4).setCellValue(customer.addressTwo);
        invoiceSheet.getRow(13).getCell(8).setCellValue(customer.postcode);
        invoiceSheet.getRow(14).getCell(4).setCellValue(customer.phoneNumber);
        return invoiceSheet;
    }

    public HSSFSheet setOrderRefsSection(HSSFSheet invoiceSheet, Invoice invoice) {
        LocalDate invoiceDate = invoice.date;
        Date date = Date.from(invoiceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Customer customer = invoice.customer;
        invoiceSheet.getRow(11).getCell(11).setCellValue(date);
        invoiceSheet.getRow(12).getCell(11).setCellValue(invoice.orderNumber);
        invoiceSheet.getRow(13).getCell(11).setCellValue(customer.accountCode);
        return invoiceSheet;
    }

    public HSSFSheet setInvoiceNumber(HSSFSheet invoiceSheet, Invoice invoice) {
        invoiceSheet.getRow(3).getCell(11).setCellValue(invoice.number);
        return invoiceSheet;
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

    public ItemLine setItemLine(HSSFSheet invoiceSheet, Invoice invoice, int itemLineNumber) {
        ItemLine itemLineDetails = invoice.itemLines.get(itemLineNumber);
        HSSFRow row = invoiceSheet.getRow(itemLineNumber + ITEM_LINES_START_AT);
        row.getCell(3).setCellValue(itemLineDetails.quantity);
        row.getCell(4).setCellValue(itemLineDetails.description);
        row.getCell(10).setCellValue(itemLineDetails.unitPrice);
        return itemLineDetails;
    }

    public Sequence<ItemLine> setItemLines(HSSFSheet invoiceSheet, Invoice invoice) {
        Sequence<ItemLine> itemLines = sequence();
        int lastItemLine = ITEM_LINES_START_AT + invoice.itemLines.size() -1;
        if (invoice.itemLines.size() <= MAX_ITEM_LINES) {
            for (int i = ITEM_LINES_START_AT; i <= lastItemLine; i++) {
                int itemLineNumber = i - ITEM_LINES_START_AT;
                setItemLine(invoiceSheet, invoice, itemLineNumber);
                itemLines = itemLines.append(invoice.itemLines.get(itemLineNumber));
            }
        } else {
            throw new RuntimeException(String.format("Too many lines for invoice number %s", invoice.number));
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