package com.afya.portal.application;

import com.nzion.dto.PatientDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Mohan Sharma on 8/17/2015.
 */
@AllArgsConstructor
@Getter
public class UpdateMemberDetailsCommand {
    private PatientDto patientDto;
}
