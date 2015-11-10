package com.afya.portal.application;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Mohan Sharma on 8/24/2015.
 */
@AllArgsConstructor
@Getter
public class UpdateSmsCountTriggeredFromTenantCommand {
    private String tenantId;
}
