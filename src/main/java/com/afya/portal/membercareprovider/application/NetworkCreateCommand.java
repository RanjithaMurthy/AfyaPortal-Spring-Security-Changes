package com.afya.portal.membercareprovider.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/4/15
 * Time: 8:30 AM
 * To change this template use File | Settings | File Templates.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetworkCreateCommand {
    private String fromClinicId;

    private String networkId;
    private String networkStatus;
    private String clinicId;
    private String clinicName;
    private String location;
    private String officePhoneNumber;
    private String faxNumber;
    private String serviceTaxNumber;
    private String panNumber;
    private String drugLicence;
    private String accrNumber;
    private Date validFrom;
    private Date validTo;
    private String adminFirstName;
    private String adminLastName;
    private String tenantId;
    private String longitude;
    private String latitude;
    private String address;
    private String additionalAddress;
    private String country;
    private String state;
    private String postalCode;
    private String email;


}
