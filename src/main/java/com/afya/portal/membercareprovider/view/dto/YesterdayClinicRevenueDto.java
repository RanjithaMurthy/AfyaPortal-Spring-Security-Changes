package com.afya.portal.membercareprovider.view.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/1/15
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class YesterdayClinicRevenueDto {

    private String clinicRevenue;
    private String smartServicesRevenue;
    private String totalRevenue;
    private String providerType;

}
