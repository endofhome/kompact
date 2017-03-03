package uk.co.endofhome.javoice.ledger

import com.googlecode.totallylazy.Option
import uk.co.endofhome.javoice.invoice.Invoice

import java.time.LocalDate

import com.googlecode.totallylazy.Option.some

class LedgerEntry private constructor(val customerName: Option<String>, val invoiceNumber: Option<String>, var valueNett: Option<Double>, val crReq: Option<String>, val allocation: Option<String>, val date: Option<LocalDate>, val notes: Option<String>) {
    companion object {

        fun ledgerEntry(customerName: Option<String>, invoiceNumber: Option<String>, nettValue: Option<Double>, crReq: Option<String>, allocation: Option<String>, date: Option<LocalDate>, notes: Option<String>): LedgerEntry {
            return LedgerEntry(customerName, invoiceNumber, nettValue, crReq, allocation, date, notes)
        }

        fun ledgerEntry(invoice: Invoice, crReq: Option<String>, allocation: Option<String>, notes: Option<String>): LedgerEntry {
            return LedgerEntry(some(invoice.customerName()), some(invoice.number), some(invoice.nettValue()), crReq, allocation, some(invoice.date), notes)
        }
    }
}
