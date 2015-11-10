package com.afya.portal.membercareprovider.view.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Created by User on 8/26/2015.
 */


@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ReferalDataDto {

    private String referralSource;
    private String totalRefAmountPayable;
    private String totalRefAmountTobePaid;

}
