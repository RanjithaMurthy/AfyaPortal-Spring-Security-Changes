package com.afya.portal.membercareprovider.view.query;

import com.afya.portal.membercareprovider.view.dto.*;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/2/15
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class MemberCareClinicConsumer {

    @Value("${CLINIC_BASE_URL}")
    private String CLINIC_BASE_URL;

    public YesterdayClinicRevenueDto getClinicYesterdayRevenue(String tenantId, String isFullMonth){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/clinicYesterdayRevenue?clinicId={tenantId}&isFullMonth=" + isFullMonth,
                HttpMethod.GET, requestEntity, String.class,tenantId);

        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        //mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withVisibility(JsonAutoDetect.Visibility.ANY));
        YesterdayClinicRevenueDto yesterdayClinicRevenueDto = null;
        try {
            yesterdayClinicRevenueDto =  mapper.readValue(json,  YesterdayClinicRevenueDto.class );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return yesterdayClinicRevenueDto;
    }

    public List<ServiceIncomeAnalysisDto> getIncomeAnalysisByServiceType(String tenantId,Map<String,Object> bodyMap){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(bodyMap,httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/memberCareClinicIncome?clinicId={tenantId}",
                HttpMethod.POST, requestEntity, String.class,tenantId);

        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        //mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withVisibility(JsonAutoDetect.Visibility.ANY));
        List<ServiceIncomeAnalysisDto> serviceIncomeAnalysisDtos = null;
        try {
            serviceIncomeAnalysisDtos =  mapper.readValue(json,  mapper.getTypeFactory().constructCollectionType(List.class, ServiceIncomeAnalysisDto.class) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serviceIncomeAnalysisDtos;
    }

    public List<SpecialtyIncomeAnalysisDto> getIncomeAnalysisBySpecialty(String tenantId,Map<String,Object> bodyMap){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(bodyMap,httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/memberCareClinicIncome?clinicId={tenantId}",
                HttpMethod.POST, requestEntity, String.class,tenantId);

        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        //mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withVisibility(JsonAutoDetect.Visibility.ANY));
        List<SpecialtyIncomeAnalysisDto> specialtyIncomeAnalysisDtos = null;
        try {
            specialtyIncomeAnalysisDtos =  mapper.readValue(json,  mapper.getTypeFactory().constructCollectionType(List.class, SpecialtyIncomeAnalysisDto.class) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return specialtyIncomeAnalysisDtos;
    }

    public List<DoctorIncomeAnalysisDto> getIncomeAnalysisByDoctor(String tenantId,Map<String,Object> bodyMap){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(bodyMap,httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/memberCareClinicIncome?clinicId={tenantId}",
                HttpMethod.POST, requestEntity, String.class,tenantId);

        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        //mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withVisibility(JsonAutoDetect.Visibility.ANY));
        List<DoctorIncomeAnalysisDto> doctorIncomeAnalysisDtos = null;
        try {
            doctorIncomeAnalysisDtos =  mapper.readValue(json,  mapper.getTypeFactory().constructCollectionType(List.class, DoctorIncomeAnalysisDto.class) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doctorIncomeAnalysisDtos;
    }

    public List<PatientCategoryIncomeAnalysisDto> getIncomeAnalysisByPatientCategory(String tenantId,Map<String,Object> bodyMap){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(bodyMap,httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(CLINIC_BASE_URL+"ospedale/clinicMaster/memberCareClinicIncome?clinicId={tenantId}",
                HttpMethod.POST, requestEntity, String.class,tenantId);

        String json = responseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        //mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withVisibility(JsonAutoDetect.Visibility.ANY));
        List<PatientCategoryIncomeAnalysisDto> patientCategoryIncomeAnalysisDtos = null;
        try {
            patientCategoryIncomeAnalysisDtos =  mapper.readValue(json,  mapper.getTypeFactory().constructCollectionType(List.class, PatientCategoryIncomeAnalysisDto.class) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return patientCategoryIncomeAnalysisDtos;
    }

}


