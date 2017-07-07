package uk.co.endofhome.javoice.invoice

import com.googlecode.totallylazy.matchers.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import uk.co.endofhome.javoice.customer.Customer
import java.time.LocalDate

class InvoiceTest {

    @Test
    fun can_get_nett_value() {
        val customer = Customer("", "", "", "", "", "")
        val firstItemLine = ItemLine(1.0, "things", 1.0)
        val secondItemLine = ItemLine(1.0, "stuff", 1.0)
        val invoice = Invoice("some num", LocalDate.now(), customer, "", listOf(firstItemLine, secondItemLine))

        assertThat(invoice.nettValue(), `is`(2.0))
    }

    @Test
    fun can_get_nett_value_when_no_itemLines() {
        val customer = Customer("", "", "", "", "", "")
        val invoice = Invoice("some num", LocalDate.now(), customer, "", emptyList())

        assertThat(invoice.nettValue(), `is`(0.0))
    }

    @Test
    fun can_get_customer_name() {
        val customer = Customer("Andrew Cyrille", "", "", "", "", "")
        val invoice = Invoice("", LocalDate.now(), customer, "", emptyList())

        assertThat(invoice.customerName(), `is`("Andrew Cyrille"))
    }
}