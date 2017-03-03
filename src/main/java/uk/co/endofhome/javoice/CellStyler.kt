package uk.co.endofhome.javoice

import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFFont
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormat
import org.apache.poi.ss.util.DateFormatConverter

import java.util.Locale

object CellStyler {
    fun excelDateCellStyleFor(workbook: HSSFWorkbook, dateCell: HSSFCell): HSSFCellStyle {
        val excelDatePattern = DateFormatConverter.convert(Locale.getDefault(), "dd/MM/yyyy")
        val dateCellStyle = dateCell.cellStyle
        val dataFormat = workbook.createDataFormat()
        dateCellStyle.dataFormat = dataFormat.getFormat(excelDatePattern)
        return dateCellStyle
    }

    fun excelSterlingCellStyleFor(workbook: HSSFWorkbook): HSSFCellStyle {
        val excelSterlingPattern = "£* #,##0.00"
        val sterlingStyle = workbook.createCellStyle()
        val dataFormat = workbook.createDataFormat()
        sterlingStyle.dataFormat = dataFormat.getFormat(excelSterlingPattern)
        return sterlingStyle
    }

    fun excelGeneralCellStyleFor(workbook: HSSFWorkbook): HSSFCellStyle {
        val excelGeneralStylePattern = "General"
        val generalStyle = workbook.createCellStyle()
        val dataFormat = workbook.createDataFormat()
        generalStyle.dataFormat = dataFormat.getFormat(excelGeneralStylePattern)
        return generalStyle
    }

    fun excelBoldCellStyleFor(workbook: HSSFWorkbook): HSSFCellStyle {
        val boldStyle = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        boldStyle.setFont(font)
        return boldStyle
    }

    fun excelBoldBorderBottomCellStyleFor(workbook: HSSFWorkbook): HSSFCellStyle {
        val boldBottomBorder = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        boldBottomBorder.setFont(font)
        boldBottomBorder.borderBottom = HSSFCellStyle.BORDER_THIN
        return boldBottomBorder
    }
}