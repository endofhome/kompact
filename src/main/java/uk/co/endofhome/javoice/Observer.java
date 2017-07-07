package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Option;
import uk.co.endofhome.javoice.customer.Customer;
import uk.co.endofhome.javoice.invoice.ItemLine;

import java.io.IOException;
import java.util.List;

public interface Observer {
    void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) throws IOException;

    Option<Customer> findCustomer(String name);

    void setCurrentCustomer(Option<Customer> customer);

    void newInvoice(Customer customer, String orderNumber, List<ItemLine> itemLines) throws IOException;
}
