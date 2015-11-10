package com.afya.portal.application;

import lombok.Getter;

/**
 * Created by Mohan Sharma on 4/11/2015.
 */
@Getter
public class PersistCorporatePatientCommand {
    private String afyaId;
    private String corporateId;
    private String corporatePlanId;
    private String corporateName;
    private String corporatePlanName;
    private String contactName;
    private String landline;
    private String employeeId;
    private String employeeRole;
}
