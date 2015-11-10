package com.afya.portal.query;

import com.afya.portal.util.UtilValidator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.nzion.dto.ModuleServiceDto;
import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mohan Sharma on 4/29/2015.
 */
@Finder
@Component
public class PortalInsuranceFinder {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final String FETCH_SERVICE_DETAILS_OF_GIVEN_MODULE = "SELECT msa.`MODULE_ID` AS moduleId, hs.`BENEFIT_NAME` AS moduleName, msa.`SERVICE_ID` AS serviceId, sm.service_name AS serviceName, COALESCE(msa.`INDIVIDUAL_LIMIT_AMOUNT`, 0) AS individualLimitAmount, COALESCE(msa.`INDIVIDUAL_LIMIT_PERCENTAGE`, 0)\n" +
            "AS individualLimitPercentage, COALESCE(msa.`FAMILY_LIMIT_AMOUNT`, 0) AS familyLimitAmount, COALESCE(msa.`FAMILY_LIMIT_PERCENTAGE`, 0) AS familyLimitPercentage, COALESCE(msa.`ILLNESS_LIMIT_AMOUNT`, 0)\n" +
            "AS illnessLimitAmount, COALESCE(msa.`ILLNESS_LIMIT_PERCENTAGE`, 0) AS illnessLimitPercentage, COALESCE(msa.`CLAIM_LIMIT_AMOUNT`, 0) AS claimLimitAmount, COALESCE(msa.`CLAIM_LIMIT_PERCENTAGE`, 0)\n" +
            "AS claimLimitPercentage, COALESCE(msa.`PER_CASE_AMOUNT`, 0) AS perCaseAmount, COALESCE(msa.`PER_CASE_PERCENTAGE`, 0) AS perCasePercentage, msa.`NUMBER_OF_CASES` AS numberOfCases,\n" +
            "msa.note AS note, COALESCE(msa.copay_amount, 0) AS copayAmount, COALESCE(msa.copay_percentage, 0) AS copayPercentage, COALESCE(msa.deductable_amount, 0) AS deductibleAmount, COALESCE(msa.deductable_percentage, 0) \n" +
            "AS deductiblePercentage, COALESCE(msa.authorization_amount, 0) AS authorizationAmount, COALESCE(msa.authorization, TRUE) AS authorization, \n" +
            "msa.authorization_inclusive_consultation AS authorizationInclusiveConsultation, msa.authorization_required_consultation\n" +
            "AS authorizationRequiredConsultation, msa.compute_by AS computeBy, msa.max_amount as maxAmount FROM `module_service_assoc` msa JOIN `service_master` sm ON sm.id = msa.service_id JOIN `modules` m ON m.id = msa.module_id JOIN `his_module` hs ON hs.id = m.his_benefit_id AND msa.module_id IN(SELECT bm.module_id FROM `benefit_module_assoc` bm WHERE bm.benefit_id=:benefitId GROUP BY bm.module_id) ORDER BY m.module_name,sm.service_name;\n";

    private static final String FETCH_ALL_TPA = "SELECT p.id AS payerId, p.payer_type AS payerType, p.`INSURANCE_CODE` AS insuranceCode, p.`INSURANCE_NAME` AS insuranceName, p.`INSURANCE_SHORT_NAME` AS insuranceShortName,\n" +
            "p.`AUTHORIZATION_NO` AS authorizationNumber, p.`STATUTORY_INFO` AS statutoryInfo, p.`MODE_OF_CLAIM` AS modeOfClaim,\n" +
            "p.`CONTACT_NAME` AS contactName, p.`ADDRESS1` AS address1, p.`ADDRESS2` AS address2, p.`STATE_ID` AS stateId, \n" +
            "p.`DISTRICT_ID` AS districtId, p.`PINCODE` AS pincode, p.`COUNTRY_ID` AS countryId, p.`FAX_NO` AS faxNo, p.`EMAIL` AS emailId, \n" +
            "p.`WEBSITE` AS website, p.`PO_BOX_NO` AS poBoxNumber FROM payer p WHERE p.payer_type =:payerType ORDER BY p.insurance_name";

    private static final String FETCH_GROUP_BY_TPA_ID = "SELECT g.`GROUP_ID` AS groupId, g.`GROUP_NAME` AS groupName, g.`GROUP_TYPE_ID` AS groupTypeId, g.`REGISTRATION_NO` AS groupRegistrationNumber,\n" +
            "g.`PAN_NO` AS groupPanNumber, g.`STD_CODE` AS groupStdCode, g.`PHONE_1` AS phone1, g.`PHONE_2` AS phone2, g.`PHONE_3` AS phone3,\n" +
            "g.`PHONE_4` AS phone4, g.`ADDRESS_LINE_1` AS addressLine1, g.`ADDRESS_LINE_2` AS addressLine2, g.`LOCATION` AS location, g.`DISTRICT_ID` AS\n" +
            "districtId, g.`STATE_ID` AS stateId, g.`PINCODE` AS pincode, g.`COUNTRY_ID` AS countryId, g.`FAX_NO` AS faxNo, g.`EMAIL` AS email, \n" +
            "g.`WEBSITE` AS website, g.`NETWORK_TYPE` AS networkType FROM `group` g JOIN insured_group ig ON g.GROUP_ID = ig.insured_group_id AND\n" +
            "ig.payer_id=:tpaId ORDER BY g.`GROUP_NAME`";

