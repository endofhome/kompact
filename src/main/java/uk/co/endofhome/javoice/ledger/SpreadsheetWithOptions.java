package uk.co.endofhome.javoice.ledger;

import com.googlecode.totallylazy.Option;
import org.apache.poi.hssf.usermodel.HSSFCell;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.option;

public interface SpreadsheetWithOptions {

    default Option<String> getStringCellValueFor(HSSFCell cell) {
        Option<String> optionString;
        if (cell != null) {
            optionString = option(cell.getStringCellValue());
        } else {
            optionString = none();
        }
        return optionString;
    }

    default Option<Double> getNumericCellValueFor(HSSFCell cell) {
        Option<Double> optionDouble;
        if (cell != null) {
            optionDouble = option(cell.getNumericCellValue());
        } else {
            optionDouble = none();
        }
        return optionDouble;
    }

    default Option<LocalDate> getDateCellValueFor(HSSFCell cell) {
        Option<LocalDate> optionLocalDate;
        if (cell != null && cell.getCellType() == 0) {
            optionLocalDate = option(cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            optionLocalDate = none();
        } return optionLocalDate;
    }
}
