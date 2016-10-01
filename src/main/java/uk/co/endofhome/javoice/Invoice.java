package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;

import java.time.LocalDate;

public class Invoice {
    private final LocalDate date;
    private final Customer customer;
    private final String orderNumber;
    private final Sequence<String> itemLines;
    private final String customerReference;

    public Invoice(LocalDate date, Customer customer, String orderNumber, String customerReference, Sequence<String> itemLines) {
        this.date = date;
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.customerReference = customerReference;
        this.itemLines = itemLines;
    }
}
