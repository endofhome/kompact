package uk.co.endofhome.javoice.invoice

import uk.co.endofhome.javoice.customer.Customer
import java.time.LocalDate

class Invoice(val number: String, val date: LocalDate, val customer: Customer, val orderNumber: String, val itemLines: List<ItemLine>) {

    fun nettValue(): Double {
        return when (itemLines.isNotEmpty()) {
            true -> itemLines
                .map { ((it.quantity ?: 0.0) * (it.unitPrice ?: 0.0)) }
                .reduce { x, y -> x + y }
            false -> 0.0
        }
    }

    fun customerName(): String {
        return customer.name
    }

    companion object {
        val ITEM_LINES_START_AT = 17
        val MAX_ITEM_LINES = 17
    }
}
