package org.nthdimenzion.security.configuration;

import org.nthdimenzion.utils.UtilValidator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by Asus on 7/20/2015.
 */
public class CustomTokenBasedRememberMeService extends TokenBasedRememberMeServices {
    static String LOGOUT_SERVER_URL = "/logout";

    public CustomTokenBasedRememberMeService() {
        super();
    }

    public CustomTokenBasedRememberMeService(String key, UserDetailsService userDetailsService) {
        super(key,userDetailsService);
    }

    private final String HEADER_SECURITY_TOKEN = "remember_me_cookie";

    @Override
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password) {
        return username;
    }

    protected boolean isTokenExpired(long tokenExpiryTime) {
        return false;
    }

    String getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();


        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        String[] paramValues = request.getParameterValues("remember_me_cookie");
        if (paramValues != null) {
            return paramValues[0];
        }

        String token = (String) request.getSession().getAttribute("remember_me_cookie");
        return token;
    }

    @Override
    protected String extractRememberMeCookie(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        String token = getCookie(request, "remember_me_cookie");

        if ((token == null) || (token.length() == 0)) {
            return null;
        }
        request.getSession().setAttribute("remember_me_cookie", token);
        return token;
    }

    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        super.onLoginSuccess(request, response, successfulAuthentication);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication)  {
        if (UtilValidator.isNotEmpty(LOGOUT_SERVER_URL)) {
            try {
                response.sendRedirect(LOGOUT_SERVER_URL);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                response.sendRedirect(request.getContextPath() + "/login");
            }  catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
