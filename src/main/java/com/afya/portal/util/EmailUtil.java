package com.afya.portal.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Mohan Sharma on 5/19/2015.
 */
public class EmailUtil {

    public static Session authenticateAndGetSession(final Properties prop){
        final Properties properties = new Properties();
        properties.put("mail.smtp.host", prop.getProperty("mail.smtp.relay.host").trim());
        properties.put("mail.smtp.port", prop.getProperty("mail.smtp.port").trim());
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", prop.getProperty("mail.smtp.relay.host"));
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(prop.getProperty("mail.smtp.auth.user").trim(), prop.getProperty("mail.smtp.auth.password").trim());
            }
        };
        return Session.getInstance(properties, auth);
    }

    public static String sendMail(Session session, Properties properties, String emailId, String cc, String bcc, String bodyOfEmail, String subjectOfEmail) throws MessagingException {
        String stacktrace;
        Message msg=new MimeMessage(session);
        msg.setFrom(new InternetAddress(properties.getProperty("mail.smtp.auth.user")));
        if(!UtilValidator.validateEmail(emailId))
            return StringUtils.EMPTY;
        InternetAddress[] toAddresses = { new InternetAddress(emailId) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        if(bcc != null) {
            InternetAddress[] bccs = {new InternetAddress(bcc)};
            msg.setRecipients(Message.RecipientType.BCC, bccs);
        }
        if(cc != null) {
            InternetAddress[] ccs = {new InternetAddress(cc)};
            msg.setRecipients(Message.RecipientType.CC, ccs);
        }
        msg.setSubject(subjectOfEmail);
        msg.setSentDate(new Date());
        MimeBodyPart messageBodyPartForBody= new MimeBodyPart();
        messageBodyPartForBody.setContent(bodyOfEmail, "text/html");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPartForBody);
        msg.setContent(multipart);
        Transport.send(msg);
        stacktrace = "email sent";
        return stacktrace;
    }

    public static boolean emailWhenTriggeredContactUs(Map<String, Object> details) throws IOException, MessagingException {
        String response = StringUtils.EMPTY;
        Properties properties = new Properties();
        properties.load(EmailUtil.class.getClassLoader().getResourceAsStream("mailContent.properties"));
        Session session = authenticateAndGetSession(properties);
        String bodyOfEmail = EmailContentConstructor.setBodyForContactUsMail(details);
        response = sendMail(session, properties, "info@afyaarabia.com", null, null, bodyOfEmail, EmailContentConstructor.setSubjectForContactUsMail(details));
        if(response.equals("email sent"))
            return true;
        else
            return false;
    }

    public static String convertStackTraceToString(Throwable e){
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
