package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Option;

import java.time.LocalDate;

public class LedgerEntry {
    public final Option<String> customerName;
    public final Option<String> invoiceNumber;
    public Option<Double> valueNett;
    public final Option<String> crReq;
    public final Option<String> allocation;
    public final Option<LocalDate> date;
    public final Option<String> notes;

    public LedgerEntry(Option<String> customerName, Option<String> invoiceNumber, Option<Double> nettValue, Option<String> crReq, Option<String> allocation, Option<LocalDate> date, Option<String> notes) {
        this.customerName = customerName;
        this.invoiceNumber = invoiceNumber;
        this.valueNett = nettValue;
        this.crReq = crReq;
        this.allocation = allocation;
        this.date = date;
        this.notes = notes;
    }
}
