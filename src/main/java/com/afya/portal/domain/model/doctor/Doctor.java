package com.afya.portal.domain.model.doctor;

import com.afya.portal.domain.model.Address;
import com.afya.portal.domain.model.DataResource;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Created by Mohan Sharma on 6/4/2015.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "doctorId")
public class Doctor implements ICrudEntity {
    @EmbeddedId
    private DoctorId doctorId;
    private String salutation;
    private String firstName;
    private String lastName;
    private String clinicName;
    private String qualification;
    private Address address;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "DOCTOR_SPECIALITY_ASSOC", joinColumns = {@JoinColumn(name = "clinicId"), @JoinColumn(name = "doctorId")}, inverseJoinColumns = {@JoinColumn(name = "specialityCode")},  uniqueConstraints = {@UniqueConstraint(columnNames={"doctorId", "clinicId"})})
    private Set<Speciality> specialities = Sets.newHashSet();
    @OneToOne(fetch = FetchType.LAZY, targetEntity = DataResource.class)
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL })
    @JoinColumn(name = "PROFILE_PICTURE")
    private DataResource profilePicture;
    private String visitingHours;
}
