package com.afya.portal.domain.model.doctor;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by Mohan Sharma on 7/29/2015.
 */
@Embeddable
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class DoctorId implements Serializable{
    private String doctorId;
    private String clinicId;
}
