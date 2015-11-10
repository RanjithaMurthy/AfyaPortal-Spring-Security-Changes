package com.afya.portal.membercareprovider.view.controller;

import com.afya.portal.membercareprovider.application.NetworkCreateCommand;
import com.afya.portal.membercareprovider.domain.*;
import com.afya.portal.membercareprovider.view.dto.*;
import com.afya.portal.membercareprovider.view.query.MemberCareClinicConsumer;
import com.afya.portal.membercareprovider.view.query.MemberCareProviderFinder;
import com.afya.portal.presentation.FacilityDto;
import com.afya.portal.query.UserLoginFinder;
import com.afya.portal.util.UtilDateTime;
import com.afya.portal.util.UtilValidator;
import net.sf.jasperreports.j2ee.servlets.BaseHttpServlet;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 7/31/15
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping(value = "/anon/membercare")
public class MemberCareProviderController {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private MemberCareClinicConsumer memberCareClinicConsumer;

    @Autowired
    private UserLoginFinder userLoginFinder;

    @Autowired
    private MemberCareProviderFinder memberCareProviderFinder;

    @Autowired
    CommandGateway defaultCommandGateway;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/yesterdayClinicRevenue")
    public @ResponseBody YesterdayClinicRevenueDto yesterdayClinicRevenue(@CookieValue("token") String token, HttpServletResponse response) throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        //String currentUser = principal.getName();
        String[] tenantIdAndFacilityType = userLoginFinder.getTenantIdAndFacilityTypeFromTenantAssoc(currentUser);
        String facilityType = tenantIdAndFacilityType[0];
        String tenantId = tenantIdAndFacilityType[1];
        YesterdayClinicRevenueDto yesterdayClinicRevenueDto = memberCareClinicConsumer.getClinicYesterdayRevenue(tenantId, "N");
        yesterdayClinicRevenueDto.setProviderType(facilityType);
        return yesterdayClinicRevenueDto;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/monthClinicRevenue")
    public @ResponseBody YesterdayClinicRevenueDto monthClinicRevenue(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        //String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        String[] tenantIdAndFacilityType = userLoginFinder.getTenantIdAndFacilityTypeFromTenantAssoc(currentUser);
        String facilityType = tenantIdAndFacilityType[0];
        String tenantId = tenantIdAndFacilityType[1];
        YesterdayClinicRevenueDto yesterdayClinicRevenueDto = memberCareClinicConsumer.getClinicYesterdayRevenue(tenantId,"Y");
        yesterdayClinicRevenueDto.setProviderType(facilityType);
        return yesterdayClinicRevenueDto;
    }

    @RequestMapping(value = "/getYesterDayReferralAmount", method = RequestMethod.GET)
    public
    @ResponseBody
    BigDecimal getYesterDayReferralAmount(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        Date fromDate = UtilDateTime.getDayStart(new Date());
        return memberCareProviderFinder.getReferralAmount(tenantId, fromDate , fromDate);
    }

