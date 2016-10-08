package uk.co.endofhome.javoice;

import java.util.Objects;

public class ItemLine {
    public final Double quantity;
    public final String description;
    public final Double unitPrice;

    public ItemLine(Double quantity, String description, Double unitPrice) {
        this.quantity = quantity;
        this.description = description;
        this.unitPrice = unitPrice;
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