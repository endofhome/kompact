package uk.co.endofhome.javoice

import java.nio.file.Path

import java.nio.file.Paths.get

class Config {
    companion object {
        private val defaultInvoiceFileTemplatePath = get(System.getProperty("user.home"), "Javoice", "Templates", "invoice-template.xls")
        private val defaultInvoiceXlsFileOutputPath = get(System.getProperty("user.home"), "Javoice", "Invoices", "XLS")
        private val defaultInvoicePdfFileOutputPath = get(System.getProperty("user.home"), "Javoice", "Invoices", "PDF")
        private val defaultSalesLedgerFileOutputPath = get(System.getProperty("user.home"), "Javoice", "Sales Ledger")
        private val defaultCustomerDataFilePath = get(System.getProperty("user.home"), "Javoice", "Customer Data", "Customers.xls")
        private val defaultLibreOfficePath = osSpecificLibreOfficePath()

        private var invoiceFileTemplatePath = defaultInvoiceFileTemplatePath
        private var invoiceXlsFileOutputPath = defaultInvoiceXlsFileOutputPath
        private var invoicePdfFileOutputPath = defaultInvoicePdfFileOutputPath
        private var salesLedgerFileOutputPath = defaultSalesLedgerFileOutputPath
        private var customerDataFilePath = defaultCustomerDataFilePath
        private var libreOfficePath = defaultLibreOfficePath

        fun invoiceFileTemplatePath(): Path {
            return invoiceFileTemplatePath
        }

        fun invoiceXlsFileOutputPath(): Path {
            return invoiceXlsFileOutputPath
        }

        fun invoicePdfFileOutputPath(): Path {
            return invoicePdfFileOutputPath
        }

        fun salesLedgerFileOutputPath(): Path {
            return salesLedgerFileOutputPath
        }

        fun customerDataFilePath(): Path {
            return customerDataFilePath
        }

        fun libreOfficePath(): Path {
            return libreOfficePath
        }

    fun setInvoiceFileTemplatePath(newPath: Path): Path {
        invoiceFileTemplatePath = newPath
        return invoiceFileTemplatePath
    }

    fun setInvoiceXlsOutputPath(newPath: Path): Path {
        invoiceXlsFileOutputPath = newPath
        return invoiceXlsFileOutputPath
    }

    fun setInvoicePdfOutputPath(newPath: Path): Path {
        invoicePdfFileOutputPath = newPath
        return invoicePdfFileOutputPath
    }

    fun setSalesLedgerFileOutputPath(newPath: Path): Path {
        salesLedgerFileOutputPath = newPath
        return salesLedgerFileOutputPath
    }

    fun setCustomerDataFileOutputPath(newPath: Path): Path {
        customerDataFilePath = newPath
        return customerDataFilePath
    }

    fun setLibreOfficePath(newPath: Path): Path {
        libreOfficePath = newPath
        return libreOfficePath
    }

        private fun osSpecificLibreOfficePath(): Path {
            val osName = System.getProperty("os.name").toLowerCase()
            return if (osName.contains("windows") && osName.contains("xp")) {
                get("C:", "Program Files", "LibreOffice 5", "program")
            } else if (osName.contains("windows")) {
                get("C:", "Program Files (x86)", "LibreOffice 5", "program")
            } else if (osName.contains("mac os")) {
                get("/Applications", "LibreOffice.app", "Contents", "MacOS")
            } else if (osName.contains("linux")) {
                get("usr", "lib", "libreoffice", "program")
            } else {
                throw RuntimeException("OS not recognised")
            }
        }
    }
}