    private static final String FETCH_ALL_GROUP = "SELECT g.`GROUP_ID` AS groupId, g.`GROUP_NAME` AS groupName, g.`GROUP_TYPE_ID` AS groupTypeId, g.`REGISTRATION_NO` AS groupRegistrationNumber,\n" +
            "g.`PAN_NO` AS groupPanNumber, g.`STD_CODE` AS groupStdCode, g.`PHONE_1` AS phone1, g.`PHONE_2` AS phone2, g.`PHONE_3` AS phone3,\n" +
            "g.`PHONE_4` AS phone4, g.`ADDRESS_LINE_1` AS addressLine1, g.`ADDRESS_LINE_2` AS addressLine2, g.`LOCATION` AS location, g.`DISTRICT_ID` AS\n" +
            "districtId, g.`STATE_ID` AS stateId, g.`PINCODE` AS pincode, g.`COUNTRY_ID` AS countryId, g.`FAX_NO` AS faxNo, g.`EMAIL` AS email, \n" +
            "g.`WEBSITE` AS website, g.`NETWORK_TYPE` AS networkType FROM `group` g ORDER BY g.`GROUP_NAME`";

    private static final String FETCH_INSURANCE_DETAILS_OF_TPA = "SELECT p.id AS payerId, p.insurance_code AS insuranceCode, p.insurance_name AS insuranceName, p.payer_type AS payerType, \n" +
            "p.`INSURANCE_SHORT_NAME` AS insuranceShortName,p.`AUTHORIZATION_NO` AS authorizationNumber, p.`STATUTORY_INFO`\n" +
            "AS statutoryInfo, p.`MODE_OF_CLAIM` AS modeOfClaim,p.`CONTACT_NAME` AS contactName, p.`ADDRESS1` AS address1,\n" +
            "p.`ADDRESS2` AS address2, p.`STATE_ID` AS stateId, p.`DISTRICT_ID` AS districtId, p.`PINCODE` AS pincode, p.`COUNTRY_ID`\n" +
            "AS countryId, p.`FAX_NO` AS faxNo, p.`EMAIL` AS emailId, p.`WEBSITE` AS website, p.`PO_BOX_NO` AS poBoxNumber FROM payer p WHERE p.payer_id IN (SELECT insurance_company_id FROM `payer_insurance_assoc` WHERE payer_id =:tpaId) ORDER BY p.insurance_name";

    private static final String FETCH_ALL_PAYER_OF_TYPE_INSURANCE = "SELECT p.id AS payerId, p.insurance_code AS insuranceCode, p.insurance_name AS insuranceName, p.payer_type AS payerType, \n" +
            "p.`INSURANCE_SHORT_NAME` AS insuranceShortName,p.`AUTHORIZATION_NO` AS authorizationNumber, p.`STATUTORY_INFO` \n" +
            "AS statutoryInfo, p.`MODE_OF_CLAIM` AS modeOfClaim,p.`CONTACT_NAME` AS contactName, p.`ADDRESS1` AS address1,\n" +
            "p.`ADDRESS2` AS address2, p.`STATE_ID` AS stateId, p.`DISTRICT_ID` AS districtId, p.`PINCODE` AS pincode, p.`COUNTRY_ID` \n" +
            "AS countryId, p.`FAX_NO` AS faxNo, p.`EMAIL` AS emailId, p.`WEBSITE` AS website, p.`PO_BOX_NO` AS poBoxNumber FROM payer p \n" +
            "WHERE p.payer_type=:payerType ORDER BY p.insurance_name";

    private static final String FETCH_ALL_HIS_MODULES = "SELECT id AS hisModuleId, benefit_name AS hisBenefitName FROM his_module ORDER BY benefit_name";

