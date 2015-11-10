package com.afya.portal.application;

import lombok.Data;

/**
 * Created by Sumit on 10/21/2015.
 */
@Data
public class PersistPracticeCommand {
    private String practiceName;
    private String accrNumber;
    private String contactPersonName;
    private String alternatePhone;
    private String email;
    private String corporateEmail;
    private String faxNumber;
    private String homePhone;
    private String mobileNumber;
    private String officeExt;
    private String officePhone;
    private String pagerNumber;
    private String address1;
    private String address2;
    private String attnName;
    private String city;
    private String countryGeo;
    private String postalCode;
    private String postalCodeExt;
    private String stateProvinceGeo;
    private String drugLicence;
    private String federalTaxId;
    private String logoUrl;
    private String npiNumber;
    private String panNumber;
    private String serviceTaxNumber;
    private String subscriptionType;
    private String isdCode;
    private String imageUrl;
    private String tenantId;

    public PersistPracticeCommand(PersistFacilityCommand cmd){
        this.practiceName = cmd.getFacilityName();
        this.accrNumber = cmd.getAccrNumber();
        this.contactPersonName = cmd.getFirstName();
        this.alternatePhone = null;
        this.email = cmd.getEmail();
        this.corporateEmail = null;
        this.faxNumber = cmd.getFaxNumber();
        this.homePhone = null;
        this.mobileNumber = cmd.getMobile();
        this.officeExt = null;
        this.officePhone = cmd.getOfficePhoneNumber();
        this.pagerNumber = null;
        this.address1 = cmd.getAddress();
        this.address2 = cmd.getAddressAdditional();
        this.attnName = null;
        this.city = cmd.getSelectedCity();
        this.countryGeo = null;
        this.postalCode = cmd.getPostalCode();
        this.postalCodeExt = null;
        this.stateProvinceGeo = null;
        this.drugLicence = cmd.getDrugLicence();
        this.federalTaxId = null;
        this.logoUrl = null;
        this.npiNumber = null;
        this.panNumber = cmd.getPanNumber();
        this.serviceTaxNumber = cmd.getServiceTaxNumber();
        this.subscriptionType = null;
        this.isdCode = null;
        this.imageUrl = null;
        this.tenantId = cmd.getTenantId();
    }
}


