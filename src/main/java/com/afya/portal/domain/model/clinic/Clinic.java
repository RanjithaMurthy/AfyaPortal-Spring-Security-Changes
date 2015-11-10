package com.afya.portal.domain.model.clinic;

import com.afya.portal.domain.model.Address;
import com.afya.portal.domain.model.Person;
import com.afya.portal.domain.model.Tenant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "clinicId")
public class Clinic extends AbstractAnnotatedAggregateRoot implements ICrudEntity{
    @Id
    private String clinicId;
    private String clinicName;
    private String location;
    private String officePhoneNumber;
    private String faxNumber;
    private String serviceTaxNumber;
    private String panNumber;
    private String drugLicence;
    private String accrNumber;
    @Temporal(TemporalType.DATE)
    private Date validFrom;
    @Temporal(TemporalType.DATE)
    private Date validTo;
    @Embedded
    Address address;
    private String adminFirstName;
    private String adminLastName;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "TENANT_ID")
    private Tenant tenant;
    private String longitude;
    private String latitude;

    public Clinic(){}

    private Clinic(String clinicId, String clinicName, String location, String officePhoneNumber, String faxNumber, String serviceTaxNumber,
                   String panNumber, String drugLicence, String accrNumber, Date validFrom, Date validTo,String adminFirstName, String adminLastName) {
        this.clinicId = clinicId;
        this.clinicName = clinicName;
        this.location = location;
        this.officePhoneNumber = officePhoneNumber;
        this.faxNumber = faxNumber;
        this.serviceTaxNumber= serviceTaxNumber;
        this.panNumber = panNumber;
        this.drugLicence = drugLicence;
        this.accrNumber = accrNumber;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
    }

    public static Clinic createClinic(String clinicId, String clinicName, String location, String officePhoneNumber, String faxNumber, String serviceTaxNumber,
                                            String panNumber,String drugLicence, String accrNumber, Date validFrom, Date validTo, String adminFirstName, String adminLastName){
        return new Clinic(clinicId, clinicName, location,officePhoneNumber,faxNumber,serviceTaxNumber,panNumber,drugLicence,accrNumber,validFrom,validTo,adminFirstName,adminLastName);
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
    private String imageUrl;
    private String logoWithAddress;
}
