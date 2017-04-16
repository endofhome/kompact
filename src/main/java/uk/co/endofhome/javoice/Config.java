package uk.co.endofhome.javoice;

import java.nio.file.Path;

import static java.nio.file.Paths.get;

public class Config {
    private static Path defaultInvoiceFileTemplatePath = get(String.format("%s/Javoice/Templates/invoice-template.xls", System.getProperty("user.home")));
    private static Path defaultInvoiceXlsFileOutputPath = get(String.format("%s/Javoice/Invoices/XLS", System.getProperty("user.home")));
    private static Path defaultInvoicePdfFileOutputPath = get(String.format("%s/Javoice/Invoices/PDF", System.getProperty("user.home")));
    private static Path defaultSalesLedgerFileOutputPath = get(String.format("%s/Javoice/Sales Ledger", System.getProperty("user.home")));
    private static Path defaultCustomerDataFilePath = get(String.format("%s/Javoice/Customer Data/Customers.xls", System.getProperty("user.home")));
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
        String property = System.getProperty("os.name").toLowerCase();
        if (property.contains("windows")) {
            return get("C:\\Program Files (x86)\\LibreOffice 5\\program\\");
        } else if (property.contains("mac os")) {
            return get("/Applications/LibreOffice.app/Contents/MacOS");
        } else if (property.contains("linux")) {
            return get("/usr/lib/libreoffice/program/");
        } else {
            throw new RuntimeException("OS not recognised");
        }
    }
}
