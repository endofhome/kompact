package uk.co.endofhome.javoice.gui;

import uk.co.endofhome.javoice.customer.Customer;

public class FakeCustomer extends Customer {
        public FakeCustomer() {
            super("Bob",
                    "10 Littlehaven Lane",
                    "Horsham",
                    "RH12 ???",
                    "01403 034552",
                    "50"
            );
    }
}
