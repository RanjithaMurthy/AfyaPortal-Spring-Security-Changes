package com.afya.portal.query;

import com.afya.portal.domain.model.LoginPreference;
import com.afya.portal.presentation.FacilityDto;
import com.nzion.dto.UserLoginDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pradyumna on 08-06-2015.
 * Modified By Mohan Sharma - to find users by tenant and check users subscribed
 */
@Component
public class UserLoginFinder {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final String FIND_FACILITY="SELECT C.clinic_id as facility_id, C.tenant_id,C.clinic_name as facility_name,A.provider_type as facility_type FROM users A JOIN clinic C ON A.`facility_id`=C.`clinic_id` WHERE A.`username`=:username" +
            " UNION " +
            " SELECT P.pharmacy_id as facility_id, P.tenant_id,P.pharmacy_name as facility_name,A.provider_type as facility_type FROM users A JOIN pharmacy P ON P.`pharmacy_id`=A.`facility_id` WHERE A.`username`=:username" +
            " UNION " +
            " SELECT L.lab_id as facility_id, L.tenant_id,L.lab_name as facility_name,A.provider_type  as facility_type FROM users A JOIN `laboratory` L ON L.`lab_id`=A.`facility_id` WHERE A.`username`=:username";

    private final String QUERY_TO_FETCH_ALL_USERS_FOR_GIVEN_TENANT = "SELECT user_name AS username FROM `user_tenant_assoc` WHERE tenant_id=:tenantId";

    private final String QUERY_TO_CHECK_IF_ANY_USER_SUBSCRIBED = "SELECT IF(COUNT(*) > 0, 'true', 'false') AS result FROM `user_package` WHERE username IN(:usernames)";

    private final String UPDATE_PROVIDER_DETAIL = "{CALL UpdateProviderDetail(:username , :tenantId, :mobileNumber, :address1, :address2, :city, :governorate, :country, :officeNumber)}";
    @Autowired
    public UserLoginFinder(@Qualifier("primaryDataSource") DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }


    public Map<String,Object> findUserWithUsername(String username) {
        Map resultSet = namedParameterJdbcTemplate.queryForMap("select (user_name) from user_login where user_name=:username", new MapSqlParameterSource().addValue("username", username));
        return resultSet;
    }

    public Map<String,Object> findEmailIdWithUsername(String userName) throws Exception{
        //Map resultSet = null;
        //try {
        Map  resultSet = namedParameterJdbcTemplate.queryForMap("SELECT email_id, first_name, last_name FROM users WHERE username = :userName", new MapSqlParameterSource().addValue("userName", userName));
        /*}catch (Exception e){
        }*/
            return resultSet;
    }

    public Map<String,Object> findTokensWithUsername(String userName) throws Exception{             // (Kannan - 2015-11-01)
        Map  resultSet = namedParameterJdbcTemplate.queryForMap("SELECT otp_token, token FROM users WHERE username = :userName", new MapSqlParameterSource().addValue("userName", userName));
        return resultSet;
    }

    public List<FacilityDto> getFacilities(String username) {
        List<FacilityDto> resultset = namedParameterJdbcTemplate.query(FIND_FACILITY, new MapSqlParameterSource().addValue("username", username), new BeanPropertyRowMapper<FacilityDto>(FacilityDto.class));
        return resultset;
    }

    public String getTenantIdFromTenantAssoc(String userName){
        SqlRowSet rowset = namedParameterJdbcTemplate.queryForRowSet("select tenant_id from user_tenant_assoc where user_name=:username", new MapSqlParameterSource().addValue("username", userName)) ;
        while (rowset.next()) {
            return rowset.getString("tenant_id");
        }
        return "";
    }

