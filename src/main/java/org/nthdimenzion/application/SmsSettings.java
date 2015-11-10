package org.nthdimenzion.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by pradyumna on 10-06-2015.
 */
@Component
@ConfigurationProperties(prefix = "spring.sms", ignoreUnknownFields = true)
@Getter
@Setter
public class SmsSettings {
    private String smsServerUrl;
    private String smsSender;
    private String smsUid;
    private String smsPassword;

    public SmsSettings(){

    }
}


