package com.afya.portal.domain.model.lab;

import com.afya.portal.domain.model.Address;
import com.afya.portal.domain.model.Person;
import com.afya.portal.domain.model.Tenant;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Mohan Sharma on 4/1/2015.
 */
@Data
@Entity
public class Laboratory implements ICrudEntity{
    @Id
    private String labId;
    private String labName;
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

    public Laboratory(){}
    private Laboratory(String labId, String labName, String location, String officePhoneNumber, String faxNumber, String serviceTaxNumber,
                   String panNumber, String drugLicence, String accrNumber, Date validFrom, Date validTo, String adminFirstName, String adminLastName) {
        this.labId = labId;
        this.labName = labName;
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

    public static Laboratory CreateLaboratoryObject(String labId, String labName, String location, String officePhoneNumber, String faxNumber, String serviceTaxNumber,
                                            String panNumber, String drugLicence, String accrNumber, Date validFrom, Date validTo, String adminFirstName, String adminLastName){
        return new Laboratory(labId, labName, location,officePhoneNumber,faxNumber,serviceTaxNumber,panNumber, drugLicence, accrNumber, validFrom, validTo, adminFirstName,adminLastName);
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
