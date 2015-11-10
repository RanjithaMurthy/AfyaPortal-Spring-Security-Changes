package com.afya.portal.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class Tenant implements ICrudEntity{
    @Id
    private String tenantId;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdTxTimestamp;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updatedTxTimestamp;
    @Embedded
    TenantCustomizationDetails tenantCustomizationDetails;
    private boolean isEnabled;
    private String tenantName;
    @Column(nullable = false, columnDefinition ="int default 0")
    private Integer sentSmsCount=0;
    private String adminUsername;
    private String smsSenderName;
    private boolean smsSenderNameVerified;

    private Tenant(String tenantId, DateTime createdTxTimestamp, DateTime updatedTxTimestamp, TenantCustomizationDetails tenantConfig){
        this.tenantId = tenantId;
        this.createdTxTimestamp = createdTxTimestamp;
        this.updatedTxTimestamp = updatedTxTimestamp;
        this.tenantCustomizationDetails = tenantConfig;
    }

    public static Tenant MakeTenant(String tenantId, String databaseUsername, String databasePassword) {
        String connectionUrl = "jdbc:mysql://127.0.0.1/"+tenantId+"?rewriteBatchedStatements=true&amp;createDatabaseIfNotExist=true&amp;characterEncoding=UTF-8&amp;characterSetResults=UTF-8";
        TenantCustomizationDetails tenantConfig = TenantCustomizationDetails.MakeTenantConfig(connectionUrl, databaseUsername, databasePassword);
        return new Tenant(tenantId, new DateTime(),null, tenantConfig);
    }
}
