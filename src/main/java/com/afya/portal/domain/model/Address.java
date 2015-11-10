package com.afya.portal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Embeddable;

/**
 * Created by Mohan Sharma on 4/1/2015.
 */
@Embeddable
public class Address {
    private String address;
    private String additionalAddress;
    private String country;
    private String state;
    private String city;
    private String postalCode;

    public Address(){}
    private Address(String address, String additionalAddress, String country, String state, String city, String postalCode) {
        this.address = address;
        this.additionalAddress = additionalAddress;
        this.country = country;
        this.state = state;
        this.city = city;
        this.postalCode = postalCode;
    }

    public static Address MakeAddressObject(String address, String additionalAddress, String country, String state, String city, String postalCode) {
        return new Address(address, additionalAddress, country, state, city, postalCode);
    }

}
