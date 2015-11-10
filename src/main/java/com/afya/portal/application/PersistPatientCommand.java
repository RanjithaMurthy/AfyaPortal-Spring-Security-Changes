package com.afya.portal.application;

import com.afya.portal.domain.model.ProviderType;
import com.nzion.dto.PatientDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Mohan Sharma on 3/31/2015.
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class PersistPatientCommand {
    private PatientDto patientDto;
    private String tenantId;
    private ProviderType facilityType;
}
