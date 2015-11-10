package com.afya.portal.application;

import lombok.AllArgsConstructor;

/**
 * Created by Mohan Sharma on 7/13/2015.
 */
@AllArgsConstructor
public class PersistUserTenantAssocCommand {
    public String userName;
    public String tenantId;
    public String facilityType;
}
