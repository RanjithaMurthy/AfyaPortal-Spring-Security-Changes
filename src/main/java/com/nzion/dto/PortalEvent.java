package com.nzion.dto;

import java.io.Serializable;

/**
 * Created by Mohan Sharma on 3/30/2015.
 */
public class PortalEvent implements Serializable{

    String sourceTenantId;
    String destinatationTenantId;
    protected  PortalEvent(String sourceTenantId){
    }
}
