package com.afya.portal.membercareprovider.view.query;

import com.afya.portal.membercareprovider.view.dto.InitiatedContractDto;
import com.afya.portal.membercareprovider.view.dto.NetworkDto;
import com.afya.portal.membercareprovider.view.dto.ReferalDataDto;
import com.afya.portal.presentation.FacilityDto;
import com.afya.portal.util.UtilDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 7/31/15
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class MemberCareProviderFinder {

    private static final String FIND_NETWORK_BY_CLINICID = "SELECT N.id as networkId, N.created_on as createdOn, N.message as message, N.status as status, C.clinic_id " +
            "as facilityId,C.clinic_name as facilityName, C.address as address, C.first_name as firstName, C.last_name as lastName, C.key_person_first_Name as keyPersonFirstName, " +
            "C.key_person_last_name as keyPersonLastName, C.email as email   " +
            "FROM NETWORK N JOIN CLINIC C WHERE N.clinic_clinicId=C.clinic_id AND N.to_clinic_clinicId = :clinicId AND N.status='PENDING'";

    private static final String FIND_ACCEPTED_NETWORK_BY_CLINICID = "SELECT N.id as networkId, N.created_on as createdOn, N.message as message, N.status as status, C.clinic_id " +
            "as facilityId,C.clinic_name as facilityName, C.address as address, C.first_name as firstName, C.last_name as lastName, C.key_person_first_Name as keyPersonFirstName, " +
            "C.key_person_last_name as keyPersonLastName, C.email as email   " +
            "FROM NETWORK N JOIN CLINIC C WHERE N.status = :status AND ((N.clinic_clinicId=C.clinic_id AND N.to_clinic_clinicId = :clinicId) OR " +
            " (N.to_clinic_clinicId=C.clinic_id AND N.clinic_clinicId = :clinicId)) "+
            "UNION "+
            "SELECT N.id as networkId, N.created_on as createdOn, N.message as message, N.status as status, p.pharmacy_id "+
            "as facilityId,p.pharmacy_name as facilityName, p.address as address, p.first_name as firstName, p.last_name as lastName, p.key_person_first_Name as keyPersonFirstName, " +
            "p.key_person_last_name as keyPersonLastName, p.email as email   " +
            "FROM NETWORK N JOIN pharmacy p WHERE N.clinic_clinicId=p.pharmacy_id AND N.to_clinic_clinicId = :clinicId AND N.status = :status ";

    private static final String FIND_BLOCKED_NETWORK_BY_CLINICID = "SELECT N.id as networkId, N.created_on as createdOn, N.message as message, N.status as status, C.clinic_id " +
            "as facilityId,C.clinic_name as facilityName, C.address as address, C.first_name as firstName, C.last_name as lastName, C.key_person_first_Name as keyPersonFirstName, " +
            "C.key_person_last_name as keyPersonLastName, C.email as email   " +
            "FROM NETWORK N JOIN CLINIC C WHERE N.clinic_clinicId=C.clinic_id AND N.to_clinic_clinicId = :clinicId AND N.status = :status "+
            "UNION "+
            "SELECT N.id as networkId, N.created_on as createdOn, N.message as message, N.status as status, p.pharmacy_id " +
            "as facilityId,p.pharmacy_name as facilityName, p.address as address, p.first_name as firstName, p.last_name as lastName, p.key_person_first_name as keyPersonFirstName, " +
            "p.key_person_last_name as keyPersonLastName, p.email as email   " +
            "FROM NETWORK N JOIN pharmacy p WHERE N.clinic_clinicId=p.pharmacy_id AND N.to_clinic_clinicId = :clinicId AND N.status = :status";

    private static final String FIND_INITIATED_CONTRACT = "SELECT CONCAT(CRC.ref_first_name, ' ', CRC.ref_last_name) AS providerName, CONCAT(CRC.provider_first_name, ' ', CRC.provider_last_name) AS memberName, CRC.REFERRAL_TYPE AS providerType,\n" +
            "            CRC.contract_date AS intiatedDate, CRC.contract_status AS STATUS FROM consolidated_referral_contract CRC WHERE \n" +
            "            CRC.contract_status IS NOT NULL AND CRC.clinic_id=:clinicId;";

    private String GET_REFERRAL_AMOUNT = "{CALL getReferralAmount(:tenantId, :fromDate, :toDate)}";

    private String GET_REFERRAL_DATA = "{CALL getReferalData(:tenantId, :fromDate, :toDate)}";


    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public MemberCareProviderFinder(@Qualifier("primaryDataSource") DataSource dataSource){
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<NetworkDto> findRequestedNetworkByClinicId(String clinicId) {
        List<NetworkDto> resultset = namedParameterJdbcTemplate.query(FIND_NETWORK_BY_CLINICID, new MapSqlParameterSource().addValue("clinicId", clinicId), new BeanPropertyRowMapper<NetworkDto>(NetworkDto.class));
        return resultset;
    }

    public List<NetworkDto> findAllAcceptedNetworkRequested(String clinicId) {
        List<NetworkDto> resultset = namedParameterJdbcTemplate.query(FIND_ACCEPTED_NETWORK_BY_CLINICID, new MapSqlParameterSource().addValue("clinicId", clinicId).addValue("status","ACCEPTED"), new BeanPropertyRowMapper<NetworkDto>(NetworkDto.class));
        return resultset;
    }

    public List<InitiatedContractDto> findAllInitiatedContractByClinicId(String clinicId) {
        List<InitiatedContractDto> resultset = namedParameterJdbcTemplate.query(FIND_INITIATED_CONTRACT, new MapSqlParameterSource().addValue("clinicId", clinicId), new BeanPropertyRowMapper<InitiatedContractDto>(InitiatedContractDto.class));

        return resultset;
    }

    public List<NetworkDto> findAllBlockedNetworkRequest(String clinicId) {
        List<NetworkDto> resultset = namedParameterJdbcTemplate.query(FIND_BLOCKED_NETWORK_BY_CLINICID, new MapSqlParameterSource().addValue("clinicId", clinicId).addValue("status","BLOCKED"), new BeanPropertyRowMapper<NetworkDto>(NetworkDto.class));
        return resultset;
    }

    public BigDecimal getReferralAmount(String tenantId, Date fromDate, Date toDate) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_REFERRAL_AMOUNT,
                new MapSqlParameterSource("tenantId", tenantId).addValue("fromDate", UtilDateTime.toSqlDateString(fromDate)).addValue("toDate", UtilDateTime.toSqlDateString(toDate)) );
        BigDecimal totalRefAmountTobePaid = BigDecimal.ZERO;
        for(Map<String,Object> map : result){
            BigDecimal amount =  new BigDecimal(map.get("totalRefAmountTobePaid").toString());
            totalRefAmountTobePaid = totalRefAmountTobePaid.add(amount);
        }
        return totalRefAmountTobePaid;
    }

    public List<ReferalDataDto> getReferralData(String tenantId, Date fromDate, Date toDate) {
        List<ReferalDataDto> resultset = namedParameterJdbcTemplate.query(GET_REFERRAL_DATA,
                new MapSqlParameterSource().addValue("tenantId", tenantId).addValue("fromDate", UtilDateTime.toSqlDateString(fromDate)).addValue("toDate", UtilDateTime.toSqlDateString(toDate)), new BeanPropertyRowMapper<ReferalDataDto>(ReferalDataDto.class));
        return resultset;
    }
}
