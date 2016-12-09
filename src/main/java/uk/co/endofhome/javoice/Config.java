package uk.co.endofhome.javoice;

import java.nio.file.Path;

import static java.nio.file.Paths.get;

public class Config {
    private static Path defaultInvoiceFileTemplatePath = get(String.format("%s/Javoice/Templates/invoice-template.xls", System.getProperty("user.home")));
    private static Path defaultInvoiceFileOutputPath = get(String.format("%s/Javoice/Invoices", System.getProperty("user.home")));
    private static Path defaultSalesLedgerFileOutputPath = get(String.format("%s/Javoice/Sales Ledger", System.getProperty("user.home")));
    private static Path defaultCustomerDataFilePath = get(String.format("%s/Javoice/Customer Data", System.getProperty("user.home")));

    private static Path invoiceFileTemplatePath = defaultInvoiceFileTemplatePath;
    private static Path invoiceFileOutputPath = defaultInvoiceFileOutputPath;
    private static Path salesLedgerFileOutputPath = defaultSalesLedgerFileOutputPath;
    private static Path customerDataFilePath = defaultCustomerDataFilePath;

    public static Path invoiceFileTemplatePath() {
        return invoiceFileTemplatePath;
    }

    public static Path invoiceFileOutputPath() {
        return invoiceFileOutputPath;
    }

    public static Path salesLedgerFileOutputPath() {
        return salesLedgerFileOutputPath;
    }

    public static Path customerDataFilePath() {
        return customerDataFilePath;
    }

    public static Path setInvoiceOutputPath(Path newPath) {
        return invoiceFileOutputPath = newPath;
    }

    public static Path setSalesLedgerFileOutputPath(Path newPath) {
        return salesLedgerFileOutputPath = newPath;
    }
}
