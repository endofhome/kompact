package uk.co.endofhome.javoice;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.util.DateFormatConverter;

import java.util.Locale;

public class DateCellFormat {
    public static HSSFCellStyle excelDateCellStyleFor(HSSFWorkbook workbook) {
        String excelFormatPattern = DateFormatConverter.convert(Locale.getDefault(), "dd/MM/yyyy");
        HSSFCellStyle dateCellStyle = workbook.createCellStyle();
        DataFormat dateFormat = workbook.createDataFormat();
        dateCellStyle.setDataFormat(dateFormat.getFormat(excelFormatPattern));
        return dateCellStyle;
    }
}
