package com.afya.portal.application;

import com.nzion.dto.PatientDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pradyumna on 12-06-2015.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterPatientCommand {
    private PatientDto patientDto;
    private boolean skipRegistrationNotification = false;   // to optionally skip registration notification through SMS or Email (Kannan - 2015-11-01)
}