    private static final String FETCH_ALL_SERVICE_OR_MODULE_DATA_SERVICE_ID = "SELECT sm.id AS serviceId, msd.moduleId, COALESCE(IFNULL(sm.copay_amount, msd.copayAmount),0) AS copayAmount, COALESCE(IFNULL(sm.copay_percentage, msd.copayPercentage),0) AS copayPercentage, \n" +
            "COALESCE(IFNULL(sm.deductable_amount, msd.deductableAmount),0) AS deductableAmount, COALESCE(IFNULL(sm.deductable_percentage, msd.deductablePercentage),0) AS deductablePercentage,\n" +
            "IFNULL(sm.authorization, msd.authorization) AS authorization, IFNULL(sm.authorization_inclusive_consultation, msd.authorizationInclusiveConsultation)\n" +
            "AS authorizationInclusiveConsultation,COALESCE(IFNULL(sm.authorization_amount, msd.authorizationAmount),0) AS authorizationAmount, \n" +
            "IFNULL(sm.authorization_required_consultation, msd.authorizationRequiredConsultation) AS authorizationRequiredConsultation \n" +
            "FROM `service_master` sm JOIN `module_service_details` msd ON sm.id = msd.serviceId AND msd.moduleId=:moduleId AND sm.id IN (:serviceIds)";

    private static final String FETCH_SERVICES_BY_SERVICEID_AND_MODULEID_GROUP_ID = "SELECT sm.module_id AS moduleId, sm.service_id AS serviceId, COALESCE(sm.copay_amount, 0) AS copayAmount, IFNULL(sm.copay_percentage, msd.copayPercentage) AS copayPercentage, \n" +
            "COALESCE(sm.deductable_amount,0) AS deductableAmount,sm.MAX_AMOUNT as maxAmount, IFNULL(sm.deductable_percentage, msd.deductablePercentage) AS deductablePercentage, \n" +
            "IFNULL(IFNULL(sm.authorization, msd.authorization), 1) AS authorization, IFNULL(sm.authorization_inclusive_consultation, msd.authorizationInclusiveConsultation)\n" +
            "AS authorizationInclusiveConsultation,COALESCE(sm.authorization_amount, 0) AS authorizationAmount, \n" +
            "IFNULL(sm.authorization_required_consultation, msd.authorizationRequiredConsultation)AS authorizationRequiredConsultation, IFNULL(sm.compute_by, msd.computeBy) AS computeBy \n" +
            "FROM `module_service_assoc` sm JOIN `module_service_details` msd ON sm.service_id = msd.serviceId AND sm.module_id IN (SELECT m.id FROM `modules` m JOIN\n" +
            "benefit_module_assoc A ON m.`ID`=A.`MODULE_ID` WHERE m.`his_benefit_id`=:hisModuleId AND A.`BENEFIT_ID`=:benefitId AND m.GROUP_ID = :groupId) AND sm.service_id IN (:serviceIds)";

    private static final String FETCH_SERVICES_BY_SERVICEID_AND_MODULEID = "SELECT sm.module_id AS moduleId, sm.service_id AS serviceId, COALESCE(sm.copay_amount, 0) AS copayAmount, IFNULL(sm.copay_percentage, msd.copayPercentage) AS copayPercentage, \n" +
            "COALESCE(sm.deductable_amount,0) AS deductableAmount,sm.MAX_AMOUNT as maxAmount, IFNULL(sm.deductable_percentage, msd.deductablePercentage) AS deductablePercentage, \n" +
            "IFNULL(IFNULL(sm.authorization, msd.authorization), 1) AS authorization, IFNULL(sm.authorization_inclusive_consultation, msd.authorizationInclusiveConsultation)\n" +
            "AS authorizationInclusiveConsultation,COALESCE(sm.authorization_amount, 0) AS authorizationAmount, \n" +
            "IFNULL(sm.authorization_required_consultation, msd.authorizationRequiredConsultation)AS authorizationRequiredConsultation, IFNULL(sm.compute_by, msd.computeBy) AS computeBy \n" +
            "FROM `module_service_assoc` sm JOIN `module_service_details` msd ON sm.service_id = msd.serviceId AND sm.module_id IN (SELECT m.id FROM `modules` m JOIN\n" +
            "benefit_module_assoc A ON m.`ID`=A.`MODULE_ID` WHERE m.`his_benefit_id`=:hisModuleId AND A.`BENEFIT_ID`=:benefitId AND m.GROUP_ID IS NULL) AND sm.service_id IN (:serviceIds)";


    private static final String FETCH_MODULE_DETAILS_BY_MODULEID = "SELECT m.id AS moduleId, COALESCE(m.copay_amount, 0) AS copayAmount, IFNULL(m.copay_percentage, 0)AS copayPercentage, COALESCE(m.deductable_amount,0) \n" +
            "AS deductableAmount, IFNULL(m.deductable_percentage, 0) AS deductablePercentage, COALESCE(m.authorization_amount, 0) AS \n" +
            "authorizationAmount, IFNULL(m.authorization, TRUE) AS authorization, m.authorization_inclusive_consultation AS authorizationInclusiveConsultation, m.authorization_required_consultation\n" +
            "AS authorizationRequiredConsultation, m.compute_by AS computeBy FROM `modules` m WHERE m.id =:moduleId";

