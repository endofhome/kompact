package uk.co.endofhome.javoice;

public class Customer {
    private final String name;
    private final String addressOne;
    private final String addressTwo;
    private final String postcode;
    private final String phoneNumber;
    private final String accountCode;

    public Customer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber, String accountCode) {
        this.name = name;
        this.addressOne = addressOne;
        this.addressTwo = addressTwo;
        this.postcode = postcode;
        this.phoneNumber = phoneNumber;
        this.accountCode = accountCode;
    }
}