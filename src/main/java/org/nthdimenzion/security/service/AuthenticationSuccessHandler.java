/*
 * Copyright (c) 1/23/15 5:06 PM.Nth Dimenzion, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package org.nthdimenzion.security.service;


import com.afya.portal.query.PortalUtilityFinder;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.nthdimenzion.common.AppConstants;
import org.nthdimenzion.presentation.AppUtils;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author: Samir
 * @since 1.0 23/01/2015
 */
@Component
public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private PortalUtilityFinder portalUtilityFinder;
    @Autowired
    private CommandGateway commandGateway;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        request.getSession().removeAttribute(AppUtils.LOGGED_IN_USER);
        request.getSession().invalidate();
        super.handle(request, response, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String userName= userDetails.getUsername();

        Boolean isVerified= portalUtilityFinder.isUserVerified(userName);
        //UserLogin userLogin = commandGateway.sendAndWait(new GenericCommandMessage("GET_USER_TYPE_COMMAND", userName,null));

        if(!isVerified){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User is not verified");
            Cookie errormessage= new Cookie("message", "User is not verified");
            response.addCookie(errormessage);
        }

        String landingPage = "";
        String role = StringUtils.EMPTY;
        String token = UUID.randomUUID().toString();
        String cookie_value=token+":1:"+auth.getName();
        byte[] byte_array =  Base64.encode(cookie_value.getBytes());
        String s = new String(byte_array);
        Cookie token_cookie = new Cookie("token", s);
        //token_cookie.setMaxAge(60*60*24);
        token_cookie.setMaxAge(60*30);
        Cookie username = new Cookie("username", request.getParameter("username"));
        //token_cookie.setMaxAge(60*60*24);
        username.setMaxAge(60*30);
        // Add both the cookies in the response header.
        response.addCookie( token_cookie );
        response.addCookie(username);
        SessionManager.getInstance().addSession(token, auth.getName());
        SessionManager.getInstance().addSession(s,auth.getName());

        String url = request.getHeader(HttpHeaders.REFERER);
        String subString = "/afya-portal";
        String after = url.substring(url.indexOf(subString) + subString.length());
        for (GrantedAuthority authority : auth.getAuthorities()) {
            switch (authority.getAuthority()) {
                case "ROLE_PATIENT":
                    if(after.length() > 11){
                        landingPage = after;
                    }else{
                        landingPage = "/#PatientDashboard";
                    }
                    role = "ROLE_PATIENT";
                    break;
                case "ROLE_PORTAL_ADMIN":
                    if(after.length() > 11){
                        landingPage = after;
                    }else{
                        landingPage = "/index.html";
                    }
                    role = "ROLE_PORTAL_ADMIN";
                    break;
                case "ROLE_FACILITY_ADMIN":
                    if(after.length() > 11){
                        landingPage = after;
                    }else{
                        //landingPage = "/web_pages/member_area/provider/Provider_account.html";
                        landingPage = "/#ProviderDashboard";
                    }
                    role = "ROLE_FACILITY_ADMIN";
                    break;
            }
        }

        if (landingPage.equals("")) {
            landingPage = "/redirect_direct";
        }

        Cookie roleCookie = new Cookie("role", role);
        roleCookie.setMaxAge(60*30);
        response.addCookie(roleCookie);
        request.getSession().setAttribute(AppUtils.LOGGED_IN_USER,userDetails);
        super.setAlwaysUseDefaultTargetUrl(true);
        super.setDefaultTargetUrl(landingPage);
        //System.out.println("\n\n\n ********* REDIRECT URL ************* "+super.getDefaultTargetUrl());
        super.onAuthenticationSuccess(request, response, auth);
    }

}
