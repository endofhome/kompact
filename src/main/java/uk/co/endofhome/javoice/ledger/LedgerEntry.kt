package uk.co.endofhome.javoice.ledger

import uk.co.endofhome.javoice.invoice.Invoice
import java.time.LocalDate

class LedgerEntry private constructor(val customerName: String?, val invoiceNumber: String?, var valueNett: Double?, val crReq: String?, val allocation: String?, val date: LocalDate?, val notes: String?) {
    companion object {
        fun ledgerEntry(customerName: String?, invoiceNumber: String?, nettValue: Double?, crReq: String?, allocation: String?, date: LocalDate?, notes: String?): LedgerEntry {
            return LedgerEntry(customerName, invoiceNumber, nettValue, crReq, allocation, date, notes)
        }

        fun ledgerEntry(invoice: Invoice, crReq: String?, allocation: String?, notes: String?): LedgerEntry {
            return LedgerEntry(invoice.customerName(), invoice.number, invoice.nettValue(), crReq, allocation, invoice.date, notes)
        }
    }
}
