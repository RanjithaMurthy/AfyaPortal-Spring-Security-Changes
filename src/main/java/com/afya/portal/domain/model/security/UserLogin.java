package com.afya.portal.domain.model.security;

import com.afya.portal.domain.model.ProviderType;
import com.afya.portal.price.UserPackage;
import com.afya.portal.price.UserPackageService;
import com.google.common.collect.Sets;
import lombok.Data;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.nthdimenzion.common.crud.ICrudEntity;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Created by pradyumna on 10-06-2015.
 */
@Entity(name = "users")
@Data
public class UserLogin implements ICrudEntity {

    @Id
    @Generated(GenerationTime.NEVER)
    private String username;
    private String password;
    @Column(name = "is_verified")
    private Boolean verified;
    @Column
    @Enumerated(EnumType.STRING)
    private UserType userType;
    private String facilityId;
    private String tenantId;
    private String token;
    private String otpToken;
    private ProviderType providerType;
    private String firstName;
    private String middleName;
    private String lastName;
    private String emailId;
    private String mobileNumber;
    private String loginPreference;
    private int availableSMSCount = 0;
    private boolean trail = false;
    // @Temporal(TemporalType.DATE)
    private Date subscriptionDate;
    // @Temporal(TemporalType.DATE)
    private Date expiryDate;
    @Column(columnDefinition="TEXT")
    private String providerAddress1;
    @Column(columnDefinition="TEXT")
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
    private Date createdTxTimestamp;
    private Date lastLoggedInTimestamp;

    @ElementCollection
    //,,
    @CollectionTable(name = "authorities", joinColumns =@JoinColumn(name = "username",nullable = false),
            uniqueConstraints =@UniqueConstraint(columnNames = {"username","authority"}))
    @Column(name="authority")
    private Collection<String> authorities;


    public Boolean isVerified(){
        if(verified == null)
            return Boolean.FALSE;
        return verified;
    }

    public UserLogin() {
    }


    public static String generateOTP()
    {
        Integer randomPIN = (int)(Math.random()*9000)+1000;
        return randomPIN.toString();
    }


    public  void reGenerateOTP()
    {
       this.otpToken = generateOTP();
    }

   @PersistenceConstructor
    public UserLogin(String username, String password, UserType userType, String facilityId, ProviderType providerType) {
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.facilityId = facilityId;
        this.token = UUID.randomUUID().toString();
        this.otpToken = generateOTP();
        this.providerType = providerType;
        authorities = userType.getAuthorities();
        availableSMSCount = 0;
    }

    @OneToMany(mappedBy = "user")
    Set<UserPackage> userPackages = Sets.newHashSet();


    public void addUserGroup(UserPackage userPackages) {
        this.userPackages.add(userPackages);
    }

    @OneToMany(mappedBy = "user")
    Set<UserPackageService> userPackageServices = Sets.newHashSet();

    public void setTrial(boolean trial){
        this.trail = trial;
    }
}
