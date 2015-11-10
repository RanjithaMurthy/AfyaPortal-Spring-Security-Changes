package com.afya.portal.service;

import com.afya.portal.util.UtilValidator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Created by Mohan Sharma on 8/15/2015.
 */
@Service
public class PortalTenantRestfulClient {

    @Value("${CLINIC_BASE_URL}")
    private String CLINIC_BASE_URL;
    @Value("${PHARMACY_BASE_URL}")
    private String PHARMACY_BASE_URL;
    @Value("${PHARMACY_USERNAME}")
    private String PHARMACY_USERNAME;
    @Value("${PHARMACY_PASSWORD}")
    private String PHARMACY_PASSWORD;

    public String getAvailableTimeslotsForAGivenTenantDateAndDoctor(String clinicId, String doctorId, String appointmentDate, String visitType) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/getAvailableTimeslotsForGivenDateAndDoctorAndBookAppointment?clinicId={clinicId}&providerId={providerId}&appointmentDate={appointmentDate}&visitType={visitType}", HttpMethod.GET, requestEntity, String.class, clinicId, doctorId, appointmentDate, visitType);
        String json = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return json;
    }

    public List<Map<String, Object>> bookAppointmentForAGivenTenantDateAndDoctor(String clinicId, String doctorId, String appointmentDate) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/getAvailableTimeslotsForGivenDateAndDoctorAndBookAppointment?clinicId={clinicId}&providerId={providerId}&appointmentDate={appointmentDate}", HttpMethod.GET, requestEntity, String.class, clinicId, doctorId, appointmentDate);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = Lists.newArrayList();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public String rescheduleAppointmentForAGivenTenantDateAndDoctor(String clinicId, String rescheduleDataAsJson) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(rescheduleDataAsJson, httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL + "ospedale/clinicMaster/updatePatientAppointmentWhenTriggeredFromMobileApp?clinicId={clinicId}", HttpMethod.POST, requestEntity, String.class, clinicId);
        String result = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public String cancelAppointmentForAGivenTenantDateAndDoctor(String clinicId, String scheduleId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL + "ospedale/clinicMaster/cancelPatientAppointmentWhenTriggeredFromMobileApp?clinicId={clinicId}&scheduleId={scheduleId}", HttpMethod.POST, requestEntity, String.class, clinicId, scheduleId);
        String result = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public String getResponseFromUrl(String requestUrl) {
        RestTemplate restTemplate = new RestTemplate();
        // HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity, String.class);
        String response = responseEntity.getBody();
        return response;
    }

    public Map<String, Object> getLogoURLFromTenant(String tenantId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/getTenantLogoURL?clinicId={clinicId}", HttpMethod.GET, requestEntity, String.class, tenantId);
        Map<String, Object> resultMap = new HashMap<>();
        String json = responseEntity.getBody();
        Gson gson = new GsonBuilder().serializeNulls().create();
        resultMap = (Map<String, Object>) gson.fromJson(json, resultMap.getClass());
        return resultMap;
    }


    public void updateClinicPassword(String clinicId, String userName, String password) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/updateClinicPassword?clinicId={clinicId}&userName={userName}&password={password}", HttpMethod.POST, requestEntity, String.class, clinicId, userName, password);

    }

    private static HttpHeaders getHttpHeader(){
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public Map<String, Object> getPharmacyLogoURL(String tenantId) throws IOException {
        if(UtilValidator.isEmpty(tenantId)){
            return Collections.emptyMap();
        }
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        try {
            HttpClient httpclient= HttpClients.custom().build();
            factory.setHttpClient(httpclient);
            HttpUriRequest login = RequestBuilder.get()
                    .setUri(new URI(PHARMACY_BASE_URL+"/ordermgr/control/login"))
                    .addParameter("USERNAME", PHARMACY_USERNAME)
                    .addParameter("PASSWORD", PHARMACY_PASSWORD)
                    .addParameter("tenantId", tenantId)
                    .build();
            HttpResponse response = httpclient.execute(login);
            RestTemplate rt = new RestTemplate(factory);
            HttpHeaders httpHeaders = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> responseEntity = rt.exchange(PHARMACY_BASE_URL+"/ordermgr/control/getPharmacyLogoImageUrl?tenantId={tenantId}", HttpMethod.GET, requestEntity, String.class, tenantId);
            String json = responseEntity.getBody();
            if(UtilValidator.isEmpty(json)){
                return Collections.emptyMap();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_MAP;
    }

    public void routingTenantDataSource(String tenantId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/routingTenantDataSource?clinicId={clinicId}", HttpMethod.GET, requestEntity, String.class,tenantId);
    }

    /* Ranjitha Changes*/
    public String requestAppointment(String clinicId, String requestAppointmentDataAsJson) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(requestAppointmentDataAsJson, httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/requestAppointment?clinicId={clinicId}", HttpMethod.POST, requestEntity, String.class, clinicId);
        String result = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public List<Map<String, Object>> getActivePrescription(String afyaId,String clinicId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/getActivePrescription?afyaId={afyaId}&clinicId={clinicId}", HttpMethod.GET, requestEntity, String.class, afyaId, clinicId);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = Lists.newArrayList();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public Map<String, Object> createActivePrescriptionOrder(String clinicId, String dataAsJson) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(dataAsJson, httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/createActivePrescriptionOrder?clinicId={clinicId}", HttpMethod.POST, requestEntity, String.class, clinicId);
        String json = responseEntity.getBody();
        Map<String, Object> result = new HashMap<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (Map<String, Object>) gson.fromJson(json, result.getClass());
        return result;
    }

    public List<Map<String, Object>> getInsuranceDetailsBySchedule(String clinicId,String invoiceId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/getInsuranceDetailsBySchedule?clinicId={clinicId}&invoiceId={invoiceId}", HttpMethod.GET, requestEntity, String.class, clinicId, invoiceId);
        String json = responseEntity.getBody();
        List<Map<String, Object>> result = Lists.newArrayList();
        Gson gson = new GsonBuilder().serializeNulls().create();
        result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public String updateScheduleTentativeGenerateInvoice(String clinicId, String scheduleId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL + "ospedale/clinicMaster/updateScheduleTentativeGenerateInvoice?clinicId={clinicId}&scheduleId={scheduleId}", HttpMethod.POST, requestEntity, String.class, clinicId, scheduleId);
        String result = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public String createActivePrescriptionPayment(String clinicId, String orderId, String afyaId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL + "ospedale/clinicMaster/createActivePrescriptionPayment?clinicId={clinicId}&orderId={orderId}&afyaId={afyaId}", HttpMethod.GET, requestEntity, String.class, clinicId, orderId, afyaId);
        String result = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public String completeOrder(String clinicId, String orderId, String afyaId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL + "ospedale/clinicMaster/completeOrder?clinicId={clinicId}&orderId={orderId}&afyaId={afyaId}", HttpMethod.POST, requestEntity, String.class, clinicId, orderId, afyaId);
        String result = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }

    public String cancelOrder(String clinicId, String orderId, String afyaId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = getHttpHeader();
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL + "ospedale/clinicMaster/cancelOrder?clinicId={clinicId}&orderId={orderId}&afyaId={afyaId}", HttpMethod.POST, requestEntity, String.class, clinicId, orderId, afyaId);
        String result = responseEntity.getBody();
        //List<Map<String, Object>> result = Lists.newArrayList();
        //Gson gson = new GsonBuilder().serializeNulls().create();
        //result = (List<Map<String, Object>>) gson.fromJson(json, result.getClass());
        return result;
    }
}
