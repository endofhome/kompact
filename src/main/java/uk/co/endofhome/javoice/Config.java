package uk.co.endofhome.javoice;

import java.nio.file.Path;

import static java.nio.file.Paths.get;

public class Config {
    private static Path defaultInvoiceFileTemplatePath = get(System.getProperty("user.home"), "Javoice", "Templates", "invoice-template.xls");
    private static Path defaultInvoiceXlsFileOutputPath = get(System.getProperty("user.home"), "Javoice", "Invoices", "XLS");
    private static Path defaultInvoicePdfFileOutputPath = get(System.getProperty("user.home"), "Javoice", "Invoices", "PDF");
    private static Path defaultSalesLedgerFileOutputPath = get(System.getProperty("user.home"), "Javoice", "Sales Ledger");
    private static Path defaultCustomerDataFilePath = get(System.getProperty("user.home"), "Javoice", "Customer Data", "Customers.xls");
    private static Path defaultLibreOfficePath = osSpecificLibreOfficePath();

    private static Path invoiceFileTemplatePath = defaultInvoiceFileTemplatePath;
    private static Path invoiceXlsFileOutputPath = defaultInvoiceXlsFileOutputPath;
    private static Path invoicePdfFileOutputPath = defaultInvoicePdfFileOutputPath;
    private static Path salesLedgerFileOutputPath = defaultSalesLedgerFileOutputPath;
    private static Path customerDataFilePath = defaultCustomerDataFilePath;
    private static Path libreOfficePath = defaultLibreOfficePath;

    public static Path invoiceFileTemplatePath() {
        return invoiceFileTemplatePath;
    }

    public static Path invoiceXlsFileOutputPath() {
        return invoiceXlsFileOutputPath;
    }

    public static Path invoicePdfFileOutputPath() {
        return invoicePdfFileOutputPath;
    }

    public static Path salesLedgerFileOutputPath() {
        return salesLedgerFileOutputPath;
    }

    public static Path customerDataFilePath() {
        return customerDataFilePath;
    }

    public static Path libreOfficePath() {
        return libreOfficePath;
    }

    public static Path setInvoiceXlsOutputPath(Path newPath) {
        return invoiceXlsFileOutputPath = newPath;
    }

    public static Path setInvoicePdfOutputPath(Path newPath) {
        return invoicePdfFileOutputPath = newPath;
    }

    public static Path setSalesLedgerFileOutputPath(Path newPath) {
        return salesLedgerFileOutputPath = newPath;
    }

    public static Path setCustomerDataFileOutputPath(Path newPath) {
        return customerDataFilePath = newPath;
    }

    public static Path setLibreOfficePath(Path newPath) {
        return libreOfficePath = newPath;
    }

    private static Path osSpecificLibreOfficePath() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows") && osName.contains("xp")) {
            return get("C:", "Program Files", "LibreOffice 5", "program");
        } else if (osName.contains("windows")) {
            return get("C:", "Program Files (x86)", "LibreOffice 5", "program");
        } else if (osName.contains("mac os")) {
            return get("/Applications", "LibreOffice.app", "Contents", "MacOS");
        } else if (osName.contains("linux")) {
            return get("usr", "lib", "libreoffice", "program");
        } else {
            throw new RuntimeException("OS not recognised");
        }
    }
}
