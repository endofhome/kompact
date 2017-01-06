package uk.co.endofhome.javoice.invoice;

import com.googlecode.totallylazy.Option;

import java.util.Objects;

import static com.googlecode.totallylazy.Option.option;

public class ItemLine {
    public final Option<Double> quantity;
    public final Option<String> description;
    public final Option<Double> unitPrice;

    public ItemLine(Option<Double> quantity, Option<String> description, Option<Double> unitPrice) {
        this.quantity = quantity;
        this.description = description;
        this.unitPrice = unitPrice;
    }

    public static ItemLine itemLine(double quantity, String description, double unitPrice) {
        return new ItemLine(option(quantity), option(description), option(unitPrice));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ItemLine)) {
            return false;
        }
        ItemLine itemLine = (ItemLine) o;
        return Objects.equals(quantity, itemLine.quantity) &&
                Objects.equals(description, itemLine.description) &&
                Objects.equals(unitPrice, itemLine.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, description, unitPrice);
    }
}