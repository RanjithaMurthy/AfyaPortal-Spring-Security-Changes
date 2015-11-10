package com.afya.portal.controller;

import com.afya.portal.application.PersistFacilityCommand;
import com.afya.portal.application.RegisterCarePayerCommand;
import com.afya.portal.application.RegisterPatientCommand;
import com.afya.portal.domain.model.ProviderType;
import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.query.PortalUtilityFinder;
import com.afya.portal.query.UserLoginFinder;
import com.afya.portal.service.PortalTenantRestfulClient;
import com.afya.portal.util.UtilValidator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.dto.PatientDto;
import com.nzion.dto.UserLoginDto;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.net.ssl.SSLContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by pradyumna on 11-06-2015.
 * <p/>
 * This will handle the registration of Patient as well as other Facility Type.
 */
@Controller
public class ProvisioningController {


    @Autowired
    private CommandGateway commandGateway;
    @Autowired
    private PortalUtilityFinder portalUtilityFinder;
    @Autowired
    private UserLoginFinder userLoginFinder;

    @Autowired
    private PortalTenantRestfulClient portalTenantRestfulClient;



    /**
     * Added by pradyumna. This can be accessed by anonymous user
     *
     * @return
     */

    @Value("${CLINIC_BASE_URL}")
    private String CLINIC_BASE_URL;

    @Value("${PHARMACY_BASE_URL}")
    private String PHARMACY_BASE_URL;

    @RequestMapping(value = "/sign_up", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView showSignUpForm() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("persistClinicCommand", new PersistFacilityCommand());
        modelAndView.addObject("persistPatientCommand", new PatientDto());
        modelAndView.setViewName("sign_up");
        return modelAndView;
    }

    @RequestMapping(value = "/ecosystem-adapation", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView ecosystemAdapation() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("echosystem-adapation");
        return modelAndView;
    }

    @RequestMapping(value = "/provider_sign_up", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public  ResponseEntity<String>  createDatabaseAndSeedFromJson(@RequestBody @Valid PersistFacilityCommand persistFacilityCommand, BindingResult bindingResult) {
        Map result = new HashMap();
        Gson gson;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();
        try {
            commandGateway.sendAndWait(persistFacilityCommand);
            portalTenantRestfulClient.routingTenantDataSource(persistFacilityCommand.getTenantId());
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            result.put("message", e.getMessage());
            result.put("errCode","501");
            System.out.println(e.toString());
            e.printStackTrace();
            return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);

        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        result.put("message","success");
        result.put("errCode", "200");
        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);
    }

    @RequestMapping(value = "/patient_sign_up", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> patientSignUp(@RequestBody @Valid PatientDto patientDto, BindingResult bindingResult) {
        Map result = new HashMap();
        Gson gson;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();

        RegisterPatientCommand registerPatientCmd = new RegisterPatientCommand(patientDto, false);  // do not skip registration notification (Kannan - 2015-11-01)
        try {
            commandGateway.sendAndWait(registerPatientCmd);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            result.put("message",e.getMessage());
            result.put("errCode","501");
            return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);

        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        result.put("message","success");
        result.put("errCode","200");
        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);

    }

    /**
     * Added by pradyumna. This can be accessed by anonymous user
     * Modified by Mohan Sharma to redirect to alert page
     *
     * @return
     */
    @RequestMapping(value = "/confirmation", method = RequestMethod.GET)
    public String confirmation(final @RequestParam("email") String emailAddress, final @RequestParam("confirmation_code") String token, @RequestParam("new_account") boolean newAccount) {
        if (newAccount) {
            Map metadata = new HashMap();
            metadata.put("confirmationCode", token);
            commandGateway.sendAndWait(new GenericCommandMessage("ACCOUNT_CONFIRMATION", emailAddress, metadata));
            return "redirect:/user_verification.html";
        }
        return null;
    }

