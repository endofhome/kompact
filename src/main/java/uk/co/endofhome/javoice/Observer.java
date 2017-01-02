package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import uk.co.endofhome.javoice.customer.Customer;
import uk.co.endofhome.javoice.invoice.ItemLine;

import java.io.IOException;

public interface Observer {
    void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) throws IOException;

    Option<Customer> findCustomer(String name);

    void setCurrentCustomer(Option<Customer> customer);

    void newInvoice(Customer customer, String orderNumber, Sequence<ItemLine> itemLines) throws IOException;
}
