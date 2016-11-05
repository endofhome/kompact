package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.util.DateFormatConverter;

import java.util.Locale;

public class CellStyler {
    public static HSSFCellStyle excelDateCellStyleFor(HSSFWorkbook workbook) {
        String excelDatePattern = DateFormatConverter.convert(Locale.getDefault(), "dd/MM/yyyy");
        HSSFCellStyle dateCellStyle = workbook.createCellStyle();
        DataFormat dateFormat = workbook.createDataFormat();
        dateCellStyle.setDataFormat(dateFormat.getFormat(excelDatePattern));
        return dateCellStyle;
    }

    public static HSSFCellStyle excelSterlingCellStyleFor(HSSFWorkbook workbook) {
        String excelSterlingPattern = "£* #,##0.00";
        HSSFCellStyle sterlingStyle = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        sterlingStyle.setDataFormat(dataFormat.getFormat(excelSterlingPattern));
        return sterlingStyle;
    }
}
