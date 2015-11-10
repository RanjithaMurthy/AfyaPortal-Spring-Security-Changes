package com.nzion.dto;

import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.domain.model.security.UserType;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nthdimenzion.utils.UtilValidator;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mohan Sharma on 7/14/2015.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLoginDto {
    private String username;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private boolean verified;
    private String mobileNumber;
    private String emailId;
    private String loginPreference;
    private String tenantId;
    private int availableSMSCount;
    private boolean trail;
    private Date subscriptionDate;
    private Date expiryDate;
    private String providerAddress1;
    private String providerAddress2;
    private String providerCity;
    private String providerPostalCode;
    private String providerGovernorate;
    private String providerCountry;
    private String providerRegistrationNo;
    private String providerLicenseNo;
    private String providerRecommendedBy;
    private String payerType;
    private String payerOrganizationName;
    private String payerOfficeNumber;
    private String officeNumber;
    private Set<String> roles = new HashSet<>();
    private String defaultRole;
    private String oldRegUsername;

    public Set<String> getAuthoritiesByRole(){
        Set<String> authorities = Sets.newLinkedHashSet();
        for(String role : this.roles){
            UserType userType = UserType.valueOf(role);
            if(userType != null)
                authorities.add(userType.toString());
        }
        return authorities;
    }

    public UserLogin setPropertiesToEntity(UserLogin userLogin) {
        if(UtilValidator.isNotEmpty(this.getFirstName()))
            userLogin.setFirstName(this.getFirstName());
        if(UtilValidator.isNotEmpty(this.getMiddleName()))
            userLogin.setMiddleName(this.getMiddleName());
        if(UtilValidator.isNotEmpty(this.getLastName()))
            userLogin.setLastName(this.getLastName());
        if(UtilValidator.isNotEmpty(this.getMobileNumber()))
            userLogin.setMobileNumber(this.getMobileNumber());
        if(UtilValidator.isNotEmpty(this.getEmailId()))
            userLogin.setEmailId(this.getEmailId());
        if(UtilValidator.isNotEmpty(this.getLoginPreference()))
            userLogin.setLoginPreference(this.getLoginPreference());
        if(UtilValidator.isNotEmpty(this.getTenantId()))
            userLogin.setTenantId(this.getTenantId());
        if(UtilValidator.isNotEmpty(this.getProviderAddress1()))
            userLogin.setProviderAddress1(this.getProviderAddress1());
        if(UtilValidator.isNotEmpty(this.getProviderAddress2()))
            userLogin.setProviderAddress2(this.getProviderAddress2());
        if(UtilValidator.isNotEmpty(this.getProviderCity()))
            userLogin.setProviderCity(this.getProviderCity());
        if(UtilValidator.isNotEmpty(this.getProviderCountry()))
            userLogin.setProviderCountry(this.getProviderCountry());
        if(UtilValidator.isNotEmpty(this.getProviderGovernorate()))
            userLogin.setProviderGovernorate(this.getProviderGovernorate());
        if(UtilValidator.isNotEmpty(this.getProviderLicenseNo()))
            userLogin.setProviderLicenseNo(this.getProviderLicenseNo());
        if(UtilValidator.isNotEmpty(this.getProviderPostalCode()))
            userLogin.setProviderPostalCode(this.getProviderPostalCode());
        if(UtilValidator.isNotEmpty(this.getProviderRegistrationNo()))
            userLogin.setProviderRegistrationNo(this.getProviderRegistrationNo());
        if(UtilValidator.isNotEmpty(this.getProviderRecommendedBy()))
            userLogin.setProviderRecommendedBy(this.getProviderRecommendedBy());
        if(UtilValidator.isNotEmpty(this.getPayerType()))
            userLogin.setPayerType(this.getPayerType());
        if(UtilValidator.isNotEmpty(this.getPayerOrganizationName()))
            userLogin.setPayerOrganizationName(this.getPayerOrganizationName());
        if(UtilValidator.isNotEmpty(this.getPayerOfficeNumber()))
            userLogin.setPayerOfficeNumber(this.getPayerOfficeNumber());
        if(UtilValidator.isNotEmpty(this.getOfficeNumber()))
            userLogin.setOfficeNumber(this.getOfficeNumber());
        return userLogin;
    }
}
