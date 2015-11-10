package com.afya.portal.domain.model.doctor;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * Created by Nthdimenzion on 6/4/2015.
 */
@Entity
@NoArgsConstructor
@Setter
@Getter
public class Speciality implements ICrudEntity {
    @Id
    private String specialityCode;
    private String description;
}
