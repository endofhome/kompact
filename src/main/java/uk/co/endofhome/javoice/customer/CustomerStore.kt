package uk.co.endofhome.javoice.customer

import com.googlecode.totallylazy.Option
import com.googlecode.totallylazy.Sequence
import com.googlecode.totallylazy.Sequences.sequence
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK
import uk.co.endofhome.javoice.CellStyler.excelBoldBorderBottomCellStyleFor
import uk.co.endofhome.javoice.CellStyler.excelBoldCellStyleFor
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.String.format
import java.nio.file.Path

class CustomerStore {
    private var customers = sequence<Customer>()
    private val CUSTOMER_STORE_COLUMN_COUNT: Int = 7

    fun customers(): Sequence<Customer> {
        return customers
    }

    fun writeFile(filePath: Path) {
        try {
            val fileOut = FileOutputStream(format(filePath.toString()))
            val workbook = createWorkbookWithOneSheet()
            resizeColumns(workbook)
            workbook.write(fileOut)
            fileOut.close()
        } catch (e: FileNotFoundException) {
            throw RuntimeException("There was a problem writing your file.")
        }
    }

    private fun createWorkbookWithOneSheet(): HSSFWorkbook {
        val workbook = HSSFWorkbook()
        workbook.createSheet()
        setHeaders(workbook)
        setCustomerLines(workbook)
        return workbook
    }

    private fun setHeaders(workbook: HSSFWorkbook) {
        val sheet = workbook.getSheetAt(0)

        val topHeaderCell = sheet.createRow(1).createCell(0)
        topHeaderCell.setCellValue("Customers")
        topHeaderCell.setCellStyle(excelBoldCellStyleFor(workbook))

        val tableHeaders = sheet.createRow(3)
        tableHeaders.createCell(0).setCellValue("Account code")
        tableHeaders.createCell(1).setCellValue("Name")
        tableHeaders.createCell(2).setCellValue("Address (1)")
        tableHeaders.createCell(3).setCellValue("Address (2)")
        tableHeaders.createCell(4).setCellValue("Postcode")
        tableHeaders.createCell(5).setCellValue("Phone number")

        tableHeaders.map {
            it.cellStyle = excelBoldBorderBottomCellStyleFor(workbook)
        }
    }

    private fun setCustomerLines(workbook: HSSFWorkbook) {
        val sheet = workbook.getSheetAt(0)
        customers.forEach {
            val nextRow = sheet.lastRowNum + 1
            val row = sheet.createRow(nextRow)
            row.createCell(0).setCellValue(it.accountCode)
            row.createCell(1).setCellValue(it.name)
            row.createCell(2).setCellValue(it.addressOne)
            row.createCell(3).setCellValue(it.addressTwo)
            row.createCell(4).setCellValue(it.postcode)
            row.createCell(5).setCellValue(it.phoneNumber)
        }
    }

    fun addCustomer(customer: Customer) {
        customers = customers.append(customer)
    }

    fun addCustomers(customerSequence: Sequence<Customer>) {
        customerSequence.forEach {
            customers = customers.append(it)
        }
    }

    private fun resizeColumns(workbook: HSSFWorkbook) {
        HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook)
        workbook.map {
            sheet -> 0.rangeTo(CUSTOMER_STORE_COLUMN_COUNT).map {
                sheet.autoSizeColumn(it)
            }
        }
    }

    fun search(nameToSearchFor: String): Option<Customer> {
        return customers.find { customer -> customer.name == nameToSearchFor }
    }

    fun nextAccountNumber(): String {
        if (customers.size == 0) return "1"
        val lastAccountNumber = Integer.parseInt(customers.last().accountCode)
        return (lastAccountNumber + 1).toString()
    }

    companion object {
        private val FIRST_DATA_ROW: Int = 4

        fun readFile(filePath: Path, vararg sheetsToGet: Int): CustomerStore {
            if (sheetsToGet.size == 1) {
                val customerStoreWorkbook = getWorkbookFromPath(filePath)
                val customerStoreSheet = customerStoreWorkbook.getSheetAt(0)
                val customers = getCustomersFrom(customerStoreSheet)
                val customerStore = CustomerStore()
                customerStore.addCustomers(customers)
                return customerStore
            }
            throw RuntimeException("Sorry, simultaneously reading in multiple sheets from one file is not yet supported.")
        }

        private fun getCustomersFrom(customerStoreSheet: HSSFSheet): Sequence<Customer> {
            var customers = sequence<Customer>()
            val customerDataRows = FIRST_DATA_ROW.rangeTo(customerStoreSheet.lastRowNum)
            customerDataRows.map {
                val row = customerStoreSheet.getRow(it)
                setStringCellType(row)
                val accountCodeCell = row.getCell(0, CREATE_NULL_AS_BLANK)
                if (accountCodeCell.stringCellValue.isNotEmpty()) {
                    val customer = getSingleCustomerFrom(row)
                    customers = customers.append(customer)
                }
            }
            return customers
        }

        private fun setStringCellType(row: HSSFRow) {
            row.map {
                it.cellType = CELL_TYPE_STRING
            }
        }

        private fun getSingleCustomerFrom(row: Row): Customer {
            val accountCode = row.getCell(0, CREATE_NULL_AS_BLANK).stringCellValue
            val name = row.getCell(1, CREATE_NULL_AS_BLANK).stringCellValue
            val addressOne = row.getCell(2, CREATE_NULL_AS_BLANK).stringCellValue
            val addressTwo = row.getCell(3, CREATE_NULL_AS_BLANK).stringCellValue
            val postcode = row.getCell(4, CREATE_NULL_AS_BLANK).stringCellValue
            val phoneNumber = row.getCell(5, CREATE_NULL_AS_BLANK).stringCellValue
            return Customer(name, addressOne, addressTwo, postcode, phoneNumber, accountCode)
        }

        fun getWorkbookFromPath(filePath: Path): HSSFWorkbook {
            val inputStream = FileInputStream(filePath.toString())
            return HSSFWorkbook(POIFSFileSystem(inputStream))
        }
    }
}
