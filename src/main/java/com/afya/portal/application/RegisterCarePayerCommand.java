package com.afya.portal.application;

import com.nzion.dto.UserLoginDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by ranjitha on 9/2/2015.
 */
@AllArgsConstructor
@Getter
public class RegisterCarePayerCommand {
    private UserLoginDto userLoginDto;
}
