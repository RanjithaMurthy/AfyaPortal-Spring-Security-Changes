package com.nzion.dto;

import com.afya.portal.domain.model.Address;
import com.afya.portal.domain.model.Tenant;
import com.afya.portal.util.CustomDateSerializer;
import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Mohan Sharma on 4/1/2015.
 */
@Data
public class ClinicDto {
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
    private String facilityType;
    private String imageUrl;
    private String isLogoWithAddress;
    private String providerMemberType;
}
