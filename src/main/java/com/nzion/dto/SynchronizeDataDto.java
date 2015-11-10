package com.nzion.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 5/5/2015.
 */
@Data
public class SynchronizeDataDto {
    private String tablename;
    private List<Map<String, Object>> newRows;
    private List<Map<String, Object>> modifiedRows;
}
