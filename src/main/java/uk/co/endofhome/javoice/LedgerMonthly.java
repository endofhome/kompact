package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;

import java.time.Month;
import java.time.Year;

import static com.googlecode.totallylazy.Sequences.sequence;

public class LedgerMonthly {
    private final Year year;
    private final Month month;
    private final Sequence<LedgerEntry> entries;

    public LedgerMonthly(Year year, Month month) {
        this.year = year;
        this.month = month;
        this.entries = sequence();
    }
}
