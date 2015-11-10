package com.afya.portal.domain.model.patient;

import com.afya.portal.domain.model.clinic.Clinic;
import com.afya.portal.domain.model.pharmacy.Pharmacy;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.nzion.dto.PatientDto;
import lombok.Getter;
import org.nthdimenzion.common.crud.ICrudEntity;
import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by Mohan Sharma on 3/27/2015.
 */
@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"first_name", "last_name", "date_of_birth"}))
public class Patient implements ICrudEntity{
    @Id
    @Column(name = "AFYA_ID")
    public String afyaId;
    @Column(name = "CIVIL_ID")
    private String civilId;
    @Column(name = "SALUTATION")
    private String salutation;
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "MIDDLE_NAME")
    private String middleName;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "END_MOST_NAME")
    private String endMostName;
    @Column(name = "NATIONALITY")
    private String nationality;
    @Column(name = "GENDER")
    private String gender;
    @Column(name = "RELIGION")
    private String religion;
    @Column(name = "DATE_OF_BIRTH")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;
    @Column(name = "AGE")
    private String age;
    @Column(name = "MARITAL_STATUS")
    private String maritalStatus;
    @Column(name = "OCCUPATION")
    private String occupation;
    @Column(name = "EMPLOYMENT_STATUS")
    private String employmentStatus;
    @Column(name = "PREFERRED_LANGUAGE")
    private String preferredLanguage;
    @Column(name = "BLOOD_GROUP")
    private String bloodGroup;
    @Column(name = "RH")
    private String rh;
    @Column(name = "EMAIL_ID")
    private String emailId;
    @Column(name = "COMMUNICATION_PREFERENCE")
    private String communicationPreference;
    @Column(name = "ISD_CODE")
    private String isdCode;
    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;
    @Column(name = "FAX_NUMBER")
    private String faxNumber;
    @Column(name = "OFFICE_PHONE")
    private String officePhone;
    @Column(name = "HOME_PHONE")
    private String homePhone;
    @Column(name = "PATIENT_TYPE")
    private String patientType;
    @Column(name = "ADDRESS")
    private String address;
    @Column(name = "ADDITIONAL_ADDRESS")
    private String additionalAddress;
    @Column(name = "CITY")
    private String city;
    @Column(name = "POSTAL_CODE")
    private String postalCode;
    @Column(name = "STATE")
    private String state;
    @Column(name = "COUNTRY")
    private String country;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "PATIENT_CLINIC_ASSOC", joinColumns = {@JoinColumn(name = "afyaId")}, inverseJoinColumns = {@JoinColumn(name = "clinicId")})
    private Set<Clinic> clinics = Sets.newHashSet();
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "PATIENT_PHARMACY_ASSOC", joinColumns = {@JoinColumn(name = "afyaId")}, inverseJoinColumns = {@JoinColumn(name = "pharmacyId")})
    private Set<Pharmacy> pharmacies = Sets.newHashSet();

    public void setPropertiesToPatientEntity(PatientDto patientDto) {
        Preconditions.checkArgument(patientDto.getAfyaId() != null, "Afya Id cannot be empty.");
        this.afyaId = patientDto.getAfyaId();
        if(patientDto.getCivilId() != null)
            this.civilId = patientDto.getCivilId();
        if(patientDto.getFirstName() != null)
            this.firstName = patientDto.getFirstName();
        if(patientDto.getMiddleName() != null)
            this.middleName = patientDto.getMiddleName();
        if(patientDto.getLastName() != null)
            this.lastName = patientDto.getLastName();
        if(patientDto.getEndMostName() != null)
            this.endMostName = patientDto.getEndMostName();
        if(patientDto.getGender() != null)
            this.gender = patientDto.getGender();
        if(patientDto.getReligion() != null)
            this.religion = patientDto.getReligion();
        if(patientDto.getDateOfBirth() != null)
            this.dateOfBirth = patientDto.getDateOfBirth();
        if(patientDto.getAge() != null)
            this.age = patientDto.getAge();
        if(patientDto.getMaritalStatus() != null)
            this.maritalStatus = patientDto.getMaritalStatus();
        if(patientDto.getEmailId() != null)
            this.emailId = patientDto.getEmailId();
        if(patientDto.getCommunicationPreference() != null)
            this.communicationPreference = patientDto.getCommunicationPreference();
        if(patientDto.getIsdCode() != null)
            this.isdCode = patientDto.getIsdCode();
        if(patientDto.getMobileNumber() != null)
            this.mobileNumber = patientDto.getMobileNumber();
        if(patientDto.getFaxNumber() != null)
            this.faxNumber = patientDto.getFaxNumber();
        if(patientDto.getOfficePhone() != null)
            this.officePhone = patientDto.getOfficePhone();
        if(patientDto.getPatientType() != null)
            this.patientType = patientDto.getPatientType();
        if(patientDto.getAddress() != null)
            this.address = patientDto.getAddress();
        if(patientDto.getAdditionalAddress() != null)
            this.additionalAddress = patientDto.getAdditionalAddress();
        if(patientDto.getCity() != null)
            this.city = patientDto.getCity();
        if(patientDto.getPostalCode() != null)
            this.postalCode = patientDto.getPostalCode();
        if(patientDto.getState() != null)
            this.state = patientDto.getState();
        if(patientDto.getCountry() != null)
            this.country = patientDto.getCountry();
        if(patientDto.getNationality() != null)
            this.nationality = patientDto.getNationality();
        if(patientDto.getHomePhone() != null)
            this.homePhone = patientDto.getHomePhone();
        if(patientDto.getBloodGroup() != null)
            this.bloodGroup = patientDto.getBloodGroup();
        if(patientDto.getRh() != null)
            this.rh = patientDto.getRh();
        if(patientDto.getSalutation() != null)
            this.salutation = patientDto.getSalutation();
    }

    public void addClinics(Clinic clinics) {
        this.clinics.add(clinics);
    }

    public void addPharmacy(Pharmacy pharmacy){
        pharmacies.add(pharmacy);
    }

    public void setPharmacies(Set<Pharmacy> pharmacies) {
        this.pharmacies = pharmacies;
    }
}
