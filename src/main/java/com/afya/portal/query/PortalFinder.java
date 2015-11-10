package com.afya.portal.query;

import com.afya.portal.faq.view.dto.FaqDto;
import com.afya.portal.util.UtilValidator;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.nzion.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by Mohan Sharma on 3/17/2015.
 */
@Finder
@Component
public class PortalFinder {

    private static final String FETCH_PROVIDERS_BY_CLINICID = "SELECT dsa.clinic_id, d.doctor_id AS providerId, d.salutation, d.first_name AS firstName, d.last_name AS lastName, c.clinic_name AS clinicName,\n" +
            " c.clinic_id AS clinicId, d.qualification AS qualification,\n" +
            " d.address AS address, d.additional_address AS additionalAddress, d.city AS city, d.state AS state, d.country AS country,\n" +
            " d.zip AS postalCode, d.latitude AS latitude, d.longitude AS longitude, d.visiting_hours AS visitingHours, dsa.`speciality_code`, dr.`binary_data` AS profilePicture  FROM doctor d LEFT JOIN data_resource dr ON d.`profile_picture` = dr.`resource_id` JOIN clinic c \n" +
            "ON c.clinic_id = d.clinic_id \n" +
            "LEFT JOIN `doctor_speciality_assoc` dsa ON dsa.`doctor_id` = d.`doctor_id` AND dsa.clinic_id =c.clinic_id\n" +
            "WHERE dsa.clinic_id =:clinicId";

    private static final String FETCH_ALL_HOSPITALS = "SELECT hospital_id AS hospitalId, hospital_name AS hospitalName, location AS location, office_phone_number AS officePhoneNumber,\n" +
            "fax_number AS faxNumber, service_tax_number AS serviceTaxNumber, pan_number AS panNumber, drug_licence AS drugLicence, accr_number AS accrNumber,\n" +
            "valid_from AS validFrom, valid_to AS validTo, admin_first_name AS adminFirstName, admin_last_name AS adminLastName,\n" +
            "tenant_id AS tenantId, longitude AS longitude, latitude AS latitude, address AS address, additional_address AS additionalAddress,\n" +
            "postal_code AS postalCode, country AS country, state AS state FROM hospital ORDER BY hospital_name";
    private static final String FETCH_ALL_SPECIALITIES = "SELECT `SPECIALITY_CODE` AS specialityCode, `DESCRIPTION` AS description FROM `speciality`";

    private static final String FETCH_ALL_CORPORATES = "SELECT c.`CORPORATE_CODE` AS corporateCode,c.`CORPORATE_NAME` AS corporateName,c.`CORPORATE_SHORT_NAME` AS corporateShortName, c.`MODE_OF_CLAIM` AS modeOfClaim ,c.`CONTACT_NAME` AS contactName,\n" +
            "c.`ADDRESS1` AS address, c.`ADDRESS2` AS additionalAddress,`CITY` AS city, c.`STATE` AS state ,c.`ZIP` AS zip,c.`LANDLINE` AS landLine,c.`EXT` AS ext\n" +
            "FROM `corporate_master` c ORDER BY c.`CORPORATE_CODE`";

    private static final String FETCH_ALL_CORPORATE_PLANS = "SELECT cpm.`PLAN_CODE` AS planCode, cpm.`PLAN_NAME` AS planName, cm.`CORPORATE_CODE`, cm.`CORPORATE_NAME` FROM `corporate_master` cm JOIN `corporate_plan_master` cpm " +
            "ON cm.`CORPORATE_CODE` = cpm.`PLAN_PROVIDER`";

    private static final String FETCH_ALL_CORPORATE_PLANS_FOR_GIVEN_CORPORATE_CODE = "SELECT cpm.`PLAN_CODE` AS planCode, cpm.`PLAN_NAME` AS planName, cm.`CORPORATE_CODE`, cm.`CORPORATE_NAME` FROM `corporate_master` cm JOIN `corporate_plan_master` cpm ON " +
            "cm.`CORPORATE_CODE` = cpm.`PLAN_PROVIDER` AND cm.CORPORATE_CODE =:corporateCode ORDER BY cpm.`PLAN_NAME`";

    private static final String FETCH_ALL_GROUPS_BY_CLINIC_ID = "SELECT g.group_id AS groupId, g.group_name AS groupName FROM `clinic` c JOIN `clinic_insured_group_assoc` cig \n" +
            "ON c.clinic_id = cig.clinic_id JOIN `insured_group` ig ON cig.group_id = ig.id JOIN `group` g ON \n" +
            "ig.insured_group_id = g.group_id AND c.clinic_id=:clinicId";

    private static final String FETCH_ALL_GROUPS = "SELECT g.group_id AS groupId, g.group_name AS groupName FROM `group` g ORDER BY g.group_name";

    private static final String GET_GROUP_PLAN_DETAILS_BY_GROUP_ID = "SELECT * FROM `group_insurance_plan_details` WHERE insuredGroupId=:groupId AND relation=:dependent AND gender=:gender";
    private static final String GET_GROUP_PLAN_DETAILS_BY_POLICY_ID = "SELECT * FROM `group_insurance_plan_details` WHERE healthPolicyId=:policyId AND relation=:dependent AND gender=:gender";

    private static final String GET_GROUP_PLAN_DETAILS_BY_POLICY_ID_POLICY_PLAN = "SELECT * FROM `group_insurance_plan_details` WHERE healthPolicyId=:policyId AND relation=:dependent AND gender=:gender AND healthPolicyName=:policyName";

    private static final String FETCH_ALL_INSURANCES_FOR_TPA = "SELECT p.id, p.insurance_code AS insuranceCode, p.insurance_name AS insuranceName, p.payer_type AS payerType, \n" +
            "p.`INSURANCE_SHORT_NAME` AS insuranceShortName,p.`AUTHORIZATION_NO` AS authorizationNumber, p.`STATUTORY_INFO`\n" +
            "AS statutoryInfo, p.`MODE_OF_CLAIM` AS modeOfClaim,p.`CONTACT_NAME` AS contactName, p.`ADDRESS1` AS address1, \n" +
            "p.`ADDRESS2` AS address2, p.`STATE_ID` AS stateId, p.`DISTRICT_ID` AS districtId, p.`PINCODE` AS pincode, p.`COUNTRY_ID`\n" +
            "AS countryId, p.`FAX_NO` AS faxNo, p.`EMAIL` AS emailId, p.`WEBSITE` AS website, p.`PO_BOX_NO` AS poBoxNumber FROM payer p \n" +
            "JOIN `payer_insurance_assoc` pia ON p.id = pia.insurance_company_id AND pia.payer_id=(SELECT payer_id FROM `insured_group` WHERE insured_group_id =:groupId)";

    private static final String FETCH_ALL_MODULES_FOR_GIVEN_GROUP_ID = "SELECT m.id AS moduleId, m.module_name AS moduleName FROM `group_insurance_plan_details` gi JOIN `benefit_module_assoc` bma ON bma.benefit_id = gi.benefitPlanId \n" +
            "JOIN modules m ON m.id = bma.module_id WHERE insuredGroupId=:groupId GROUP BY m.id";

    private static final String FETCH_ALL_MODULES_FOR_GIVEN_POLICY_ID = "SELECT m.id AS moduleId, m.module_name AS moduleName FROM `group_insurance_plan_details` gi JOIN `benefit_module_assoc` bma ON bma.benefit_id = gi.benefitPlanId \n" +
            "JOIN modules m ON m.id = bma.module_id WHERE healthPolicyId=:policyId GROUP BY m.id";

    private static final String FETCH_ALL_MODULES_BY_BENEFIT_ID = "SELECT m.id AS moduleId, m.MODULE_NAME AS moduleName, m.SUM_INSURED AS sumAssured, m.FROM_AGE AS fromAge, m.HEALTH_POLICY_ID AS healthPolicyId,\n" +
            "m.TO_AGE AS toAge, m.PRE_HOSPITALISATION_LIMIT AS preHospitalisationLimit, m.PRE_HOSPITALISATION_DAYS AS\n" +
            "preHospitalisationDays, m.POST_HOSPITALISATION_LIMIT AS postHospitalisationLimit, m.POST_HOSPITALISATION_DAYS AS \n" +
            "postHospitalisationDays, m.WAITING_PERIOD AS waitingPeriod, m.EXCLUSIONPERIOD AS exclusionPeriod, m.SPECIAL_CLAUSE AS\n" +
            "specialClause, m.SPECIAL_EXCLUSIONS AS specialExclusions, m.NOTES AS notes, m.IS_MATERNITY AS isMaternity, m.IS_ACTIVE AS isActive,\n" +
            "COALESCE(m.copay_amount,0) AS copayAmount, COALESCE(m.copay_percentage,0) AS copayPercentage, COALESCE(m.deductable_amount,0) AS deductableAmount, \n" +
            "COALESCE(m.deductable_percentage,0) AS deductablePercentage, m.authorization AS authorization, m.authorization_inclusive_consultation \n" +
            "AS authorizationInclusiveConsultation,COALESCE(m.authorization_amount,0) AS authorizationAmount, m.authorization_required_consultation \n" +
            "AS authorizationRequiredConsultation   FROM `modules` m JOIN `benefit_module_assoc` bma ON \n" +
            "bma.module_id = m.id JOIN `benefit_plan` bp ON bp.id = bma.benefit_id AND bp.id =:benefitId";

    private final String FIND_ALL_INSURANCE_COMPANY_MASTER = "SELECT i.`INSURANCE_CODE` as insuranceCode,i.`INSURANCE_NAME` as insuranceName,i.`INSURANCE_SHORT_NAME` as insuranceShortName,i.`COMPANY_TYPE` as companyType," +
            "i.`PAYER_ID` as payerId, i.`AUTHORIZATION_NO` as authorizationId, i.`STATUTORY_INFO` as statuatoryInfo,i.`MODE_OF_CLAIM` as modeOfClaim ,i.`CONTACT_NAME` as contactName," +
            "i.`ADDRESS1` as address, i.`ADDRESS2` as additionalAddress,`CITY` as city, i.`STATE` as state ,i.`ZIP` as zip,i.`LANDLINE` as landLine,i.`EXT` as ext" +
            " FROM insurance_company_master i ORDER BY i.`INSURANCE_NAME`";

