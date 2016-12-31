package uk.co.endofhome.javoice.ui;

public class FakeCustomer {
    final String accountCode;
    public final String name;
    final String addressOne;
    final String addressTwo;
    final String phoneNumber;
    final String postcode;

    public FakeCustomer() {
        accountCode = "50";
        name = "Bob";
        addressOne = "10 Littlehaven Lane";
        addressTwo = "Horsham";
        postcode = "RH12 ???";
        phoneNumber = "01403 034552";
    }
}
