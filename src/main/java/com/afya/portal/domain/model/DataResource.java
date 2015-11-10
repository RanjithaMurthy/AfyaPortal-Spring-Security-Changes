package com.afya.portal.domain.model;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Entity
@Table(name = "DATA_RESOURCE")
@org.hibernate.annotations.Entity(dynamicInsert = false, dynamicUpdate = false)
@Filters( {
        @Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
@NoArgsConstructor
public class DataResource implements ICrudEntity {

    private static final long serialVersionUID = 1L;

    private long resourceId;

    private Blob resource;

    @Column(name = "BINARY_DATA")
    @Lob
    public Blob getResource() {
        return resource;
    }

    public void setResource(Blob resource) {
        this.resource = resource;
    }

    @Id
    @Column(name = "RESOURCE_ID")
    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

}
