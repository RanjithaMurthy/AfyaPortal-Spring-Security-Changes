package com.afya.portal.query;

import com.afya.portal.util.UtilValidator;
import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 8/15/2015.
 */
@Component
@Finder
public class PortalTenantFinder {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String QUERY_TO_FETCH_SMS_SENDER_NAME_FOR_GIVEN_TENANT = "SELECT sms_sender_name, sms_sender_name_verified FROM tenant WHERE tenant_id=:tenantId";
    private static final String QUERY_TO_CHECK_IF_TENANT_SUBSCRIBED_TO_JOIN_IN_PACKAGE = "SELECT activated AS result FROM `user_package` WHERE package_id=1 AND username = (SELECT admin_username FROM `tenant` WHERE tenant_id=:tenantId)";

    @Autowired
    public PortalTenantFinder(@Qualifier("primaryDataSource") DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Map<String, Object> getSMSSenderNameForGivenTenant(String tenantId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_SMS_SENDER_NAME_FOR_GIVEN_TENANT, new MapSqlParameterSource("tenantId", tenantId));
        if(UtilValidator.isNotEmpty(result)){
            return result.get(0);
        }
        return Collections.EMPTY_MAP;
    }

    public Map<String, Object> checkIfTenantIsSubscribedToJoinInPackage(String tenantId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(QUERY_TO_CHECK_IF_TENANT_SUBSCRIBED_TO_JOIN_IN_PACKAGE, new MapSqlParameterSource("tenantId", tenantId));
        if(UtilValidator.isNotEmpty(result)){
            Map<String, Object> resultMap = result.get(0);
            if(resultMap.get("result").equals(1)){
                resultMap.put("result", Boolean.TRUE);
            } else{
                resultMap.put("result", Boolean.FALSE);
            }
            return resultMap;
        }
        return Collections.EMPTY_MAP;
    }
}
