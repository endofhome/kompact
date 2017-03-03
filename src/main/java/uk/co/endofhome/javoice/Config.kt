package uk.co.endofhome.javoice

import java.nio.file.Path

import java.nio.file.Paths.get

object Config {
    private val defaultInvoiceFileTemplatePath = get(String.format("%s/Javoice/Templates/invoice-template.xls", System.getProperty("user.home")))
    private val defaultInvoiceFileOutputPath = get(String.format("%s/Javoice/Invoices", System.getProperty("user.home")))
    private val defaultSalesLedgerFileOutputPath = get(String.format("%s/Javoice/Sales Ledger", System.getProperty("user.home")))
    private val defaultCustomerDataFilePath = get(String.format("%s/Javoice/Customer Data/Customers.xls", System.getProperty("user.home")))

    private val invoiceFileTemplatePath = defaultInvoiceFileTemplatePath
    private var invoiceFileOutputPath = defaultInvoiceFileOutputPath
    private var salesLedgerFileOutputPath = defaultSalesLedgerFileOutputPath
    private var customerDataFilePath = defaultCustomerDataFilePath

    fun invoiceFileTemplatePath(): Path {
        return invoiceFileTemplatePath
    }

    fun invoiceFileOutputPath(): Path {
        return invoiceFileOutputPath
    }

    fun salesLedgerFileOutputPath(): Path {
        return salesLedgerFileOutputPath
    }

    fun customerDataFilePath(): Path {
        return customerDataFilePath
    }

    fun setInvoiceOutputPath(newPath: Path) {
        invoiceFileOutputPath = newPath
    }

    fun setSalesLedgerFileOutputPath(newPath: Path) {
        salesLedgerFileOutputPath = newPath
    }

    fun setCustomerDataFileOutputPath(newPath: Path) {
        customerDataFilePath = newPath
    }
}
