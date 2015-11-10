package com.afya.portal.query;

import com.afya.portal.util.UtilDateTime;
import com.google.common.collect.Maps;
import com.nzion.dto.PatientDto;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.nthdimenzion.ddd.domain.annotations.Finder;
import org.nthdimenzion.utils.UtilValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 7/9/2015.
 */
@Component
@Finder
public class PortalUtilityFinder {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final String QUERY_TO_GET_IS_VERIFIED = "Select is_verified from users where username=:username";

    private static final String QUERY_TO_GET_NATIONALITY_BY_NATIONALITY_CODE = "SELECT n.id, n.nationality, n.code FROM nationality n WHERE n.code=:nationalityCode";
    private static final String GET_PACKAGE_SERVICE_FOR_SERVICE_TYPE_PATIENT = "SELECT package_service_id AS packageServiceId, package_id AS packageId, service_name serviceName, service_type AS serviceType FROM package_service WHERE service_type = 'Patient'";
    private static final String QUERY_TO_GET_PACKAGES_BY_GIVEN_CRITERIA = "SELECT p.`package_id` AS packageId, p.`package_name` AS packageName, p.`solution_name` AS packageDescription, \n" +
                                                                            "p.`package_type` AS packageType, p.`UOM`, p.`currency` FROM `price_package` p JOIN `user_package` ups \n" +
                                                                            "ON p.`package_id` = ups.`package_id` JOIN users u ON u.username = ups.`username` AND u.username=:username AND p.package_type=:facilityTypeOfUser AND p.package_category='SERVICE'";
    private static final String QUERY_TO_GET_PACKAGES_BY_USER_FACILITY_TYPE = "SELECT p.`package_id` AS packageId, p.`package_name` AS packageName, p.`solution_name` AS packageDescription, \n" +
                                                                            "p.`package_type` AS packageType, p.`UOM`, p.`currency` FROM `price_package` p WHERE p.package_type=:facilityTypeOfUser AND p.package_category='SERVICE'";
    private static final String QUERY_TO_GET_FACILITY_TYPE_FOR_USER = "SELECT `facility_type` as facilityType FROM `user_tenant_assoc` WHERE user_name = :username";
    private static final String QUERY_TO_GET_TRAINING_SESSION_SCHEDULES_FOR_GIVEN_PACKAGE = "SELECT ts.`id`, ts.`session`, ts.`training_session_description` AS trainingSessionDescription, ts.`training_duration` AS trainingDuration, \n" +
                                                                                            "ts.`practice_session` AS practiceSession, ts.`practice_duration` AS practiceDuration, ts.`assessment_session` AS assessmentSession,\n" +
                                                                                            "ts.`assessment_duration` AS assessmentDuration, ts.`is_active_service` AS isActiveService, p.`package_id` AS packageId, p.`package_name` \n" +
                                                                                            "AS packageName, p.`solution_name` AS packageDescription, p.`package_type` AS packageType, p.`UOM`, p.`currency` FROM `training_session_schedules` \n" +
                                                                                            "ts JOIN `price_package` p ON ts.package_id = p.package_id AND ts.`package_id`=:packageId ORDER BY ts.session, ts.order_of_display";
    private static final String QUERY_TO_GET_VISIT_HISTORY_FOR_VIEW = "SELECT p.afya_id AS patientId, s.id AS appointmentNo, p.first_name AS firstName, p.last_name AS lastName, s.start_date AS appointmentDate, p.gender AS gender, p.age AS age, s.visit_type AS service,\n" +
                                                                        "s.start_time AS appointmentTime, s.comments AS remarks FROM `consolidated_schedule` s JOIN patient p ON s.afya_id = p.afya_id AND p.afya_id=:afyaId AND s.start_date=:startDate AND s.person_id=:doctorId AND s.start_time=:visitStartDateTime AND end_time=:visitEndDateTime";
    private static final String QUERY_TO_FETCH_INVOICE_DETAILS_FOR_GIVEN_VISIT_FROM_GIVEN_TENANT = "{CALL getInvoiceDetailsForGivenVisit(:tenantId, :afyaId, :doctorId, :startDate, :visitStartDateTime, :visitEndDateTime)}";
    private static final String QUERY_TO_FETCH_INVOICE_DETAILS_FOR_GIVEN_INVOICE_FROM_GIVEN_TENANT = "{CALL getInvoiceDetailsForGivenInvoice(:tenantId, :invoiceId)}";
    private static final String QUERY_TO_FETCH_INVOICE_ITEMS_FOR_GIVEN_INVOICE = "{CALL getInvoiceLineItemByInvoiceId(:tenantId, :invoiceId)}";
    private static final String QUERY_TO_FETCH_INVOICE_PAYMENT_FOR_INVOICE_ID = "{CALL getInvoicePaymentForInvoiceId(:tenantId, :invoiceId)}";
    private static final String QUERY_TO_FETCH_PRESCRIPTIONS_FOR_GIVEN_VISIT = "{CALL getPrescriptionforPatientSchedule(:tenantId, :afyaId, :startDate,:doctorId, :visitStartDateTime, :visitEndDateTime)}";
    private static final String QUERY_TO_FETCH_ALL_PRESCRIPTION_INVOICE_DETAILS_OF_PATIENT = "{CALL getPrescriptionInvoiceForAfyaId(:afyaId)}";
    private static final String QUERY_TO_FETCH_ALL_INVOICE_ITEM_DETAILS_FOR_GIVEN_INVOICE = "{CALL getPrescriptionInvoiceItemsForPharmacyInvoice(:invoiceId, :tenantId)}";
    private static final String QUERY_TO_GET_PATIENT_CHIEF_COMPLAINTS_FOR_GIVEN_VISIT = "{CALL getPatientChiefComplaintForGivenVisit(:tenantId, :afyaId,:doctorId, :startDate, :visitStartDateTime, :visitEndDateTime)}";
    private static final String QUERY_TO_GET_PATIENT_ALLERGY_FOR_GIVEN_VISIT = "{CALL getPatientAllegryForGivenVisit(:tenantId, :afyaId,:doctorId, :startDate, :visitStartDateTime, :visitEndDateTime)}";
    private static final String QUERY_TO_GET_PATIENT_DIAGNOSIS_FOR_GIVEN_VISIT = "{CALL getPatientDiagnosisForGivenVisit(:tenantId, :afyaId, :doctorId, :startDate, :visitStartDateTime, :visitEndDateTime)}";
    private static final String QUERY_TO_GET_PATIENT_LAB_ORDER_FOR_GIVEN_VISIT = "{CALL getPatientLabOrderForGivenVisit(:tenantId, :afyaId, :doctorId, :startDate, :visitStartDateTime, :visitEndDateTime)}";
    private static final String QUERY_TO_GET_PATIENT_PROCEDURE_FOR_GIVEN_VISIT = "{CALL getPatientCptForGivenVisit(:tenantId, :afyaId, :doctorId, :startDate, :visitStartDateTime, :visitEndDateTime)}";

