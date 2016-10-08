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

    public LedgerMonthly(Year year, Month month) {
        this.year = year;
        this.month = month;
        this.entries = sequence();
    }

    public int totalEntries() {
        //TODO: implement this. Get the total entries dynamically.

        return 80;
    }
}
