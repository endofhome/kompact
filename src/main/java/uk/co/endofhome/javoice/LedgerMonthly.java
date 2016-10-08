package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;

import java.time.Month;
import java.time.Year;

import static com.googlecode.totallylazy.Sequences.sequence;

public class LedgerMonthly {
    private final Year year;
    private final Month month;
    public Sequence<LedgerEntry> entries;
    public static final int LEDGER_ENTRIES_START_AT = 4;
    public static final int TOTAL_WHITESPACE_ROWS = 2;
    public static final int TOTAL_FOOTER_ROWS = 2;

    public LedgerMonthly(Year year, Month month) {
        this.year = year;
        this.month = month;
        this.entries = sequence();
    }
}
