package com.afya.portal.util;

import com.afya.portal.controller.PortalTenantInteractiveController;
import freemarker.template.Configuration;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ranjitha on 9/23/2015.
 */
@Component
public class SmsUtil {

    private Configuration freemarkerConfiguration;
    private static final String autoRegisteredPatientPasswordTemplate = "sms/autoRegisteredPatientPassword.ftl";

    static String SMS_SERVER_URL = null;
    static String SMS_SENDER = null;
    static String SMS_UID = null;
    static String SMS_PASSWORD = null;
    static {
        Properties properties = new Properties();
        try {
            String profileName = System.getProperty("profile.name") != null ? System.getProperty("profile.name") : "dev";
            properties.load(SmsUtil.class.getClassLoader().getResourceAsStream("application-"+profileName+".properties"));
            SMS_SERVER_URL = (String)properties.get("SMS_SERVER_URL");
            SMS_SENDER = (String)properties.get("SMS_SENDER");
            SMS_UID = (String)properties.get("SMS_UID");
            SMS_PASSWORD = (String)properties.get("SMS_PASSWORD");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    public SmsUtil(Configuration freemarkerConfiguration){          // kannan (2015-11-01)
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public static void sendOTPSMS(Map<String, Object> detail) {
        sendSMS(detail);
    }

    public void sendAutoRegisteredPatientPassword(Map<String, Object> detail) {            // kannan (2015-11-01)
        sendSMSWithFTLTemplate(autoRegisteredPatientPasswordTemplate, detail);
    }

    public void sendSMSWithFTLTemplate(String templateName, Map<String, Object> detail){            // kannan (2015-11-01)
        ResponseEntity<String> responseEntity;
        try {
            String message = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(templateName, "UTF-8"), detail);
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
            String phoneNumber = constructPhoneNumber(detail);
            //String phoneNumber = null;
            if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                return;

            responseEntity = restTemplate.exchange(SMS_SERVER_URL, HttpMethod.POST, requestEntity, String.class, SMS_UID, SMS_PASSWORD, SMS_SENDER, "L", phoneNumber, message);

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static void sendSMS(Map<String, Object> detail){
        ResponseEntity<String> responseEntity = null;
        try {
            String message= null;
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
            String phoneNumber = constructPhoneNumber(detail);
            //String phoneNumber = null;
            if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                return;

            message = constructOTPMessage(detail);


            responseEntity = restTemplate.exchange(SMS_SERVER_URL, HttpMethod.POST, requestEntity, String.class, SMS_UID, SMS_PASSWORD, SMS_SENDER, "L", phoneNumber, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HttpComponentsClientHttpRequestFactory getHttpComponentsClientHttpRequestFactory(){
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClient httpclient= getCloseableHttpClient();
        factory.setHttpClient(httpclient);
        return factory;
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

    private static HttpHeaders getHttpHeader(){
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static String constructOTPMessage(Map<String, Object> detail) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello " + constructName(detail) +",\n");
        builder.append("Thanks for registering with Afyaarabia. OTP for completing your registration process is " + detail.get("token") + ".\nThanks\nCommunity Care\nAfyaarabia");
        return builder.toString();
    }

    private static String constructPhoneNumber(Map<String, Object> detail) {
        String  isdCode = "965"; // To Do: This should be sent in detail
        return detail.get("mobileNumber") != null ? (isdCode + detail.get("mobileNumber").toString()) : null;
        //return detail.get("isdCode") != null ? detail.get("mobileNumber") != null ? detail.get("isdCode").toString() + detail.get("mobileNumber").toString() : null : null;
    }

    public static String constructName(Map<String, Object> object){
        StringBuilder stringBuilder = new StringBuilder();
        String name=null;

        if(object.get("firstName") != null)
            stringBuilder.append(object.get("firstName") + " ");
        if(object.get("lastName") != null)
            stringBuilder.append(object.get("lastName"));
        name = stringBuilder.toString();

        return name;
    }
}
