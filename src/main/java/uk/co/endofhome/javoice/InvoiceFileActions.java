package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFCell;
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

public class InvoiceFileActions {
    public final String rootPath;
    public final HSSFWorkbook workBook;
    private final HSSFSheet sheet;

    public InvoiceFileActions(HSSFWorkbook workBook) {
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
        ItemLine itemLineDetails = invoice.itemLines.get(0);
        HSSFCell quantityCell = invoiceSheet.getRow(itemLineNumber).getCell(3);
        HSSFCell descriptionCell = invoiceSheet.getRow(itemLineNumber).getCell(4);
        HSSFCell unitPriceCell = invoiceSheet.getRow(itemLineNumber).getCell(10);

        quantityCell.setCellValue(itemLineDetails.quantity);
        descriptionCell.setCellValue(itemLineDetails.description);
        unitPriceCell.setCellValue(itemLineDetails.unitPrice);
        return new ItemLine(itemLineDetails.quantity, itemLineDetails.description, itemLineDetails.unitPrice);
    }

    private Invoice buildInvoice(String invoiceNumber, Sequence<String> customerDetails, Sequence<String> orderRefs, Sequence<ItemLine> itemLines) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.getDefault());
        LocalDate invoiceDate = LocalDate.parse(orderRefs.get(0), formatter);
        Customer customer = new Customer(customerDetails.get(0), customerDetails.get(1), customerDetails.get(2), customerDetails.get(3), customerDetails.get(4), "");
        return new Invoice(invoiceNumber, invoiceDate, customer, orderRefs.get(1), itemLines);
    }
}