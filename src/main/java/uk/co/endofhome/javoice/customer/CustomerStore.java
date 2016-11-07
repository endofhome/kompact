package uk.co.endofhome.javoice.customer;

import com.googlecode.totallylazy.Sequence;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import static com.googlecode.totallylazy.Sequences.sequence;

public class CustomerStore {
    private HSSFWorkbook workbook;
    private Sequence<Customer> customers;

    public CustomerStore() {
        this.customers = sequence();
    }

    public Sequence<Customer> customers() {
        return customers;
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
