package com.afya.portal.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

/**
 * Created by Mohan Sharma on 3/13/2015.
 */
@Embeddable
@NoArgsConstructor
@Getter(AccessLevel.PACKAGE)
@Setter
public class TenantCustomizationDetails {
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;

    private TenantCustomizationDetails(String jdbcUrl, String jdbcUsername, String jdbcPassword){
        this.jdbcUrl = jdbcUrl;
        this.jdbcUsername = jdbcUsername;
        this.jdbcPassword = jdbcPassword;
    }
    public static TenantCustomizationDetails MakeTenantConfig(String databaseUrl, String databaseUsername, String databasePassword) {
        return new TenantCustomizationDetails(databaseUrl, databaseUsername, databasePassword);
    }
}
