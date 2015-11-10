package com.afya.portal.service;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.nthdimenzion.application.MailSettings;
import org.nthdimenzion.application.SmsSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by ranjitha on 9/23/2015.
 */
@Service
@EnableConfigurationProperties(SmsSettings.class)
public class SmsService {

    @Autowired
    protected MessageSource messageSource;
    Locale locale = null;

    @Autowired
    private SmsSettings smsSettings = new SmsSettings();

    public void sendOTPSMS(Map<String, Object> detail) {
        sendSMS(detail);
    }

    private void sendSMS(Map<String, Object> detail){
        ResponseEntity<String> responseEntity = null;
        String message= null;
        try {
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            HttpHeaders headers = getHttpHeader();
            HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
            String phoneNumber = constructPhoneNumber(detail);
            //String phoneNumber = null;
            if(phoneNumber == null || !phoneNumber.matches("\\d+"))
                return;

            //message = constructOTPMessage(detail);
            //The above methos was commented out and replaced by the below method to make use of
            // ResourceBundle created by Raghu
            message = constructMessage(detail);
            responseEntity = restTemplate.exchange(smsSettings.getSmsServerUrl(), HttpMethod.POST, requestEntity, String.class, smsSettings.getSmsUid(), smsSettings.getSmsPassword(), smsSettings.getSmsSender(), "L", phoneNumber, message);
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
    //This message meakes use of ResourceBundle created by Raghu
    private String constructMessage(Map<String, Object> detail) {

        locale = LocaleContextHolder.getLocale();

        StringBuilder builder = new StringBuilder();
        String message = null;

        if ( detail.get("trial") != null && ((Boolean)detail.get("trial")).equals(Boolean.TRUE)) {
            message = messageSource.getMessage((String) detail.get("key"), null, locale);
            message = MessageFormat.format(message, (String) detail.get("expiryDate"));
            message = message.substring(1,message.length()-1);
                    //message = messageSource.getMessage((String) detail.get("key"), new Object[]{(String) detail.get("expiryDate")}, locale);
        } else {
            message = messageSource.getMessage((String)detail.get("key"), null, locale);
            message = message.substring(1,message.length()-1);
        }

        builder.append(message);
    //    builder.append("Thanks for registering with Afyaarabia. OTP for completing your registration process is " + detail.get("token") + ".\nThanks\nCommunity Care\nAfyaarabia");
        return builder.toString();
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
