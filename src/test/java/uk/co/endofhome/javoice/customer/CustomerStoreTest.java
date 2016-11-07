package uk.co.endofhome.javoice.customer;

import org.junit.Test;

import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomerStoreTest {
    @Test
    public void can_set_a_new_entry_in_customer_store() {
        CustomerStore customerStore = new CustomerStore();
        Customer customer = new Customer("Kevin Shea", "Some address", "some more address", "NY9876", "938273", "ACC-33");
        customerStore.setNewEntry(customer);

        assertThat(customerStore.customers().size(), is(1));
        assertThat(customerStore.customers().get(0).name, is("Kevin Shea"));
    }
}