    @RequestMapping(value = "/getMonthReferralAmount", method = RequestMethod.GET)
    public
    @ResponseBody
    BigDecimal getMonthReferralAmount(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        Date thruDate = UtilDateTime.getDayStart(new Date());
        Date fromDate = UtilDateTime.getMonthStart(thruDate, -30);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        return memberCareProviderFinder.getReferralAmount(tenantId, fromDate , thruDate);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/incomeAnalysisByServiceType")
    public @ResponseBody List<ServiceIncomeAnalysisDto> getIncomeAnalysisByServiceType(@RequestBody ServiceTypeIncomeAnalysisDateRange serviceTypeIncomeAnalysisDateRange, @CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        Map<String,Object> map = new HashMap<>();
        map.put("type","Service");
        map.put("fromDate", serviceTypeIncomeAnalysisDateRange.getStartDate());
        map.put("thruDate", serviceTypeIncomeAnalysisDateRange.getEndDate());
        List<ServiceIncomeAnalysisDto> serviceIncomeAnalysisDtos =  memberCareClinicConsumer.getIncomeAnalysisByServiceType(tenantId,map);
        return serviceIncomeAnalysisDtos;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/incomeAnalysisBySpecialty")
    public @ResponseBody List<SpecialtyIncomeAnalysisDto> getIncomeAnalysisBySpecialty(@RequestBody SpecialtyIncomeAnalysisDateRange specialtyIncomeAnalysisDateRange, @CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        Map<String,Object> map = new HashMap<>();
        map.put("type","Specialty");
        map.put("fromDate", specialtyIncomeAnalysisDateRange.getStartDate());
        map.put("thruDate", specialtyIncomeAnalysisDateRange.getEndDate());
        List<SpecialtyIncomeAnalysisDto> specialtyIncomeAnalysisDtos =  memberCareClinicConsumer.getIncomeAnalysisBySpecialty(tenantId, map);
        return specialtyIncomeAnalysisDtos;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/inviteNetwork")
    public @ResponseBody Network inviteNetwork(@RequestBody NetworkCreateCommand networkCreateCommand,@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        String clinicId = userLoginFinder.getClinicIdFromClinic(tenantId);
        networkCreateCommand.setFromClinicId(clinicId);
        Network network = defaultCommandGateway.sendAndWait(networkCreateCommand);
        return network;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/getAllNetworkRequested")
    public @ResponseBody List<NetworkDto> getAllNetworkRequested(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        List<FacilityDto> facilityDtos = userLoginFinder.getFacilities(currentUser);
        List<NetworkDto> networkDtos = new ArrayList<>();
        for(FacilityDto facilityDto : facilityDtos){
            if("0".equals(facilityDto.getFacilityType())){
              String clinicId = facilityDto.getFacilityId();
              networkDtos =  memberCareProviderFinder.findRequestedNetworkByClinicId(clinicId);
            }
        }
        return networkDtos;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/getAllAcceptedNetworkRequested")
    public @ResponseBody List<NetworkDto> getAllAcceptedNetworkRequested(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        List<FacilityDto> facilityDtos = userLoginFinder.getFacilities(currentUser);
        List<NetworkDto> networkDtos = new ArrayList<>();
        for(FacilityDto facilityDto : facilityDtos){
            if("0".equals(facilityDto.getFacilityType())){
                String clinicId = facilityDto.getFacilityId();
                networkDtos =  memberCareProviderFinder.findAllAcceptedNetworkRequested(clinicId);
            }
        }
        return networkDtos;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/getAllBlockedNetworkRequest")
    public @ResponseBody List<NetworkDto> getAllBlockedNetworkRequest(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        List<FacilityDto> facilityDtos = userLoginFinder.getFacilities(currentUser);
        List<NetworkDto> networkDtos = new ArrayList<>();
        for(FacilityDto facilityDto : facilityDtos){
            if("0".equals(facilityDto.getFacilityType())){
                String clinicId = facilityDto.getFacilityId();
                networkDtos =  memberCareProviderFinder.findAllBlockedNetworkRequest(clinicId);
            }
        }
        return networkDtos;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/acceptNetwork")
    public @ResponseBody HttpEntity<String> acceptNetwork(@RequestBody NetworkDto networkDto){
        NetworkCreateCommand networkCreateCommand = new NetworkCreateCommand();
        networkCreateCommand.setNetworkId(networkDto.getNetworkId());
        networkCreateCommand.setNetworkStatus(Network.STATUS.ACCEPTED.toString());
        Network network = defaultCommandGateway.sendAndWait(networkCreateCommand);
        return new HttpEntity<>("success");
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/rejectNetwork")
    public @ResponseBody HttpEntity<String> rejectNetwork(@RequestBody NetworkDto networkDto){
        NetworkCreateCommand networkCreateCommand = new NetworkCreateCommand();
        networkCreateCommand.setNetworkId(networkDto.getNetworkId());
        networkCreateCommand.setNetworkStatus(Network.STATUS.REJECTED.toString());
        Network network = defaultCommandGateway.sendAndWait(networkCreateCommand);
        return new HttpEntity<>("success");
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/blockNetwork")
    public @ResponseBody HttpEntity<String> blockNetwork(@RequestBody NetworkDto networkDto){
        NetworkCreateCommand networkCreateCommand = new NetworkCreateCommand();
        networkCreateCommand.setNetworkId(networkDto.getNetworkId());
        networkCreateCommand.setNetworkStatus(Network.STATUS.BLOCKED.toString());
        Network network = defaultCommandGateway.sendAndWait(networkCreateCommand);
        return new HttpEntity<>("success");
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/unBlockNetwork")
    public @ResponseBody HttpEntity<String> unBlockNetwork(@RequestBody NetworkDto networkDto){
        NetworkCreateCommand networkCreateCommand = new NetworkCreateCommand();
        networkCreateCommand.setNetworkId(networkDto.getNetworkId());
        networkCreateCommand.setNetworkStatus(Network.STATUS.UNBLOCKED.toString());
        Network network = defaultCommandGateway.sendAndWait(networkCreateCommand);
        return new HttpEntity<>("success");
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/incomeAnalysisByDoctor")
    public @ResponseBody List<DoctorIncomeAnalysisDto> getIncomeAnalysisByDoctor(@RequestBody DoctorIncomeAnalysisDateRange doctorIncomeAnalysisDateRange, @CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        Map<String,Object> map = new HashMap<>();
        map.put("type","Doctor");
        map.put("fromDate", doctorIncomeAnalysisDateRange.getStartDate());
        map.put("thruDate", doctorIncomeAnalysisDateRange.getEndDate());
        List<DoctorIncomeAnalysisDto> doctorIncomeAnalysisDtos =  memberCareClinicConsumer.getIncomeAnalysisByDoctor(tenantId, map);
        return doctorIncomeAnalysisDtos;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/incomeAnalysisByPatientCategory")
    public @ResponseBody List<PatientCategoryIncomeAnalysisDto> getIncomeAnalysisByPatientCategory(@RequestBody PatientCategoryIncomeAnalysisDateRange patientCategoryIncomeAnalysisDateRange, @CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        Map<String,Object> map = new HashMap<>();
        map.put("type","PatientCategory");
        map.put("fromDate", patientCategoryIncomeAnalysisDateRange.getStartDate());
        map.put("thruDate", patientCategoryIncomeAnalysisDateRange.getEndDate());
        List<PatientCategoryIncomeAnalysisDto> patientCategoryIncomeAnalysisDtos =  memberCareClinicConsumer.getIncomeAnalysisByPatientCategory(tenantId, map);
        return patientCategoryIncomeAnalysisDtos;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/getAllInitiatedContract")
    public @ResponseBody List<InitiatedContractDto> getAllInitiatedContract(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        String clinicId = userLoginFinder.getClinicIdFromClinic(tenantId);
        List<InitiatedContractDto> initiatedContractDtos = new ArrayList<>();
        initiatedContractDtos =  memberCareProviderFinder.findAllInitiatedContractByClinicId(clinicId);
        return initiatedContractDtos;
    }

    @RequestMapping(value = "/getReferralData",produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody List<ReferalDataDto> getReferralData(@CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        Date thruDate = UtilDateTime.getDayStart(new Date(),1);
        Date fromDate = UtilDateTime.getDayStart(new Date(), -61);
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        List<ReferalDataDto> referalDataDtos = new ArrayList<>();
        referalDataDtos = memberCareProviderFinder.getReferralData(tenantId, fromDate, thruDate);
        return referalDataDtos;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/getReferralDataByDate")
    public @ResponseBody List<ReferalDataDto> getReferralDataByDate(@RequestBody ReferralDataDateRange referralDataDateRange, @CookieValue("token") String token, HttpServletResponse response)throws Exception{
        if(UtilValidator.isEmpty(token)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not logged in");
        }
        String currentUser = SessionManager.getInstance().getSession(token);
        Date thruDate = referralDataDateRange.getEndDate();
        Date fromDate = referralDataDateRange.getStartDate();
        String tenantId = userLoginFinder.getTenantIdFromTenantAssoc(currentUser);
        List<ReferalDataDto> referalDataDtos = new ArrayList<>();
        referalDataDtos = memberCareProviderFinder.getReferralData(tenantId, fromDate, thruDate);
        return referalDataDtos;
    }

}
