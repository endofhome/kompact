package uk.co.endofhome.javoice;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private static Path defaultInvoiceFileOutputPath = Paths.get(String.format("%s/Javoice/Invoices", System.getProperty("user.home")));
    private static Path defaultInvoiceFileTemplatePath = Paths.get(String.format("%s/Javoice/Invoices", System.getProperty("user.home")));
    private static Path defaultSalesLedgerFileOutputPath = Paths.get(String.format("%s/Javoice/Sales Ledger", System.getProperty("user.home")));
    private static Path defaultCustomerDataFileOutputPath = Paths.get(String.format("%s/Javoice/Customer Data", System.getProperty("user.home")));

    private static Path invoiceFileOutputPath = defaultInvoiceFileOutputPath;
    private static Path invoiceFileTemplatePath = defaultInvoiceFileTemplatePath;
    private static Path salesLedgerFileOutputPath = defaultSalesLedgerFileOutputPath;
    private static Path customerDataFileOutputPath = defaultCustomerDataFileOutputPath;

    public static Path invoiceFileOutputPath() {
        return invoiceFileOutputPath;
    }

    public static Path invoiceFileTemplatePath() {
        return invoiceFileTemplatePath;
    }

    public static Path salesLedgerFileOutputPath() {
        return salesLedgerFileOutputPath;
    }

    public static Path customerDataFileOutputPath() {
        return customerDataFileOutputPath;
    }
}
