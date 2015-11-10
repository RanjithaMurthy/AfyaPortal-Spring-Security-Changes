package com.afya.portal.application;

import com.google.common.collect.Sets;
import com.nzion.dto.InsuredPatientDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Created by Mohan Sharma on 4/10/2015.
 */
@NoArgsConstructor
public class PersistPatientInsuranceCommand {
    private String afyaId;
    private Set<InsuredPatientDto> insuredPatientDtos = Sets.newHashSet();

    public String getAfyaId() {
        return afyaId;
    }

    public void setAfyaId(String afyaId) {
        this.afyaId = afyaId;
    }

    public Set<InsuredPatientDto> getInsuredPatientDtos() {
        return insuredPatientDtos;
    }

    public void setInsuredPatientDtos(Set<InsuredPatientDto> insuredPatientDtos) {
        this.insuredPatientDtos = insuredPatientDtos;
    }
}
