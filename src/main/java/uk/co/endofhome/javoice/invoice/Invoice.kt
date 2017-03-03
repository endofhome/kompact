package uk.co.endofhome.javoice.invoice

import com.googlecode.totallylazy.Sequence
import uk.co.endofhome.javoice.customer.Customer

import java.time.LocalDate

class Invoice(val number: String, val date: LocalDate, val customer: Customer, val orderNumber: String, val itemLines: Sequence<ItemLine>) {

    fun nettValue(): Double {
        if (itemLines.isEmpty) {
            return 0.0
        }
        return itemLines.map { item -> item.quantity.getOrElse(0.0) * item.unitPrice.getOrElse(0.0) }
            .reduce<Double> { x, y -> x as Double + y!! }
    }

    fun customerName(): String {
        return customer.name
    }

    companion object {

        val ITEM_LINES_START_AT = 17
        val MAX_ITEM_LINES = 17
    }
}
