package com.afya.portal.controller;

import com.afya.portal.query.PortalTenantFinder;
import com.afya.portal.service.PortalTenantRestfulClient;
import org.apache.commons.lang3.StringUtils;
import org.nthdimenzion.utils.UtilValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 8/15/2015.
 */
@RestController
public class PortalTenantInteractiveController {

    private PortalTenantRestfulClient portalTenantRestfulClient;
    private PortalTenantFinder portalTenantFinder;

    @Autowired
    public PortalTenantInteractiveController(PortalTenantRestfulClient portalTenantRestfulClient, PortalTenantFinder portalTenantFinder){
        this.portalTenantRestfulClient = portalTenantRestfulClient;
        this.portalTenantFinder = portalTenantFinder;
    }

    @RequestMapping(value = "/anon/getAvailableTimeslotsForAGivenTenantDateAndDoctor", method = RequestMethod.GET)
    public @ResponseBody String getAvailableTimeslotsForAGivenTenantDateAndDoctor(@RequestParam String tenantId , @RequestParam String doctorId, @RequestParam String appointmentDate, @RequestParam String visitType, HttpServletResponse response) throws IOException {
        if(tenantId == null || doctorId == null || appointmentDate == null || visitType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return null;
        }
        return portalTenantRestfulClient.getAvailableTimeslotsForAGivenTenantDateAndDoctor(tenantId, doctorId, appointmentDate, visitType);
    }

    @RequestMapping(value = "/anon/rescheduleAppointmentForAGivenTenantDateAndDoctor", method = RequestMethod.POST)
    public @ResponseBody String rescheduleAppointmentForAGivenTenantDateAndDoctor(@RequestParam String tenantId, @RequestBody String rescheduleDataAsJson, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(tenantId) || UtilValidator.isEmpty(rescheduleDataAsJson)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return null;
        }
        return portalTenantRestfulClient.rescheduleAppointmentForAGivenTenantDateAndDoctor(tenantId, rescheduleDataAsJson);
    }

    @RequestMapping(value = "/anon/cancelAppointmentForAGivenTenantDateAndDoctor", method = RequestMethod.POST)
    public @ResponseBody String cancelAppointmentForAGivenTenantDateAndDoctor(@RequestParam String tenantId, @RequestParam String scheduleId, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(tenantId) || UtilValidator.isEmpty(scheduleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return null;
        }
        return portalTenantRestfulClient.cancelAppointmentForAGivenTenantDateAndDoctor(tenantId, scheduleId);
    }

    @RequestMapping(value = "/anon/getResponseFromUrl", method = RequestMethod.GET)
    public @ResponseBody
    String getResponseFromUrl(@RequestParam String requestUrl, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(requestUrl)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return "";
        }
        return portalTenantRestfulClient.getResponseFromUrl(requestUrl);
    }

    @RequestMapping(value = "/anon/getLogoURLFromTenant", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> getLogoURLFromTenant(@RequestParam String tenantId, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(tenantId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalTenantRestfulClient.getLogoURLFromTenant(tenantId);
    }

    @RequestMapping(value = "/anon/getPharmacyLogoURL", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> getPharmacyLogoURL(@RequestParam String tenantId, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(tenantId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalTenantRestfulClient.getPharmacyLogoURL(tenantId);
    }

    @RequestMapping(value = "/anon/getSMSSenderNameForGivenTenant", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> getSMSSenderNameForGivenTenant(@RequestParam String tenantId, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(tenantId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalTenantFinder.getSMSSenderNameForGivenTenant(tenantId);
    }

    @RequestMapping(value = "/anon/checkIfTenantIsSubscribedToJoinInPackage", method = RequestMethod.GET)
    public Map<String, Object> checkIfTenantIsSubscribedToJoinInPackage(@RequestParam String tenantId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(tenantId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalTenantFinder.checkIfTenantIsSubscribedToJoinInPackage(tenantId);
    }

    /* Ranjitha Changes*/
    @RequestMapping(value = "/anon/requestAppointment", method = RequestMethod.POST)
    public @ResponseBody String requestAppointment(@RequestParam String clinicId, @RequestBody String requestDataAsJson, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(clinicId) || UtilValidator.isEmpty(requestDataAsJson)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return null;
        }
        return portalTenantRestfulClient.requestAppointment(clinicId, requestDataAsJson);
    }

    @RequestMapping(value = "/anon/getActivePrescription", method = RequestMethod.GET)
    public @ResponseBody
    List<Map<String, Object>> getActivePrescription(@RequestParam String afyaId , @RequestParam String clinicId, HttpServletResponse response) throws IOException {
        if(afyaId == null || clinicId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalTenantRestfulClient.getActivePrescription(afyaId, clinicId);
    }

    @RequestMapping(value = "/anon/createActivePrescriptionOrder", method = RequestMethod.POST)
    public @ResponseBody Map<String, Object> createActivePrescriptionOrder(@RequestParam String clinicId, @RequestBody String dataAsJson, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(clinicId) || UtilValidator.isEmpty(dataAsJson)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalTenantRestfulClient.createActivePrescriptionOrder(clinicId, dataAsJson);
    }

    @RequestMapping(value = "/anon/getInsuranceDetailsBySchedule", method = RequestMethod.GET)
    public @ResponseBody
    List<Map<String, Object>> getInsuranceDetailsBySchedule(@RequestParam String clinicId , @RequestParam String invoiceId, HttpServletResponse response) throws IOException {
        if(clinicId == null || invoiceId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return Collections.EMPTY_LIST;
        }
        return portalTenantRestfulClient.getInsuranceDetailsBySchedule(clinicId, invoiceId);
    }

    @RequestMapping(value = "/anon/updateScheduleTentativeGenerateInvoice", method = RequestMethod.POST)
    public @ResponseBody String updateScheduleTentativeGenerateInvoice(@RequestParam String clinicId, @RequestParam String scheduleId, HttpServletResponse response) throws IOException {
        if(UtilValidator.isEmpty(clinicId) || UtilValidator.isEmpty(scheduleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return "";
        }
        return portalTenantRestfulClient.updateScheduleTentativeGenerateInvoice(clinicId, scheduleId);
    }

    @RequestMapping(value = "/anon/createActivePrescriptionPayment", method = RequestMethod.GET)
    public @ResponseBody String createActivePrescriptionPayment(@RequestParam String clinicId , @RequestParam String orderId, @RequestParam String afyaId, HttpServletResponse response) throws IOException {
        if(clinicId == null || orderId == null || afyaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return "";
        }
        return portalTenantRestfulClient.createActivePrescriptionPayment(clinicId, orderId, afyaId);
    }

    @RequestMapping(value = "/anon/completeOrder", method = RequestMethod.POST)
    public @ResponseBody String completeOrder(@RequestParam String clinicId , @RequestParam String orderId, @RequestParam String afyaId, HttpServletResponse response) throws IOException {
        if(clinicId == null || orderId == null || afyaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return "";
        }
        return portalTenantRestfulClient.completeOrder(clinicId, orderId, afyaId);
    }

    @RequestMapping(value = "/anon/cancelOrder", method = RequestMethod.POST)
    public @ResponseBody String cancelOrder(@RequestParam String clinicId , @RequestParam String orderId, @RequestParam String afyaId, HttpServletResponse response) throws IOException {
        if(clinicId == null || orderId == null || afyaId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be empty");
            return "";
        }
        return portalTenantRestfulClient.cancelOrder(clinicId, orderId, afyaId);
    }
}
