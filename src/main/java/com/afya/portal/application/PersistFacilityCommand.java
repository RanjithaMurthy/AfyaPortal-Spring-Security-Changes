package com.afya.portal.application;

import com.afya.portal.domain.model.LoginPreference;
import com.afya.portal.domain.model.ProviderType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */
@Data
public class PersistFacilityCommand {
    public ProviderType facilityType;
    public String facilityName;
    public String location;
    public String username;
    public String password;
    public String officePhoneNumber;
    public String faxNumber;
    public String serviceTaxNumber;
    public String panNumber;
    public String drugLicence;
    public String accrNumber;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date validFrom = new Date();
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date validTo = new Date();
    public String address;
    public String addressAdditional;
    public String selectedCity;
    public String selectedCountry;
    public String selectedState;
    public String postalCode;
    public String firstName;
    public String middleName;
    public String lastName;
    public String confirmPassword;
    private LoginPreference loginPreference;
    @NotNull
    private String email;
    @NotNull
    private String mobile;
    private String tenantId;
    private String providerRegistrationNo;
    private String oldRegUsername;
    private String smsSenderId;
}
