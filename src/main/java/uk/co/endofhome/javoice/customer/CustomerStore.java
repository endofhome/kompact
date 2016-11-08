package uk.co.endofhome.javoice.customer;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;
import static uk.co.endofhome.javoice.CellStyler.excelBoldBorderBottomCellStyleFor;
import static uk.co.endofhome.javoice.CellStyler.excelBoldCellStyleFor;

public class CustomerStore {
    private Sequence<Customer> customers;

    public CustomerStore() {
        this.customers = sequence();
    }

    public Sequence<Customer> customers() {
        return customers;
    }

    public static CustomerStore readFile(String filePath, int... sheetsToGet) throws IOException {
        if (sheetsToGet.length == 1) {
            HSSFWorkbook customerStoreWorkbook = getWorkbookFromPath(filePath);
            HSSFSheet customerStoreSheet = customerStoreWorkbook.getSheetAt(0);
            Sequence<Customer> customers = getCustomersFrom(customerStoreSheet);
            CustomerStore customerStore = new CustomerStore();
            customerStore.addCustomers(customers);
            return customerStore;
        }
        throw new RuntimeException("Sorry, simultaneously reading in multiple sheets from one file is not yet supported.");
    }

    public void writeFile(Path filePath) throws IOException {
        try {
            FileOutputStream fileOut = new FileOutputStream(format("%s/Customers.xls", filePath));
            HSSFWorkbook workbook = createWorkbookWithOneSheet();
            resizeColumns(workbook);
            workbook.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("There was a problem writing your file.");
        }
    }

    private HSSFWorkbook createWorkbookWithOneSheet() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet();
        setHeaders(workbook);
        setCustomerLines(workbook);
        return workbook;
    }

    private void setHeaders(HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.getSheetAt(0);

        HSSFCell topHeaderCell = sheet.createRow(1).createCell(0);
        topHeaderCell.setCellValue("Customers");
        topHeaderCell.setCellStyle(excelBoldCellStyleFor(workbook));

        HSSFRow tableHeaders = sheet.createRow(3);
        tableHeaders.createCell(0).setCellValue("Account code");
        tableHeaders.createCell(1).setCellValue("Name");
        tableHeaders.createCell(2).setCellValue("Address (1)");
        tableHeaders.createCell(3).setCellValue("Address (2)");
        tableHeaders.createCell(4).setCellValue("Postcode");
        tableHeaders.createCell(5).setCellValue("Phone number");

        for (Cell cell : tableHeaders) {
            cell.setCellStyle(excelBoldBorderBottomCellStyleFor(workbook));
        }
    }

    private void setCustomerLines(HSSFWorkbook workbook) {
        HSSFSheet sheet = workbook.getSheetAt(0);
        for (Customer customer : customers) {
            int nextRow = sheet.getLastRowNum() + 1;
            HSSFRow row = sheet.createRow(nextRow);
            row.createCell(0).setCellValue(customer.accountCode);
            row.createCell(1).setCellValue(customer.name);
            row.createCell(2).setCellValue(customer.addressOne);
            row.createCell(3).setCellValue(customer.addressTwo);
            row.createCell(4).setCellValue(customer.postcode);
            row.createCell(5).setCellValue(customer.phoneNumber);
        }
    }

    private static Sequence<Customer> getCustomersFrom(HSSFSheet customerStoreSheet) {
        Sequence<Customer> customers = sequence();
        int firstRowNum = 4;
        for (int i = firstRowNum; i <= customerStoreSheet.getLastRowNum(); i++) {
            HSSFRow row = customerStoreSheet.getRow(i);
            Customer customer = getSingleCustomerFrom(row);
            customers = customers.append(customer);
        }
        return customers;
    }

    private static Customer getSingleCustomerFrom(Row row) {
        String accountCode = row.getCell(0).getStringCellValue();
        String name = row.getCell(1).getStringCellValue();
        String addressOne = row.getCell(2).getStringCellValue();
        String addressTwo = row.getCell(3).getStringCellValue();
        String postcode = row.getCell(4).getStringCellValue();
        String phoneNumber = row.getCell(5).getStringCellValue();
        return new Customer(name, addressOne, addressTwo, postcode, phoneNumber, accountCode);
    }

    public static HSSFWorkbook getWorkbookFromPath(String filePath) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        return new HSSFWorkbook(new POIFSFileSystem(inputStream));
    }

    public void addCustomer(Customer customer) {
        customers = customers.append(customer);
    }

    public void addCustomers(Sequence<Customer> customerSequence) {
        for (Customer customer : customerSequence) {
            customers = customers.append(customer);
        }
    }

    private void resizeColumns(HSSFWorkbook workbook) {
        HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        for (Sheet sheet : workbook) {
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }
}
