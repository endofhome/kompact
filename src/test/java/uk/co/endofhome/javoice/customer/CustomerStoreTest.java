package uk.co.endofhome.javoice.customer;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomerStoreTest {
    @Before
    public void setUp() throws IOException {
        writeCustomerStoreToFileSystem();
    }

    @Test
    public void can_add_one_customer() {
        CustomerStore customerStore = new CustomerStore();
        Customer customer = new Customer("Kevin Shea", "Some address", "some more address", "NY9876", "938273", "33");
        customerStore.addCustomer(customer);

        assertThat(customerStore.customers().size(), is(1));
        assertThat(customerStore.customers().get(0).getName(), is("Kevin Shea"));
    }

    @Test
    public void can_add_multiple_customers() {
        CustomerStore customerStore = new CustomerStore();
        Customer customer1 = new Customer("Kevin Shea", "Some address", "some more address", "NY9876", "938273", "33");
        Customer customer2 = new Customer("Kevin Cascell", "Another address", "yet more address", "PO421", "54321", "34");
        Sequence<Customer> twoCustomers = sequence(customer1, customer2);
        customerStore.addCustomers(twoCustomers);

        assertThat(customerStore.customers().size(), is(2));
        assertThat(customerStore.customers().get(0).getName(), is("Kevin Shea"));
        assertThat(customerStore.customers().get(1).getName(), is("Kevin Cascell"));
    }

    @Test
    public void can_read_in_a_customer_store_from_file() throws IOException {
        CustomerStore customerStore = CustomerStore.readFile(Paths.get("src/test/resources/Customers.xls"), 0);
        assertThat(customerStore.customers().size(), is(2));
    }

    @Test
    public void can_search_for_customer_in_store_by_exact_name() {
        CustomerStore customerStore = customerStoreWithTwoCustomers();
        Option<Customer> shouldBeFound = customerStore.search("Donald");
        Option<Customer> shouldNotBeFound = customerStore.search("Bilbo");

        assertThat(shouldBeFound.get().getPostcode(), is("TRUMP"));
        assertThat(shouldNotBeFound, is(none()));
    }

    @Test
    public void can_get_next_account_number() throws Exception {
        CustomerStore customerStore = customerStoreWithTwoCustomers();
        assertThat(customerStore.nextAccountNumber(), Is.is("56"));
    }

    private CustomerStore customerStoreWithTwoCustomers() {
        CustomerStore customerStore = new CustomerStore();
        Customer customer1 = new Customer("Donald", "Some address", "some more address", "TRUMP", "938273", "44");
        Customer customer2 = new Customer("Hillary", "Another address", "yet more address", "CLINTON", "54321", "55");
        Sequence<Customer> twoCustomers = sequence(customer1, customer2);
        customerStore.addCustomers(twoCustomers);
        return customerStore;
    }

    private void writeCustomerStoreToFileSystem() throws IOException {
        CustomerStore customerStore = customerStoreWithTwoCustomers();
        customerStore.writeFile(Paths.get("src/test/resources/Customers.xls"));
    }
}