    private static final String QUERY_TO_FETCH_SERVICE_ITEM_COUNT_BY_PACKAGE_ID = "SELECT COUNT(service_item_id) AS serviceItemCount FROM price_service_item_assoc WHERE service_id IN(SELECT service_id FROM price_package_service_assoc WHERE package_id=:packageId)";

    private static final String QUERY_TO_FETCH_SUBSCRIBED_PACKAGE_DETAILS_BY_TENANT_ID = "SELECT * FROM `user_package` up JOIN `price_package` pp ON (pp.`package_id` = up.`package_id` \n" +
                                                                                         "AND pp.package_category='SERVICE'  AND up.activated=TRUE)\n" +
                                                                                         "WHERE up.username IN (SELECT admin_username FROM tenant WHERE tenant_id=:tenantId)";
    private static final String QUERY_TO_FETCH_ALL_DEFAULT_ROLES_FOR_USER = "SELECT * FROM authorities WHERE username=:username";

    @Autowired
    public PortalUtilityFinder(@Qualifier("primaryDataSource") DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Map<String, Object> getNationalityByNationalityCode(String nationalityCode) {
        List<Map<String, Object>> nationalityList = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_NATIONALITY_BY_NATIONALITY_CODE, new MapSqlParameterSource("nationalityCode", nationalityCode));
        if(nationalityList.size() > 0)
            return nationalityList.get(0);
        else
            return Collections.EMPTY_MAP;
    }



    public List<Map<String, Object>> getPackageServiceForServiceTypePatient() {
        return namedParameterJdbcTemplate.queryForList(GET_PACKAGE_SERVICE_FOR_SERVICE_TYPE_PATIENT, EmptySqlParameterSource.INSTANCE);
    }