    private final String GET_ALL_COUNTRIES = "SELECT c.id, c.country, c.country_code as countryCode FROM country c ORDER BY c.country";

    private final String GET_ALL_NATIONALITY = "SELECT n.id, n.nationality FROM nationality n ORDER BY n.nationality";

    private final String GET_ALL_STATES = "SELECT s.id, s.state, s.state_code AS stateCode FROM state s ORDER BY s.state";

    private final String GET_ALL_CITIES = "SELECT c.id, c.city, c.city_code as cityCode FROM city c ORDER BY c.city";

    private final String GET_STATE_COUNTRY_BY_CITY = "SELECT * FROM city_state_country WHERE city=:city";

    private final String GET_ALL_STATES_OF_GIVEN_COUNTRY = "SELECT g.GEO_ID as geoId, g.geo_name as geoName ,cc.COUNTRY_NAME as countryName FROM geo g JOIN geo_assoc ga ON g.GEO_ID = ga.GEO_ID_TO JOIN `country_code` cc ON ga.GEO_ID = cc.COUNTRY_ABBR";

    private final String GET_PATIENT_BASED_ON_GIVEN_CRITERIA = "SELECT afya_id as afyaId, salutation AS salutation, first_name AS firstName, middle_name AS middleName, last_name AS lastName, gender AS gender, religion AS religion,\n" +
            "date_of_birth AS dateOfBirth, age AS age, marital_status AS maritalStatus, occupation AS occupation, employment_status AS employmentStatus, preferred_language AS preferredLanguage, email_id AS emailId, \n" +
            "communication_preference AS communicationPreference, mobile_number AS mobileNumber, fax_number AS faxNumber, office_phone AS officePhone, patient_type AS patientType, address AS address, additional_address AS additionalAddress, \n" +
            "city AS city, postal_code AS postalCode, state AS state, country AS country FROM patient WHERE first_name = :firstName AND last_name = :lastName AND DATE(date_of_birth)=DATE(:dateOfBirth) AND mobile_number=:mobileNumber";

    private final String GET_PATIENT_BY_AFYA_ID = "SELECT afya_id AS afyaId, salutation AS salutation, first_name AS firstName, middle_name AS middleName, last_name AS lastName, national_id_number AS nationalIdNumber, gender AS gender, religion AS religion,\n" +
            "date_of_birth AS dateOfBirth, age AS age, marital_status AS maritalStatus, occupation AS occupation, employment_status AS employmentStatus, preferred_language AS preferredLanguage, email_id AS emailId, \n" +
            "communication_preference AS communicationPreference, mobile_number AS mobileNumber, fax_number AS faxNumber, office_phone AS officePhone, patient_type AS patientType, address AS address, additional_address AS additionalAddress, \n" +
            "city AS city, postal_code AS postalCode, state AS state, country AS country FROM patient WHERE afya_id=:afyaId";

    private final String FETCH_ALL_CLINICS_AND_PHARMACIES = "SELECT clinic_id AS clinicId, clinic_name AS clinicName, location AS location, office_phone_number AS officePhoneNumber,\n" +
            "  fax_number AS faxNumber, service_tax_number AS serviceTaxNumber, pan_number AS panNumber, drug_licence AS drugLicence, accr_number AS accrNumber,\n" +
            "  valid_from AS validFrom, valid_to AS validTo, admin_first_name AS adminFirstName, admin_last_name AS adminLastName,\n" +
            "  tenant_id AS tenantId, longitude AS longitude, latitude AS latitude, address AS address, additional_address AS additionalAddress,\n" +
            "  postal_code AS postalCode, country AS country, state AS state,email AS email, 'clinic' AS facilityType FROM clinic\n" +
            "  WHERE(clinic_id) NOT IN\n" +
            "  (SELECT N.to_clinic_clinicId FROM NETWORK N JOIN clinic c ON N.clinic_clinicId = C.clinic_id\n" +
            "  WHERE N.status NOT IN ('REJECTED','UNBLOCKED') AND C.tenant_id =:tenantId\n" +
            "  UNION\n" +
            "  SELECT N.clinic_clinicId FROM NETWORK N JOIN clinic c ON N.to_clinic_clinicId = C.clinic_id\n" +
            "  WHERE  N.status NOT IN ('REJECTED','UNBLOCKED') AND C.tenant_id =:tenantId\n" +
            "  ) AND tenant_id !=:tenantId " +
            "  AND checkForActiveSubscription(clinic.tenant_id) = 1 AND (:packageServiceName IS NULL OR CHAR_LENGTH(:packageServiceName) = 0 " +
            "       OR checkForServiceSubscription(clinic.tenant_id, :packageServiceName) = 1)\n" +
            "  UNION\n" +
            "  SELECT pharmacy_id AS clinicId, pharmacy_name AS clinicName, location AS location, office_phone_number AS officePhoneNumber,\n" +
            "        fax_number AS faxNumber, service_tax_number AS serviceTaxNumber, pan_number AS panNumber, drug_licence AS drugLicence, accr_number AS accrNumber,\n" +
            "        valid_from AS validFrom, valid_to AS validTo, admin_first_name AS adminFirstName, admin_last_name AS adminLastName,\n" +
            "        tenant_id AS tenantId, longitude AS longitude, latitude AS latitude, address AS address, additional_address AS additionalAddress,\n" +
            "        postal_code AS postalCode, country AS country, state AS state,email AS email, 'pharmacy' AS facilityType FROM pharmacy\n" +
            "  WHERE(pharmacy_id) NOT IN\n" +
            "  (SELECT N.to_clinic_clinicId FROM NETWORK N JOIN pharmacy p ON N.clinic_clinicId = p.pharmacy_id\n" +
            "  WHERE N.status NOT IN ('REJECTED','UNBLOCKED') AND p.tenant_id =:tenantId\n" +
            "  UNION\n" +
            "  SELECT N.clinic_clinicId FROM NETWORK N JOIN pharmacy p ON N.to_clinic_clinicId = p.pharmacy_id\n" +
            "  WHERE N.status NOT IN ('REJECTED','UNBLOCKED') " +
            "       AND p.tenant_id =:tenantId) AND tenant_id !=:tenantId AND checkForActiveSubscription(pharmacy.tenant_id) = 1 AND (:packageServiceName IS NULL " +
            "       OR CHAR_LENGTH(:packageServiceName) = 0 OR checkForServiceSubscription(pharmacy.tenant_id, :packageServiceName) = 1)";

    private final String FETCH_ALL_CLINICS = "SELECT clinic_id as clinicId, clinic_name as clinicName, location as location, office_phone_number as officePhoneNumber," +
            "fax_number as faxNumber, service_tax_number as serviceTaxNumber, pan_number as panNumber, drug_licence as drugLicence, accr_number as accrNumber, " +
            "valid_from as validFrom, valid_to as validTo, admin_first_name as adminFirstName, admin_last_name as adminLastName," +
            "tenant_id as tenantId, longitude as longitude, latitude as latitude, address as address, additional_address as additionalAddress," +
            "postal_code as postalCode, country as country, state as state,email as email, " +
            "CONCAT(:imageBaseUrl, image_url) as imageUrl, logo_with_address as isLogoWithAddress, IF(checkForActiveSubscription(tenant_id) = 1, 'PREMIUM', 'COMMUNITY') AS providerMemberType " +
            "FROM clinic WHERE ((:includePremium = true AND checkForActiveSubscription(tenant_id) = 1) OR (:includeCommunity = true AND checkForActiveSubscription(tenant_id) = 0))" +
            "    AND (:packageServiceName IS NULL OR CHAR_LENGTH(:packageServiceName) = 0 OR checkForServiceSubscription(tenant_id, :packageServiceName) = 1) " +
            "ORDER BY clinic_name";

    private final String FETCH_ALL_PHARMACIES = "SELECT pharmacy_id AS pharmacyId, pharmacy_name AS pharmacyName, location AS location, office_phone_number AS officePhoneNumber,\n" +
            "fax_number AS faxNumber, service_tax_number AS serviceTaxNumber, pan_number AS panNumber, drug_licence AS drugLicence, accr_number AS accrNumber,\n" +
            "valid_from AS validFrom, valid_to AS validTo, admin_first_name AS adminFirstName, admin_last_name AS adminLastName,\n" +
            "tenant_id AS pharmacyTenantId, longitude AS longitude, latitude AS latitude, address AS address, additional_address AS additionalAddress,\n" +
            "postal_code AS postalCode, country AS country, state AS state  " +
            "FROM pharmacy WHERE checkForActiveSubscription(tenant_id) = 1 AND (:packageServiceName IS NULL OR CHAR_LENGTH(:packageServiceName) = 0 OR checkForServiceSubscription(tenant_id, :packageServiceName) = 1) " +
            "ORDER BY pharmacy_name";

    private final String FETCH_ALL_LABORATORIES = "SELECT lab_id AS labId, lab_name AS labName, location AS location, office_phone_number AS officePhoneNumber,\n" +
            "fax_number AS faxNumber, service_tax_number AS serviceTaxNumber, pan_number AS panNumber, drug_licence AS drugLicence, accr_number AS accrNumber,\n" +
            "valid_from AS validFrom, valid_to AS validTo, admin_first_name AS adminFirstName, admin_last_name AS adminLastName,\n" +
            "tenant_id AS tenantId, longitude AS longitude, latitude AS latitude, address AS address, additional_address AS additionalAddress,\n" +
            "postal_code AS postalCode, country AS country, state AS state  FROM laboratory ORDER BY lab_name";

    private final String FETCH_ALL_BLOOD_BANKS = "SELECT blood_bank_id as bloodBankId, blood_bank_name as bloodBankName,address as address, additional_address as additionalAddress, city as city, state as state, country as country, zip as postalCode, latitude as latitude, longitude as longitude FROM blood_bank ORDER BY blood_bank_name";

