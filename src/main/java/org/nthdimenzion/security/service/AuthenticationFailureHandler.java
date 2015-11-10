/*
 * Copyright (c) 1/23/15 5:06 PM.Nth Dimenzion, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package org.nthdimenzion.security.service;


import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.util.UtilValidator;
import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.nthdimenzion.presentation.AppUtils;
import org.nthdimenzion.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
/**
 * @author: Shreedhar
 * @since 1.0 04/11/2015
 */
@Component
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private CommandGateway commandGateway;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {


            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username and password.");
        Cookie errormessage= new Cookie("message", "Invalid username and password.");
        response.addCookie(errormessage);

        super.onAuthenticationFailure(request, response, exception);
    }

}
