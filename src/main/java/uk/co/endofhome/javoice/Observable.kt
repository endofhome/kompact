package uk.co.endofhome.javoice

import java.io.IOException

interface Observable {
    fun registerObserver(observer: Observer)
    @Throws(IOException::class)
    fun newCustomer(name: String, addressOne: String, addressTwo: String, postcode: String, phoneNumber: String)

    @Throws(Exception::class)
    fun searchForCustomer(name: String)
}