    public String getClinicIdFromClinic(String tenantId){
        SqlRowSet rowset = namedParameterJdbcTemplate.queryForRowSet("select clinic_id from clinic where tenant_id=:tenantId",new MapSqlParameterSource().addValue("tenantId", tenantId)) ;
        if (rowset.next()) {
            return rowset.getString("clinic_id");
        }else{
            SqlRowSet rowset1 = namedParameterJdbcTemplate.queryForRowSet("select pharmacy_id from pharmacy where tenant_id=:tenantId",new MapSqlParameterSource().addValue("tenantId", tenantId)) ;
            if (rowset1.next()) {
                return rowset1.getString("pharmacy_id");
            }
        }
        return "";
    }

    public List<Map<String, Object>> getAllUsersForGivenTenant(String tenantId){
        return namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_ALL_USERS_FOR_GIVEN_TENANT, new MapSqlParameterSource("tenantId", tenantId));
    }

    public Boolean checkIfAnyUserOfTheTenantSubscribed(Set<String> usernames) {
        return namedParameterJdbcTemplate.queryForObject(QUERY_TO_CHECK_IF_ANY_USER_SUBSCRIBED, new MapSqlParameterSource("usernames", usernames), Boolean.class);
    }

    public String updateProviderDetail(UserLoginDto userLoginDto) {
        String username = userLoginDto.getLoginPreference().equals(LoginPreference.EMAIL.toString()) ? userLoginDto.getEmailId() : userLoginDto.getMobileNumber();
        String tenantId  = getTenantIdFromTenantAssoc(username);
        if(userLoginDto.getOfficeNumber() == null)
        {
            List<Map<String, Object>> tenantDetail = getGivenTenantDetail(tenantId);
            if(tenantDetail != null && tenantDetail.get(0) != null && tenantDetail.get(0).get("office_phone_number") != null)
                userLoginDto.setOfficeNumber(tenantDetail.get(0).get("office_phone_number").toString());
        }
        namedParameterJdbcTemplate.update(UPDATE_PROVIDER_DETAIL,
                new MapSqlParameterSource("username", userLoginDto.getUsername()).addValue("tenantId", tenantId).addValue("mobileNumber", userLoginDto.getMobileNumber()).addValue("address1", userLoginDto.getProviderAddress1())
                        .addValue("address2", userLoginDto.getProviderAddress2()).addValue("city", userLoginDto.getProviderCity()).addValue("governorate", userLoginDto.getProviderGovernorate()).addValue("country", userLoginDto.getProviderCountry())
                        .addValue("officeNumber", userLoginDto.getOfficeNumber()));
        return "SUCCESS";
    }

    public String[] getTenantIdAndFacilityTypeFromTenantAssoc(String userName){
        String[] arr = new String[2];
        SqlRowSet rowset = namedParameterJdbcTemplate.queryForRowSet("select facility_type, tenant_id from user_tenant_assoc where user_name=:username",new MapSqlParameterSource().addValue("username", userName)) ;
        while (rowset.next()) {
            arr[0] = rowset.getString("facility_type");
            arr[1] = rowset.getString("tenant_id");
            return arr;
        }
        return arr;
    }

    public String getTenantIdFromUsers(String userName){
        SqlRowSet rowset = namedParameterJdbcTemplate.queryForRowSet("select tenant_id from users where username=:username", new MapSqlParameterSource().addValue("username", userName)) ;
        while (rowset.next()) {
            return rowset.getString("tenant_id");
        }
        return "";
    }

    public List<Map<String, Object>> getGivenTenantDetail(String tenantId)
    {
        List<Map<String, Object>> clinicResult = namedParameterJdbcTemplate.queryForList("select * from clinic where tenant_id=:tenantId", new MapSqlParameterSource().addValue("tenantId", tenantId)) ;
        if (clinicResult.size() > 0) {
            return clinicResult;
        }else{
            List<Map<String, Object>>  pharmacyResult = namedParameterJdbcTemplate.queryForList("select * from pharmacy where tenant_id=:tenantId", new MapSqlParameterSource().addValue("tenantId", tenantId)) ;
            if (pharmacyResult.size() > 0) {
                return pharmacyResult;
            }
        }
        return null;
    }
}
