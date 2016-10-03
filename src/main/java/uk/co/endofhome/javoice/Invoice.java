package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;

import java.time.LocalDate;

public class Invoice {
    public final String number;
    public final LocalDate date;
    public final Customer customer;
    public final String orderNumber;
    public final Sequence<ItemLine> itemLines;

    public Invoice(String invoiceNumber, LocalDate date, Customer customer, String orderNumber, Sequence<ItemLine> itemLines) {
        this.number = invoiceNumber;
        this.date = date;
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.itemLines = itemLines;
    }
}
