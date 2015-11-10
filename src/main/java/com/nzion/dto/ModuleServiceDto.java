package com.nzion.dto;

import lombok.Data;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 4/22/2015.
 */
@Data
@JsonSerialize
public class ModuleServiceDto implements Serializable{
    private BigDecimal totalCopayAmount;
    private BigDecimal totalDeductableAmount;
    private BigDecimal totalAuthorizationAmount;
    private List<Map<String, Object>> serviceDetails;
    private Map<String, Object> moduleDetails;
}