    private final String FETCH_ALL_INSURANCE_PLAN_MASTER = "SELECT ipm.plan_code AS planCode, ipm.PLAN_NAME AS planName, ipm.CONTACT_NAME AS contactName, ipm.ADDRESS1 AS ADDRESS, \n" +
            "ipm.ADDRESS2 AS additionalAddress, ipm.city AS city, ipm.state AS state, ipm.zip AS zip, ipm.state AS state, ipm.landline AS landline, ipm.ext AS ext, imm.INSURANCE_NAME AS insuranceName, imm.INSURANCE_CODE AS insuranceCode\n" +
            "FROM insurance_plan_master ipm JOIN insurance_company_master imm ON ipm.PLAN_PROVIDER = imm.insurance_code ORDER BY ipm.PLAN_NAME";

    private final String FETCH_ALL_SERVICES = "SELECT smg.`CODE` AS serviceMainGroupCode, smg.`SERVICE_MAIN_GROUP` AS serviceMainGroupDescription, \n" +
            "ssg.`code` AS serviceSubGroupCode, ssg.`description` AS serviceSubGroupDescription, s.`service_code` AS serviceCode,\n" +
            "s.`service_name` AS serviceName, s.`service_pneumonic` AS servicePneumonic FROM `service_main_group` smg JOIN `service_sub_group` ssg\n" +
            "ON smg.`CODE` = ssg.`service_main_group_id` JOIN `services` s ON s.`service_sub_group_id` = ssg.`code`";

    private final String FETCH_ALL_DOCTOR = "SELECT d.doctor_id AS providerId, d.salutation, d.first_name AS firstName, d.last_name AS lastName, c.clinic_name AS clinicName,c.clinic_id AS clinicId, c.location, d.qualification AS qualification, \n" +
            "d.address AS address, d.additional_address AS additionalAddress, d.city AS city, d.state AS state, d.country AS country,\n" +
            "d.zip AS postalCode, d.latitude AS latitude, d.longitude AS longitude, d.visiting_hours AS visitingHours, dsa.`speciality_code`, dr.`binary_data` as profilePicture, " +
            "c.longitude as longitude, c.latitude as latitude, " +
            "CONCAT(:imageBaseUrl, c.image_url) as imageUrl, c.logo_with_address as isLogoWithAddress, IF(checkForActiveSubscription(c.tenant_id) = 1, 'PREMIUM', 'COMMUNITY') AS providerMemberType " +
            "FROM doctor d  LEFT JOIN data_resource dr ON d.`profile_picture` = dr.`resource_id` JOIN clinic c \n" +
            "ON c.clinic_id = d.clinic_id LEFT JOIN `doctor_speciality_assoc` dsa ON dsa.`doctor_id` = d.doctor_id AND dsa.clinic_id =c.clinic_id " +
            "WHERE checkForActiveSubscription(tenant_id) = 1 AND (:packageServiceName IS NULL OR CHAR_LENGTH(:packageServiceName) = 0 OR checkForServiceSubscription(tenant_id, :packageServiceName) = 1) " +
            "ORDER BY d.first_name, d.last_name";

    private final String FETCH_USER_CIVIL_ID = "select civil_id as civilId, first_name as firstName, middle_name as middleName, last_name as lastName, end_most_name as endMostName," +
            "nationality as nationality, gender as gender, date_of_birth as dateOfBirth, blood_group as bloodGroup, rh as rh, " +
            "email_id as emailId, mobile_number as mobileNumber, telephone_number as officePhone, address as address, city as city, state as state, country as country from civil_data where civil_id =:civilId ";

    private final String FETCH_PATIENT_AFYAID = "SELECT p.afya_id AS afyaId, p.civil_id AS civilId, p.salutation AS salutation, p.first_name AS firstName, p.middle_name AS middleName, \n" +
            "p.last_name AS lastName,  p.end_most_name AS endMostName, p.gender, p.additional_address AS additionalAddress, p.address AS address, p.age AS age,\n" +
            "p.communication_preference AS communicationPreference, p.date_of_birth AS dateOfBirth, p.email_id AS emailId,\n" +
            "p.fax_number AS faxNumber, p.marital_status AS maritalStatus, p.mobile_number AS mobileNumber, p.isd_code AS isdCode, p.office_phone AS officePhone,\n" +
            "p.patient_type AS patientType, p.postal_code AS postalCode, p.city AS city, p.state AS state, p.country AS country, p.blood_group AS bloodGroup, p.rh AS rh,\n" +
            "p.home_phone AS homePhone, p.nationality AS nationality, pin.insurance_provider_id AS insuranceProviderId, pin.insurance_plan_id AS insurancePlanId,\n" +
            "pin.insurance_name AS insuranceName, pin.plan_name AS planName, pin.patient_plan_id AS patientPlanId, pin.patient_registration_no AS patientRegistrationNo, \n" +
            "pc.corporate_id AS corporateId, pc.corporate_plan_id AS corporatePlanId, pc.corporate_name AS corporateName, pc.corporate_plan_name AS corporatePlanName,\n" +
            "pc.contact_name AS contactName, pc.landline AS landline, pc.employee_id AS employeeId, pc.employee_role AS employeeRole FROM patient p \n" +
            "LEFT OUTER JOIN patient_insurance pin ON p.afya_id = pin.afya_id AND p.patient_type='INSURANCE' \n" +
            "LEFT OUTER JOIN patient_corporate pc ON p.afya_id = pc.afya_id AND p.patient_type='CORPORATE' WHERE p.afya_id = :afyaId";

    private final String FETCH_PATIENT_BY_CIVIL_ID = "SELECT p.afya_id AS afyaId, p.civil_id AS civilId, p.salutation AS salutation, p.first_name AS firstName, p.middle_name AS middleName, \n" +
            "p.last_name AS lastName,  p.end_most_name AS endMostName, p.gender, p.additional_address AS additionalAddress, p.address AS address, p.age AS age,\n" +
            "p.communication_preference AS communicationPreference, p.date_of_birth AS dateOfBirth, p.email_id AS emailId,\n" +
            "p.fax_number AS faxNumber, p.marital_status AS maritalStatus, p.mobile_number AS mobileNumber, p.isd_code AS isdCode, p.office_phone AS officePhone,\n" +
            "p.patient_type AS patientType, p.postal_code AS postalCode, p.city AS city, p.state AS state, p.country AS country, p.blood_group AS bloodGroup, p.rh AS rh,\n" +
            "p.home_phone AS homePhone, p.nationality AS nationality, pin.insurance_provider_id AS insuranceProviderId, pin.insurance_plan_id AS insurancePlanId,\n" +
            "pin.insurance_name AS insuranceName, pin.plan_name AS planName, pin.patient_plan_id AS patientPlanId, pin.patient_registration_no AS patientRegistrationNo, \n" +
            "pc.corporate_id AS corporateId, pc.corporate_plan_id AS corporatePlanId, pc.corporate_name AS corporateName, pc.corporate_plan_name AS corporatePlanName,\n" +
            "pc.contact_name AS contactName, pc.landline AS landline, pc.employee_id AS employeeId, pc.employee_role AS employeeRole FROM patient p \n" +
            "LEFT OUTER JOIN patient_insurance pin ON p.afya_id = pin.afya_id AND p.patient_type='INSURANCE' \n" +
            "LEFT OUTER JOIN patient_corporate pc ON p.afya_id = pc.afya_id AND p.patient_type='CORPORATE' WHERE p.civil_id = :civilId";

    private static final String FETCH_MODULE_DETAILS_BY_MODULEID = "SELECT m.id AS moduleId, COALESCE(m.copay_amount, 0) AS copayAmount, IFNULL(m.copay_percentage, 0)AS copayPercentage, COALESCE(m.deductable_amount,0) \n" +
            "AS deductableAmount, IFNULL(m.deductable_percentage, 0) AS deductablePercentage, COALESCE(m.authorization_amount, 0) AS \n" +
            "authorizationAmount, IFNULL(m.authorization, TRUE) AS authorization, m.authorization_inclusive_consultation AS authorizationInclusiveConsultation, m.authorization_required_consultation\n" +
            "AS authorizationRequiredConsultation, m.compute_by AS computeBy FROM `modules` m WHERE m.id =:moduleId";

    private static final String FETCH_SERVICES_BY_SERVICEID_AND_MODULEID = "SELECT sm.module_id AS moduleId, sm.service_id AS serviceId, COALESCE(sm.copay_amount, 0) AS copayAmount, IFNULL(sm.copay_percentage, msd.copayPercentage) AS copayPercentage, \n" +
            "COALESCE(sm.deductable_amount,0) AS deductableAmount, IFNULL(sm.deductable_percentage, msd.deductablePercentage) AS deductablePercentage, \n" +
            "IFNULL(IFNULL(sm.authorization, msd.authorization), 1) AS authorization, IFNULL(sm.authorization_inclusive_consultation, msd.authorizationInclusiveConsultation)\n" +
            "AS authorizationInclusiveConsultation,COALESCE(sm.authorization_amount, 0) AS authorizationAmount, \n" +
            "IFNULL(sm.authorization_required_consultation, msd.authorizationRequiredConsultation)AS authorizationRequiredConsultation, IFNULL(sm.compute_by, msd.computeBy) AS computeBy \n" +
            "FROM `module_service_assoc` sm JOIN `module_service_details` msd ON sm.service_id = msd.serviceId AND sm.module_id =:moduleId AND sm.service_id IN (:serviceIds)";

    private final String GET_PATIENT_BY_USERNAME = "{CALL getPatientByUsername(:username)}";

    private final String GET_PROVIDER_BY_USERNAME = "{CALL getProviderByUsername(:username)}";

    private String GET_CLINIC_BY_CLINICID = "SELECT * FROM clinic WHERE tenant_id=:clinicId";

    private String GET_PHARMACY_BY_PHARMACY_ID = "SELECT * FROM pharmacy WHERE tenant_id=:pharmacyId";

