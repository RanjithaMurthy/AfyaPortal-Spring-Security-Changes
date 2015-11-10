package com.afya.portal.controller;

import com.afya.portal.application.*;
import com.afya.portal.domain.model.LoginPreference;
import com.afya.portal.domain.model.ProviderType;
import com.afya.portal.faq.view.dto.FaqDto;
import com.afya.portal.query.PortalFinder;
import com.afya.portal.query.UserLoginFinder;
import com.afya.portal.service.MailService;
import com.afya.portal.service.TemplateNames;
import com.afya.portal.util.SmsUtil;
import com.afya.portal.util.UtilValidator;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nzion.dto.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nthdimenzion.presentation.AppUtils.getLoggedInUser;

/**
 * Created by Mohan Sharma on 3/17/2015.
 */
@RestController
public class PortalRestController {

    private PortalFinder portalFinder;
    private CommandGateway commandGateway;
    private PortalCommandHandler portalCommandHandler;
    private SmsUtil smsUtil;
    private MailService mailService;

    @Value("${CLINIC_BASE_URL}")
    private String CLINIC_BASE_URL;

    @Autowired
    public PortalRestController(PortalFinder clinicalFinder, CommandGateway commandGateway, PortalCommandHandler portalCommandHandler, SmsUtil smsUtil, MailService mailService) {
        this.portalFinder = clinicalFinder;
        this.commandGateway = commandGateway;
        this.portalCommandHandler = portalCommandHandler;
        this.smsUtil = smsUtil;
        this.mailService = mailService;
    }

    @Autowired
    private UserLoginFinder userLoginFinder;

