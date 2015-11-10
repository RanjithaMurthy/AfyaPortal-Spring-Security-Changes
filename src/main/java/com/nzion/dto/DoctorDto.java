package com.nzion.dto;

import com.afya.portal.domain.model.Address;
import com.afya.portal.domain.model.DataResource;
import com.afya.portal.domain.model.doctor.Doctor;
import com.afya.portal.domain.model.doctor.DoctorId;
import com.afya.portal.domain.model.doctor.Speciality;
import com.afya.portal.util.UtilValidator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mohan Sharma on 6/4/2015.
 */
@AllArgsConstructor
@Getter
@Setter
public class DoctorDto {
    private String doctorId;
    private String salutation;
    private String firstName;
    private String lastName;
    private String clinicName;
    private String qualification;
    private String address;
    private String additionalAddress;
    private String city;
    private String state;
    private String country;
    private String zip;
    private Set<SpecialityDto> specialities = new HashSet<SpecialityDto>();
    private byte[] profilePicture;
    private long resourceId;
    private String visitingHours;

    public static Doctor setPropertiesFromDoctorDtoToDoctor(DoctorDto doctorDto, Set<Speciality> specialities, String clinicId, SimpleJpaRepository<Doctor, DoctorId> doctorRepository) {
        Doctor doctor = doctorRepository.findOne(new DoctorId(doctorDto.getDoctorId(), clinicId));
        DataResource fsrc = null;
        if(UtilValidator.isEmpty(doctor)) {
            doctor = new Doctor();
            doctor.setDoctorId(new DoctorId(doctorDto.getDoctorId(), clinicId));
        }
        if(UtilValidator.isNotEmpty(doctor.getProfilePicture()))
            fsrc = doctor.getProfilePicture();
        else{
            fsrc = new DataResource();
            fsrc.setResourceId(doctorDto.getResourceId());
        }
        Address address = Address.MakeAddressObject(doctorDto.address, doctorDto.additionalAddress, doctorDto.country, doctorDto.state, doctorDto.city, doctorDto.zip);
        doctor.setSalutation(doctorDto.salutation);
        doctor.setFirstName(doctorDto.firstName);
        doctor.setLastName(doctorDto.lastName);
        doctor.setClinicName(doctorDto.clinicName);
        doctor.setQualification(doctorDto.qualification);
        doctor.setAddress(address);
        doctor.setSpecialities(specialities);
        doctor.setVisitingHours(doctorDto.getVisitingHours());
        if(UtilValidator.isNotEmpty(doctorDto.getProfilePicture())) {
            try {
                fsrc.setResource(new SerialBlob(doctorDto.getProfilePicture()));
                doctor.setProfilePicture(fsrc);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return doctor;
    }

    public class SpecialityDto {
        public String specialityCode;
        public String description;

        public SpecialityDto(String specialityCode, String description) {
            this.specialityCode = specialityCode;
            this.description = description;
        }
    }
}