    private static final String FETCH_GROUPS_FOR_PAYER = "SELECT  GRP.`GROUP_ID`,GRP.`GROUP_NAME` FROM insured_group IG  JOIN `group` GRP ON IG.`INSURED_GROUP_ID` = GRP.`GROUP_ID` \n" +
            "    AND IG.`PAYER_ID` IS NOT NULL ;";

    private static final String FETCH_DATA ="SELECT `ID` AS id, `BENEFIT_NAME` AS benefitName, `HEALTH_POLICY_ID` AS healthPolicyId FROM `benefit_plan` WHERE id = '549'";

    private static final String FETCH_MODULE_DETAILS_HIS_MODULE_ID_BENEFIT_ID_MODULE_ID = "SELECT m.id AS moduleId, COALESCE(m.copay_amount, 0) AS copayAmount, IFNULL(m.copay_percentage, 0)AS copayPercentage, COALESCE(m.deductable_amount,0)\n" +
            "AS deductableAmount, IFNULL(m.deductable_percentage, 0) AS deductablePercentage, COALESCE(m.authorization_amount, 0) AS\n" +
            "authorizationAmount, IFNULL(m.authorization, TRUE) AS authorization, m.authorization_inclusive_consultation AS authorizationInclusiveConsultation,\n" +
            "m.authorization_required_consultation AS authorizationRequiredConsultation, m.compute_by AS computeBy FROM `modules` m JOIN\n" +
            "benefit_module_assoc A ON m.`ID`=A.`MODULE_ID` WHERE m.`his_benefit_id`=:hisModuleId AND A.`BENEFIT_ID`=:benefitId AND m.GROUP_ID = :groupId";

    private static final String FETCH_MODULE_DETAILS_HIS_MODULE_ID_BENEFIT_ID_GROUP_ID = "SELECT m.id AS moduleId, COALESCE(m.copay_amount, 0) AS copayAmount, IFNULL(m.copay_percentage, 0)AS copayPercentage, COALESCE(m.deductable_amount,0)\n" +
            "AS deductableAmount, IFNULL(m.deductable_percentage, 0) AS deductablePercentage, COALESCE(m.authorization_amount, 0) AS\n" +
            "authorizationAmount, IFNULL(m.authorization, TRUE) AS authorization, m.authorization_inclusive_consultation AS authorizationInclusiveConsultation,\n" +
            "m.authorization_required_consultation AS authorizationRequiredConsultation, m.compute_by AS computeBy FROM `modules` m JOIN\n" +
            "benefit_module_assoc A ON m.`ID`=A.`MODULE_ID` WHERE m.`his_benefit_id`=:hisModuleId AND A.`BENEFIT_ID`=:benefitId AND m.GROUP_ID = :groupId";

    private static final String FETCH_MODULE_DETAILS_HIS_MODULE_ID_BENEFIT_ID = "SELECT m.id AS moduleId, COALESCE(m.copay_amount, 0) AS copayAmount, IFNULL(m.copay_percentage, 0)AS copayPercentage, COALESCE(m.deductable_amount,0)\n" +
            "AS deductableAmount, IFNULL(m.deductable_percentage, 0) AS deductablePercentage, COALESCE(m.authorization_amount, 0) AS\n" +
            "authorizationAmount, IFNULL(m.authorization, TRUE) AS authorization, m.authorization_inclusive_consultation AS authorizationInclusiveConsultation,\n" +
            "m.authorization_required_consultation AS authorizationRequiredConsultation, m.compute_by AS computeBy FROM `modules` m JOIN\n" +
            "benefit_module_assoc A ON m.`ID`=A.`MODULE_ID` WHERE m.`his_benefit_id`=:hisModuleId AND A.`BENEFIT_ID`=:benefitId AND m.GROUP_ID IS NULL";