    private String GET_CORPORATE_MASTER_BY_CORPORATE_ID = "SELECT * FROM corporate_master WHERE corporate_code=:corporateId;";

    private final String FETCH_ENGLISH_CITY_BY_ARABIC_CITY = "SELECT city FROM city WHERE city_arabic=:city";


    private String GET_ALL_SERVICE_MASTER_ID = "SELECT m.`ID` as id, m.`SERVICE_NAME` as serviceName," +
            " m.`CODE` as  code, m.`PARENT_ID` as parentId ," +
            " m.`OP_SERVICE` as opCode FROM service_master m order by m.SERVICE_NAME";


    private String GET_USER_LOGIN_BY_USER_NAME = "SELECT u.`username` as userName, u.`password` as password, tenant_id as tenantId" +
            " FROM USERS u where u.username = :userName";

    private String GET_USER_LOGIN_BY_SESSION_ID = "SELECT u.`username` as userName, u.`password` as password" +
            " FROM USERS u, session s where u.username = s.user_name and s.session_id = :sessionId";

    private String GET_DOCTOR_TARIFF = "SELECT trf.BILLABLE_AMOUNT, trf.BILLABLE_AMOUNT_RCM , trf.BILLABLE_AMOUNT_TOTAL FROM consolidated_clinic_tariff trf " +
            " WHERE trf.CLINIC_ID = :clinicId AND trf.VISIT_TYPE_NAME = :visitTypeName AND trf.DOCTOR = :doctorId  " +
            "  AND trf.PATIENT_CATEGORY = '01'AND trf.TARIFF_CATEGORY = '00' ";

    private String GET_UPCOMING_APPOINTMENTS = "{CALL getUpcomingAppointments(:afya_id)}";

    private String GET_PATIENT_SPENDING_BY_SERVICES_CONSOLIDATED = "{CALL getPatientSpendingByServicesConsolidated(:afya_id, :fromDate, :toDate)}";

    private  String GET_PATIENT_VISIT_COUNT_BY_FACILITY_CONSOLIDATED = "{CALL getPatientVisitCountByFacilityConsolidated(:afya_id, :fromDate, :toDate)}";

    private String GET_PATIENT_VISIT_COUNT_BY_SERVICES_CONSOLIDATED = "{CALL getPatientVisitCountByServicesConsolidated(:afya_id, :fromDate, :toDate)}";

    private String ADD_PAYMENT_TRANSACTION_FOR_APPOINTMENT = "{CALL addPaymentTransactionForAppointment(:transactionType, :transactionAmount, :transactionTimestamp, :isysTrackingRef, :afyaId, :apptClinicId, :apptDoctorId, :apptSlot, :pharmacyOrderId, :username, :packageId, :processingFees, :payerType, :paymentChannel)}";
    //private String ADD_PAYMENT_TRANSACTION_FOR_APPOINTMENT = "{CALL addPaymentTransactionForAppointment(:transactionType, :transactionAmount, :transactionTimestamp, :isysTrackingRef, :afyaId, :apptClinicId, :apptDoctorId, :apptSlot)}";

    private String UPDATE_PAYMENT_TRANSACTION_WITH_ISYS_STATUS = "{CALL updatePaymentTransactionWithIsysStatus(:paymentId , :isysTrackingRef, :isysPaymentStatus, :isysPaymentStatusTimestamp, :isysHttpResponseStatus, :isysMerchantRef)}";
    //private String UPDATE_PAYMENT_TRANSACTION_WITH_ISYS_STATUS = "{CALL updatePaymentTransactionWithIsysStatus(:paymentId , :isysTrackingRef, :isysPaymentStatus, :isysPaymentStatusTimestamp, :isysHttpResponseStatus)}";

    private String GET_VISIT_HISTORY_CONSOLIDATED = "{CALL getVisitHistoryConsolidated(:afyaId , :fromDate, :toDate, :clinicName, :doctorName, :visitType)}";

    private String GET_NEWS = "SELECT news_id as newId, news_date as newsDate, news_content as newsContent FROM news WHERE news_target = :newsTarget ORDER BY newsDate DESC LIMIT 10";

    private String GET_PATIENT_ACTIVE_PRESCRIPTION = "{CALL getPatientActivePrescription(:afyaId, :fromDate, :toDate, :drugName, :clinicName , :doctorName)}";

    private String GET_INVOICE_RECORDS_CONSOLIDATED = "{CALL getInvoiceRecordsConsolidated(:afyaId, :fromDate, :toDate, :providerType, :doctorName , :invoiceNumber)}";

    private String GET_INVOICE_RECORDS_CONSOLIDATED_FOR_SCHEDULE = "{CALL getInvoiceRecordsConsolidatedForSchedule(:clinicId, :scheduleId)}";

    private String GET_PHARMACY_INVOICE_RECORDS_CONSOLIDATED_FOR_ORDER = "{CALL getPharmacyInvoiceRecordsConsolidatedForOrder(:pharmacyId, :orderId)}";

    private static final String GET_INSURANCE_BY_AFYA_ID = "SELECT ID, GROUP_NAME AS groupName, POLICY_NO AS policyNo, HEALTH_POLICY_ID AS healthPolicyId, HEALTH_POLICY_NAME AS healthPolicyName, \n" +
            "BENEFIT_ID AS benefitId, BENEFIT_NAME AS benefitName, UHID, PATIENT_TYPE AS memberType, RELATION AS relationship, MEMBERPATIENT AS primaryUHID, \n" +
            "INSURANCE_TYPE AS insuranceType, GROUP_ID AS groupId, INSURANCE_CODE AS insuranceCode, INSURANCE_NAME AS insuranceName, \n" +
            "START_DATE AS startDate, END_DATE AS endDate FROM consolidated_patient_insurance WHERE AFYA_ID =:afyaId";
    private static final String QUERY_TO_GET_ALL_FAQs ="select question_id, question,answer,question_category, question_level from faq";
    private static final String QUERY_TO_SEARCH_FAQs ="select question_id, question,answer,question_category, question_level  from faq where question LIKE :searchParam";

    private static final String GET_PATIENT_CONSENT_BY_USERNAME = "SELECT pcm.consent_id AS consentId, pcm.question\n" +
            ",  CASE IFNULL(pc.consent_yes, 0) WHEN 1 THEN 'true' ELSE 'false' END AS consentIsYes\n" +
            "FROM patient_consent_master pcm\n" +
            "INNER JOIN users ON users.username = :username\n" +
            "LEFT OUTER JOIN patient_consent pc ON pc.consent_id = pcm.consent_id AND pc.username = :username;";

    private static final String UPDATE_PATIENT_CONSENT_FOR_USERNAME = "INSERT INTO patient_consent(username, consent_id, consent_yes, created_tx_timestamp, updated_tx_timestamp)\n" +
            "VALUES (:username, :consent_id, :consent_yes, NOW(), NOW()) \n" +
            "ON DUPLICATE KEY UPDATE consent_yes = :consent_yes, updated_tx_timestamp = NOW()";

    private static final String GET_PATIENT_CONSENT_BY_AFYA_ID = "SELECT pcm.consent_id AS consentId, pcm.question, CASE IFNULL(pc.consent_yes, 0) WHEN 1 THEN 'true' ELSE 'false' END AS answer\n" +
            "FROM patient_consent_master pcm \n" +
            "LEFT OUTER JOIN patient p ON p.afya_id=:afyaId \n" +
            "LEFT OUTER JOIN users ON ((users.username = p.email_id AND users.login_preference = 'EMAIL')\n" +
            "OR (users.username = p.mobile_number AND users.login_preference = 'MOBILE'))\n" +
            "LEFT JOIN patient_consent pc ON pc.consent_id = pcm.consent_id  AND pc.username = users.username";

    private static final String GET_PATIENT_GATEWAY_TRANS_BY_ID = "SELECT payment_id as paymentId, transaction_type as transactionType," +
            "transaction_timestamp as transactionTimestamp, isys_tracking_ref as isysTrackingRef, payer_type as payerType," +
            "payment_channel as paymentChannel FROM payment_gateway_transaction WHERE payment_id=:paymentId";

   /* private static final String UPDATE_PATIENT_CONSENT_AFYA_ID = "INSERT INTO patient_consent(username, consent_id, consent_yes, created_tx_timestamp, updated_tx_timestamp)\n" +
            "VALUES ((SELECT * FROM users  WHERE (users.username = )), :consent_id, :consent_yes, NOW(), NOW()) \n" +
            "ON DUPLICATE KEY UPDATE consent_yes = :consent_yes, updated_tx_timestamp = NOW()";*/

    private static final String UPDATE_SCHEDULE_STATUS_TO_CONFIRMED = "{CALL updateScheduleStatusToConfirmed(:scheduleId, :clinicId, :username)}";

    private static final String GET_PHARMACY_ORDERS_FROM_AFYAID = "{CALL getPharmacyOrdersFromAfyaId(:afyaId, :openOrdersOnly)}";

    private String GET_PROVIDER_SERVICES = "{CALL getProviderServices(:packageType , :facilityId)}";

    private String GET_SUBSCRIPTION_HISTORY = "{CALL getPackageSubscriptionHistoryFromProviderUsername(:username)}";

    private String GET_PATIENT_SMART_SERVICES_PACKAGE_HISTORY = "{CALL getPackagePatientSmartServicesUsageFromProviderUsername(:username, :visitType)}";

    private String GET_NOTIFICATION_HISTORY = "{CALL getPackageNotificationHistory(:username)}";

    private String GET_NOTIFICATION_SUBSCRIPTION = "{CALL getPackageNotificationSubscription(:username, :dataOption)}";

    private String GET_PROVIDER_SERVICECOMPARELIST = "{CALL getProviderServiceCompareList(:packageIds)}";

    private String GET_PROVIDER_SERVICE_ITEMLIST = "{CALL getProviderServiceItemDetails(:packageId)}";

    private String GET_PACKAGE_TRANSCTION_DETAIL = "{CALL getPackageTransctionDetail(:paymentId)}";

