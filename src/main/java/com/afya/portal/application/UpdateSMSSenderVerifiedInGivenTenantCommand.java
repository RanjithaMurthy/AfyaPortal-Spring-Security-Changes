package com.afya.portal.application;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by Kannan on 09/09/2015.
 */
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class UpdateSMSSenderVerifiedInGivenTenantCommand {
    private String tenantId;
}
