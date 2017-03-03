package uk.co.endofhome.javoice.ledger

import com.googlecode.totallylazy.Option
import com.googlecode.totallylazy.Sequence

import java.time.Month
import java.time.Year

import com.googlecode.totallylazy.Maps.map
import com.googlecode.totallylazy.Option.none
import com.googlecode.totallylazy.Option.some
import com.googlecode.totallylazy.Sequences.sequence
import java.lang.String.format

class MonthlyReport(val year: Year, val month: Month) {
    val footer: Map<String, Sequence<Option<String>>>
    var entries: Sequence<LedgerEntry>

    init {
        this.entries = sequence<LedgerEntry>()
        this.footer = map(
            "rowOne", sequence(
            none<String>(),
            none<String>(),
            some("Nett"),
            some("VAT"),
            some("Gross"),
            none<String>(),
            none<String>(),
            none<String>()
        ),
            "rowTwo", sequence(
            none<String>(),
            none<String>(),
            some(format("SUM(C%s:C)", LEDGER_ENTRIES_START_AT + 1)),
            some(format("SUM(D%s:D)", LEDGER_ENTRIES_START_AT + 1)),
            some(format("SUM(E%s:E)", LEDGER_ENTRIES_START_AT + 1)),
            none<String>(),
            none<String>(),
            none<String>()
        )
        )
    }

    companion object {
        val LEDGER_ENTRIES_START_AT = 4
        val TOTAL_FOOTER_ROWS = 2
    }
}