    @RequestMapping(value = "/api_login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> api_login(final @RequestParam("username") String userName, final @RequestParam("password") String password, HttpServletRequest request, HttpServletResponse response) {
        Map metadata = new HashMap();
        Map result = new HashMap();
        metadata.put("password", password);
        UserLogin userLogin;
        String resultString;
        Gson gson;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();
        try {
            userLogin = commandGateway.sendAndWait(new GenericCommandMessage("API_LOGIN_COMMAND", userName, metadata));
        } catch(Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            result.put("message","error");
            return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);
        }
        String token = UUID.randomUUID().toString();
        String cookie_value=token+":1:"+userName;
        byte[] byte_array =  Base64.encode(cookie_value.getBytes());
        String s = new String(byte_array);
        Cookie token_cookie = new Cookie("token", s);
        token_cookie.setMaxAge(60*60*24);
        Cookie username = new Cookie("username", request.getParameter("username"));
        token_cookie.setMaxAge(60*60*24);
        // Add both the cookies in the response header.
        response.addCookie(token_cookie);
        response.addCookie(username);
        SessionManager.getInstance().addSession(token,userName);
        SessionManager.getInstance().addSession(s,userName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //headers.add("Location", request.getHeader(HttpHeaders.REFERER));
        /*headers.add("Set-Cookie", "remember_me_cookie=" + s);*/
        // userLogin.setPassword("");   -- kannan temporary to keep the consistant with login via popup
        // update last logged-in timestamp in users table
        commandGateway.sendAndWait(new GenericCommandMessage("UPDATE_LOGGED_IN_TIMESTAMP", userName, null));
        result.put("message","success");
        result.put("user",userLogin);
        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);
    }

    @RequestMapping(value = "/api_logout", method = RequestMethod.GET)
    public  ResponseEntity<String>  api_logout() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.add("Set-Cookie","remember_me_cookie=abc;expires=Thu, 01 Jan 1970 00:00:00 GMT");
        return new ResponseEntity<String>("{message : 'success'}",headers,HttpStatus.OK);
    }

    @RequestMapping(value = "/otp_confirmation", method = RequestMethod.GET)
    public  ResponseEntity<String>  confirmation_otp(final @RequestParam("emailId") String emailAddress, final @RequestParam("otp_token") String otp_token) {
        Map metadata = new HashMap();
        metadata.put("otp_token", otp_token);
        try {
            String result = commandGateway.sendAndWait(new GenericCommandMessage("OTP_CONFIRMATION", emailAddress, metadata));
        } catch(Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity<String>(e.getMessage(),headers,HttpStatus.OK);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>("success",headers,HttpStatus.OK);
    }

    @RequestMapping(value = "/regenerate_otp", method = RequestMethod.GET)
    public  ResponseEntity<String>  regenrate_otp(final @RequestParam("username") String username) {
        String result;
        try {
            result = commandGateway.sendAndWait(new GenericCommandMessage("REGENERATE_OTP", username, null));
        } catch(Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity<String>("error",headers,HttpStatus.OK);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>(result,headers,HttpStatus.OK);
    }

    @RequestMapping(value = "/redirect", method = RequestMethod.GET)

    public ResponseEntity<String> redirect(@CookieValue("token") String token,final @RequestParam(value="url", required=false) String url) {
        System.out.println("Got the session token" + token);
        String redirect_url= url;
        if (redirect_url == null) {
            redirect_url = "http://5.9.249.196:7577/ospedale/practice/practiceView.zul";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", redirect_url + "?token=" + token);
        return new ResponseEntity<String>(null,headers,HttpStatus.FOUND);
    }

    @RequestMapping(value = "/redirect_direct", method = RequestMethod.GET)
    public ResponseEntity<String> redirect_direct(@CookieValue("token") String token, @CookieValue("username") String username, HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders headers;
        String role = StringUtils.EMPTY;
        String url = request.getHeader(HttpHeaders.REFERER);
        String subString = "/afya-portal";
        String after = url.substring(url.indexOf(subString) + subString.length());
        Preconditions.checkNotNull(CLINIC_BASE_URL, "CLINIC_BASE_URL cannot be null");
        System.out.println("Got the session token" + token);
        String userName = SessionManager.getInstance().getSession(token);
        if (UtilValidator.isEmpty(userName)) {
            return new ResponseEntity<String>("Invalid User",null,HttpStatus.FOUND);
        }
        UserLogin userLogin;
        String redirect_url= StringUtils.EMPTY;
        try {
            userLogin = commandGateway.sendAndWait(new GenericCommandMessage("GET_USER_TYPE_COMMAND", userName,null));
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getMessage()+"->" + userName +" for token "+ token,null,HttpStatus.FOUND);
        }

        String userType = userLogin.getAuthorities().iterator().next();
        if (UtilValidator.isEmpty(userType)) {
            return new ResponseEntity<String>("no user login object for " + userName +" for token "+ token,null,HttpStatus.FOUND);
        }
        if(tenantSubscribed(username) || userType.equals("ROLE_FACILITY_ADMIN")) {
            if ((userLogin.getProviderType() != null) &&
                    (userLogin.getProviderType().ordinal() == ProviderType.PHARMACY.ordinal())) {
                redirect_url = PHARMACY_BASE_URL + "ordermgr/control/main";
                role = "PHARMACY_ROLE";
            } else {
                String default_role = getDefaultRoleOfUserIfAny(userLogin.getUsername());
                if(UtilValidator.isNotEmpty(default_role)){
                    redirect_url = getURLForDefaultRole(default_role);
                    role = default_role;
                } else {
                    redirect_url = getURLForDefaultRole(userType);
                    role = userType;
                }
                if (userType.equals("ROLE_PATIENT")) {
                    redirect_url = CLINIC_BASE_URL + "ospedale/patient/patientDashboard.zul";
                    role = "ROLE_PATIENT";
                }
            }
            headers = new HttpHeaders();
            try {
                if (token != null) {
                    redirect_url = redirect_url + "?token=" + URLEncoder.encode(token, "UTF-8");
                }
                if (userLogin.getTenantId() != null) {
                    redirect_url = redirect_url + "&j_tenantId=" + URLEncoder.encode(userLogin.getTenantId(), "UTF-8");
                }
                headers.add("Location", redirect_url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Cookie roleCookie = new Cookie("role", role);
            response.addCookie(roleCookie);
            return new ResponseEntity<String>(null,headers,HttpStatus.FOUND);
        } else {
            headers = new HttpHeaders();
            headers.add("Location", "/afya-portal/no-subscription-index.html");
            return new ResponseEntity<String>(null,headers,HttpStatus.FOUND);
        }
    }

    private String getURLForDefaultRole(String default_role) {
        String defaultURLTORedirect = StringUtils.EMPTY;
        if (default_role.equals("ROLE_DOCTOR")) {
            defaultURLTORedirect = CLINIC_BASE_URL + "ospedale/dashboards/providerDashboard.zul";
        } else if (default_role.equals("ROLE_NURSE")) {
            defaultURLTORedirect = CLINIC_BASE_URL + "ospedale/dashboards/nurseDashboard.zul";
        } else if (default_role.equals("ROLE_RECEPTION")) {
            defaultURLTORedirect = CLINIC_BASE_URL + "ospedale/dashboards/frontDeskDashBoard.zul";
        } else if (default_role.equals("ROLE_BILLING")) {
            defaultURLTORedirect = CLINIC_BASE_URL + "ospedale//billing/billingDashboard.zul";
        } else if (default_role.equals("ROLE_ADMIN")) {
            defaultURLTORedirect = CLINIC_BASE_URL + "ospedale/practice/practiceView.zul";
        } else if (default_role.equals("ROLE_FACILITY_ADMIN")) {
            defaultURLTORedirect = CLINIC_BASE_URL + "ospedale/practice/practiceView.zul";
        }
        return defaultURLTORedirect;
    }

    private String getDefaultRoleOfUserIfAny(String username) {
        return portalUtilityFinder.getDefaultRoleOfUserIfAny(username);
    }

   @RequestMapping(value = "/anon/redirect_clinic", method = RequestMethod.GET)
   public ResponseEntity<String> redirect_clinic(@CookieValue("username") String userName) {
       HttpHeaders headers = null;
       if(CLINIC_BASE_URL == null)
           CLINIC_BASE_URL = "http://localhost:7574/";
       // System.out.println("Got the session token" + token);
       //String userName = SessionManager.getInstance().getSession(token);
       if (userName == null) {
           return new ResponseEntity<String>("Invalid User",null,HttpStatus.FOUND);
       }
       UserLogin userLogin = null;
       String redirect_url= "http://localhost:7574/ospedale/practice/practiceView.zul";
       try {
           userLogin = commandGateway.sendAndWait(new GenericCommandMessage("GET_USER_TYPE_COMMAND", userName,null));
       } catch(Exception e) {
           e.printStackTrace();
           return new ResponseEntity<String>(e.getMessage()+"->" + userName ,null,HttpStatus.FOUND);
       }

       String userType = userLogin.getAuthorities().iterator().next();
       if (userType == null) {
           return new ResponseEntity<String>("no user login object for " + userName ,null,HttpStatus.FOUND);
       }
       if(tenantSubscribed(userName)) {
           if ((userLogin.getProviderType() != null) &&
                   (userLogin.getProviderType().ordinal() == ProviderType.PHARMACY.ordinal())) {
               redirect_url = PHARMACY_BASE_URL + "ordermgr/control/main";
           } else {
               if(UtilValidator.isNotEmpty(userType)){
                   redirect_url = getURLForDefaultRole(userType);
               } else {
                   return new ResponseEntity<String>("No rule to redirect", null, HttpStatus.FOUND);
               }
           }
           headers = new HttpHeaders();
           try {
               /* if (token != null) {
                    redirect_url = redirect_url + "?token=" + URLEncoder.encode(token, "UTF-8");
                }*/
               if (userLogin.getTenantId() != null) {
                   redirect_url = redirect_url + "?j_tenantId=" + URLEncoder.encode(userLogin.getTenantId(), "UTF-8");
               }
               headers.add("Location", redirect_url);
           } catch (Exception e) {
               e.printStackTrace();
           }
           return new ResponseEntity<String>(null,headers,HttpStatus.FOUND);
       } else {
           headers = new HttpHeaders();
           headers.add("Location", "/afya-portal/no-subscription-index.html");
           return new ResponseEntity<String>(null,headers,HttpStatus.FOUND);
       }
   }

    private static CloseableHttpClient getCloseableHttpClient() {
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] certificate, String authType) {
                return true;
            }
        };
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();

    }


    private boolean tenantSubscribed(String username) {
        Boolean result = Boolean.FALSE;
        String tenantIdOfGivenUser = userLoginFinder.getTenantIdFromTenantAssoc(username);
        if(UtilValidator.isNotEmpty(tenantIdOfGivenUser)){
            List<Map<String, Object>> users = userLoginFinder.getAllUsersForGivenTenant(tenantIdOfGivenUser);
            Set<String> usernames = getSetOfUsers(users);
            result = userLoginFinder.checkIfAnyUserOfTheTenantSubscribed(usernames);
        }
        return result;
    }

    private Set<String> getSetOfUsers(List<Map<String, Object>> users) {
        Set<String> usernames = Sets.newHashSet();
        for(Map<String, Object> map  : users){
            usernames.add((String)map.get("username"));
        }
        return usernames;
    }


    /**
     * Added by pradyumna. This can be accessed by anonymous user
     *
     * @return
     */
    @RequestMapping(value = "/send-password-req", method = RequestMethod.GET)
    public String sendPasswordRequest(final @RequestBody String emailAddress) {
        return null;
    }

    @RequestMapping(value = "/loginViaPopUp", method = RequestMethod.POST)
    public ResponseEntity<String> loginViaPopUp(final @RequestParam String username, @RequestParam String password, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new GsonBuilder().serializeNulls().create();
        if(UtilValidator.isEmpty(username) || UtilValidator.isEmpty(password)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "user login details missing");
            return new ResponseEntity<String>(gson.toJson("user login details missing"),null,HttpStatus.BAD_REQUEST);
        }
        UserLogin userLogin = commandGateway.sendAndWait(new GenericCommandMessage("GET_USER_TYPE_COMMAND", username,null));
        if(UtilValidator.isEmpty(userLogin)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Its an Invalid User Name");
            return new ResponseEntity<String>(gson.toJson("No user found with given details"),null,HttpStatus.BAD_REQUEST);
        }
        if(!userLogin.getPassword().equals(password)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Its an Invalid Password, please enter the right password.");
            return new ResponseEntity<String>(gson.toJson("Its an Invalid Password, please enter the right password."),null,HttpStatus.BAD_REQUEST);
        }
        if(!userLogin.isVerified()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User is not verified");
            return new ResponseEntity<String>(gson.toJson("User is not verified"),null,HttpStatus.BAD_REQUEST);
        }
        String userType = userLogin.getAuthorities().iterator().next();
        if (UtilValidator.isEmpty(userType)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "user has no role");
            return new ResponseEntity<String>(gson.toJson("no role associated with users" + userLogin.getUsername()),null,HttpStatus.FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String role = StringUtils.EMPTY;
        String token = UUID.randomUUID().toString();
        String cookie_value=token+":1:"+userLogin.getUsername();
        byte[] byte_array =  Base64.encode(cookie_value.getBytes());
        String s = new String(byte_array);
        Cookie token_cookie = new Cookie("token", s);
        //token_cookie.setMaxAge(60*60*24);
        token_cookie.setMaxAge(60*30);
        Cookie user = new Cookie("username", userLogin.getUsername());
        //token_cookie.setMaxAge(60*60*24);
        user.setMaxAge(60*30);
        response.addCookie(token_cookie);
        response.addCookie(user);
        SessionManager.getInstance().addSession(token, userLogin.getUsername());
        SessionManager.getInstance().addSession(s, userLogin.getUsername());
        if(tenantSubscribed(userLogin.getUsername()) || userType.equals("ROLE_FACILITY_ADMIN") || userType.equals("ROLE_PATIENT")) {
            if ((userLogin.getProviderType() != null) && (userLogin.getProviderType().ordinal() == ProviderType.PHARMACY.ordinal())) {
                role = "PHARMACY_ROLE";
            } else if (userType.equals("ROLE_ADMIN")) {
                role = "ROLE_ADMIN";
            } else if(userType.equals("ROLE_FACILITY_ADMIN")){
                role = "ROLE_FACILITY_ADMIN";
            } else if (userType.equals("ROLE_PATIENT")) {
                role = "ROLE_PATIENT";
            } else {
                role = userType;
            }
            Cookie roleCookie = new Cookie("role", role);
            roleCookie.setMaxAge(60 * 30);
            response.addCookie(roleCookie);
            // update last logged-in timestamp in users table
            commandGateway.sendAndWait(new GenericCommandMessage("UPDATE_LOGGED_IN_TIMESTAMP", username, null));
            // return login response
            return new ResponseEntity<String>(gson.toJson("successful"),headers,HttpStatus.OK);
        }
        else {
            headers.add("Location", "/afya-portal/no-subscription-index.html");
            return new ResponseEntity<String>(gson.toJson("no subscription"),headers,HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/anon/care_payer_sign_up", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.ALL_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<String> carePayerSignUp(@RequestBody @Valid UserLoginDto userLoginDto, BindingResult bindingResult) {
        Map result = new HashMap();
        Gson gson;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().serializeNulls().create();

        RegisterCarePayerCommand registerCarePayerCmd = new RegisterCarePayerCommand(userLoginDto);
        try {
            commandGateway.sendAndWait(registerCarePayerCmd);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            result.put("message",e.getMessage());
            result.put("errCode","501");
            return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);

        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        result.put("message","success");
        result.put("errCode","200");
        return new ResponseEntity<String>(gson.toJson(result),headers,HttpStatus.OK);

    }

    @RequestMapping(value = "/anon/resend_email", method = RequestMethod.GET)
    public  ResponseEntity<String>  resend_email(final @RequestParam("username") String username) {
        String result;
        try {
            result = commandGateway.sendAndWait(new GenericCommandMessage("RESEND_EMAIL", username, null));
        } catch(Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity<String>("error",headers,HttpStatus.OK);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<String>(result, headers,HttpStatus.OK);
    }
}