    private String GET_TRANSCTION_DETAIL_BY_USERNAME = "{CALL getTransactionDetailByUsername(:username)}";

    private String GET_ALL_PREMIUM_MEMBER = "{CALL getAllPremiumMember()}";

    private String GET_PAYMENT_PROCESSING_FEES = "{CALL getPaymentProcessingFees(:payerType, :amount)}";

    private String GET_ACTIVE_PACKAGE_SERVICE_USAGE_FOR_TENANT = "{CALL getActivePackageServiceUsageForTenant(:tenantId)}";

    private String GET_PACKAGE_NOTIFICATION_USAGE_FROM_PROVIDER_USERNAME = "{CALL getPackageNotificationUsageFromProviderUsername(:username)}";

    private String GET_USER_SUBSCRIPTION_HISTORY = "SELECT pp.package_id AS packageId, pp.package_name AS packageName, pps.service_id AS serviceId, pps.service_name AS serviceName\n" +
            "\t, pps.service_type AS serviceType, pp.package_category AS packageCategory, pp.solution_name AS solutionName, ush.amount AS amount, ush.expiry_date AS expiryDate, ush.expiry_days AS expiryDays\n" +
            "\t, ush.number_ofsms AS numberOfSMS, ush.packs AS packs, ush.rate AS rate, ush.subscription_date AS subscriptionDate \n" +
            "\t, ush.number_of_hours AS numberOfHours, ush.username AS username -- , ush.* \n" +
            "FROM user_notification_subscription_history ush\n" +
            "INNER JOIN price_package_service pps ON pps.service_id = ush.service_id\n" +
            "INNER JOIN price_package pp ON pp.package_id = ush.package_id\n" +
            "WHERE pps.service_type = :serviceType AND ush.username = :username" +
            "\tAND (:packageId IS NULL OR :packageId = ush.package_id) AND (:packageCategory IS NULL OR :packageCategory = pp.package_category)\n" +
            "\tAND (:isSubscriptionActive IS NULL OR (:isSubscriptionActive = 0 AND ush.expiry_date < CURDATE()) OR (:isSubscriptionActive = 1 AND ush.expiry_date >= CURDATE()));\n";

    private  String GET_PROVIDER_POILICY_FOR_SERVICE = "SELECT afya_provider_policy FROM smart_service_policy WHERE service_name = :serviceName";

    private  String GET_PATIENT_POILICY_FOR_SERVICE = "SELECT afya_patient_policy FROM smart_service_policy WHERE service_name = :serviceName";

    private String GET_ALL_INVITED_PREMIUM_MEMBER = "{CALL getInvitedPremiumMember(:tenantId)}";

    private String VALIDATE_RCM_FOR_RESCHEDULE = "{CALL validateRcmForReschedule(:clinicId, :scheduleId)}";

    private final String GET_PROVIDER_SUMMARY = "{CALL getProviderSummary(:summaryType)}";

