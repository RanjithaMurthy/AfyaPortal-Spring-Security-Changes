package com.afya.portal.domain.model.pharmacy;

import com.afya.portal.domain.model.Address;
import com.afya.portal.domain.model.Person;
import com.afya.portal.domain.model.Tenant;
import com.afya.portal.domain.model.patient.Patient;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by Mohan Sharma on 4/1/2015.
 */
@Entity
@Getter
@Setter
public class Pharmacy implements ICrudEntity {
    @Id
    private String pharmacyId;
    private String pharmacyName;
    private String location;
    private String officePhoneNumber;
    private String faxNumber;
    private String serviceTaxNumber;
    private String panNumber;
    @Temporal(TemporalType.DATE)
    private Date validFrom;
    @Temporal(TemporalType.DATE)
    private Date validTo;
    private String adminFirstName;
    private String adminLastName;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "TENANT_ID")
    @Fetch(FetchMode.SELECT)
    private Tenant tenant;
    private String longitude;
    private String latitude;
    @Embedded
    private Address address;
    private String drugLicence;
    private String accrNumber;
    @ManyToMany
    private Set<Patient> patients = Sets.newHashSet();

    public Pharmacy(){}
    private Pharmacy(String pharmacyId, String pharmacyName, String location, String officePhoneNumber, String faxNumber, String serviceTaxNumber,
                       String panNumber, String drugLicence, String accrNumber,  Date validFrom, Date validTo, String adminFirstName, String adminLastName) {
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.location = location;
        this.officePhoneNumber = officePhoneNumber;
        this.faxNumber = faxNumber;
        this.serviceTaxNumber= serviceTaxNumber;
        this.panNumber = panNumber;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.accrNumber = accrNumber;
        this.drugLicence = drugLicence;
    }

    public static Pharmacy CreatePharmacyObject(String pharmacyId, String pharmacyName, String location, String officePhoneNumber, String faxNumber, String serviceTaxNumber,
                                                    String panNumber, String drugLicence, String accrNumber, Date validFrom, Date validTo, String adminFirstName, String adminLastName){
        return new Pharmacy(pharmacyId, pharmacyName, location,officePhoneNumber,faxNumber,serviceTaxNumber,panNumber,drugLicence,accrNumber, validFrom, validTo, adminFirstName,adminLastName);
    }

    @Embedded
    @AttributeOverrides(value = {@AttributeOverride(name = "firstName", column = @Column(name = "key_person_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "key_person_last_name")),
            @AttributeOverride(name = "middleName", column = @Column(name = "key_person_middle_name")),
            @AttributeOverride(name = "email", column = @Column(name = "key_person_email")),
            @AttributeOverride(name = "mobile", column = @Column(name = "key_person_mobile"))})
    private Person keyPerson;
    @Embedded
    private Person adminDetail;




}
