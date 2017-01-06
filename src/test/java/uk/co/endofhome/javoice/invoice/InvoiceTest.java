package uk.co.endofhome.javoice.invoice;

import org.junit.Test;
import uk.co.endofhome.javoice.customer.Customer;

import java.time.LocalDate;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static org.junit.Assert.assertThat;

public class InvoiceTest {

    @Test
    public void can_get_nett_value() {
        Customer customer = new Customer("", "", "", "", "", "");
        ItemLine firstItemLine = new ItemLine(1.0, "things", 1.0);
        ItemLine secondItemLine = new ItemLine(1.0, "stuff", 1.0);
        Invoice invoice = new Invoice("some num", LocalDate.now(), customer, "", sequence(firstItemLine, secondItemLine));

        assertThat(invoice.nettValue(), is(2.0));
    }

    @Test
    public void can_get_nett_value_when_no_itemLines() {
        Customer customer = new Customer("", "", "", "", "", "");
        Invoice invoice = new Invoice("some num", LocalDate.now(), customer, "", sequence());

        assertThat(invoice.nettValue(), is(0.0));
    }

    @Test
    public void can_get_customer_name() {
        Customer customer = new Customer("Andrew Cyrille", "", "", "", "", "");
        Invoice invoice = new Invoice("", LocalDate.now(), customer, "", sequence());

        assertThat(invoice.customerName(), is("Andrew Cyrille"));
    }
}