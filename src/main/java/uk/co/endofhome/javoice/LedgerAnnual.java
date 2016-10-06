package uk.co.endofhome.javoice;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;

public class LedgerAnnual {
    private final Sequence monthlyReports;

    public LedgerAnnual() {
        this.monthlyReports = sequence();
    }
}
