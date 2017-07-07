package uk.co.endofhome.javoice.ledger

import java.time.Month
import java.time.Year

class MonthlyReport(val year: Year, val month: Month) {
    var entries: MutableList<LedgerEntry> = mutableListOf()
    val footer: Map<String, List<String?>> = mapOf(
            "rowOne" to listOf(
                null,
                null,
                "Nett",
                "VAT",
                "Gross",
                null,
                null,
                null
            ),
            "rowTwo" to listOf(
                null,
                null,
                "SUM(C${LEDGER_ENTRIES_START_AT + 1}:C)",
                "SUM(D${LEDGER_ENTRIES_START_AT + 1}:D)",
                "SUM(E${LEDGER_ENTRIES_START_AT + 1}:E)",
                null,
                null,
                null
            )
        )

    companion object {
        val LEDGER_ENTRIES_START_AT = 4
        val TOTAL_FOOTER_ROWS = 2
    }
}
