package uk.co.endofhome.javoice;

import java.io.IOException;

public interface Observer {
    void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) throws IOException;
}