    @RequestMapping(value = "/anon/insuranceMaster/allInsuranceCompany", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllInsuranceCompanyMaster() {
        return portalFinder.getAllInsuranceCompanyMaster();
    }

    @RequestMapping(value = "/anon/getAllFaq", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllFaq() throws IOException {
        return portalFinder.getAllFaq();
    }

    @RequestMapping(value = "/anon/searchFaq",produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody List<FaqDto> searchFaq(@RequestParam String searchParam) throws IOException {
        return portalFinder.searchFaq(searchParam);
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/anon/getFaqCategory")
    public @ResponseBody List<FaqDto> getFaqCategory(){
        return portalFinder.getFaqBySearchParam();
    }

    @RequestMapping(value = "/anon/corporateMaster/allCorporateMaster", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllCorporateMaster() {
        return portalFinder.getAllCorporateMaster();
    }

    @RequestMapping(value = "/anon/patient/retrieveAfyaId", method = RequestMethod.POST)
    public
    @ResponseBody
    HttpEntity<String> getPatientBasedOnFirstNameLastNameAndDateOfBirth(@RequestParam String tenantId, @RequestParam String facilityType, @RequestBody PatientDto patientDto) {
        PatientDto portalPatientDto = portalFinder.getPatientBasedOnGivenCriteria(patientDto.getFirstName(), patientDto.getLastName(), patientDto.getDateOfBirth(), patientDto.getAfyaId(), patientDto.getMobileNumber());
        String afyaId;
        if (portalPatientDto != null) {
            afyaId = portalPatientDto.getAfyaId();
        } else {
            long afyaSeqId = portalFinder.getNextAfyaId();
            afyaId = "KWT" + afyaSeqId;
            // do automatic user registration, if user is not already registered (Kannan - 2015-11-01)
            Preconditions.checkNotNull(patientDto.getEmailId(), "Email Id cannot be null or empty, to create User");
            Preconditions.checkNotNull(patientDto.getMobileNumber(), "Mobile Number cannot be null or empty, to create User");
            Preconditions.checkArgument(patientDto.getEmailId().length() > 0, "Email Id cannot be null or empty, to create User");
            Preconditions.checkArgument(patientDto.getMobileNumber().length() > 0, "Mobile Number cannot be null or empty, to create User");
            // try to find user by Patient's email-id
            // Map<String, Object> userDetail = portalFinder.getPatientByUsername(patientDto.getEmailId()); // kannan (2015-11-02) Removed check for Email, to keep consistant with Portal Patient Registration behaviour
            // if(userDetail == null || userDetail.size() == 0) {
                // if user not found with Email-ID, try to find user by Patient's mobile number
                Map<String, Object> userDetail = portalFinder.getPatientByUsername(patientDto.getMobileNumber());
                if(userDetail == null || userDetail.size() == 0) {
                    // if user not found with both Mobile Number also, then create a new user through Patient-sign-up
                    if(patientDto.getLoginPreference() == null) // if no login-preference, set it to Mobile
                        patientDto.setLoginPreference(LoginPreference.MOBILE);
                    // Generate password, if required
                    if(patientDto.getPassword() == null){
                        String password = "Welcome@" + patientDto.getMobileNumber();
                        patientDto.setPassword(password);
                        patientDto.setConfirmPassword(password);
                    }
                    // register patient and send notifications
                    try {
                        // register patient
                        RegisterPatientCommand registerPatientCmd = new RegisterPatientCommand(patientDto, true);
                        commandGateway.sendAndWait(registerPatientCmd);
                        // get the tokens for the user registered above
                        Map<String,Object> tokens = userLoginFinder.findTokensWithUsername(patientDto.getMobileNumber());
                        // get clinic details from tenantId
                        List<Map<String,Object>> clinicDetailLst = userLoginFinder.getGivenTenantDetail(tenantId.toString());
                        Preconditions.checkArgument(clinicDetailLst != null && clinicDetailLst.size() > 0, "Clinic Details could not be fetched for the given tenantId");
                        // send sms with password
                        Map<String,Object> smsMap =  new HashMap<String, Object>();
                        smsMap.put("firstName", patientDto.getFirstName());
                        smsMap.put("lastName", patientDto.getLastName());
                        smsMap.put("mobileNumber", patientDto.getMobileNumber());
                        smsMap.put("afyaId", afyaId);
                        smsMap.put("userName", patientDto.getMobileNumber());
                        smsMap.put("password", patientDto.getPassword());
                        smsMap.put("clinicName", clinicDetailLst.get(0).get("clinic_name"));
                        smsMap.put("clinicOwnerName", clinicDetailLst.get(0).get("first_name").toString() + " " + clinicDetailLst.get(0).get("last_name").toString());
                        smsUtil.sendAutoRegisteredPatientPassword(smsMap);
                        // send email with password
                        Map<String,Object> emailMap =  new HashMap<String, Object>();
                        emailMap.put("sentFrom", "admin@aafya.com");
                        emailMap.put("firstName", patientDto.getFirstName());
                        emailMap.put("lastName", patientDto.getLastName());
                        emailMap.put("sendTo", patientDto.getEmailId());
                        emailMap.put("mobileNumber", patientDto.getMobileNumber());
                        emailMap.put("afyaId", afyaId);
                        emailMap.put("userName", patientDto.getMobileNumber());
                        emailMap.put("password", patientDto.getPassword());
                        emailMap.put("clinicName", clinicDetailLst.get(0).get("clinic_name"));
                        emailMap.put("clinicOwnerName", clinicDetailLst.get(0).get("first_name").toString() + " " + clinicDetailLst.get(0).get("last_name").toString());
                        //emailMap.put("key", TemplateNames.AUTO_REGISTERED_PATIENT_PASSWORD.name());
                        mailService.sendAutoRegisteredPatientPassword(emailMap);
                        // do automatic verification (through OTP token)
                        Map metadata = new HashMap();
                        metadata.put("otp_token", tokens.get("otp_token"));
                        commandGateway.sendAndWait(new GenericCommandMessage("OTP_CONFIRMATION", patientDto.getMobileNumber(), metadata));
                    } catch (Exception e) {
                        Map result = new HashMap();
                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        result.put("message",e.getMessage());
                        result.put("errCode","501");
                        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.NOT_IMPLEMENTED);   // creating user failed, then may need to return error
                    }
                    /*ResponseEntity<String> response = provisioningController.patientSignUp(patientDto, null);
                    if(!(response.getStatusCode() == HttpStatus.OK && response.getBody().indexOf("success") > 0))
                        return new HttpEntity<>("");    // creating user failed, then may need to return error
                    */
                }
            }
        //}
        patientDto.setAfyaId(afyaId);
        PersistPatientCommand persistPatientCommand = new PersistPatientCommand(patientDto, tenantId, ProviderType.valueOf(facilityType.toUpperCase()));
        commandGateway.send(persistPatientCommand);

        return new HttpEntity<>(patientDto.getAfyaId());
    }

    @RequestMapping(value = "/anon/getAllClinics", method = RequestMethod.GET)
    public
    @ResponseBody
    List<ClinicDto> getAllClinics(HttpServletRequest request, @RequestParam(required = false) String packageServiceName
            , @RequestParam(required = false, defaultValue = "true")Boolean includePremium, @RequestParam(required = false, defaultValue = "false")Boolean includeCommunity ) {
        // default values
        if(includePremium == null)
            includePremium = true;
        if(includeCommunity == null)
            includeCommunity = false;
        // fetch data
        String imageBaseUrl = CLINIC_BASE_URL + "ospedale";
        UserDetails loggedInUser = getLoggedInUser(request);
        String currentUser = loggedInUser!=null?loggedInUser.getUsername():null;
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        return portalFinder.getAllClinics(tenantId, packageServiceName, includePremium, includeCommunity, imageBaseUrl);
    }

    @RequestMapping(value = "/anon/getAllClinicsAndPharmacies", method = RequestMethod.GET)
    public
    @ResponseBody
    List<ClinicDto> getAllClinicsAndPharmacies(HttpServletRequest request, @RequestParam(required = false) String packageServiceName, @CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);

        UserDetails loggedInUser = getLoggedInUser(request);
        //String currentUser = loggedInUser!=null?loggedInUser.getUsername():null;
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        return portalFinder.getAllClinicsAndPharmacies(tenantId, packageServiceName);
    }

    @RequestMapping(value = "/anon/getAllPharmacies", method = RequestMethod.GET)
    public
    @ResponseBody
    List<PharmacyDto> getAllPharmacies(@RequestParam(required = false) String packageServiceName) {
        return portalFinder.getAllPharmacies(packageServiceName);
    }

    @RequestMapping(value = "/anon/getAllLaboratories", method = RequestMethod.GET)
    public
    @ResponseBody
    List<LabDto> getAllLaboratories() {
        return portalFinder.getAllLaboratories();
    }

    @RequestMapping(value = "/anon/getAllBloodBanks", method = RequestMethod.GET)
    public List<Map<String, Object>> getAllBloodBanks() {
        return portalFinder.getAllBloodBanks();
    }

    @RequestMapping(value = "/anon/insuranceMaster/allInsurancePlan", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllInsurancePlanMaster() {
        return portalFinder.getAllInsurancePlanMaster();
    }

    @RequestMapping(value = "/anon/clinic/getAllServices", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllServices() {
        return portalFinder.getAllServices();
    }

    @RequestMapping(value = "/anon/fetchUserByCivilId", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> FetchUserByCivilId(@RequestParam String civilId) {
        return portalFinder.fetchUserByCivilId(civilId);
    }

    @RequestMapping(value = "/anon/getAllDoctors", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllDoctors(@RequestParam(required = false) String packageServiceName) {
        String imageBaseUrl = CLINIC_BASE_URL + "ospedale";
        return portalFinder.getAllDoctors(packageServiceName, imageBaseUrl);
    }

    @RequestMapping(value = "/anon/persistCivilIdData", method = RequestMethod.POST)
    public
    @ResponseBody
    String persistCivilIdData(@RequestBody String jsonOfCivilIdData) {
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").registerTypeAdapter(CivilUserDto.class, new CivilUserDto().new CustomJsonDeserializer()).create();
        CivilUserDto civilUserDto = gson.fromJson(jsonOfCivilIdData, CivilUserDto.class);
        PersistCivilIdDtoCommand persistCivilIdDtoCommand = new PersistCivilIdDtoCommand(civilUserDto);
        return commandGateway.sendAndWait(persistCivilIdDtoCommand);
    }

    @RequestMapping(value = "/anon/fetchPatientByAfyaId", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> fetchPatientByAfyId(@RequestParam String afyaId) {
        return portalFinder.fetchPatientByAfyaId(afyaId);
    }

    @RequestMapping(value = "/anon/fetchPatientByCivilId", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> fetchPatientByCivilId(@RequestParam String civilId) {
        return portalFinder.fetchPatientByCivilId(civilId);
    }

    @RequestMapping(value = "/anon/fetchPatientsByGivenCriteria", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> fetchPatientsByGivenCriteria(@RequestParam String civilId, @RequestParam String afyaId, @RequestParam String firstName, @RequestParam String lastName, @RequestParam String mobileNumber, @RequestParam String gender, @RequestParam String dateOfBirth, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(civilId) && UtilValidator.isEmpty(afyaId) && UtilValidator.isEmpty(firstName) && UtilValidator.isEmpty(lastName) && UtilValidator.isEmpty(mobileNumber) && UtilValidator.isEmpty(gender) && UtilValidator.isEmpty(dateOfBirth)){
            return Collections.EMPTY_LIST;
        }

        return portalFinder.fetchPatientsByGivenCriteria(civilId, afyaId, firstName, lastName, mobileNumber, gender, dateOfBirth);
    }

    @RequestMapping(value = "/anon/persistPatientInsuranceDetails", method = RequestMethod.POST)
    public String persistPatientInsuranceDetails(@RequestBody String jsonOfPatientInsuranceDetails) {
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create();
        PersistPatientInsuranceCommand persistPatientInsuranceCommand = gson.fromJson(jsonOfPatientInsuranceDetails, PersistPatientInsuranceCommand.class);
        Preconditions.checkNotNull(persistPatientInsuranceCommand.getAfyaId(), "AfyaId cannot be Null");
        if (persistPatientInsuranceCommand.getInsuredPatientDtos().size() == 0)
            return "Nothing to update as InsuredPatientDtoList is empty";
        return commandGateway.sendAndWait(persistPatientInsuranceCommand);
    }

    @RequestMapping(value = "/anon/persistCorporatePatientDetails", method = RequestMethod.POST)
    public String persistCorporatePatientDetails(@RequestBody String jsonOfPatientInsuranceDetails) {
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create();
        PersistCorporatePatientCommand persistCorporatePatientCommand = gson.fromJson(jsonOfPatientInsuranceDetails, PersistCorporatePatientCommand.class);
        Preconditions.checkNotNull(persistCorporatePatientCommand.getAfyaId(), "AfyaId cannot be Null");
        return commandGateway.sendAndWait(persistCorporatePatientCommand);
    }

    @RequestMapping(value = "/anon/fetchProvidersByClinicId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> fetchProvidersByClinicId(@RequestParam String clinicId) {
        return portalFinder.fetchProvidersByClinicId(clinicId);
    }

    @RequestMapping(value = "/anon/getAllHospitals", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllHospitals() {
        return portalFinder.getAllHospitals();
    }

    @RequestMapping(value = "/anon/getAllSpecialities", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllSpecialities() {
        return portalFinder.getAllSpecialities();
    }

    @RequestMapping(value = "/anon/corporateMaster/allCorporatePlan", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllCorporatePlan() {
        return portalFinder.getAllCorporatePlan();
    }

    @RequestMapping(value = "/anon/corporateMaster/fetchCorporatePlanByCorporateCode", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> fetchCorporatePlanByCorporateCode(@RequestParam String corporateCode) {
        return portalFinder.fetchCorporatePlanByCorporateCode(corporateCode);
    }

    @RequestMapping(value = "/anon/getAllCities", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllCities(){
        return portalFinder.getAllCities();
    }

    @RequestMapping(value = "/anon/getAllStates", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllStates(){
        return portalFinder.getAllStates();
    }

    @RequestMapping(value = "/anon/getAllCountries", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllCountries(){
        return portalFinder.getAllCountries();
    }

    @RequestMapping(value = "/anon/getAllNationality", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllNationality(){
        return portalFinder.getAllNationality();
    }

    @RequestMapping(value = "/anon/getStateCountryBasedOnCity", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getStateCountryBasedOnCity(@RequestParam String city){
        return portalFinder.getStateCountryBasedOnCity(city);
    }

    @RequestMapping(value = "/anon/getClinicDetailsByClinicId", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getClinicDetailsByClinicId(@RequestParam String clinicId){
        return portalFinder.getClinicDetailsByClinicId(clinicId);
    }

    @RequestMapping(value = "/anon/getPharmacyDetailsByPharmacyId", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getPharmacyDetailsByPharmacyId(@RequestParam String pharmacyId){
        return portalFinder.getPharmacyDetailsByPharmacyId(pharmacyId);
    }

    @RequestMapping(value = "/anon/persistDoctor", method = RequestMethod.POST)
    public @ResponseBody HttpEntity<String> persistDoctor(@RequestParam String tenantId, @RequestBody String doctorDtoJson) {
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create();
        DoctorDto doctorDto = gson.fromJson(doctorDtoJson, DoctorDto.class);
        PersistDoctorCommand persistDoctorCommand = new PersistDoctorCommand(tenantId, doctorDto);
        return new HttpEntity<>(commandGateway.sendAndWait(persistDoctorCommand).toString());
    }

    @RequestMapping(value = "/anon/getCorporateMasterByCorporateId", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getCorporateMasterByCorporateId(@RequestParam String corporateId){
        return portalFinder.getCorporateMasterByCorporateId(corporateId);
    }

    @RequestMapping(value = "/anon/getAllServiceMaster", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getAllServiceMaster() {
        return portalFinder.getAllServiceMaster();
    }

    @RequestMapping(value = "/anon/getUserLoginByName", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getUserLoginByName(@RequestParam String userName) {
        Map<String, Object> map = portalFinder.getUserLoginByUserName(userName);
        if ((map == null) || (map.size() == 0)) {
            String user_name = SessionManager.getInstance().getSession(userName);
            if (user_name != null) {
                return portalFinder.getUserLoginByUserName(user_name);
            }
        }
        return map;
    }

    @RequestMapping(value = "/getPatientByUsername", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getPatientByUsername(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        return portalFinder.getPatientByUsername(username);
    }

    @RequestMapping(value = "/anon/getPatientByUsername", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getPatientByUsername(@CookieValue(value = "token", required = false) String token, @RequestParam(required = false) String username, HttpServletResponse response) throws IOException {
        if((username == null || username.equals("")) && (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.emptyMap();
        }
        if ((username == null) && (token != null)) {
            username = SessionManager.getInstance().getSession(token);
        }
        return portalFinder.getPatientByUsername(username);
    }

    @RequestMapping(value = "/getProviderByUsername", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getProviderByUsername(@CookieValue(value = "token", required = false) String token, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.emptyMap();
        }

        String username;
        username = SessionManager.getInstance().getSession(token);

        return portalFinder.getProviderByUsername(username);
    }

    @RequestMapping(value = "/anon/getProviderByUsername", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getProviderByUsername(@CookieValue(value = "token", required = false) String token, @RequestParam(required = false) String username, HttpServletResponse response) throws IOException {
        if((username == null || username.equals("")) && (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.emptyMap();
        }
        if ((username == null) && (token != null)) {
            username = SessionManager.getInstance().getSession(token);
        }
        return portalFinder.getProviderByUsername(username);
    }

    @RequestMapping(value = "/anon/getDoctorTariff", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> getDoctorTariff(@RequestParam String clinicId, @RequestParam String visitTypeName, @RequestParam String doctorId){
        return portalFinder.getDoctorTariff(clinicId, visitTypeName, doctorId);
    }

    @RequestMapping(value = "/getUpcomingAppointments", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getUpcomingAppointments(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        if(userDetail.get("afyaId") == null || userDetail.get("afyaId") == "") {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "afyaId required");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getUpcomingAppointments(userDetail.get("afyaId").toString());
    }

    @RequestMapping(value = "/anon/getUpcomingAppointments", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getUpcomingAppointments(@RequestParam String afyaId){
        return portalFinder.getUpcomingAppointments(afyaId);
    }

    @RequestMapping(value = "/getPatientSpendingByServicesConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientSpendingByServicesConsolidated(@CookieValue("token") String token, @RequestParam String fromDate, @RequestParam String toDate, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        if(userDetail.get("afyaId") == null || userDetail.get("afyaId") == "") {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "afyaId required");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getPatientSpendingByServicesConsolidated(userDetail.get("afyaId").toString(), fromDate, toDate);
    }

    @RequestMapping(value = "/anon/getPatientSpendingByServicesConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientSpendingByServicesConsolidated(@RequestParam String afyaId, @RequestParam String fromDate, @RequestParam String toDate){
        return portalFinder.getPatientSpendingByServicesConsolidated(afyaId, fromDate, toDate);
    }

    @RequestMapping(value = "/getPatientVisitCountByFacilityConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientVisitCountByFacilityConsolidated(@CookieValue("token") String token, @RequestParam String fromDate, @RequestParam String toDate, HttpServletResponse response) throws IOException{
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getPatientVisitCountByFacilityConsolidated(userDetail.get("afyaId").toString(), fromDate, toDate);
    }

    @RequestMapping(value = "/anon/getPatientVisitCountByFacilityConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientVisitCountByFacilityConsolidated(@RequestParam String afyaId, @RequestParam String fromDate, @RequestParam String toDate){
        return portalFinder.getPatientVisitCountByFacilityConsolidated(afyaId, fromDate, toDate);
    }

    @RequestMapping(value = "/getPatientVisitCountByServicesConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientVisitCountByServicesConsolidated(@CookieValue("token") String token, @RequestParam String fromDate, @RequestParam String toDate, HttpServletResponse response) throws IOException{
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getPatientVisitCountByServicesConsolidated(userDetail.get("afyaId").toString(), fromDate, toDate);
    }

    @RequestMapping(value = "/anon/getPatientVisitCountByServicesConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientVisitCountByServicesConsolidated(@RequestParam String afyaId, @RequestParam String fromDate, @RequestParam String toDate){
        return portalFinder.getPatientVisitCountByServicesConsolidated(afyaId, fromDate, toDate);
    }

    @RequestMapping(value = "/getVisitHistoryConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getVisitHistoryConsolidated(@CookieValue("token") String token, @RequestParam String fromDate, @RequestParam String toDate, @RequestParam String clinicName, @RequestParam String doctorName, @RequestParam String visitType,HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getVisitHistoryConsolidated(userDetail.get("afyaId").toString(), fromDate, toDate, clinicName, doctorName, visitType);
    }

    @RequestMapping(value = "/anon/getVisitHistoryConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getVisitHistoryConsolidated(@RequestParam String afyaId, @RequestParam String fromDate, @RequestParam String toDate, @RequestParam String clinicName, @RequestParam String doctorName, @RequestParam String visitType){
        return portalFinder.getVisitHistoryConsolidated(afyaId, fromDate, toDate, clinicName, doctorName, visitType);
    }



    @RequestMapping(value = "/anon/addPaymentTransactionForAppointment", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> addPaymentTransactionForAppointment(@RequestParam String transactionType, @RequestParam String transactionAmount, @RequestParam String transactionTimestamp,
                                                            @RequestParam String isysTrackingRef, @RequestParam String afyaId, @RequestParam String apptClinicId, @RequestParam String apptDoctorId,
                                                            @RequestParam String apptSlot, @RequestParam(required = false) String pharmacyOrderId,
                                                            @RequestParam(required = false) String username, @RequestParam(required = false) String packageId,
                                                            @RequestParam(required = false) String processingFees, @RequestParam(required = false) String payerType,
                                                            @RequestParam(required = false) String paymentChannel){
        return portalFinder.addPaymentTransactionForAppointment(transactionType, transactionAmount, transactionTimestamp, isysTrackingRef, afyaId, apptClinicId,
                apptDoctorId, apptSlot, pharmacyOrderId, username, packageId, processingFees, payerType, paymentChannel);
    }

    @RequestMapping(value = "/anon/updatePaymentTransactionWithIsysStatus", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> updatePaymentTransactionWithIsysStatus(@RequestParam String paymentId, @RequestParam String isysTrackingRef, @RequestParam String isysPaymentStatus, @RequestParam String isysPaymentStatusTimestamp, @RequestParam String isysHttpResponseStatus, @RequestParam(required = false) String isysMerchantRef){
        return portalFinder.updatePaymentTransactionWithIsysStatus(paymentId, isysTrackingRef, isysPaymentStatus, isysPaymentStatusTimestamp, isysHttpResponseStatus, isysMerchantRef);
    }

    @RequestMapping(value = "/anon/getNews", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getNews(@RequestParam(required = false) String newsTarget){
        if (newsTarget == null)
            newsTarget = "PATIENT";
        return portalFinder.getNews(newsTarget);
    }

    @RequestMapping(value = "/anon/getPatientActivePrescription", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientActivePrescription(@RequestParam String afyaId, @RequestParam(required = false) String fromDate, @RequestParam(required = false) String toDate, @RequestParam(required = false) String drugName, @RequestParam(required = false) String clinicName, @RequestParam(required = false) String doctorName) {
        return portalFinder.getPatientActivePrescription(afyaId, fromDate, toDate, drugName, clinicName, doctorName);
    }

    @RequestMapping(value = "/getInvoiceRecordsConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getInvoiceRecordsConsolidated(@CookieValue("token") String token, @RequestParam String fromDate, @RequestParam String toDate, @RequestParam String providerType, @RequestParam String doctorName , @RequestParam String invoiceNumber, HttpServletResponse response)throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getInvoiceRecordsConsolidated(userDetail.get("afyaId").toString(), fromDate, toDate, providerType, doctorName, invoiceNumber);
    }

    @RequestMapping(value = "/anon/getInvoiceRecordsConsolidated", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getInvoiceRecordsConsolidated(@RequestParam String afyaId, @RequestParam String fromDate, @RequestParam String toDate, @RequestParam String providerType, @RequestParam String doctorName , @RequestParam String invoiceNumber) {
        return portalFinder.getInvoiceRecordsConsolidated(afyaId, fromDate, toDate, providerType, doctorName, invoiceNumber);
    }

    @RequestMapping(value = "/anon/getInvoiceRecordsConsolidatedForSchedule", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getInvoiceRecordsConsolidatedForSchedule(@RequestParam String clinicId, @RequestParam String scheduleId, HttpServletResponse response) throws IOException {
        if(clinicId == null || scheduleId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getInvoiceRecordsConsolidatedForSchedule(clinicId, scheduleId);
    }

    @RequestMapping(value = "/anon/getPharmacyInvoiceRecordsConsolidatedForOrder", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPharmacyInvoiceRecordsConsolidatedForOrder(@RequestParam String pharmacyId, @RequestParam String orderId, HttpServletResponse response) throws IOException {
        if(pharmacyId == null || orderId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getPharmacyInvoiceRecordsConsolidatedForOrder(pharmacyId, orderId);
    }

    @RequestMapping(value = "/getInsuranceByAfyaId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getInsuranceByAfyaId(@CookieValue("token") String token, HttpServletResponse response)throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getInsuranceByAfyaId(userDetail.get("afyaId").toString());
    }

    @RequestMapping(value = "/anon/getInsuranceByAfyaId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getInsuranceByAfyaId(@RequestParam String afyaId) {
        return portalFinder.getInsuranceByAfyaId(afyaId);
    }

    @RequestMapping(value = "getPatientConsentByUsername", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientConsentByUsername(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }

        String username;
        username = SessionManager.getInstance().getSession(token);
        return portalFinder.getPatientConsentByUsername(username);
    }

    @RequestMapping(value = "/anon/getPatientConsentByUsername", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientConsentByUsername(@RequestParam String username) {
        return portalFinder.getPatientConsentByUsername(username);
    }

    @RequestMapping(value = "/anon/getPatientConsentForAfyaId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientConsentForAfyaId(@RequestParam String afyaId) {
        return portalFinder.getPatientConsentForAfyaId(afyaId);
    }


    @RequestMapping(value = "/anon/getPaymentGatewayTransactionById", method = RequestMethod.GET)
    public @ResponseBody List<Map<String,Object>> getPaymentGatewayTransactionById(@RequestParam String paymentId){
          return portalFinder.getPaymentGatewayTransactionById(paymentId);
    }

    @RequestMapping(value = "/updatePatientConsentForUsername", method = RequestMethod.POST)
    public
    @ResponseBody
    String updatePatientConsentForUsername(@CookieValue("token") String token, @RequestBody String jsonOfPatientConsent, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return "";
        }

        String username;
        username = SessionManager.getInstance().getSession(token);

        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create();
        Type listType = new TypeToken<List<PatientConsentDto>>() {}.getType();
        List<PatientConsentDto> patientConsentDtoList = gson.fromJson(jsonOfPatientConsent, listType);
        return portalFinder.updatePatientConsentForUsername(username, patientConsentDtoList);
    }

    @RequestMapping(value = "/anon/updatePatientConsentForUsername", method = RequestMethod.POST)
    public
    @ResponseBody
    String updatePatientConsentForUsername(@RequestParam String username, @RequestBody String jsonOfPatientConsent) {
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create();
        Type listType = new TypeToken<List<PatientConsentDto>>() {}.getType();
        List<PatientConsentDto> patientConsentDtoList = gson.fromJson(jsonOfPatientConsent, listType);
        return portalFinder.updatePatientConsentForUsername(username, patientConsentDtoList);
    }

    @RequestMapping(value = "/anon/updateScheduleStatusToConfirmed", method = RequestMethod.POST)
    public
    @ResponseBody
    String updateScheduleStatusToConfirmed(@RequestParam String scheduleId, @RequestParam String clinicId, @RequestParam String username) {
        return portalFinder.updateScheduleStatusToConfirmed(scheduleId, clinicId, username);
    }

    @RequestMapping(value = "/getPharmacyOrdersFromAfyaId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPharmacyOrdersFromAfyaId(@CookieValue("token") String token, @RequestParam boolean openOrdersOnly, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return (List<Map<String, Object>>)Collections.emptyMap();
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Username");
            return Collections.EMPTY_LIST;
        }
        return portalFinder.getPharmacyOrdersFromAfyaId(userDetail.get("afyaId").toString(), openOrdersOnly);
    }

    @RequestMapping(value = "/anon/getPharmacyOrdersFromAfyaId", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPharmacyOrdersFromAfyaId(@RequestParam String afyaId, @RequestParam boolean openOrdersOnly) {
        return portalFinder.getPharmacyOrdersFromAfyaId(afyaId, openOrdersOnly);
    }

    @RequestMapping(value = "/anon/getProviderServices", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getProviderServices(@RequestParam String packageType, @RequestParam String facilityId) {
        return portalFinder.getProviderServices(packageType, facilityId);
    }

    // Service to Compute Hash for the Payment Gateway
    @RequestMapping(value = "/anon/computeHashForPaymentGateway", method = RequestMethod.GET)
    public
    @ResponseBody
    String computeHashForPaymentGateway(@RequestParam String original, @RequestParam String dataToComputeHash){
        try {
            // step : Construct MAC object for HmacSHA256 algorithm
            Mac mac = Mac.getInstance("HmacSHA256");
            // step : convert original text to byte array
            byte[] originalBytes = original.getBytes("UTF-8");
            // step : init mac with original as Key
            mac.init(new SecretKeySpec(originalBytes, "HmacSHA1"));
            // step : compute the hash to get byte array
            byte[] hashBytes = mac.doFinal(dataToComputeHash.getBytes("UTF-8"));
            // step : convert byte array to string
            String hashStr = bytesToHexString(hashBytes);

            Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create();
            String jsonResult = gson.toJson(hashStr);
            // return hash
            return jsonResult;
        }
        catch (Exception e){
            return "Error Generating Hash";
        }
    }

    @RequestMapping(value = "/getSubscriptionHistory", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getSubscriptionHistory(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        return portalFinder.getSubscriptionHistory(username);
    }

    @RequestMapping(value = "/anon/getSubscriptionHistory", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getSubscriptionHistory(@RequestParam String username) {
        return portalFinder.getSubscriptionHistory(username);
    }

    @RequestMapping(value = "/getPatientSmartServicesPackageHistory", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientSmartServicesPackageHistory(@CookieValue("token") String token, @RequestParam String visitType, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        visitType = (visitType == null || visitType.length() == 0) ? null : visitType;

        return portalFinder.getPatientSmartServicesPackageHistory(username, visitType);
    }

    @RequestMapping(value = "/anon/getPatientSmartServicesPackageHistory", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPatientSmartServicesPackageHistory(@RequestParam String username, @RequestParam String visitType) {
        visitType = (visitType == null || visitType.length() == 0) ? null : visitType;
        return portalFinder.getPatientSmartServicesPackageHistory(username, visitType);
    }

    @RequestMapping(value = "/anon/getPackageNotificationHistory", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPackageNotificationHistory(@RequestParam String username) {
        return portalFinder.getPackageNotificationHistory(username);
    }

    @RequestMapping(value = "/getPackageNotificationSubscription", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPackageNotificationSubscription(@CookieValue("token") String token, @RequestParam String dataOption, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        return portalFinder.getPackageNotificationSubscription(username, dataOption);
    }

    @RequestMapping(value = "/anon/getPackageNotificationSubscription", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getPackageNotificationSubscription(@RequestParam String username, @RequestParam String dataOption) {
        return portalFinder.getPackageNotificationSubscription(username, dataOption);
    }

    @RequestMapping(value = "/anon/getProviderServiceCompareList", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getProviderServiceCompareList(@RequestParam String packageIds) {
        return portalFinder.getProviderServiceCompareList(packageIds);
    }

    @RequestMapping(value = "/anon/getProviderServiceItemDetails", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getProviderServiceItemDetails(@RequestParam String packageId) {
        return portalFinder.getProviderServiceItemDetails(packageId);
    }

    @RequestMapping(value = "/anon/getPackageTransctionDetail", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPackageTransctionDetail(@RequestParam int paymentId) {
        return portalFinder.getPackageTransctionDetail(paymentId);
    }

    @RequestMapping(value = "/getTransactionDetailByUsername", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getTransactionDetailByUsername(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        return portalFinder.getTransactionDetailByUsername(username);
    }

    @RequestMapping(value = "/anon/getTransactionDetailByUsername", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getTransactionDetailByUsername(@RequestParam String username) {
        return portalFinder.getTransactionDetailByUsername(username);
    }

    @RequestMapping(value = "/anon/getAllPremiumMember", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllPremiumMember() {
        return portalFinder.getAllPremiumMember();
    }

    @RequestMapping(value = "/anon/getPaymentProcessingFees", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPaymentProcessingFees(@RequestParam String payerType, @RequestParam String amount) {
        return portalFinder.getPaymentProcessingFees(payerType, amount);
    }

    @RequestMapping(value = "/anon/getActivePackageServiceUsageForTenant", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getActivePackageServiceUsageForTenant(@RequestParam String tenantId) {
        return portalFinder.getActivePackageServiceUsageForTenant(tenantId);
    }

    @RequestMapping(value = "/getPackageNotificationUsageFromProviderUsername", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPackageNotificationUsageFromProviderUsername(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        return portalFinder.getPackageNotificationUsageFromProviderUsername(username);
    }

    @RequestMapping(value = "/anon/getPackageNotificationUsageFromProviderUsername", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPackageNotificationUsageFromProviderUsername(@RequestParam String username) {
        return portalFinder.getPackageNotificationUsageFromProviderUsername(username);
    }

    @RequestMapping(value = "/getUserSubscriptionHistory", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getUserSubscriptionHistory(@CookieValue("token") String token, @RequestParam String serviceType
            , @RequestParam(required = false) String packageId, @RequestParam(required = false) String packageCategory
            , @RequestParam(required = false) Boolean isSubscriptionActive, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        if (packageId.length() == 0)
            packageId = null;
        if(packageCategory.length() == 0)
            packageCategory = null;
        return portalFinder.getUserSubscriptionHistory(username, serviceType, packageId, packageCategory, isSubscriptionActive);
    }

    @RequestMapping(value = "/anon/getUserSubscriptionHistory", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getUserSubscriptionHistory(@RequestParam String username, @RequestParam String serviceType
            , @RequestParam(required = false) String packageId, @RequestParam(required = false) String packageCategory
            , @RequestParam(required = false) Boolean isSubscriptionActive) {
        if(packageId.length() == 0)
            packageId = null;
        if(packageCategory.length() == 0)
            packageCategory = null;
        return portalFinder.getUserSubscriptionHistory(username, serviceType, packageId, packageCategory, isSubscriptionActive);
    }

    @RequestMapping(value = "/anon/getProviderPoilicyForService", method = RequestMethod.GET)
    public @ResponseBody String getProviderPoilicyForService(@RequestParam String serviceName) {
        return portalFinder.getProviderPoilicyForService(serviceName);
    }

    @RequestMapping(value = "/anon/getPatientPoilicyForService", method = RequestMethod.GET)
    public @ResponseBody String getPatientPoilicyForService(@RequestParam String serviceName) {
        return portalFinder.getPatientPoilicyForService(serviceName);
    }

    @RequestMapping(value = "/getAllInvitedPremiumMember", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllPremiumMember(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if((token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(username);
        return portalFinder.getAllInvitedPremiumMember(tenantId);
    }

    @RequestMapping(value = "/anon/getAllInvitedPremiumMember", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getAllPremiumMember(@RequestParam String username) {
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(username);
        return portalFinder.getAllInvitedPremiumMember(tenantId);
    }

    @RequestMapping(value = "/anon/validateRcmForReschedule", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> validateRcmForReschedule(@RequestParam String clinicId, @RequestParam long scheduleId, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(clinicId)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "clinicId Cannot be empty");
            return Collections.emptyMap();
        }
        return portalFinder.validateRcmForReschedule(clinicId, scheduleId);
    }

    @RequestMapping(value = "/anon/getProviderSummary", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Map<String, Object>> getProviderSummary(@RequestParam(required = false) String summaryType) {
        return portalFinder.getProviderSummary(summaryType);
    }

    // Helper use in computing Hash for the Payment Gateway
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
