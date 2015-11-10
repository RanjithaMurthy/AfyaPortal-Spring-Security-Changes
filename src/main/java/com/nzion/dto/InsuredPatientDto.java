package com.nzion.dto;

import lombok.Data;

/**
 * Created by Mohan Sharma on 4/10/2015.
 */
@Data
public class InsuredPatientDto {

    private String insuranceProviderId;
    private String insurancePlanId;
    private String insuranceName;
    private String planName;
    private String patientPlanId;
    private String patientRegistrationNo;

}
