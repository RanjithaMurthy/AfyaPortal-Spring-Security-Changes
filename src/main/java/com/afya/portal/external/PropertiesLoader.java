package com.afya.portal.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Mohan Sharma on 3/13/2015.
 */
@Configuration
public class PropertiesLoader {

    @Value("${jdbc.url}")
    private String DATABASE_URL;
    @Value("${jdbc.username}")
    private String USERNAME;
    @Value("${jdbc.password}")
    private String PASSWORD;
    @Value("${clinicHost}")
    private String hostForClinic;
    @Value("${pharmacyHost}")
    private String hostForPharmacy;
    @Value("${labHost}")
    private String hostForLab;
    @Value("${portalHost}")
    private String portalHost;
    @Value("${portalDatabaseName}")
    private String portalDatabaseName;

    @Value("${pharmacyTenantDatabase}")
    private String pharmacyTenantDatabase;

    public String getDatabaseUrl(){
        return DATABASE_URL;
    }
    public String getDatabaseUsername(){
        return USERNAME;
    }
    public String getDatabasePassword(){
        return PASSWORD;
    }
    public String getPortalDatabaseName(){
        return portalDatabaseName;
    }

    public String getHostForClinic() {
        return hostForClinic;
    }

    public String getHostForPharmacy() {
        return hostForPharmacy;
    }

    public String getHostForLab() {
        return hostForLab;
    }
    public String getHostForPortal() {
        return portalHost;
    }

    public String getPharmacyTenantDatabase() {
        if (pharmacyTenantDatabase == null)
            return "afya_pharmacy_tenant";
        return pharmacyTenantDatabase;
    }

}
