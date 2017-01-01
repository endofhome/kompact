package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Option;
import uk.co.endofhome.javoice.customer.Customer;

import java.io.IOException;

public interface Observer {
    void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) throws IOException;
    Option<Customer> findCustomer(String name);
    void currentCustomerIs(Customer customer);
}
