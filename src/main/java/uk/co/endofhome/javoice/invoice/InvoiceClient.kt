package uk.co.endofhome.javoice.invoice

import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import uk.co.endofhome.javoice.CellStyler.excelDateCellStyleFor
import uk.co.endofhome.javoice.Config
import uk.co.endofhome.javoice.customer.Customer
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.String.format
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class InvoiceClient private constructor(private var workBook: HSSFWorkbook, private val fileTemplatePath: Path, private val fileOutputPath: Path) {

    @Throws(IOException::class)
    fun writeFile(filePath: Path, invoice: Invoice) {
        try {
            val fileOut = FileOutputStream(invoiceFilePath(filePath, invoice).toString())
            removeUnnecessarySheets()
            workBook.write(fileOut)
            fileOut.close()
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException("There was a problem writing your file.")
        }

    }

    private fun removeUnnecessarySheets() {
        if (workBook.numberOfSheets > 1) {
            workBook.removeSheetAt(1)
        }
    }

    fun invoiceFilePath(filePath: Path, invoice: Invoice): Path {
        return Paths.get(format("%s/%s.xls", filePath.toString(), invoice.number))
    }

    @Throws(IOException::class)
    fun readFile(filePath: String, vararg sheetsToGet: Int): Invoice {
        if (sheetsToGet.size == 1) {
            val invoiceSheet = getSingleSheetFromPath(Paths.get(filePath), sheetsToGet[0])
            val customerDetails = getCustomerSectionFrom(invoiceSheet)
            val orderRefs = getOrderRefsSectionFrom(invoiceSheet)
            val invoiceNumber = getInvoiceNumberFrom(invoiceSheet)
            val itemLines = getItemLinesFrom(invoiceSheet)
            return buildInvoice(invoiceNumber, customerDetails, orderRefs, itemLines)
        }
        throw RuntimeException("Sorry, simultaneously reading in multiple sheets from one file is not yet supported.")
    }

    @Throws(IOException::class)
    fun getSingleSheetFromPath(filePath: Path, sheetNum: Int): HSSFSheet {
        val inputStream = FileInputStream(filePath.toString())
        workBook = HSSFWorkbook(POIFSFileSystem(inputStream))
        return workBook.getSheetAt(sheetNum)
    }

    fun setCustomerSection(invoiceSheet: HSSFSheet, invoice: Invoice): HSSFSheet {
        val (name, addressOne, addressTwo, postcode, phoneNumber) = invoice.customer
        invoiceSheet.getRow(11).getCell(4).setCellValue(name)
        invoiceSheet.getRow(12).getCell(4).setCellValue(addressOne)
        invoiceSheet.getRow(13).getCell(4).setCellValue(addressTwo)
        invoiceSheet.getRow(13).getCell(8).setCellValue(postcode)
        invoiceSheet.getRow(14).getCell(4).setCellValue(phoneNumber)
        return invoiceSheet
    }

    fun setOrderRefsSection(invoiceSheet: HSSFSheet, invoice: Invoice): HSSFSheet {
        val invoiceDate = invoice.date
        val date = Date.from(invoiceDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val customer = invoice.customer
        val dateCell = invoiceSheet.getRow(11).getCell(11)
        dateCell.setCellValue(date)
        dateCell.setCellStyle(excelDateCellStyleFor(workBook, dateCell))
        invoiceSheet.getRow(12).getCell(11).setCellValue(invoice.orderNumber)
        invoiceSheet.getRow(13).getCell(11).setCellValue(customer.accountCode)
        return invoiceSheet
    }

    fun setInvoiceNumber(invoiceSheet: HSSFSheet, invoice: Invoice): HSSFSheet {
        invoiceSheet.getRow(3).getCell(11).setCellValue(invoice.number)
        return invoiceSheet
    }

    fun setItemLine(invoiceSheet: HSSFSheet, invoice: Invoice, itemLineNumber: Int): HSSFSheet {
        val (quantity, description, unitPrice) = invoice.itemLines[itemLineNumber]
        val row = invoiceSheet.getRow(itemLineNumber + Invoice.ITEM_LINES_START_AT)
        quantity?.let { row.getCell(3).setCellValue(quantity) }
        description?.let { row.getCell(4).setCellValue(description) }
        unitPrice?.let { row.getCell(10).setCellValue(unitPrice) }
        return invoiceSheet
    }

    fun setItemLines(invoiceSheet: HSSFSheet, invoice: Invoice): List<ItemLine> {
        val lastItemLine = Invoice.ITEM_LINES_START_AT + invoice.itemLines.size - 1

        if (invoice.itemLines.size <= Invoice.MAX_ITEM_LINES) {
            return (Invoice.ITEM_LINES_START_AT..lastItemLine).map {
                val itemLineNumber = it - Invoice.ITEM_LINES_START_AT
                setItemLine(invoiceSheet, invoice, itemLineNumber)
                invoice.itemLines[itemLineNumber]
            }.toList()

        } else {
            throw RuntimeException(format("Too many lines for invoice number %s", invoice.number))
        }
    }

    fun setSections(invoiceSheet: HSSFSheet, invoice: Invoice) {
        setInvoiceNumber(invoiceSheet, invoice)
        setCustomerSection(invoiceSheet, invoice)
        setOrderRefsSection(invoiceSheet, invoice)
        setItemLines(invoiceSheet, invoice)
    }

    fun getCustomerSectionFrom(invoiceSheet: HSSFSheet): List<String> {
        val customerName = invoiceSheet.getRow(11).getCell(4).stringCellValue
        val addressOne = invoiceSheet.getRow(12).getCell(4).stringCellValue
        val addressTwo = invoiceSheet.getRow(13).getCell(4).stringCellValue
        val postcode = invoiceSheet.getRow(13).getCell(8).stringCellValue
        val phoneNumber = invoiceSheet.getRow(14).getCell(4).stringCellValue
        return listOf(customerName, addressOne, addressTwo, postcode, phoneNumber)
    }

    fun getOrderRefsSectionFrom(invoiceSheet: HSSFSheet): List<String> {
        val date = invoiceSheet.getRow(11).getCell(11).dateCellValue
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val invoiceDate = formatter.format(date)
        val orderNumber = invoiceSheet.getRow(12).getCell(11).stringCellValue
        val accountCode = invoiceSheet.getRow(13).getCell(11).stringCellValue
        return listOf(invoiceDate, orderNumber, accountCode)
    }

    fun getInvoiceNumberFrom(invoiceSheet: HSSFSheet): String {
        return invoiceSheet.getRow(3).getCell(11).stringCellValue
    }

    fun getItemLinesFrom(invoiceSheet: HSSFSheet): List<ItemLine> {
        val lastPossibleItemLine = Invoice.ITEM_LINES_START_AT + Invoice.MAX_ITEM_LINES - 1

        return (Invoice.ITEM_LINES_START_AT..lastPossibleItemLine).map {
            val quantity: Double? = invoiceSheet.getRow(it).getCell(3).numericCellValue
            val description: String? = invoiceSheet.getRow(it).getCell(4).stringCellValue
            val unitPrice: Double? = invoiceSheet.getRow(it).getCell(10).numericCellValue
            ItemLine(quantity, description, unitPrice)
        }.toList()
    }

    private fun buildInvoice(invoiceNumber: String, customerDetails: List<String>, orderRefs: List<String>, itemLines: List<ItemLine>): Invoice {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.getDefault())
        val invoiceDate = LocalDate.parse(orderRefs[0], formatter)
        val customer = Customer(customerDetails[0], customerDetails[1], customerDetails[2], customerDetails[3], customerDetails[4], "")
        return Invoice(invoiceNumber, invoiceDate, customer, orderRefs[1], itemLines)
    }

    fun fileTemplatePath(): Path {
        return fileTemplatePath
    }

    fun fileOutputPath(): Path {
        return fileOutputPath
    }

    companion object {
        fun invoiceClient(workbook: HSSFWorkbook): InvoiceClient {
            return InvoiceClient(workbook, Config.invoiceFileTemplatePath(), Config.invoiceXlsFileOutputPath())
        }

        fun invoiceClientCustomConfig(workbook: HSSFWorkbook, templatePath: Path, outputPath: Path): InvoiceClient {
            return InvoiceClient(workbook, templatePath, outputPath)
        }
    }
}