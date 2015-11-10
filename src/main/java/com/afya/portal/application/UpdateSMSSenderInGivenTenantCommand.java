package com.afya.portal.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Mohan Sharma on 8/24/2015.
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class UpdateSMSSenderInGivenTenantCommand {
    private String tenantId;
    private String smsSenderName;
}
