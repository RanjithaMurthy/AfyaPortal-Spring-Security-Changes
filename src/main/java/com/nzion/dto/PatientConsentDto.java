package com.nzion.dto;

import com.afya.portal.domain.model.LoginPreference;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Created by USER on 13-Aug-15.
 */
@Data
@NoArgsConstructor
public class PatientConsentDto {
    private int consentId;
    private boolean consent;
    public  int getConsentId(){
        return  consentId;
    }
    public  Boolean getConsent(){
        return  consent;
    }
}
