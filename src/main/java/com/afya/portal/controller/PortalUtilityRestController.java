package com.afya.portal.controller;

import com.afya.portal.application.PersistCivilDataImageCommand;
import com.afya.portal.application.UpdateMemberDetailsCommand;
import com.afya.portal.query.PortalFinder;
import com.afya.portal.query.PortalInsuranceFinder;
import com.afya.portal.query.PortalUtilityFinder;
import com.afya.portal.service.PortalTenantRestfulClient;
import com.afya.portal.util.EmailUtil;
import com.afya.portal.util.UtilValidator;
import com.nzion.dto.PatientDto;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 6/22/2015.
 */
@RestController
public class PortalUtilityRestController {
    private PortalFinder portalFinder;
    private PortalInsuranceFinder portalInsuranceFinder;
    private CommandGateway commandGateway;
    private PortalUtilityFinder portalUtilityFinder;

    @Autowired
    public PortalUtilityRestController(PortalFinder clinicalFinder, PortalInsuranceFinder portalInsuranceFinder,CommandGateway commandGateway, PortalUtilityFinder portalUtilityFinder) {
        this.portalFinder = clinicalFinder;
        this.portalInsuranceFinder = portalInsuranceFinder;
        this.commandGateway = commandGateway;
        this.portalUtilityFinder = portalUtilityFinder;
    }

