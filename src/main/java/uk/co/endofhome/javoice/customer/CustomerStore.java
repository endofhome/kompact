package uk.co.endofhome.javoice.customer;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;

public class CustomerStore {
    private Sequence<Customer> customers;

    public CustomerStore() {
        this.customers = sequence();
    }

    public Sequence<Customer> customers() {
        return customers;
    }

    public void setNewEntry(Customer customer) {
        customers = customers.append(customer);
    }
}
