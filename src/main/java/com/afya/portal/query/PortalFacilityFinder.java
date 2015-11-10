package com.afya.portal.query;

import com.afya.portal.util.UtilValidator;
import com.google.common.collect.Sets;
import com.nzion.dto.UserLoginDto;
import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.*;

/**
 * Created by Mohan Sharma on 7/13/2015.
 */
@Component
@Finder
public class PortalFacilityFinder {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final static String QUERY_TO_FETCH_USER_LOGIN_DETAILS_BY_USERNAME = "SELECT u.username, u.password, u.enabled FROM users u WHERE u.username=:userName";
    private final static String QUERY_TO_FETCH_AUTHORITIES_BY_USERNAME_AND_ROLE = "SELECT * FROM authorities WHERE username=:username ORDER BY authority";
    private final static String QUERY_TO_FIND_SMS_CONSUMED_COUNT = "SELECT (IFNULL(u.availablesmscount, 0)-IFNULL(t.sent_sms_count,0)) AS availableSmsCount FROM users u JOIN tenant t\n" +
                                                                    "ON u.username= t.admin_username AND t.tenant_id=:tenantId";

    @Autowired
    public PortalFacilityFinder(@Qualifier("primaryDataSource") DataSource dataSource){
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Map<String, Object> getUserLoginDetailsForUserName(String userName) {
        List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_USER_LOGIN_DETAILS_BY_USERNAME, new MapSqlParameterSource("userName", userName));
        if(users.size() > 0)
            return users.get(0);
        return Collections.emptyMap();
    }

    public Set<String> findIfAuthorizationAlreadyExistForUser(UserLoginDto userLoginDto) {
        List<Map<String, Object>> users = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_AUTHORITIES_BY_USERNAME_AND_ROLE, new MapSqlParameterSource("username", userLoginDto.getUsername()));
        Set<String> existingRoles = Sets.newLinkedHashSet();
        for(Map<String, Object> map : users){
            existingRoles.add((String)map.get("authorities"));
        }
        return checkIfUserHasRoleAlreadyDefined(existingRoles, userLoginDto.getAuthoritiesByRole());
    }

    private Set<String> checkIfUserHasRoleAlreadyDefined(Set<String> existingRoles, Set<String> newRoles) {
        newRoles.removeAll(existingRoles);
        return newRoles;

    }

    public boolean checkIfSmsAvailableForTenant(String tenantId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(QUERY_TO_FIND_SMS_CONSUMED_COUNT, new MapSqlParameterSource("tenantId", tenantId));
        if(UtilValidator.isNotEmpty(result)){
            Map<String, Object> map = result.get(0);
            if(UtilValidator.isNotEmpty(map.get("availableSmsCount"))){
                if(Integer.parseInt(map.get("availableSmsCount").toString()) > 0)
                    return true;
            }
        }
        return false;
    }
}
