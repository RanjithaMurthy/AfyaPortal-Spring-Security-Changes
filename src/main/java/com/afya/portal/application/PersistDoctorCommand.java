package com.afya.portal.application;

import com.nzion.dto.DoctorDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Mohan Sharma on 6/4/2015.
 */
@AllArgsConstructor
@Getter
public class PersistDoctorCommand {
    private String clinicId;
    private DoctorDto doctorDto;
}
