package com.afya.portal.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenerationTime;
import org.nthdimenzion.common.crud.ICrudEntity;



import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */
@Entity
@NoArgsConstructor
@Getter(AccessLevel.PACKAGE)
@Setter
public class UserTenantAssoc implements ICrudEntity {
    @Id
    @org.hibernate.annotations.Generated(GenerationTime.NEVER)
    private String userName;
    private String tenantId;
    private String facilityType;

    public  UserTenantAssoc(String tenantId, String userName,String facilityType){
        this.tenantId = tenantId;
        this.userName = userName;
        this.facilityType = facilityType;
    }

}