    private final String UPDATE_LAST_LOGGEDIN_TIMESTAMP = "UPDATE users SET users.last_logged_in_timestamp = NOW() WHERE users.username = :username AND is_verified = 1;";

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public PortalFinder(@Qualifier("primaryDataSource") DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> getAllInsuranceCompanyMaster() {
        return namedParameterJdbcTemplate.queryForList(FIND_ALL_INSURANCE_COMPANY_MASTER, new TreeMap<String, Object>());
    }
    public List<FaqDto> searchFaq(String searchParam) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("searchParam",searchParam);
        return namedParameterJdbcTemplate.query("select * from faq where question LIKE '%" + searchParam + "%'", parameterSource, new BeanPropertyRowMapper<>(FaqDto.class));
    }
    public List<Map<String, Object>> getAllFaq() {
        return namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_ALL_FAQs, EmptySqlParameterSource.INSTANCE);
    }
    public List<Map<String, Object>> getAllCountries() {
        return namedParameterJdbcTemplate.queryForList(GET_ALL_COUNTRIES, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> getAllCities() {
        return namedParameterJdbcTemplate.queryForList(GET_ALL_CITIES, EmptySqlParameterSource.INSTANCE);
    }

    public List<Map<String, Object>> getAllServiceMaster() {
        return namedParameterJdbcTemplate.queryForList(GET_ALL_SERVICE_MASTER_ID, new TreeMap<String, Object>());
    }

    public List<Map<String, Collection<Object>>> getAllStatesOfGivenCountry() {
        SqlParameterSource parameterSource = new MapSqlParameterSource();
        return namedParameterJdbcTemplate.query(GET_ALL_STATES_OF_GIVEN_COUNTRY, parameterSource, new StateExtractor());
    }

    public PatientDto getPatientBasedOnGivenCriteria(String firstName, String lastName, Date dateOfBirth, String afyaId, String mobileNumber) {
        PatientDto patientDto = null;
        if(UtilValidator.isNotEmpty(afyaId)){
            PatientDto patientDtoByAfyaId = fetchPatientBasedOnAfyaId(afyaId.trim());
            if(patientDtoByAfyaId != null)
                patientDto = patientDtoByAfyaId;
        }else if( UtilValidator.isEmpty(afyaId)){
            PatientDto patientDtoByCriteria = fetchPatientByGivenCriteria(firstName, lastName, dateOfBirth, mobileNumber);
            if(patientDtoByCriteria != null)
                patientDto = patientDtoByCriteria;
        }
        return patientDto;
    }

    private PatientDto fetchPatientByGivenCriteria(String firstName, String lastName, Date dateOfBirth, String mobileNumber) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("firstName", firstName).addValue("lastName", lastName).addValue("dateOfBirth", dateOfBirth).addValue("mobileNumber", mobileNumber);
        try {
            List<PatientDto> patientDto = namedParameterJdbcTemplate.query(GET_PATIENT_BASED_ON_GIVEN_CRITERIA, parameterSource, new BeanPropertyRowMapper<>(PatientDto.class));
            if (patientDto.size() == 0)
                return null;
            else
                return patientDto.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private PatientDto fetchPatientBasedOnAfyaId(String afyaId) {
        try {
            List<PatientDto> patientDto = namedParameterJdbcTemplate.query(GET_PATIENT_BY_AFYA_ID, new MapSqlParameterSource("afyaId", afyaId), new BeanPropertyRowMapper<>(PatientDto.class));
            if (patientDto.size() == 0)
                return null;
            else
                return patientDto.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ClinicDto> getAllClinics(String tenantId, String packageServiceName, boolean includePremium, boolean includeCommunity, String imageBaseUrl) {
        return namedParameterJdbcTemplate.query(FETCH_ALL_CLINICS, new MapSqlParameterSource().addValue("tenantId", tenantId)
                .addValue("packageServiceName", packageServiceName).addValue("includePremium", includePremium)
                .addValue("includeCommunity", includeCommunity).addValue("imageBaseUrl", imageBaseUrl), new BeanPropertyRowMapper<>(ClinicDto.class));
    }

    public List<ClinicDto> getAllClinicsAndPharmacies(String tenantId, String packageServiceName) {
        return namedParameterJdbcTemplate.query(FETCH_ALL_CLINICS_AND_PHARMACIES, new MapSqlParameterSource().addValue("tenantId", tenantId).addValue("packageServiceName", packageServiceName), new BeanPropertyRowMapper<>(ClinicDto.class));
    }

    public List<PharmacyDto> getAllPharmacies(String packageServiceName) {
        return namedParameterJdbcTemplate.query(FETCH_ALL_PHARMACIES, new MapSqlParameterSource().addValue("packageServiceName", packageServiceName), new BeanPropertyRowMapper<>(PharmacyDto.class));
    }

    public List<LabDto> getAllLaboratories() {
        return namedParameterJdbcTemplate.query(FETCH_ALL_LABORATORIES, new TreeMap<String, Object>(), new BeanPropertyRowMapper<>(LabDto.class));
    }

    public List<Map<String, Object>> getAllBloodBanks() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_BLOOD_BANKS, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> getAllInsurancePlanMaster() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_INSURANCE_PLAN_MASTER, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> getAllServices() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_SERVICES, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> getAllDoctors(String packageServiceName, String imageBaseUrl) {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_DOCTOR, new MapSqlParameterSource().addValue("packageServiceName", packageServiceName).addValue("imageBaseUrl",imageBaseUrl));
    }

    public Map<String, Object> fetchUserByCivilId(String civilId) {
        Map<String, Object> map;
        SqlParameterSource mapSqlParameterSource = new MapSqlParameterSource("civilId", civilId);
        List<Map<String, Object>> civilUserDtos = namedParameterJdbcTemplate.queryForList(FETCH_USER_CIVIL_ID, mapSqlParameterSource);
        if (civilUserDtos.size() == 0) {
            map = new HashMap<>();
            map.put("civilId", civilId);
        } else {
            map = civilUserDtos.get(0);
            map = furnishCity(map);
        }
        return map;
    }

    private Map<String, Object> furnishCity(Map<String, Object> map) {
        String city = (String)map.get("city");
        if(city.equals("") || city == null)
            return map;
        List<Map<String, Object>> cityList = namedParameterJdbcTemplate.queryForList(FETCH_ENGLISH_CITY_BY_ARABIC_CITY, new MapSqlParameterSource("city", city));
        if(cityList.size() > 0){
            Map<String, Object> result = cityList.get(0);
            if(!result.isEmpty())
                map.put("city", result.get("city"));
        }
        return map;
    }

    public Map<String, Object> fetchPatientByAfyaId(String afyaId) {
        List<Map<String, Object>> patientList = namedParameterJdbcTemplate.queryForList(FETCH_PATIENT_AFYAID, new MapSqlParameterSource("afyaId", afyaId));
        if (patientList.size() != 0)
            return patientList.get(0);
        else
            return new HashMap<>();
    }

    public Map<String, Object> fetchPatientByCivilId(String civilId) {
        List<Map<String, Object>> patientList = namedParameterJdbcTemplate.queryForList(FETCH_PATIENT_BY_CIVIL_ID, new MapSqlParameterSource("civilId", civilId));
        if (patientList.size() != 0)
            return patientList.get(0);
        else
            return new HashMap<>();
    }

    public List<Map<String, Object>> fetchProvidersByClinicId(String clinicId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_PROVIDERS_BY_CLINICID, new MapSqlParameterSource("clinicId", clinicId));
    }

    public List<Map<String, Object>> getAllHospitals() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_HOSPITALS, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> getAllSpecialities() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_SPECIALITIES, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> getAllCorporateMaster() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_CORPORATES, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> getAllCorporatePlan() {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_CORPORATE_PLANS, new TreeMap<String, Object>());
    }

    public List<Map<String, Object>> fetchCorporatePlanByCorporateCode(String corporateCode) {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_CORPORATE_PLANS_FOR_GIVEN_CORPORATE_CODE, new MapSqlParameterSource("corporateCode", corporateCode));
    }

    public List<Map<String, Object>> fetchListOfGroupNamesByClinicId(String clinicId) {
        List<Map<String, Object>> result;
        if (clinicId == null) {
            result = namedParameterJdbcTemplate.queryForList(FETCH_ALL_GROUPS, new TreeMap<String, Object>());
        } else {
            result = namedParameterJdbcTemplate.queryForList(FETCH_ALL_GROUPS_BY_CLINIC_ID, new MapSqlParameterSource("clinicId", clinicId));
        }
        return result;
    }

    public GroupInsuredPlanDto getPlanDetailsForGroupId(String groupId,String dependent,String gender) {
        List<Map<String, Object>> groupDetails = namedParameterJdbcTemplate.queryForList(GET_GROUP_PLAN_DETAILS_BY_GROUP_ID,
                new MapSqlParameterSource("groupId", groupId).addValue("dependent",dependent).addValue("gender",gender));
        GroupInsuredPlanDto groupInsuredPlanDto = new GroupInsuredPlanDto();
        if (groupDetails.size() > 0) {
            Map<String, Object> plan = groupDetails.get(0);
            Preconditions.checkNotNull(plan.get("payerType"), "Payer Type cannot be null");
            groupInsuredPlanDto.setInsuredGroupDetails(plan);
            groupInsuredPlanDto.setHealthPolicyDetails(plan);
            groupInsuredPlanDto.setBenefitDetails(groupDetails);
            String payerType = plan.get("payerType").toString();
            if (payerType.equals("TPA")) {
                groupInsuredPlanDto.populateTpaDetails(plan);
                groupInsuredPlanDto.setTpa(true);
                List<Map<String, Object>> insurancesForTpa = namedParameterJdbcTemplate.queryForList(FETCH_ALL_INSURANCES_FOR_TPA, new MapSqlParameterSource("groupId", groupId));
                groupInsuredPlanDto.setInsuranceForTpa(insurancesForTpa);
            } else {
                groupInsuredPlanDto.populateInsuranceDetails(plan);
            }
            List<Map<String, Object>> modules = namedParameterJdbcTemplate.queryForList(FETCH_ALL_MODULES_FOR_GIVEN_GROUP_ID, new MapSqlParameterSource("groupId", groupId));
            groupInsuredPlanDto.setModules(modules);
        }
        return groupInsuredPlanDto;
    }

    public GroupInsuredPlanDto getPlanDetailsForPolicyId(String policyId,String dependent,String gender) {
        List<Map<String, Object>> groupDetails = namedParameterJdbcTemplate.queryForList(GET_GROUP_PLAN_DETAILS_BY_POLICY_ID,
                new MapSqlParameterSource("policyId", policyId).addValue("dependent",dependent).addValue("gender",gender));
        GroupInsuredPlanDto groupInsuredPlanDto = new GroupInsuredPlanDto();
        if (groupDetails.size() > 0) {
            Map<String, Object> plan = groupDetails.get(0);
            Preconditions.checkNotNull(plan.get("payerType"), "Payer Type cannot be null");
            groupInsuredPlanDto.setInsuredGroupDetails(plan);
            groupInsuredPlanDto.setHealthPolicyDetails(plan);
            groupInsuredPlanDto.setBenefitDetails(groupDetails);
            List<Map<String, Object>> modules = namedParameterJdbcTemplate.queryForList(FETCH_ALL_MODULES_FOR_GIVEN_POLICY_ID, new MapSqlParameterSource("policyId", policyId));
            groupInsuredPlanDto.setModules(modules);
        }
        return groupInsuredPlanDto;
    }

    public GroupInsuredPlanDto getPlanDetailsForPolicyIdAndPolicyName(String policyId,String policyName,String dependent,String gender) {
        List<Map<String, Object>> groupDetails = namedParameterJdbcTemplate.queryForList(GET_GROUP_PLAN_DETAILS_BY_POLICY_ID_POLICY_PLAN,
                new MapSqlParameterSource("policyId", policyId).addValue("dependent",dependent).addValue("gender",gender).addValue("policyName",policyName));
        GroupInsuredPlanDto groupInsuredPlanDto = new GroupInsuredPlanDto();
        if (groupDetails.size() > 0) {
            Map<String, Object> plan = groupDetails.get(0);
            Preconditions.checkNotNull(plan.get("payerType"), "Payer Type cannot be null");
            groupInsuredPlanDto.setInsuredGroupDetails(plan);
            groupInsuredPlanDto.setHealthPolicyDetails(plan);
            groupInsuredPlanDto.setBenefitDetails(groupDetails);
            List<Map<String, Object>> modules = namedParameterJdbcTemplate.queryForList(FETCH_ALL_MODULES_FOR_GIVEN_POLICY_ID, new MapSqlParameterSource("policyId", policyId));
            groupInsuredPlanDto.setModules(modules);
        }
        return groupInsuredPlanDto;
    }


    public List<Map<String, Object>> getModulesByBenefitId(String benefitId) {
        return namedParameterJdbcTemplate.queryForList(FETCH_ALL_MODULES_BY_BENEFIT_ID, new MapSqlParameterSource("benefitId", benefitId));
    }

    public ModuleServiceDto getServiceOrModuleDataByServiceId(String moduleId, Set<Integer> serviceIds) {
        ModuleServiceDto moduleServiceDto;
        List<Map<String, Object>> modules = namedParameterJdbcTemplate.queryForList(FETCH_MODULE_DETAILS_BY_MODULEID, new MapSqlParameterSource("moduleId", moduleId));
        Map<String, Object> module;
        Preconditions.checkArgument(!modules.isEmpty(), "No Modules Found");
        module = modules.get(0);
        List<Map<String, Object>> services = namedParameterJdbcTemplate.queryForList(FETCH_SERVICES_BY_SERVICEID_AND_MODULEID,
                new MapSqlParameterSource("serviceIds", serviceIds).addValue("moduleId", moduleId));
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

    public ModuleServiceDto getModuleDataByModuleId(String moduleId) {
        List<Map<String, Object>> modules = namedParameterJdbcTemplate.queryForList(FETCH_MODULE_DETAILS_BY_MODULEID, new MapSqlParameterSource("moduleId", moduleId));
        Map<String, Object> module;
        Preconditions.checkArgument(!modules.isEmpty(), "No Modules Found");
        module = modules.get(0);
        return prepareModuleServiceDtoWhenNoServices(module);
    }

    public long getNextAfyaId() {
        long id = namedParameterJdbcTemplate.queryForLong("select sequence_cur_value from entity_sequence where sequence_name='patient_sequence'", EmptySqlParameterSource
                .INSTANCE);
        id=id+1;
        namedParameterJdbcTemplate.execute("update entity_sequence set sequence_cur_value= " + id + " where sequence_name='patient_sequence'", EmptySqlParameterSource.INSTANCE,
                new PreparedStatementCallback<Object>() {
                    @Override
                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        return ps.execute();
                    }
                });
        return id;
    }

    public Map<String, Object> getStateCountryBasedOnCity(String city) {
        List<Map<String, Object>> countries = namedParameterJdbcTemplate.queryForList(GET_STATE_COUNTRY_BY_CITY, new MapSqlParameterSource("city", city));
        return countries.size() > 0 ? countries.get(0) : Collections.EMPTY_MAP;
    }

    public List<Map<String, Object>> getAllStates() {
        return namedParameterJdbcTemplate.queryForList(GET_ALL_STATES, EmptySqlParameterSource.INSTANCE);
    }

    public Map<String, Object> getClinicDetailsByClinicId(String clinicId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_CLINIC_BY_CLINICID, new MapSqlParameterSource("clinicId", clinicId));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public Map<String, Object> getPharmacyDetailsByPharmacyId(String pharmacyId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PHARMACY_BY_PHARMACY_ID, new MapSqlParameterSource("pharmacyId", pharmacyId));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public List<Map<String, Object>> getAllNationality() {
        return namedParameterJdbcTemplate.queryForList(GET_ALL_NATIONALITY, EmptySqlParameterSource.INSTANCE);
    }

    public List<Map<String, Object>> fetchPatientsByGivenCriteria(String civilId, String afyaId, String firstName, String lastName, String mobileNumber, String gender, String dateOfBirth) {
        String QUERY_TO_FETCH_PATIENT_BY_CRITERIA = constructCriteriaQuery(civilId, afyaId, firstName, lastName, mobileNumber, gender, dateOfBirth);
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_PATIENT_BY_CRITERIA, new MapSqlParameterSource("civilId", constructLikeConditionString(civilId)).addValue("afyaId", constructLikeConditionString(afyaId)).addValue("firstName", constructLikeConditionString(firstName)).addValue("lastName", constructLikeConditionString(lastName)).addValue("mobileNumber", constructLikeConditionString(mobileNumber)).addValue("gender", constructLikeConditionString(gender)).addValue("dateOfBirth", dateOfBirth));
        return result;
    }

    private String constructCriteriaQuery(String civilId, String afyaId, String firstName, String lastName, String mobileNumber, String gender, String dateOfBirth) {
        String halfBakedQuery = "SELECT p.afya_id AS afyaId, p.civil_id AS civilId, p.salutation AS salutation, p.first_name AS firstName, p.middle_name AS middleName,p.last_name AS lastName,  p.end_most_name AS endMostName, p.gender, p.additional_address AS additionalAddress, p.address AS address, p.age AS age,p.communication_preference AS communicationPreference, p.date_of_birth AS dateOfBirth, p.email_id AS emailId,p.fax_number AS faxNumber, p.marital_status AS maritalStatus, p.mobile_number AS mobileNumber, p.isd_code AS isdCode, p.office_phone AS officePhone,p.patient_type AS patientType, p.postal_code AS postalCode, p.city AS city, p.state AS state, p.country AS country, p.blood_group AS bloodGroup, p.rh AS rh,p.home_phone AS homePhone, p.nationality AS nationality FROM PATIENT p WHERE";
        String str = StringUtils.EMPTY;
        StringBuffer stringBuffer = new StringBuffer(halfBakedQuery);
        if(UtilValidator.isNotEmpty(civilId))
            stringBuffer.append("(civil_id LIKE :civilId AND :civilId IS NOT NULL AND :civilId <> '') AND");
        if(UtilValidator.isNotEmpty(afyaId))
            stringBuffer.append(" (afya_id LIKE :afyaId AND :afyaId IS NOT NULL AND :afyaId  <> '') AND");
        if(UtilValidator.isNotEmpty(firstName))
            stringBuffer.append(" (first_name LIKE :firstName AND :firstName IS NOT NULL AND :firstName  <> '') AND");
        if(UtilValidator.isNotEmpty(lastName))
            stringBuffer.append(" (last_name LIKE :lastName AND :lastName IS NOT NULL AND :lastName <> '') AND");
        if(UtilValidator.isNotEmpty(mobileNumber))
            stringBuffer.append(" (mobile_number LIKE :mobileNumber AND :mobileNumber IS NOT NULL AND :mobileNumber  <> '') AND");
        if(UtilValidator.isNotEmpty(gender))
            stringBuffer.append(" (gender LIKE :gender AND :gender IS NOT NULL AND :gender  <> '') AND");
        if(UtilValidator.isNotEmpty(dateOfBirth))
            stringBuffer.append(" (date_of_birth=:dateOfBirth AND :dateOfBirth IS NOT NULL AND :dateOfBirth  <> '') AND");
        if(stringBuffer.toString().trim().endsWith("AND")){
            str = stringBuffer.substring(0, stringBuffer.length()-3);
        }
        stringBuffer = new StringBuffer(str);
        stringBuffer.append(" ORDER BY p.first_name, p.last_name");
        return stringBuffer.toString();
    }

    private String constructLikeConditionString(String value) {
        if(UtilValidator.isNotEmpty(value))
            return value.trim()+"%";
        return value;
    }

    private class StateExtractor implements ResultSetExtractor<List<Map<String, Collection<Object>>>> {
        List<Map<String, Collection<Object>>> allStates = Lists.newArrayList();
        Multimap<String, Object> countryWithStates = LinkedListMultimap.create();

        @Override
        public List<Map<String, Collection<Object>>> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            while (resultSet.next()) {
                countryWithStates.put(resultSet.getString("countryName"), resultSet.getString("geoName"));
            }
            allStates.add(countryWithStates.asMap());
            return allStates;
        }
    }

    public Map<String, Object> getUserLoginByUserName(String userName) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_USER_LOGIN_BY_USER_NAME, new MapSqlParameterSource("userName", userName));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public Map<String, Object> getUserLoginBySessionId(String sessionId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_USER_LOGIN_BY_SESSION_ID, new MapSqlParameterSource("sessionId", sessionId));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public Map<String, Object> getCorporateMasterByCorporateId(String corporateId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_CORPORATE_MASTER_BY_CORPORATE_ID, new MapSqlParameterSource("corporateId", corporateId));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }


    public Map<String, Object> getPatientByUsername(String username) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_BY_USERNAME,
                new MapSqlParameterSource("username", username));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public Map<String, Object> getProviderByUsername(String username) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PROVIDER_BY_USERNAME,
                new MapSqlParameterSource("username", username));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public Map<String, Object> getDoctorTariff(String clinicId, String visitTypeName, String doctorId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_DOCTOR_TARIFF,
                new MapSqlParameterSource("clinicId", clinicId).addValue("visitTypeName", visitTypeName)
                        .addValue("doctorId", doctorId));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public List<Map<String, Object>> getUpcomingAppointments(String afyaId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_UPCOMING_APPOINTMENTS,
                new MapSqlParameterSource("afya_id", afyaId));
        return result;
    }

    public List<Map<String, Object>> getPatientSpendingByServicesConsolidated(String afyaId, String fromDate, String toDate) {

        fromDate  = fromDate.length() == 0 ? null : fromDate;
        toDate  = toDate.length() == 0 ? null : toDate;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_SPENDING_BY_SERVICES_CONSOLIDATED,
                new MapSqlParameterSource("afya_id", afyaId).addValue("fromDate", fromDate, Types.TIMESTAMP).addValue("toDate", toDate, Types.TIMESTAMP));
        return result;
    }

    public List<Map<String, Object>> getPatientVisitCountByFacilityConsolidated(String afyaId, String fromDate, String toDate) {

        fromDate  = fromDate.length() == 0 ? null : fromDate;
        toDate  = toDate.length() == 0 ? null : toDate;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_VISIT_COUNT_BY_FACILITY_CONSOLIDATED,
                new MapSqlParameterSource("afya_id", afyaId).addValue("fromDate", fromDate, Types.TIMESTAMP).addValue("toDate", toDate, Types.TIMESTAMP));
        return result;
    }

    public List<Map<String, Object>> getPatientVisitCountByServicesConsolidated(String afyaId, String fromDate, String toDate) {

        fromDate  = fromDate.length() == 0 ? null : fromDate;
        toDate  = toDate.length() == 0 ? null : toDate;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_VISIT_COUNT_BY_SERVICES_CONSOLIDATED,
                new MapSqlParameterSource("afya_id", afyaId).addValue("fromDate", fromDate, Types.TIMESTAMP).addValue("toDate", toDate, Types.TIMESTAMP));
        return result;
    }

    public List<Map<String, Object>> getVisitHistoryConsolidated(String afyaId, String fromDate, String toDate, String clinicName, String doctorName, String visitType) {

        fromDate = fromDate.length() == 0 ? null : fromDate;
        toDate = toDate.length() == 0 ? null : toDate;
        clinicName = clinicName.length() == 0 ? null : clinicName;
        doctorName = doctorName.length() == 0 ? null : doctorName;
        visitType = visitType.length() == 0 ? null : visitType;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_VISIT_HISTORY_CONSOLIDATED,
                new MapSqlParameterSource("afyaId", afyaId).addValue("fromDate", fromDate, Types.TIMESTAMP).addValue("toDate", toDate, Types.TIMESTAMP)
                        .addValue("clinicName", clinicName).addValue("doctorName", doctorName).addValue("visitType", visitType));
        return result;
    }

    public Map<String, Object> addPaymentTransactionForAppointment(String transactionType, String transactionAmount, String transactionTimestamp,
                                                                   String isysTrackingRef, String afyaId, String apptClinicId, String apptDoctorId,
                                                                   String apptSlot, String pharmacyOrderId, String username, String packageId, String processingFees,
                                                                   String payerType, String paymentChannel) {

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(ADD_PAYMENT_TRANSACTION_FOR_APPOINTMENT,
                new MapSqlParameterSource("transactionType", transactionType).addValue("transactionAmount", transactionAmount)
                        .addValue("transactionTimestamp", transactionTimestamp, Types.TIMESTAMP).addValue("isysTrackingRef", isysTrackingRef)
                        .addValue("afyaId", afyaId).addValue("apptClinicId", apptClinicId).addValue("apptDoctorId", apptDoctorId)
                        .addValue("apptSlot", apptSlot).addValue("pharmacyOrderId", pharmacyOrderId).addValue("username", username)
                        .addValue("packageId", packageId).addValue("processingFees", processingFees).addValue("payerType", payerType)
                        .addValue("paymentChannel", paymentChannel));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public Map<String, Object> updatePaymentTransactionWithIsysStatus(String paymentId, String isysTrackingRef, String isysPaymentStatus, String isysPaymentStatusTimestamp, String isysHttpResponseStatus, String isysMerchantRef) {

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(UPDATE_PAYMENT_TRANSACTION_WITH_ISYS_STATUS,
                new MapSqlParameterSource("paymentId", paymentId).addValue("isysTrackingRef", isysTrackingRef)
                        .addValue("isysPaymentStatus", isysPaymentStatus).addValue("isysPaymentStatusTimestamp", isysPaymentStatusTimestamp)
                        .addValue("isysHttpResponseStatus", isysHttpResponseStatus).addValue("isysMerchantRef", isysMerchantRef));
        if(result.size() > 0)
            return result.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public List<Map<String, Object>> getNews(String newsTarget) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_NEWS,new MapSqlParameterSource().addValue("newsTarget", newsTarget));
        return result;
    }

    public List<Map<String, Object>> getPatientActivePrescription(String afyaId, String fromDate, String toDate, String drugName, String clinicName, String doctorName) {

        fromDate = (fromDate == null || fromDate.length() == 0) ? null : fromDate;
        toDate = (toDate == null || toDate.length() == 0) ? null : toDate;
        drugName = (drugName == null || drugName.length() == 0) ? null : drugName;
        clinicName = (clinicName == null || clinicName.length() == 0) ? null : clinicName;
        doctorName = (doctorName == null || doctorName.length() == 0) ? null : doctorName;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_ACTIVE_PRESCRIPTION,
                new MapSqlParameterSource("afyaId", afyaId).addValue("fromDate", fromDate, Types.TIMESTAMP).addValue("toDate", toDate, Types.TIMESTAMP)
                        .addValue("clinicName", clinicName).addValue("doctorName", doctorName).addValue("drugName", drugName));
        return result;
    }

    public List<Map<String, Object>> getInvoiceRecordsConsolidated(String afyaId, String fromDate, String toDate, String providerType, String doctorName , String invoiceNumber) {

        fromDate = fromDate.length() == 0 ? null : fromDate;
        toDate = toDate.length() == 0 ? null : toDate;
        doctorName = doctorName.length() == 0 ? null : doctorName;
        providerType = providerType.length() == 0 ? null : providerType;
        invoiceNumber = invoiceNumber.length() == 0 ? null : invoiceNumber;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_INVOICE_RECORDS_CONSOLIDATED,
                new MapSqlParameterSource("afyaId", afyaId).addValue("fromDate", fromDate, Types.TIMESTAMP).addValue("toDate", toDate, Types.TIMESTAMP)
                        .addValue("doctorName", doctorName).addValue("doctorName", doctorName).addValue("providerType", providerType)
                        .addValue("invoiceNumber", invoiceNumber));
        return result;
    }

    public List<Map<String, Object>> getInvoiceRecordsConsolidatedForSchedule(String clinicId, String scheduleId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_INVOICE_RECORDS_CONSOLIDATED_FOR_SCHEDULE,
                new MapSqlParameterSource("clinicId", clinicId).addValue("scheduleId", scheduleId));
        return result;
    }

    public List<Map<String, Object>> getPharmacyInvoiceRecordsConsolidatedForOrder(String pharmacyId, String orderId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PHARMACY_INVOICE_RECORDS_CONSOLIDATED_FOR_ORDER,
                new MapSqlParameterSource("pharmacyId", pharmacyId).addValue("orderId", orderId));
        return result;
    }

    public List<Map<String, Object>> getInsuranceByAfyaId(String afyaId) {

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_INSURANCE_BY_AFYA_ID,
                new MapSqlParameterSource("afyaId", afyaId));
        return result;
    }

    public List<Map<String, Object>> getPatientConsentByUsername(String username) {

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_CONSENT_BY_USERNAME,
                new MapSqlParameterSource("username", username));
        return result;
    }

    public /*Map<String, String>*/ String updatePatientConsentForUsername(String username, List<PatientConsentDto> patientConsentDtoList) {
        for(int i=0; i < patientConsentDtoList.size(); i++){
            namedParameterJdbcTemplate.update(UPDATE_PATIENT_CONSENT_FOR_USERNAME, new MapSqlParameterSource("username", username)
                    .addValue("consent_id",patientConsentDtoList.get(i).getConsentId())
                    .addValue("consent_yes", patientConsentDtoList.get(i).getConsent() == true ? 1 : 0));
        }
        //Map<String, String> result = new HashMap<String, String>(); result.put("result","success");
        // return result;
        return   "SUCCESS";
    }

    public List<Map<String, Object>> getPatientConsentForAfyaId(String afyaId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_CONSENT_BY_AFYA_ID, new MapSqlParameterSource("afyaId", afyaId));
        return result;
    }


    public List<Map<String, Object>> getPaymentGatewayTransactionById(String paymentId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_GATEWAY_TRANS_BY_ID, new MapSqlParameterSource("paymentId", paymentId));
        return result;
    }

   /* public String updatePatientConsentForAfyaId(String afyaId, List<PatientConsentDto> patientConsentDtoList) {
        for(int i=0; i < patientConsentDtoList.size(); i++){
            namedParameterJdbcTemplate.update(UPDATE_PATIENT_CONSENT_FOR_AFYA_ID, new MapSqlParameterSource("username", username)
                    .addValue("consent_id",patientConsentDtoList.get(i).getConsentId())
                    .addValue("consent_yes", patientConsentDtoList.get(i).getConsent() == true ? 1 : 0));
        }
        //Map<String, String> result = new HashMap<String, String>(); result.put("result","success");
        // return result;
        return   "SUCCESS";
    }*/

    public String updateScheduleStatusToConfirmed(String scheduleId, String clinicId, String username) {
        namedParameterJdbcTemplate.update(UPDATE_SCHEDULE_STATUS_TO_CONFIRMED
                , new MapSqlParameterSource("scheduleId", scheduleId)
                .addValue("clinicId",clinicId).addValue("username",username));
        return  "SUCCESS";
    }

    public List<Map<String, Object>> getPharmacyOrdersFromAfyaId(String afyaId, boolean openOrdersOnly) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PHARMACY_ORDERS_FROM_AFYAID
                , new MapSqlParameterSource("afyaId", afyaId).addValue("openOrdersOnly", openOrdersOnly));
        return  result;
    }

    public List<Map<String, Object>> getProviderServices(String packageType, String facilityId) {

        packageType = packageType.length() == 0 ? null : packageType;
        facilityId = facilityId.length() == 0 ? null : facilityId;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PROVIDER_SERVICES,
                new MapSqlParameterSource("packageType", packageType).addValue("facilityId", facilityId));
        return result;
    }

    public List<Map<String, Object>> getSubscriptionHistory(String username) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_SUBSCRIPTION_HISTORY,
                new MapSqlParameterSource("username", username));
        return result;
    }

    public List<Map<String, Object>> getPatientSmartServicesPackageHistory(String username, String visitType) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_SMART_SERVICES_PACKAGE_HISTORY,
                new MapSqlParameterSource("username", username).addValue("visitType", visitType));
        return result;
    }

    public List<Map<String, Object>> getPackageNotificationHistory(String username) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_NOTIFICATION_HISTORY,
                new MapSqlParameterSource("username", username));
        return result;
    }

    public List<Map<String, Object>> getPackageNotificationSubscription(String username, String dataOption) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_NOTIFICATION_SUBSCRIPTION,
                new MapSqlParameterSource("username", username).addValue("dataOption", dataOption));
        return result;
    }

    public List<Map<String, Object>> getProviderServiceCompareList(String packageIds) {

        packageIds = packageIds.length() == 0 ? null : packageIds;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PROVIDER_SERVICECOMPARELIST,
                new MapSqlParameterSource("packageIds", packageIds));
        return result;
    }

    public List<Map<String, Object>> getProviderServiceItemDetails(String packageId) {

        packageId = packageId.length() == 0 ? null : packageId;

        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PROVIDER_SERVICE_ITEMLIST,
                new MapSqlParameterSource("packageId", packageId));
        return result;
    }

    public List<Map<String, Object>> getPackageTransctionDetail(int paymentId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PACKAGE_TRANSCTION_DETAIL,
                new MapSqlParameterSource("paymentId", paymentId));
        return result;
    }

    public List<Map<String, Object>> getTransactionDetailByUsername(String username) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_TRANSCTION_DETAIL_BY_USERNAME,
                new MapSqlParameterSource("username", username));
        return result;
    }

    public List<FaqDto> getFaqBySearchParam(){
        SqlParameterSource parameterSource = new MapSqlParameterSource();
        List<FaqDto> faqDtos = namedParameterJdbcTemplate.query("SELECT * FROM `faq` GROUP BY `question_level` LIMIT 3", parameterSource, new BeanPropertyRowMapper<>(FaqDto.class));
        return faqDtos;
    }

    public List<Map<String, Object>> getAllPremiumMember() {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_ALL_PREMIUM_MEMBER,
                new MapSqlParameterSource());
        return result;
    }

    public List<Map<String, Object>> getPaymentProcessingFees(String payerType, String amount) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PAYMENT_PROCESSING_FEES,
                new MapSqlParameterSource("payerType", payerType).addValue("amount", amount));
        return result;
    }

    public List<Map<String, Object>> getActivePackageServiceUsageForTenant(String tenantId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_ACTIVE_PACKAGE_SERVICE_USAGE_FOR_TENANT,
                new MapSqlParameterSource("tenantId", tenantId));
        return result;
    }

    public List<Map<String, Object>> getPackageNotificationUsageFromProviderUsername(String username) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PACKAGE_NOTIFICATION_USAGE_FROM_PROVIDER_USERNAME,
                new MapSqlParameterSource("username", username));
        return result;
    }

    public List<Map<String, Object>> getUserSubscriptionHistory(String username, String serviceType, String packageId, String packageCategory, Boolean isSubscriptionActive) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_USER_SUBSCRIPTION_HISTORY,
                new MapSqlParameterSource("username", username).addValue("serviceType", serviceType).addValue("packageId", packageId)
                        .addValue("packageCategory", packageCategory).addValue("isSubscriptionActive", isSubscriptionActive));
        return result;
    }

    public String getProviderPoilicyForService(String serviceName) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PROVIDER_POILICY_FOR_SERVICE,
                new MapSqlParameterSource("serviceName", serviceName));
        return result.isEmpty() ? "": result.get(0).get("afya_provider_policy").toString();
    }

    public String getPatientPoilicyForService(String serviceName) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_PATIENT_POILICY_FOR_SERVICE,
                new MapSqlParameterSource("serviceName", serviceName));
        return result.isEmpty() ? "": result.get(0).get("afya_patient_policy").toString();
    }

    public List<Map<String, Object>> getAllInvitedPremiumMember(String tenantId) {
        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(GET_ALL_INVITED_PREMIUM_MEMBER,
                new MapSqlParameterSource("tenantId", tenantId));
        return result;
    }

    public Map<String, Object> validateRcmForReschedule(String clinicId, long scheduleId) {
        List<Map<String, Object>> resultSet = namedParameterJdbcTemplate.queryForList(VALIDATE_RCM_FOR_RESCHEDULE, new MapSqlParameterSource("clinicId", clinicId).addValue("scheduleId", scheduleId));
        if(resultSet.size() > 0)
            return resultSet.get(0);
        else
            return Collections.EMPTY_MAP;
    }

    public List<Map<String, Object>> getProviderSummary(String summaryType) {
        return namedParameterJdbcTemplate.queryForList(GET_PROVIDER_SUMMARY, new MapSqlParameterSource().addValue("summaryType", summaryType));
    }

    public int updateLastLoggedInTimestamp(String userName){
        return namedParameterJdbcTemplate.update(UPDATE_LAST_LOGGEDIN_TIMESTAMP, new MapSqlParameterSource().addValue("username", userName));
    }
}