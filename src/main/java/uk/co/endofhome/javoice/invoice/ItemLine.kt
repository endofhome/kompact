package uk.co.endofhome.javoice.invoice

import com.googlecode.totallylazy.Option

import java.util.Objects

import com.googlecode.totallylazy.Option.option

class ItemLine(val quantity: Option<Double>, val description: Option<String>, val unitPrice: Option<Double>) {

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ItemLine) {
            return false
        }
        val itemLine = o
        return quantity == itemLine.quantity &&
            description == itemLine.description &&
            unitPrice == itemLine.unitPrice
    }

    override fun hashCode(): Int {
        return Objects.hash(quantity, description, unitPrice)
    }

    companion object {

        fun itemLine(quantity: Double, description: String, unitPrice: Double): ItemLine {
            return ItemLine(option(quantity), option(description), option(unitPrice))
        }
    }
}