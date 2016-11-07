package uk.co.endofhome.javoice.customer;

import com.googlecode.totallylazy.Sequence;
import org.junit.Test;

import java.io.IOException;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomerStoreTest {
    @Test
    public void can_add_one_customer() {
        CustomerStore customerStore = new CustomerStore();
        Customer customer = new Customer("Kevin Shea", "Some address", "some more address", "NY9876", "938273", "ACC-33");
        customerStore.addCustomer(customer);

        assertThat(customerStore.customers().size(), is(1));
        assertThat(customerStore.customers().get(0).name, is("Kevin Shea"));
    }

    @Test
    public void can_add_multiple_customers() {
        CustomerStore customerStore = new CustomerStore();
        Customer customer1 = new Customer("Kevin Shea", "Some address", "some more address", "NY9876", "938273", "ACC-33");
        Customer customer2 = new Customer("Kevin Cascell", "Another address", "yet more address", "PO421", "54321", "ACC-34");
        Sequence<Customer> twoCustomers = sequence(customer1, customer2);
        customerStore.addCustomers(twoCustomers);

        assertThat(customerStore.customers().size(), is(2));
        assertThat(customerStore.customers().get(0).name, is("Kevin Shea"));
        assertThat(customerStore.customers().get(1).name, is("Kevin Cascell"));
    }

    @Test
    public void can_read_in_a_customer_store_from_file() throws IOException {
        try {
            //TODO: replace sample file with a test file once we can write one.
            CustomerStore customerStore = CustomerStore.readFile("data/sample-customer-db.xls", 0);
            assertThat(customerStore.customers().size(), is(3));
        } catch (IOException e) {
            throw new IOException("Sorry, there was a problem reading in your file");
        }
    }
}