    private static final String FETCH_MODULE_DETAILS_BY_BENEFIT_ID = "SELECT m.id AS id, hs.`BENEFIT_NAME` AS moduleName, COALESCE(m.`SUM_INSURED`, 0) AS sumInsured, m.`FROM_AGE` AS fromAge, m.`HEALTH_POLICY_ID` AS healthPolicyId,\n" +
            "m.`TO_AGE` AS toAge, m.`PRE_HOSPITALISATION_LIMIT` AS preHospitalisationLimit, m.`PRE_HOSPITALISATION_DAYS` AS preHospitalisationDays,\n" +
            "m.`POST_HOSPITALISATION_LIMIT` AS postHospitalisationLimit, m.`POST_HOSPITALISATION_DAYS` AS postHospitalisationDays,\n" +
            "m.`WAITING_PERIOD` AS waitingPeriod, m.`EXCLUSIONPERIOD` AS exclusionPeriod, m.`SPECIAL_CLAUSE` AS specialClause, m.`SPECIAL_EXCLUSIONS` AS\n" +
            "specialExclusions, m.`NOTES` AS notes, m.`IS_MATERNITY` AS isMaternity, m.`IS_ACTIVE` AS isActive, COALESCE(m.copay_amount, 0) AS copayAmount, COALESCE(m.copay_percentage, 0) AS copayPercentage, COALESCE(m.deductable_amount, 0)\n" +
            "AS deductibleAmount, COALESCE(m.deductable_percentage, 0) AS deductiblePercentage, COALESCE(m.authorization_amount,0) AS \n" +
            "authorizationAmount, COALESCE(m.authorization, TRUE) AS authorization, m.authorization_inclusive_consultation AS authorizationInclusiveConsultation, m.authorization_required_consultation\n" +
            "AS authorizationRequiredConsultation, m.compute_by AS computeBy, m.`his_benefit_id` AS hisBenefitId FROM `modules` m \n" +
            "JOIN `benefit_module_assoc` bma ON m.id = bma.`MODULE_ID` JOIN `his_module` hs ON hs.id = m.`his_benefit_id`  WHERE bma.benefit_id=:benefitId ORDER BY hs.`BENEFIT_NAME`";

    private static final String FETCH_HEALTH_POLICY_BY_PAYER_ID = "SELECT hp.id, hp.policy_name AS policyName FROM `health_policy` hp JOIN `insured_group` ig ON hp.id = ig.health_policy_id WHERE ig.payer_id =:payerId ORDER BY hp.policy_name";

    private static final String FETCH_BENEFIT_NAME_BY_HIS_BENEFIT_ID = "SELECT `benefit_name` AS benefitName FROM `his_module` WHERE `id` =:benefitId";

    private static final String FETCH_ALL_PAYERS = "SELECT p.id AS payerId, p.payer_type AS payerType, p.`INSURANCE_CODE` AS insuranceCode, p.`INSURANCE_NAME` AS insuranceName, p.`INSURANCE_SHORT_NAME` AS insuranceShortName,\n" +
            "p.`AUTHORIZATION_NO` AS authorizationNumber, p.`STATUTORY_INFO` AS statutoryInfo, p.`MODE_OF_CLAIM` AS modeOfClaim,\n" +
            "p.`CONTACT_NAME` AS contactName, p.`ADDRESS1` AS address1, p.`ADDRESS2` AS address2, p.`STATE_ID` AS stateId, \n" +
            "p.`DISTRICT_ID` AS districtId, p.`PINCODE` AS pincode, p.`COUNTRY_ID` AS countryId, p.`FAX_NO` AS faxNo, p.`EMAIL` AS emailId, \n" +
            "p.`WEBSITE` AS website, p.`PO_BOX_NO` AS poBoxNumber FROM payer p";

    private static final String FETCH_ALL_CORPORATES = "SELECT c.`CORPORATE_CODE` AS corporateCode, c.`CORPORATE_NAME` AS corporateName, c.`CORPORATE_SHORT_NAME` AS corporateShortName, c.`MODE_OF_CLAIM` AS modeOfClaim,\n" +
            "c.`CONTACT_NAME` AS contactName, c.`ADDRESS1` AS address1, c.`ADDRESS2` AS address2, c.`PIN_CODE` AS pincode, c.`COUNTRY_ID` AS countryId, c.`FAX_NO` AS faxNo, c.`EMAIL` AS emailId, \n" +
            "c.`WEBSITE` AS website FROM `corporate_master` c ORDER BY c.`CORPORATE_NAME`";

    private static final String FETCH_POLICY_BY_GROUP_ID =  "SELECT g.`INSURED_GROUP_ID` AS InsuredGroupId,\n" +
            " g.`PLAN_START_DATE` AS planStartDate,\n" +
            " g.`PLAN_END_DATE` AS planEndDate,\n" +
            " g.`HEALTH_POLICY_ID`  AS healthPolicyId ,\n" +
            " g.`PAYER_ID` as payerId,\n" +
            " g.`POLICY_NO` AS policyNo,hp.POLICY_NAME as policyName \n" +
            " FROM insured_group g JOIN health_policy hp WHERE g.HEALTH_POLICY_ID = hp.ID AND g.INSURED_GROUP_ID = :groupId ORDER BY g.`POLICY_NO` ";

    private static final String FETCH_POLICY_FOR_INDIVIDUAL_ID =  "SELECT g.`INSURED_GROUP_ID` AS InsuredGroupId,\n" +
            " g.`PLAN_START_DATE` AS planStartDate,\n" +
            " g.`PLAN_END_DATE` AS planEndDate,\n" +
            " g.`HEALTH_POLICY_ID`  AS healthPolicyId ,\n" +
            " g.`PAYER_ID` as payerId,\n" +
            " g.`POLICY_NO` AS policyNo,hp.POLICY_NAME as policyName  \n" +
            " FROM insured_group g JOIN health_policy hp WHERE g.HEALTH_POLICY_ID = hp.ID AND g.INSURED_GROUP_ID IS NULL ORDER BY g.`POLICY_NO` ";

