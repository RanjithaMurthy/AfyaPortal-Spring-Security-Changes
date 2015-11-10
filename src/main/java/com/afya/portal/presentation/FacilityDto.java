package com.afya.portal.presentation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by pradyumna on 11-06-2015.
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class FacilityDto {

    private String facilityId;
    private String facilityName;
    private String facilityType;
    private String clinicId;
}
