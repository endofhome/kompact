package uk.co.endofhome.javoice.invoice

import com.googlecode.totallylazy.Sequence
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import uk.co.endofhome.javoice.Config
import uk.co.endofhome.javoice.customer.Customer

import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

import com.googlecode.totallylazy.Option.option
import com.googlecode.totallylazy.Sequences.sequence
import java.lang.String.format
import uk.co.endofhome.javoice.CellStyler.excelDateCellStyleFor
import uk.co.endofhome.javoice.invoice.Invoice.Companion.ITEM_LINES_START_AT
import uk.co.endofhome.javoice.invoice.Invoice.Companion.MAX_ITEM_LINES

class InvoiceClient private constructor(private var workBook: HSSFWorkbook, private val fileTemplatePath: Path, private val fileOutputPath: Path) {

    fun writeFile(filePath: Path, invoice: Invoice) {
        try {
            val fileOut = FileOutputStream(format("%s/%s.xls", filePath.toString(), invoice.number))
            workBook.write(fileOut)
            fileOut.close()
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException("There was a problem writing your file.")
        }

    }

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

    fun getSingleSheetFromPath(filePath: Path, sheetNum: Int): HSSFSheet {
        val inputStream = FileInputStream(filePath.toString())
        workBook = HSSFWorkbook(POIFSFileSystem(inputStream))
        return workBook.getSheetAt(sheetNum)
    }

    fun setCustomerSection(invoiceSheet: HSSFSheet, invoice: Invoice): HSSFSheet {
        val customer = invoice.customer
        invoiceSheet.getRow(11).getCell(4).setCellValue(customer.name)
        invoiceSheet.getRow(12).getCell(4).setCellValue(customer.addressOne)
        invoiceSheet.getRow(13).getCell(4).setCellValue(customer.addressTwo)
        invoiceSheet.getRow(13).getCell(8).setCellValue(customer.postcode)
        invoiceSheet.getRow(14).getCell(4).setCellValue(customer.phoneNumber)
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
        val itemLineDetails = invoice.itemLines.get(itemLineNumber)
        val row = invoiceSheet.getRow(itemLineNumber + ITEM_LINES_START_AT)
        if (itemLineDetails.quantity.isDefined) row.getCell(3).setCellValue(itemLineDetails.quantity.get())
        if (itemLineDetails.description.isDefined) row.getCell(4).setCellValue(itemLineDetails.description.get())
        if (itemLineDetails.unitPrice.isDefined) row.getCell(10).setCellValue(itemLineDetails.unitPrice.get())
        return invoiceSheet
    }

    fun setItemLines(invoiceSheet: HSSFSheet, invoice: Invoice): Sequence<ItemLine> {
        var itemLines = sequence<ItemLine>()
        val lastItemLine = ITEM_LINES_START_AT + invoice.itemLines.size - 1
        if (invoice.itemLines.size <= MAX_ITEM_LINES) {
            for (i in ITEM_LINES_START_AT..lastItemLine) {
                val itemLineNumber = i - ITEM_LINES_START_AT
                setItemLine(invoiceSheet, invoice, itemLineNumber)
                itemLines = itemLines.append(invoice.itemLines.get(itemLineNumber))
            }
        } else {
            throw RuntimeException(format("Too many lines for invoice number %s", invoice.number))
        }
        return itemLines
    }

    fun setSections(invoiceSheet: HSSFSheet, invoice: Invoice) {
        setInvoiceNumber(invoiceSheet, invoice)
        setCustomerSection(invoiceSheet, invoice)
        setOrderRefsSection(invoiceSheet, invoice)
        setItemLines(invoiceSheet, invoice)
    }

    fun getCustomerSectionFrom(invoiceSheet: HSSFSheet): Sequence<String> {
        val customerName = invoiceSheet.getRow(11).getCell(4).stringCellValue
        val addressOne = invoiceSheet.getRow(12).getCell(4).stringCellValue
        val addressTwo = invoiceSheet.getRow(13).getCell(4).stringCellValue
        val postcode = invoiceSheet.getRow(13).getCell(8).stringCellValue
        val phoneNumber = invoiceSheet.getRow(14).getCell(4).stringCellValue
        return sequence(customerName, addressOne, addressTwo, postcode, phoneNumber)
    }

    fun getOrderRefsSectionFrom(invoiceSheet: HSSFSheet): Sequence<String> {
        val date = invoiceSheet.getRow(11).getCell(11).dateCellValue
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val invoiceDate = formatter.format(date)
        val orderNumber = invoiceSheet.getRow(12).getCell(11).stringCellValue
        val accountCode = invoiceSheet.getRow(13).getCell(11).stringCellValue
        return sequence(invoiceDate, orderNumber, accountCode)
    }

    fun getInvoiceNumberFrom(invoiceSheet: HSSFSheet): String {
        return invoiceSheet.getRow(3).getCell(11).stringCellValue
    }

    fun getItemLinesFrom(invoiceSheet: HSSFSheet): Sequence<ItemLine> {
        var itemLines = sequence<ItemLine>()
        val lastPossibleItemLine = ITEM_LINES_START_AT + MAX_ITEM_LINES - 1
        for (i in ITEM_LINES_START_AT..lastPossibleItemLine) {
            val quantity = option(invoiceSheet.getRow(i).getCell(3).numericCellValue)
            val description = option(invoiceSheet.getRow(i).getCell(4).stringCellValue)
            val unitPrice = option(invoiceSheet.getRow(i).getCell(10).numericCellValue)
            val itemLine = ItemLine(quantity, description, unitPrice)
            itemLines = itemLines.append(itemLine)
        }
        return itemLines
    }

    private fun buildInvoice(invoiceNumber: String, customerDetails: Sequence<String>, orderRefs: Sequence<String>, itemLines: Sequence<ItemLine>): Invoice {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.getDefault())
        val invoiceDate = LocalDate.parse(orderRefs.get(0), formatter)
        val customer = Customer(customerDetails.get(0), customerDetails.get(1), customerDetails.get(2), customerDetails.get(3), customerDetails.get(4), "")
        return Invoice(invoiceNumber, invoiceDate, customer, orderRefs.get(1), itemLines)
    }

    fun fileTemplatePath(): Path {
        return fileTemplatePath
    }

    fun fileOutputPath(): Path {
        return fileOutputPath
    }

    companion object {

        fun invoiceClient(workbook: HSSFWorkbook): InvoiceClient {
            return InvoiceClient(workbook, Config.invoiceFileTemplatePath(), Config.invoiceFileOutputPath())
        }

        fun invoiceClientCustomConfig(workbook: HSSFWorkbook, templatePath: Path, outputPath: Path): InvoiceClient {
            return InvoiceClient(workbook, templatePath, outputPath)
        }
    }
}