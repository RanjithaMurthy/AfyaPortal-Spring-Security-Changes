package com.nzion.dto;

import com.afya.portal.domain.model.Tenant;
import com.afya.portal.util.CustomDateSerializer;
import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;

/**
 * Created by Mohan Sharma on 4/1/2015.
 */
@Data
public class PharmacyDto {
    private String pharmacyId;
    private String pharmacyName;
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
    private String pharmacyTenantId;
    private String longitude;
    private String latitude;
    private String address;
    private String additionalAddress;
    private String country;
    private String state;
    private String postalCode;
}
