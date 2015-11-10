package com.afya.portal.controller;

import com.afya.portal.application.*;
import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.query.PortalFacilityFinder;
import com.afya.portal.query.PortalUtilityFinder;
import com.afya.portal.query.UserLoginFinder;
import com.afya.portal.util.UtilValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.dto.UserLoginDto;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.common.service.JpaRepositoryFactory;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mohan Sharma on 7/13/2015.
 */
@RestController
public class PortalFacilityUtilityController {

    private PortalFacilityFinder portalFacilityFinder;
    private PortalUtilityFinder portalUtilityFinder;
    private CommandGateway commandGateway;
    private SimpleJpaRepository<UserLogin, String> userLoginRepository;

    @Autowired
    private UserLoginFinder userLoginFinder;

    @Autowired
    public PortalFacilityUtilityController(PortalFacilityFinder portalFacilityFinder, CommandGateway commandGateway, PortalUtilityFinder portalUtilityFinder, JpaRepositoryFactory jpaRepositoryFactory){
        this.portalFacilityFinder = portalFacilityFinder;
        this.commandGateway = commandGateway;
        this.portalUtilityFinder = portalUtilityFinder;
        this.userLoginRepository = jpaRepositoryFactory.getCrudRepository(UserLogin.class);
    }

    @RequestMapping(value = "/persistUserLoginFacilityAssociation", method = RequestMethod.POST)
    public String persistUserLoginFacilityAssociation(@CookieValue("token") String token, @RequestParam String tenantId, @RequestParam String facilityType, HttpServletResponse response, boolean optinal) throws IOException, SQLException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return StringUtils.EMPTY;
        }
        String userName;
        userName = SessionManager.getInstance().getSession(token);

        if(userName == null || userName.equals("") || tenantId == null || tenantId.equals("") || facilityType == null || facilityType.equals("")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parametes cannot be empty");
            return StringUtils.EMPTY;
        }
        PersistUserTenantAssocCommand persistUserTenantAssocCommand = new PersistUserTenantAssocCommand(userName, tenantId, facilityType);
        return commandGateway.sendAndWait(persistUserTenantAssocCommand);
    }

    @RequestMapping(value = "/anon/persistUserLoginFacilityAssociation", method = RequestMethod.POST)
    public String persistUserLoginFacilityAssociation(@RequestParam String userName, @RequestParam String tenantId, @RequestParam String facilityType, HttpServletResponse response) throws IOException, SQLException {
        if(userName == null || userName.equals("") || tenantId == null || tenantId.equals("") || facilityType == null || facilityType.equals("")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parametes cannot be empty");
            return StringUtils.EMPTY;
        }
        PersistUserTenantAssocCommand persistUserTenantAssocCommand = new PersistUserTenantAssocCommand(userName, tenantId, facilityType);
        return commandGateway.sendAndWait(persistUserTenantAssocCommand);
    }

    @RequestMapping(value = "/getUserLoginDetailsForUserName", method = RequestMethod.GET)
    public Map<String, Object> getUserLoginDetailsForUserName(@CookieValue("token") String token, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return Collections.emptyMap();
        }
        String userName;
        userName = SessionManager.getInstance().getSession(token);

        if(userName == null || userName.equals("")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parameters cannot be null");
            return Collections.emptyMap();
        }
        return portalFacilityFinder.getUserLoginDetailsForUserName(userName);
    }

    @RequestMapping(value = "/anon/persistUsersLoginFromTenant", method = RequestMethod.POST)
    public String persistUsersLoginFromTenant(@RequestBody String userLoginJson,HttpServletResponse response) throws IOException, SQLException {
        if(userLoginJson == null || userLoginJson.equals("")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parametes cannot be empty");
            return StringUtils.EMPTY;
        }
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        UserLoginDto userLoginDto = gson.fromJson(userLoginJson, UserLoginDto.class);
        if(UtilValidator.isEmpty(userLoginDto.getUsername()) || UtilValidator.isEmpty(userLoginDto.getPassword()) || UtilValidator.isEmpty(userLoginDto.getRoles())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request parametes cannot be empty");
            return StringUtils.EMPTY;
        }
        PersistUserLoginFromTenantCommand persistUserLoginFromTenantCommand = new PersistUserLoginFromTenantCommand(userLoginDto);
        String result = commandGateway.sendAndWait(persistUserLoginFromTenantCommand);
        Set<String> roles = portalFacilityFinder.findIfAuthorizationAlreadyExistForUser(userLoginDto);
        if(result.equals("success") && UtilValidator.isNotEmpty(roles)){
            PersistAuthorityForUserCommand persistAuthorityForUserCommand = new PersistAuthorityForUserCommand(userLoginDto, roles);
            return commandGateway.sendAndWait(persistAuthorityForUserCommand);
        }
        return null;
    }


    @RequestMapping(value = "/anon/getServiceItemCountForGivenPackageId", method = RequestMethod.GET)
    public Map<String, Object> getServiceItemCountForGivenPackageId(@RequestParam String packageId, HttpServletResponse response) throws IOException, MessagingException {
        if(UtilValidator.isEmpty(packageId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "packageId cannot be empty");
            return Collections.EMPTY_MAP;
        }
        return portalUtilityFinder.getServiceItemCountForGivenPackageId(packageId);
    }

    @RequestMapping(value = "/anon/modifyUserLoginDetails", method = RequestMethod.POST)
    public String modifyUserLoginDetails(@RequestBody UserLoginDto userLoginDto, HttpServletResponse response) throws IOException, MessagingException {
        String result = "unsuccessful";
        if(UtilValidator.isEmpty(userLoginDto)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "bad request body");
            return result;
        }
        ModifyUserLoginCommand modifyUserLoginCommand = new ModifyUserLoginCommand(userLoginDto);
        if(commandGateway.sendAndWait(modifyUserLoginCommand)) {
            // updating clinic and pharmacy table field on premium member registration
            UserLogin userLogin = userLoginRepository.findOne(userLoginDto.getUsername());
            userLoginDto.setLoginPreference(userLogin.getLoginPreference());
            userLoginDto.setEmailId(userLogin.getEmailId());
            userLoginDto.setMobileNumber(userLogin.getMobileNumber());
            String updateResult = userLoginFinder.updateProviderDetail(userLoginDto);
            if(updateResult == "SUCCESS")
                return "successfully update";
            else
                return "error";
        }
        return result;
    }

    @RequestMapping(value = "/anon/updateSMSCountForGivenTenant", method = RequestMethod.POST)
    public boolean updateSMSCountForGivenTenant(@RequestParam String tenantId, HttpServletResponse response) throws IOException {
        Boolean result = Boolean.FALSE;
        if(UtilValidator.isEmpty(tenantId)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "TenantId Cannot be empty");
            return result;
        }
        UpdateSmsCountTriggeredFromTenantCommand updateSmsCountTriggeredFromTenantCommand = new UpdateSmsCountTriggeredFromTenantCommand(tenantId);
        return commandGateway.sendAndWait(updateSmsCountTriggeredFromTenantCommand);
    }

    @RequestMapping(value = "/anon/checkIfSmsAvailableForTenant", method = RequestMethod.GET)
    public boolean checkIfSmsAvailableForTenant(@RequestParam String tenantId, HttpServletResponse response) throws IOException {
        Boolean result = Boolean.FALSE;
        if(UtilValidator.isEmpty(tenantId)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "TenantId Cannot be empty");
            return result;
        }
        return portalFacilityFinder.checkIfSmsAvailableForTenant(tenantId);
    }

    @RequestMapping(value = "/anon/updateSMSSenderInGivenTenant", method = RequestMethod.POST)
    public boolean updateSMSSenderInGivenTenant(@RequestBody UpdateSMSSenderInGivenTenantCommand updateSMSSenderInGivenTenantCommand, HttpServletResponse response) throws IOException {
        Boolean result = Boolean.FALSE;
        if(UtilValidator.isEmpty(updateSMSSenderInGivenTenantCommand.getTenantId()) || UtilValidator.isEmpty(updateSMSSenderInGivenTenantCommand.getSmsSenderName())){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "TenantId or smsSenderName Cannot be empty");
            return result;
        }
        return commandGateway.sendAndWait(updateSMSSenderInGivenTenantCommand);
    }

    @RequestMapping(value = "/anon/updateSMSSenderVerifiedInGivenTenant", method = RequestMethod.POST)
    public boolean updateSMSSenderVerifiedInGivenTenant(@RequestBody UpdateSMSSenderVerifiedInGivenTenantCommand updateSMSSenderVerifiedInGivenTenantCommand, HttpServletResponse response) throws IOException {
        Boolean result = Boolean.FALSE;
        if(UtilValidator.isEmpty(updateSMSSenderVerifiedInGivenTenantCommand.getTenantId())){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "TenantId Cannot be empty");
            return result;
        }
        return commandGateway.sendAndWait(updateSMSSenderVerifiedInGivenTenantCommand);
    }

    @RequestMapping(value = "/anon/updateProviderDetails", method = RequestMethod.POST)
    public String updateProviderDetails(@RequestBody UserLoginDto userLoginDto, HttpServletResponse response) throws IOException, MessagingException {
        return userLoginFinder.updateProviderDetail(userLoginDto);
    }
}
