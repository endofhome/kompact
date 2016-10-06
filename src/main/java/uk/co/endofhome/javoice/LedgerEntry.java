package uk.co.endofhome.javoice;

public class LedgerEntry {
    private final Invoice invoice;
    private final String crReq;
    private final String allocation;
    private final String notes;

    public LedgerEntry(Invoice invoice, String crReq, String allocation, String notes) {
        this.invoice = invoice;
        this.crReq = crReq;
        this.allocation = allocation;
        this.notes = notes;
    }
}
