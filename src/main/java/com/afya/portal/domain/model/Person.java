package com.afya.portal.domain.model;

import lombok.Getter;

import javax.persistence.Embeddable;

/**
 * Created by pradyumna on 12-06-2015.
 */
@Embeddable
@Getter
final public class Person {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobile;

    public Person(String firstName, String middleName, String lastName, String email, String mobile) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
    }

    public Person() {
    }
}
