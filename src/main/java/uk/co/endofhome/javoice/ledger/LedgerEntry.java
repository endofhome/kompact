package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Option;
import uk.co.endofhome.javoice.invoice.Invoice;

import java.time.LocalDate;

import static com.googlecode.totallylazy.Option.some;

public class LedgerEntry {
    public final Option<String> customerName;
    public final Option<String> invoiceNumber;
    public Option<Double> valueNett;
    public final Option<String> crReq;
    public final Option<String> allocation;
    public final Option<LocalDate> date;
    public final Option<String> notes;

    private LedgerEntry(Option<String> customerName, Option<String> invoiceNumber, Option<Double> nettValue, Option<String> crReq, Option<String> allocation, Option<LocalDate> date, Option<String> notes) {
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.valueNett = nettValue;
        this.crReq = crReq;
        this.allocation = allocation;
        this.date = date;
        this.notes = notes;
    }

    public static LedgerEntry ledgerEntry(Option<String> customerName, Option<String> invoiceNumber, Option<Double> nettValue, Option<String> crReq, Option<String> allocation, Option<LocalDate> date, Option<String> notes) {
        return new LedgerEntry(customerName, invoiceNumber, nettValue, crReq, allocation, date, notes);
    }

    public static LedgerEntry ledgerEntry(Invoice invoice, Option<String> crReq, Option<String> allocation, Option<String> notes) {
        return new LedgerEntry(some(invoice.customerName()), some(invoice.number), some(invoice.nettValue()), crReq, allocation, some(invoice.date), notes);
    }
}
