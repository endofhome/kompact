package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;

import java.time.Month;
import java.time.Year;
import java.util.Map;

import static com.googlecode.totallylazy.Maps.map;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;
import static java.lang.String.format;

public class LedgerMonthly {
    private final Year year;
    private final Month month;
    public final Map<String, Sequence<Option<String>>> footer;
    public Sequence<LedgerEntry> entries;
    public static final int LEDGER_ENTRIES_START_AT = 4;
    public static final int TOTAL_FOOTER_ROWS = 2;

    @SuppressWarnings("unchecked")
    public LedgerMonthly(Year year, Month month) {
        this.year = year;
        this.month = month;
        this.entries = sequence();
        this.footer = map(
                "rowOne", sequence(
                        none(),
                        none(),
                        some("Nett"),
                        some("VAT"),
                        some("Gross"),
                        none(),
                        none(),
                        none()
                ),
                "rowTwo", sequence(
                        none(),
                        none(),
                        some(format("SUM(C%s:C)", LEDGER_ENTRIES_START_AT + 1)),
                        some(format("SUM(D%s:D)", LEDGER_ENTRIES_START_AT + 1)),
                        some(format("SUM(E%s:E)", LEDGER_ENTRIES_START_AT + 1)),
                        none(),
                        none(),
                        none()
                )
        );
    }
}
