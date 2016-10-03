package uk.co.endofhome.javoice;

public class Customer {
    public final String name;
    public final String addressOne;
    public final String addressTwo;
    public final String postcode;
    public final String phoneNumber;
    public final String accountCode;

    public Customer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber, String accountCode) {
        this.name = name;
        this.addressOne = addressOne;
        this.addressTwo = addressTwo;
        this.postcode = postcode;
        this.phoneNumber = phoneNumber;
        this.accountCode = accountCode;
    }
}