package uk.co.endofhome.javoice;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private static Path defaultInvoiceFileOutputPath = Paths.get(String.format("%s/Javoice/Invoices", System.getProperty("user.home")));
    private static Path defaultSalesLedgerFileOutputPath = Paths.get(String.format("%s/Javoice/Sales Ledger", System.getProperty("user.home")));
    private static Path defaultCustomerDataFileOutputPath = Paths.get(String.format("%s/Javoice/Customer Data", System.getProperty("user.home")));

    public static Path defaultInvoiceFileOutputPath() {
        return defaultInvoiceFileOutputPath;
    }

    public static Path defaultSalesLedgerFileOutputPath() {
        return defaultSalesLedgerFileOutputPath;
    }

    public static Path defaultCustomerDataFileOutputPath() {
        return defaultCustomerDataFileOutputPath;
    }
}