    @RequestMapping(value = "/anon/persistImageOfCivilData", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity persistImageOfCivilData(@RequestParam String civilId, @RequestParam MultipartFile image, HttpServletResponse response) throws IOException {
        if(civilId.equals("") || civilId == null || image == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return null;
        }
        commandGateway.sendAndWait(new PersistCivilDataImageCommand(civilId, image));
        return  new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/anon/getListOfInsuranceForGivenTenant", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getListOfInsuranceForGivenTenant(@RequestParam String tenantId, @RequestParam String facilityType, HttpServletResponse response) throws IOException {
        if(tenantId.equals("") || tenantId == null || facilityType == null || facilityType.equals("")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalInsuranceFinder.getListOfInsuranceForGivenTenant(tenantId, facilityType);
    }

    @RequestMapping(value = "/anon/getPlansForGivenInsurance", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPlansForGivenInsurance(@RequestParam Long payerId, HttpServletResponse response) throws IOException {
        if(payerId.equals("") || payerId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalInsuranceFinder.getPlansForGivenInsurance(payerId);
    }

    @RequestMapping(value = "/anon/getNationalityByNationalityCode", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getNationalityByNationalityCode(@RequestParam String nationalityCode, HttpServletResponse response) throws IOException {
        if(nationalityCode.equals("") || nationalityCode == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalUtilityFinder.getNationalityByNationalityCode(nationalityCode);
    }

    @RequestMapping(value = "/anon/getPackageServiceForServiceTypePatient", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPackageServiceForServiceTypePatient() throws IOException {
        return portalUtilityFinder.getPackageServiceForServiceTypePatient();
    }

    @RequestMapping(value = "/getPackageByUserOrGetAllPackagesForUserFacility", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPackageByUserOrGetAllPackagesForUserFacility(@CookieValue(value = "token", required = false) String token, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        return portalUtilityFinder.getPackageByUserOrGetAllPackagesForUserFacility(username);
    }

    @RequestMapping(value = "/anon/getPackageByUserOrGetAllPackagesForUserFacility", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPackageByUserOrGetAllPackagesForUserFacility(@RequestParam String username, @CookieValue(value = "token", required = false) String token, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(username == null)
            username = SessionManager.getInstance().getSession(token);
        return portalUtilityFinder.getPackageByUserOrGetAllPackagesForUserFacility(username);
    }

    @RequestMapping(value = "/anon/getTrainingSessionSchedulesByPackageId", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getTrainingSessionSchedulesByPackageId(@RequestParam Integer packageId, HttpServletResponse response) throws IOException {
        if(packageId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getTrainingSessionSchedulesByPackageId(packageId);
    }

    @RequestMapping(value = "/getVisitHistoryForView", method = RequestMethod.GET)
    public @ResponseBody Map<String, Object> getVisitHistoryForView(@CookieValue("token") String token, @RequestParam String startDate, @RequestParam String doctorId,@RequestParam String visitStartDateTime, @RequestParam String visitEndDateTime, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_MAP;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);

        if((userDetail.size() <= 0 || userDetail.get("afyaId").toString() == "") || doctorId == null || visitStartDateTime == null || visitEndDateTime == null || startDate == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalUtilityFinder.getVisitHistoryForView(userDetail.get("afyaId").toString(), startDate, doctorId, visitStartDateTime, visitEndDateTime);
    }

    @RequestMapping(value = "/anon/getInvoiceDetailsForGivenVisitFromGivenTenant", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getInvoiceDetailsForGivenVisit(@RequestParam String tenantId , @RequestParam String afyaId, @RequestParam String startDate, @RequestParam String doctorId,@RequestParam String visitStartDateTime, @RequestParam String visitEndDateTime, HttpServletResponse response) throws IOException {
        if(tenantId == null || afyaId == null || doctorId == null || visitStartDateTime == null || visitEndDateTime == null || startDate == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getInvoiceDetailsForGivenVisitFromGivenTenant(tenantId, afyaId, startDate, doctorId, visitStartDateTime, visitEndDateTime);
    }

    @RequestMapping(value = "/anon/getInvoiceDetailsForGivenInvoiceFromGivenTenant", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getInvoiceDetailsForGivenInvoice(@RequestParam String tenantId, @RequestParam String invoiceId, HttpServletResponse response) throws IOException {
        if(tenantId == null || invoiceId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getInvoiceDetailsForGivenInvoice(tenantId, invoiceId);
    }

    @RequestMapping(value = "/anon/getInvoiceLineItemsForGivenInvoiceFromGivenTenant", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getInvoiceDetailsForGivenVisit(@RequestParam String tenantId , @RequestParam long invoiceId, HttpServletResponse response) throws IOException {
        if(tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "tenantId cannot be null");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getInvoiceLineItemsForGivenInvoiceFromGivenTenant(tenantId, invoiceId);
    }

    @RequestMapping(value = "/anon/getInvoicePaymentForInvoiceId", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getInvoicePaymentForInvoiceId(@RequestParam String tenantId, @RequestParam long invoiceId, HttpServletResponse response) throws IOException {
        if(tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getInvoicePaymentForInvoiceId(tenantId, invoiceId);
    }

    @RequestMapping(value = "/getPrescriptionDetailsForGivenVisitFromGivenTenant", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPrescriptionDetailsForGivenVisitFromGivenTenant(@CookieValue("token") String token, @RequestParam String tenantId , @RequestParam String startDate, @RequestParam String doctorId,@RequestParam String visitStartDateTime, @RequestParam String visitEndDateTime, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);

        if((userDetail.size() <= 0 || userDetail.get("afyaId").toString() == "") || tenantId == null || doctorId == null || visitStartDateTime == null || visitEndDateTime == null || startDate == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getPrescriptionDetailsForGivenVisitFromGivenTenant(tenantId, userDetail.get("afyaId").toString(), startDate, doctorId, visitStartDateTime, visitEndDateTime);
    }

    @RequestMapping(value = "/getPrescriptionInvoiceDetailsForGivenPatient", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPrescriptionInvoiceDetailsForGivenPatient(@CookieValue("token") String token, HttpServletResponse response, boolean optinal) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_LIST;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);
        if(userDetail.size() <= 0 || userDetail.get("afyaId").toString() == "") {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "afya Id cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getPrescriptionInvoiceDetailsForGivenPatient(userDetail.get("afyaId").toString());
    }

    @RequestMapping(value = "/anon/getPrescriptionInvoiceDetailsForGivenPatient", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPrescriptionInvoiceDetailsForGivenPatient(@RequestParam String afyaId, HttpServletResponse response) throws IOException {
        if(afyaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "afya Id cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getPrescriptionInvoiceDetailsForGivenPatient(afyaId);
    }

    @RequestMapping(value = "/anon/getPrescriptionInvoiceLineItemDetailsForGivenPatient", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, Object>> getPrescriptionInvoiceLineItemDetailsForGivenPatient(@RequestParam String tenantId, @RequestParam String invoiceId, HttpServletResponse response) throws IOException {
        if(tenantId == null || invoiceId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalUtilityFinder.getPrescriptionInvoiceLineItemDetailsForGivenPatient(tenantId, invoiceId);
    }

    @RequestMapping(value = "/getEncounterDetailsForGivenPatientVisit", method = RequestMethod.GET)
    public @ResponseBody Map<String, List<Map<String, Object>>> getEncounterDetailsForGivenPatient(@CookieValue("token") String token, @RequestParam String tenantId , @RequestParam String startDate, @RequestParam String doctorId,@RequestParam String visitStartDateTime, @RequestParam String visitEndDateTime, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.EMPTY_MAP;
        }
        String username;
        username = SessionManager.getInstance().getSession(token);
        Map<String, Object> userDetail = portalFinder.getPatientByUsername(username);

        if((userDetail.size() <= 0 || userDetail.get("afyaId").toString() == null) || tenantId == null || doctorId == null || visitStartDateTime == null || visitEndDateTime == null || startDate == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalUtilityFinder.getEncounterDetailsForGivenPatient(tenantId, userDetail.get("afyaId").toString(), startDate, doctorId, visitStartDateTime, visitEndDateTime);
    }

    @RequestMapping(value = "/anon/updateMemberDetails", method = RequestMethod.POST)
    public boolean updateMemberDetails(@RequestBody PatientDto patientDto, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(patientDto)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request data cannot be empty");
            return false;
        }
        UpdateMemberDetailsCommand updateMemberDetailsCommand = new UpdateMemberDetailsCommand(patientDto);
        return commandGateway.sendAndWait(updateMemberDetailsCommand);
    }

    @RequestMapping(value = "/anon/emailWhenTriggeredContactUs", method = RequestMethod.POST)
    public boolean emailWhenTriggeredContactUs(@RequestBody Map<String, Object> details, HttpServletResponse response) throws IOException, MessagingException {
        if(UtilValidator.isEmpty(details)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request data cannot be empty");
            return false;
        }
       return EmailUtil.emailWhenTriggeredContactUs(details);
    }
}
