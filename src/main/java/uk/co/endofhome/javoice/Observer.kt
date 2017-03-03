package uk.co.endofhome.javoice

import com.googlecode.totallylazy.Option
import com.googlecode.totallylazy.Sequence
import uk.co.endofhome.javoice.customer.Customer
import uk.co.endofhome.javoice.invoice.ItemLine

import java.io.IOException

interface Observer {
    @Throws(IOException::class)
    fun newCustomer(name: String, addressOne: String, addressTwo: String, postcode: String, phoneNumber: String)

    fun findCustomer(name: String): Option<Customer>

    @Throws(IOException::class)
    fun newInvoice(customer: Customer, orderNumber: String, itemLines: Sequence<ItemLine>)

    fun updateCurrentCustomer(customer: Option<Customer>)
}
