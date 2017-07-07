package uk.co.endofhome.javoice.customer

import com.googlecode.totallylazy.matchers.Matchers.`is`
import org.hamcrest.core.Is
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.nio.file.Paths
import java.util.Arrays

class CustomerStoreTest {
    @Before
    @Throws(IOException::class)
    fun setUp() {
        writeCustomerStoreToFileSystem()
    }

    @Test
    fun `can add one customer`() {
        val customerStore = CustomerStore()
        val customer = Customer("Kevin Shea", "Some address", "some more address", "NY9876", "938273", "33")
        customerStore.addCustomer(customer)

        assertThat(customerStore.customers.size, `is`(1))
        assertThat(customerStore.customers[0].name, `is`("Kevin Shea"))
    }

    @Test
    fun `can add multiple customers`() {
        val customerStore = CustomerStore()
        val customer1 = Customer("Kevin Shea", "Some address", "some more address", "NY9876", "938273", "33")
        val customer2 = Customer("Kevin Cascell", "Another address", "yet more address", "PO421", "54321", "34")
        val twoCustomers = Arrays.asList(customer1, customer2)
        customerStore.addCustomers(twoCustomers)

        assertThat(customerStore.customers.size, `is`(2))
        assertThat(customerStore.customers[0].name, `is`("Kevin Shea"))
        assertThat(customerStore.customers[1].name, `is`("Kevin Cascell"))
    }

    @Test
    @Throws(IOException::class)
    fun `can read in a customer store from file`() {
        val customerStore = CustomerStore.readFile(Paths.get("src/test/resources/Customers.xls"), 0)
        assertThat(customerStore.customers.size, `is`(2))
    }

    @Test
    fun `can search for customer in store by exact name`() {
        val customerStore = customerStoreWithTwoCustomers()
        val shouldBeFound = customerStore.search("Donald")
        val shouldNotBeFound = customerStore.search("Bilbo")

        assertThat<String>(shouldBeFound!!.postcode, `is`("TRUMP"))
        assertNull(shouldNotBeFound)
    }

    @Test
    @Throws(Exception::class)
    fun `can get next account number`() {
        val customerStore = customerStoreWithTwoCustomers()
        assertThat(customerStore.nextAccountNumber(), Is.`is`("56"))
    }

    private fun customerStoreWithTwoCustomers(): CustomerStore {
        val customerStore = CustomerStore()
        val customer1 = Customer("Donald", "Some address", "some more address", "TRUMP", "938273", "44")
        val customer2 = Customer("Hillary", "Another address", "yet more address", "CLINTON", "54321", "55")
        val twoCustomers = Arrays.asList(customer1, customer2)
        customerStore.addCustomers(twoCustomers)
        return customerStore
    }

    @Throws(IOException::class)
    private fun writeCustomerStoreToFileSystem() {
        val customerStore = customerStoreWithTwoCustomers()
        customerStore.writeFile(Paths.get("src/test/resources/Customers.xls"))
    }
}