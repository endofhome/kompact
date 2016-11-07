package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.util.DateFormatConverter;

import java.util.Locale;

public class CellStyler {
    public static HSSFCellStyle excelDateCellStyleFor(HSSFWorkbook workbook, HSSFCell dateCell) {
        String excelDatePattern = DateFormatConverter.convert(Locale.getDefault(), "dd/MM/yyyy");
        HSSFCellStyle dateCellStyle = dateCell.getCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        dateCellStyle.setDataFormat(dataFormat.getFormat(excelDatePattern));
        return dateCellStyle;
    }

    public static HSSFCellStyle excelSterlingCellStyleFor(HSSFWorkbook workbook) {
        String excelSterlingPattern = "£* #,##0.00";
        HSSFCellStyle sterlingStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        sterlingStyle.setDataFormat(dataFormat.getFormat(excelSterlingPattern));
        return sterlingStyle;
    }

    public static HSSFCellStyle excelGeneralCellStyleFor(HSSFWorkbook workbook) {
        String excelGeneralStylePattern = "General";
        HSSFCellStyle generalStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        generalStyle.setDataFormat(dataFormat.getFormat(excelGeneralStylePattern));
        return generalStyle;
    }

    public static HSSFCellStyle excelBoldCellStyleFor(HSSFWorkbook workbook) {
        HSSFCellStyle boldStyle = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        boldStyle.setFont(font);
        return boldStyle;
    }

    public static HSSFCellStyle excelBoldBorderBottomCellStyleFor(HSSFWorkbook workbook) {
        HSSFCellStyle boldBottomBorder = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        boldBottomBorder.setFont(font);
        boldBottomBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        return boldBottomBorder;
    }
}