    public List<Map<String, Object>> getPackageByUserOrGetAllPackagesForUserFacility(String username) {
        String facilityTypeOfUser = getFacilityTypeOfUser(username);
        List<Map<String, Object>> packages = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_PACKAGES_BY_GIVEN_CRITERIA, new MapSqlParameterSource("username", username).addValue("facilityTypeOfUser", facilityTypeOfUser));
        if(UtilValidator.isEmpty(packages)){
            if(StringUtils.isEmpty(facilityTypeOfUser))
                return Collections.EMPTY_LIST;
            packages = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_PACKAGES_BY_USER_FACILITY_TYPE, new MapSqlParameterSource("facilityTypeOfUser", facilityTypeOfUser));
        }
        return packages;
    }

    private String getFacilityTypeOfUser(String username) {
        List<Map<String, Object>> packages = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_FACILITY_TYPE_FOR_USER, new MapSqlParameterSource("username", username));
        if(UtilValidator.isEmpty(packages))
            return StringUtils.EMPTY;
        return UtilValidator.isEmpty(packages.get(0)) ? StringUtils.EMPTY : (String)packages.get(0).get("facilityType");
    }

    public List<Map<String, Object>> getTrainingSessionSchedulesByPackageId(Integer packageId) {
        return namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_TRAINING_SESSION_SCHEDULES_FOR_GIVEN_PACKAGE, new MapSqlParameterSource("packageId", packageId));
    }

    public Map<String, Object> getVisitHistoryForView(String afyaId, String startDate, String doctorId, String visitStartDateTime, String visitEndDateTime) {
        List<Map<String, Object>> schedules = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_VISIT_HISTORY_FOR_VIEW, new MapSqlParameterSource("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        if(UtilValidator.isEmpty(schedules))
            return Collections.EMPTY_MAP;
        return schedules.get(0);
    }

    public List<Map<String, Object>> getInvoiceDetailsForGivenVisitFromGivenTenant(String tenantId, String afyaId, String startDate, String doctorId, String visitStartDateTime, String visitEndDateTime) {
        List<Map<String, Object>> invoiceDetailList =  namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_INVOICE_DETAILS_FOR_GIVEN_VISIT_FROM_GIVEN_TENANT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        return calculateAgeIfNotPresent(invoiceDetailList);
    }

    public List<Map<String, Object>> getInvoiceDetailsForGivenInvoice(String tenantId, String invoiceId) {
        List<Map<String, Object>> invoiceDetailList =  namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_INVOICE_DETAILS_FOR_GIVEN_INVOICE_FROM_GIVEN_TENANT, new MapSqlParameterSource("tenantId", tenantId).addValue("invoiceId", invoiceId));
        return calculateAgeIfNotPresent(invoiceDetailList);
    }

    public List<Map<String, Object>> getInvoiceLineItemsForGivenInvoiceFromGivenTenant(String tenantId, long invoiceId) {
        return namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_INVOICE_ITEMS_FOR_GIVEN_INVOICE, new MapSqlParameterSource("tenantId", tenantId).addValue("invoiceId", invoiceId));
    }

    public List<Map<String, Object>> getInvoicePaymentForInvoiceId(String tenantId, long invoiceId) {
        return namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_INVOICE_PAYMENT_FOR_INVOICE_ID, new MapSqlParameterSource("tenantId", tenantId).addValue("invoiceId", invoiceId));
    }

    public List<Map<String, Object>> getPrescriptionDetailsForGivenVisitFromGivenTenant(String tenantId, String afyaId, String startDate, String doctorId, String visitStartDateTime, String visitEndDateTime) {
        List<Map<String, Object>> prescriptionList =  namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_PRESCRIPTIONS_FOR_GIVEN_VISIT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        return calculateAgeIfNotPresent(prescriptionList);
    }

    public List<Map<String, Object>> getPrescriptionInvoiceDetailsForGivenPatient(String afyaId) {
        return namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_ALL_PRESCRIPTION_INVOICE_DETAILS_OF_PATIENT, new MapSqlParameterSource("afyaId", afyaId));
    }

    public List<Map<String, Object>> getPrescriptionInvoiceLineItemDetailsForGivenPatient(String tenantId, String invoiceId) {
        return namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_ALL_INVOICE_ITEM_DETAILS_FOR_GIVEN_INVOICE, new MapSqlParameterSource("tenantId", tenantId).addValue("invoiceId", invoiceId));
    }

    public Map<String, List<Map<String, Object>>> getEncounterDetailsForGivenPatient(String tenantId, String afyaId, String startDate, String doctorId, String visitStartDateTime, String visitEndDateTime) {
        List<Map<String, Object>> chiefComplaints = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_PATIENT_CHIEF_COMPLAINTS_FOR_GIVEN_VISIT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        List<Map<String, Object>> allegies = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_PATIENT_ALLERGY_FOR_GIVEN_VISIT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        List<Map<String, Object>> diagnosis = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_PATIENT_DIAGNOSIS_FOR_GIVEN_VISIT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        List<Map<String, Object>> labOrders = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_PATIENT_LAB_ORDER_FOR_GIVEN_VISIT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        List<Map<String, Object>> procedures = namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_PATIENT_PROCEDURE_FOR_GIVEN_VISIT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        List<Map<String, Object>> prescriptions = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_PRESCRIPTIONS_FOR_GIVEN_VISIT, new MapSqlParameterSource("tenantId", tenantId).addValue("afyaId", afyaId).addValue("startDate", startDate).addValue("doctorId", doctorId).addValue("visitStartDateTime", visitStartDateTime).addValue("visitEndDateTime", visitEndDateTime));
        return constructEncounterDetails(chiefComplaints, allegies, diagnosis, labOrders, prescriptions, procedures);
    }

    private Map<String, List<Map<String, Object>>> constructEncounterDetails(List<Map<String, Object>> chiefComplaints, List<Map<String, Object>> allegry, List<Map<String, Object>> diagnosis, List<Map<String, Object>> labOrder, List<Map<String, Object>> prescriptons, List<Map<String, Object>> procedures) {
        Map<String, List<Map<String, Object>>> encounter = Maps.newHashMap();
        encounter.put("chiefComplaints", calculateAgeIfNotPresent(chiefComplaints));
        encounter.put("allegies", allegry);
        encounter.put("diagnosis", diagnosis);
        encounter.put("labOrders", labOrder);
        encounter.put("procedures", procedures);
        encounter.put("prescriptions", prescriptons);
        return encounter;
    }

    private List<Map<String, Object>> calculateAgeIfNotPresent(List<Map<String, Object>> list) {
        for(Map<String, Object> map : list){
            LocalDate date = new LocalDate(map.get("dateOfBirth"));
            map.put("age", UtilDateTime.calculateAge(date.toDate()));
        }
        return list;
    }

    public Map<String, Object> getServiceItemCountForGivenPackageId(String packageId) {
        Map<String, Object> resultMap = Maps.newHashMap();
        List<Map<String, Object>> resultSet = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_SERVICE_ITEM_COUNT_BY_PACKAGE_ID, new MapSqlParameterSource("packageId", packageId));
        if(UtilValidator.isNotEmpty(resultSet))
            resultMap = resultSet.get(0);
        return resultMap;
    }

    public Map<String, Object> getSubscribedPackageDetailsForGivenTenant(String tenantId) {
        Map<String, Object> resultMap = Maps.newHashMap();
        List<Map<String, Object>> resultSet = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_SUBSCRIBED_PACKAGE_DETAILS_BY_TENANT_ID, new MapSqlParameterSource("tenantId", tenantId));
        if(UtilValidator.isNotEmpty(resultSet))
            resultMap = resultSet.get(0);
        return resultMap;
    }

    public String getDefaultRoleOfUserIfAny(String username) {
        List<Map<String, Object>> resultSet = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_ALL_DEFAULT_ROLES_FOR_USER, new MapSqlParameterSource("username", username));
        for(Map<String, Object> row : resultSet){
            if(com.afya.portal.util.UtilValidator.isNotEmpty(row.get("default_role"))){
                return row.get("default_role").toString();
            }
        }
        return null;
    }

    public boolean isUserVerified( String username) {
        List<Map<String, Object>> userList= namedParameterJdbcTemplate.queryForList(QUERY_TO_GET_IS_VERIFIED, new MapSqlParameterSource("username", username));
        if(userList.size() > 0)


            if (userList.get(0).get("is_verified").equals(true))
                return true;
            else
                return false;
        else
            return false;
    }
}
