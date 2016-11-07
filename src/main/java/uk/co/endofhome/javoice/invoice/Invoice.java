package uk.co.endofhome.javoice.invoice;

import com.googlecode.totallylazy.Sequence;
import uk.co.endofhome.javoice.customer.Customer;

import java.time.LocalDate;

public class Invoice {
    public final String number;
    public final LocalDate date;
    public final Customer customer;
    public final String orderNumber;
    public final Sequence<ItemLine> itemLines;

    public static final int ITEM_LINES_START_AT = 17;
    public static final int MAX_ITEM_LINES = 17;

    public Invoice(String invoiceNumber, LocalDate date, Customer customer, String orderNumber, Sequence<ItemLine> itemLines) {
        this.number = invoiceNumber;
        this.date = date;
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.itemLines = itemLines;
    }

    public double nettValue() {
        return itemLines.map((item -> (item.quantity * item.unitPrice)))
                .reduce((x, y) -> ((double) x) + y);
    }

    public String customerName() {
        return customer.name;
    }
}
