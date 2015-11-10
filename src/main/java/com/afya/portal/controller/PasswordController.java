package com.afya.portal.controller;

import com.afya.portal.domain.model.UserDet;
import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.presentation.FacilityDto;
import com.afya.portal.query.PasswordReset;
import com.afya.portal.query.UserLoginFinder;
import com.afya.portal.service.MailService;
import com.afya.portal.service.PortalTenantRestfulClient;
import com.afya.portal.util.UtilValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//import javax.websocket.server.PathParam;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pradyumna on 08-06-2015.
 */

@Controller
public class PasswordController {

    @Autowired
    UserLoginFinder userLoginFinder;

    @Autowired
    MailService mailService;

    @Autowired
    PasswordReset passwordReset;

    @Autowired
    private PortalTenantRestfulClient portalTenantRestfulClient;


    /*@Value("${PORTAL_BASE_URL}")
    private String PORTAL_BASE_URL;*/

    /*@RequestMapping(name = "/reset-request")
    @ResponseBody
    public ResponseEntity<String> generateResetPasswordUrl(@PathParam("username")String username){
        Map userLoginData = userLoginFinder.findUserWithUsername(username);
        if(userLoginData==null) return null;
        if(userLoginData.get("username")==null){
        }
        return null;
    }*/



//@CookieValue(value = "token", required = false) String token,
    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/anon/getEmailId")
    public @ResponseBody String getEmailId(@RequestParam(required = false) String username, HttpServletResponse response)throws Exception{
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        response.setContentType("text/plain");
        Map userLoginData = null;
        String msg ="";
        /*if((username == null || username.equals("")) && (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return "";
        }
        if ((username == null) && (token != null)) {
            username = SessionManager.getInstance().getSession(token);
        }*/
        try {
             userLoginData = userLoginFinder.findEmailIdWithUsername(username);
        }catch (Exception e){
            msg = "error";
            return msg;
        }
        if(userLoginData==null) {msg="error";}
        else if(userLoginData.get("email_id")== null){
            msg = "error";
        }else{
            Map<String, Object> mailModel = new HashMap<String, Object>();
            mailModel.put("sendTo", userLoginData.get("email_id"));
            //mailModel.put("confirmationCode", "");
            mailModel.put("user", username);
            mailModel.put("firstName", (userLoginData.get("first_name")==null)? "":userLoginData.get("first_name"));
            mailModel.put("lastName", (userLoginData.get("last_name")==null)? "":userLoginData.get("last_name"));
            mailService.sendMailResetPWD(mailModel);
            msg = "success";
        }

        return msg;
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/anon/resetPassword")
    public @ResponseBody String resetPassword(@RequestBody UserDet userDetails, HttpServletResponse response){
        response.setContentType("text/plain");
        int numberOfRows = passwordReset.resetPassword(userDetails.getUserName(), userDetails.getNewPassword());
        String tenantId = userLoginFinder.getTenantIdFromUsers(userDetails.getUserName());
        if(UtilValidator.isNotEmpty(tenantId)){
            portalTenantRestfulClient.updateClinicPassword(tenantId,userDetails.getUserName(),userDetails.getNewPassword());
        }
        if(numberOfRows == 1){
            return "success";
        }
        return "error";
    }

    @RequestMapping(value = "/ValidateCurrentPassword", method = RequestMethod.GET)
    public @ResponseBody String ValidateCurrentPassword(@CookieValue("token") String token, @RequestParam String password, HttpServletResponse response) throws IOException {
        if(  (token == null || token.equals(""))){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required either token or username");
            return "";
        }
        String username;
        username = SessionManager.getInstance().getSession(token);

        response.setContentType("text/plain");
        int numberOfRows = passwordReset.validateCurrentPassword(username ,password);
        if (numberOfRows >= 1) {
            return "success";
        }
        else
            return "Please enter valid current Password";
    }
}
