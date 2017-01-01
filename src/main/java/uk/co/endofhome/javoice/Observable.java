package uk.co.endofhome.javoice;

import java.io.IOException;

public interface Observable {
    void registerObserver(Observer observer);
    void newCustomer(String name, String addressOne, String addressTwo, String postcode, String phoneNumber) throws IOException;
    void searchForCustomer(String name) throws Exception;
}