    private static final String FETCH_PAYER_BY_ID =  "SELECT p.id AS payerId, p.payer_type AS payerType, " +
            " p.`INSURANCE_CODE` AS insuranceCode, " +
            " p.`INSURANCE_NAME` AS insuranceName, p.`INSURANCE_SHORT_NAME` AS insuranceShortName,\n" +
            " p.`AUTHORIZATION_NO` AS authorizationNumber, p.`STATUTORY_INFO` AS statutoryInfo, p.`MODE_OF_CLAIM` AS modeOfClaim,\n" +
            " p.`CONTACT_NAME` AS contactName, p.`ADDRESS1` AS address1, p.`ADDRESS2` AS address2, p.`STATE_ID` AS stateId, \n" +
            " p.`DISTRICT_ID` AS districtId, p.`PINCODE` AS pincode, p.`COUNTRY_ID` AS countryId, p.`FAX_NO` AS faxNo, p.`EMAIL` AS emailId,\n" +
            " p.`WEBSITE` AS website, p.`PO_BOX_NO` AS poBoxNumber FROM payer p WHERE p.`PAYER_ID` = :payerId";

    private static final String FETCH_HEALTH_POLICY_BY_ID =  "SELECT h.`ID` as id, h.`POLICY_NAME` AS policyName, h.`INSURANCE_ID` AS insuranceId, tpa_id As tpaId FROM health_policy h WHERE h.`ID` =:id";

    private static final String FETCH_PAYERS_BY_TENANT_ID_AND_FACILITY_TYPE = "SELECT p.id AS payerId, p.insurance_code AS insuranceCode, p.insurance_name AS insuranceName, p.payer_type AS payerType, \n" +
            "p.`INSURANCE_SHORT_NAME` AS insuranceShortName,p.`AUTHORIZATION_NO` AS authorizationNumber, p.`STATUTORY_INFO` \n" +
            "AS statutoryInfo, p.`MODE_OF_CLAIM` AS modeOfClaim,p.`CONTACT_NAME` AS contactName, p.`ADDRESS1` AS address1,\n" +
            "p.`ADDRESS2` AS address2, p.`STATE_ID` AS stateId, p.`DISTRICT_ID` AS districtId, p.`PINCODE` AS pincode, p.`COUNTRY_ID` \n" +
            "AS countryId, p.`FAX_NO` AS faxNo, p.`EMAIL` AS emailId, p.`WEBSITE` AS website, p.`PO_BOX_NO` AS poBoxNumber FROM payer p \n" +
            "JOIN facility_payer_assoc fpa ON p.id = fpa.payer_id WHERE fpa.facility_type =:facilityType AND fpa.tenant_id =:tenantId ORDER BY p.insurance_name";

    private static final String FETCH_PLANS_BY_PAYER_ID = "SELECT policy_name as policyName FROM health_policy WHERE insurance_id =:payerId OR tpa_id =:payerId";

    @Autowired
    public PortalInsuranceFinder(@Qualifier("primaryDataSource") DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> getAllTPA() {
     return namedParameterJdbcTemplate.queryForList(FETCH_ALL_TPA, new MapSqlParameterSource("payerType", "TPA"));
    }

    public List<Map<String, Object>> getGroupsByTpaId(String tpaId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_GROUP_BY_TPA_ID, new MapSqlParameterSource("tpaId", tpaId));
    }

