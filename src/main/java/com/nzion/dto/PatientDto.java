package com.nzion.dto;

import com.afya.portal.domain.model.LoginPreference;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mohan Sharma on 3/25/2015.
 */
@Data
@NoArgsConstructor
public class PatientDto{

    private String civilId;
    private String afyaId;
    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private String endMostName;
    private String nationality;
    private String gender;
    private String religion;
    private Date dateOfBirth;
    private String age;
    private String maritalStatus;
    private String bloodGroup;
    private String rh;
    private String emailId;
    private String communicationPreference;
    private String isdCode;
    private String mobileNumber;
    private String faxNumber;
    private String homePhone;
    private String officePhone;
    private String patientType;
    private String address;
    private String additionalAddress;
    private String city;
    private String postalCode;
    private String state;
    private String country;

    //The below fields are used to sign up the Patient.
    private LoginPreference loginPreference;
    private String password;
    private String confirmPassword;
    /*public PatientDto(String tenantId) {
        super(tenantId);
    }*/
    private String oldRegUsername;
}
