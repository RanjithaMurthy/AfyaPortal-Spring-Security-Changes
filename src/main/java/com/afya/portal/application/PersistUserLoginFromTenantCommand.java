package com.afya.portal.application;

import com.nzion.dto.UserLoginDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Mohan Sharma on 7/14/2015.
 */
@AllArgsConstructor
@Getter
public class PersistUserLoginFromTenantCommand {
    private UserLoginDto userLoginDto;
}
