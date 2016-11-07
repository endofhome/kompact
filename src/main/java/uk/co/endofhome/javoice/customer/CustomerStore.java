package uk.co.endofhome.javoice.customer;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.googlecode.totallylazy.Sequences.sequence;

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
            HSSFSheet customerStoreSheet = getSingleSheetFromPath(filePath, sheetsToGet[0]);
            Sequence<Customer> customers = getCustomersFrom(customerStoreSheet);
            CustomerStore customerStore = new CustomerStore();
            customerStore.addCustomers(customers);
            return customerStore;
        }
        throw new RuntimeException("Sorry, simultaneously reading in multiple sheets from one file is not yet supported.");
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
        Double phoneNumDouble = row.getCell(5).getNumericCellValue();
        String phoneNumber = phoneNumDouble.toString();
        return new Customer(name, addressOne, addressTwo, postcode, phoneNumber, accountCode);
    }

    public static HSSFSheet getSingleSheetFromPath(String filePath, int sheetNum) throws IOException {
        InputStream inputStream = new FileInputStream(filePath);
        HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(inputStream));
        return workbook.getSheetAt(sheetNum);
    }

    public void addCustomer(Customer customer) {
        customers = customers.append(customer);
    }

    public void addCustomers(Sequence<Customer> customerSequence) {
        for (Customer customer : customerSequence) {
            customers = customers.append(customer);
        }
    }
}