    public List<Map<String, Object>> getInsuranceDetailsOfTpa(String tpaId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_INSURANCE_DETAILS_OF_TPA, new MapSqlParameterSource("tpaId", tpaId));
    }

    public List<Map<String, Object>> getAllPayerOfTypeInsurance() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_PAYER_OF_TYPE_INSURANCE, new MapSqlParameterSource("payerType", "INSURANCE"));
    }

    /*public List<Map<String, Object>> getData() {
        return namedParameterJdbcTemplate.queryForList(FETCH_DATA, new MapSqlParameterSource());
    }*/

    public List<Map<String, Object>> getHISModules() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_HIS_MODULES, new MapSqlParameterSource());
    }

    public ModuleServiceDto getModuleDataByModuleId(String benefitId,String hisModuleId, String groupId) {
        List<Map<String, Object>> modules = namedParameterJdbcTemplate.queryForList(FETCH_MODULE_DETAILS_HIS_MODULE_ID_BENEFIT_ID_GROUP_ID, new MapSqlParameterSource("benefitId", benefitId).addValue("hisModuleId",hisModuleId).addValue("groupId",groupId));
        if(UtilValidator.isEmpty(modules)){
            modules = namedParameterJdbcTemplate.queryForList(FETCH_MODULE_DETAILS_HIS_MODULE_ID_BENEFIT_ID, new MapSqlParameterSource("benefitId", benefitId).addValue("hisModuleId",hisModuleId).addValue("groupId",null));
        }
        Map<String, Object> module;
        Preconditions.checkArgument(!modules.isEmpty(), "No Modules Found");
        module = modules.get(0);
        return prepareModuleServiceDtoWhenNoServices(module);
    }

    public ModuleServiceDto getServiceOrModuleDataByServiceId(String benefitId, String hisModuleId, Set<Integer> serviceIds, String groupId) {
        ModuleServiceDto moduleServiceDto;
        List<Map<String, Object>> modules = namedParameterJdbcTemplate.queryForList(FETCH_MODULE_DETAILS_HIS_MODULE_ID_BENEFIT_ID_GROUP_ID, new MapSqlParameterSource("benefitId", benefitId).addValue("hisModuleId",hisModuleId).addValue("groupId",groupId));
        if(UtilValidator.isEmpty(modules)){
            groupId = "NULL";
            modules = namedParameterJdbcTemplate.queryForList(FETCH_MODULE_DETAILS_HIS_MODULE_ID_BENEFIT_ID, new MapSqlParameterSource("benefitId", benefitId).addValue("hisModuleId",hisModuleId).addValue("groupId",null));
        }
        Map<String, Object> module;
        Preconditions.checkArgument(!modules.isEmpty(), "No Modules Found");
        module = modules.get(0);
        List<Map<String, Object>> services = namedParameterJdbcTemplate.queryForList(FETCH_SERVICES_BY_SERVICEID_AND_MODULEID_GROUP_ID, new MapSqlParameterSource("serviceIds", serviceIds).addValue("benefitId", benefitId).addValue("hisModuleId", hisModuleId).addValue("groupId",groupId));
        if(UtilValidator.isEmpty(services)){
            services = namedParameterJdbcTemplate.queryForList(FETCH_SERVICES_BY_SERVICEID_AND_MODULEID, new MapSqlParameterSource("serviceIds", serviceIds).addValue("benefitId", benefitId).addValue("hisModuleId", hisModuleId).addValue("groupId",null));
        }
        moduleServiceDto = prepareModuleServiceDto(services, module);
        return moduleServiceDto;
    }

    private ModuleServiceDto prepareModuleServiceDto(List<Map<String, Object>> services, Map<String, Object> module) {
        boolean totalCopayAmountFlag = true;
        boolean totalDeductableAmountFlag = true;
        boolean totalAuthorizationAmountFlag = true;
        BigDecimal totalCopayAmountOfServices = BigDecimal.ZERO;
        BigDecimal totalDeductableAmountOfServices = BigDecimal.ZERO;
        BigDecimal totalAuthorizationAmountOfServices = BigDecimal.ZERO;
        ModuleServiceDto moduleServiceDto = new ModuleServiceDto();
        for (Map<String, Object> service : services) {
            for(Map.Entry entry : service.entrySet()){
                if(entry.getKey().equals("authorization") && entry.getValue().equals(1l)){
                    service.put((String)entry.getKey(), true);
                }
                if(entry.getKey().equals("authorization") && entry.getValue().equals(0l)){
                    service.put((String)entry.getKey(), false);
                }
            }
            totalCopayAmountOfServices = totalCopayAmountOfServices.add((BigDecimal) service.get("copayAmount"));
            totalDeductableAmountOfServices = totalDeductableAmountOfServices.add((BigDecimal) service.get("deductableAmount"));
            totalAuthorizationAmountOfServices = totalAuthorizationAmountOfServices.add((BigDecimal) service.get("authorizationAmount"));
            if (!((BigDecimal) service.get("copayAmount")).setScale(3,BigDecimal.ROUND_HALF_UP).equals(BigDecimal.ZERO)) {
                totalCopayAmountFlag = false;
            }
            if (!((BigDecimal) service.get("deductableAmount")).setScale(3,BigDecimal.ROUND_HALF_UP).equals(BigDecimal.ZERO)) {
                totalDeductableAmountFlag = false;
            }
            if (!((BigDecimal) service.get("authorizationAmount")).setScale(3,BigDecimal.ROUND_HALF_UP).equals(BigDecimal.ZERO)) {
                totalAuthorizationAmountFlag = false;
            }
        }
        if (totalCopayAmountFlag) {
            moduleServiceDto.setTotalCopayAmount((BigDecimal) module.get("copayAmount"));
        } else {
            moduleServiceDto.setTotalCopayAmount(((BigDecimal) module.get("copayAmount")).subtract(totalCopayAmountOfServices));
        }
        if (totalDeductableAmountFlag) {
            moduleServiceDto.setTotalDeductableAmount((BigDecimal) module.get("deductableAmount"));
        } else {
            moduleServiceDto.setTotalDeductableAmount(((BigDecimal) module.get("deductableAmount")).subtract(totalDeductableAmountOfServices));
        }
        if (totalAuthorizationAmountFlag) {
            moduleServiceDto.setTotalAuthorizationAmount((BigDecimal) module.get("authorizationAmount"));
        } else {
            moduleServiceDto.setTotalAuthorizationAmount(((BigDecimal) module.get("authorizationAmount")).subtract(totalAuthorizationAmountOfServices));
        }
        moduleServiceDto.setServiceDetails(services);
        return moduleServiceDto;
    }

    private ModuleServiceDto prepareModuleServiceDtoWhenNoServices(Map<String, Object> module) {
        ModuleServiceDto moduleServiceDto = new ModuleServiceDto();
        moduleServiceDto.setTotalCopayAmount((BigDecimal) module.get("copayAmount"));
        moduleServiceDto.setTotalDeductableAmount((BigDecimal) module.get("deductableAmount"));
        moduleServiceDto.setTotalAuthorizationAmount((BigDecimal) module.get("authorizationAmount"));
        for(Map.Entry entry : module.entrySet()){
            if(entry.getKey().equals("authorization") && entry.getValue().equals(1l)){
                module.put((String)entry.getKey(), true);
            }
            if(entry.getKey().equals("authorization") && entry.getValue().equals(0l)){
                module.put((String)entry.getKey(), false);
            }
        }
        moduleServiceDto.setModuleDetails(module);
        return moduleServiceDto;
    }

    public Map<String, Object> getServiceOrModuleDataByBenefitId(String benefitId) {
        Map<String, Object> details = Maps.newHashMap();
        List<Map<String, Object>> moduleDetails = namedParameterJdbcTemplate.queryForList(FETCH_MODULE_DETAILS_BY_BENEFIT_ID, new MapSqlParameterSource("benefitId", benefitId));
        List<Map<String, Object>> associatedServiceDetailsOfTheModule = namedParameterJdbcTemplate.queryForList(FETCH_SERVICE_DETAILS_OF_GIVEN_MODULE, new MapSqlParameterSource("benefitId", benefitId));
        details.put("moduleDetails", moduleDetails);
        details.put("associatedServiceDetailsOfTheModule", associatedServiceDetailsOfTheModule);
        return details;
    }

    public List<Map<String, Object>> getHealthPoliciesByPayer(String payerId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_HEALTH_POLICY_BY_PAYER_ID, new MapSqlParameterSource("payerId", payerId));
    }

    public Map<String, Object> getBenefitNameById(String benefitId) {
        Map<String, Object> result = namedParameterJdbcTemplate.queryForMap(FETCH_BENEFIT_NAME_BY_HIS_BENEFIT_ID, new MapSqlParameterSource("benefitId", benefitId));
        if(result != null) {
            return result;
        }
        return null;
    }

    public List<Map<String, Object>> getAllPayers() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_PAYERS, EmptySqlParameterSource.INSTANCE);
    }

    public List<Map<String, Object>> getAllCorporates() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_CORPORATES, EmptySqlParameterSource.INSTANCE);
    }
    public List<Map<String, Object>> getAllGroups() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_GROUP, EmptySqlParameterSource.INSTANCE);
    }

    public List<Map<String, Object>> getPolicyForIndividual() {
        return namedParameterJdbcTemplate.queryForList(FETCH_POLICY_FOR_INDIVIDUAL_ID,EmptySqlParameterSource.INSTANCE);
    }

    public List<Map<String, Object>> getPolicyByGroupId(String groupId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_POLICY_BY_GROUP_ID, new MapSqlParameterSource("groupId", groupId));
    }

    public List<Map<String, Object>> getPayerById(String payerId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_PAYER_BY_ID, new MapSqlParameterSource("payerId", payerId));
    }

    public List<Map<String, Object>> getHealthPolicyById(int id) {
        return namedParameterJdbcTemplate.queryForList(FETCH_HEALTH_POLICY_BY_ID, new MapSqlParameterSource("id", id));
    }

    public List<Map<String, Object>> getListOfInsuranceForGivenTenant(String tenantId, String facilityType) {
        return namedParameterJdbcTemplate.queryForList(FETCH_PAYERS_BY_TENANT_ID_AND_FACILITY_TYPE, new MapSqlParameterSource("facilityType", facilityType.trim().toUpperCase()).addValue("tenantId", tenantId.trim()));
    }

    public List<Map<String, Object>> getPlansForGivenInsurance(Long payerId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_PLANS_BY_PAYER_ID, new MapSqlParameterSource("payerId", payerId));
    }
}
