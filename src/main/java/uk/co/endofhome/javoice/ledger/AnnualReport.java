package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Sequence;

import static com.googlecode.totallylazy.Sequences.sequence;

public class AnnualReport {
    private final Sequence monthlyReports;

    public AnnualReport() {
        this.monthlyReports = sequence();
    }
}
