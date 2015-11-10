package org.nthdimenzion.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by pradyumna on 10-06-2015.
 */
@Component
@ConfigurationProperties(prefix = "spring.mail", ignoreUnknownFields = true)
@Getter
@Setter
public class MailSettings {
    private boolean debug;
    private String host;
    private int port;
    private boolean auth;
    private String socketFactoryClass;
    private String sentFrom;
    private String username;
    private String password;
    private boolean starttlsEnable;
    private String confirmationUrl;
    private String confirmationSubject;
    private String resetUrl;
    private String resetSubject;
    private String packageQuotationSubject;
    private String trialSubscriptionSubject;
    private String subscriptionSubject;
    private String providerConfirmationSubject;
    private String patientConfirmationSubject;
    private String autoRegisteredPatientPasswordSubject;
    private String payerConfirmationSubject;
    private String patientRegistrationSubject;

    public MailSettings(){
        //System.out.println(" Mail Settings created");
    }
}
