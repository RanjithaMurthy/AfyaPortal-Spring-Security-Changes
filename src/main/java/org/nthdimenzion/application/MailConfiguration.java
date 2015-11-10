package org.nthdimenzion.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Created by pradyumna on 10-06-2015.
 */
@Configuration
@EnableConfigurationProperties(MailSettings.class)
public class MailConfiguration {

    @Autowired
    private MailSettings mailSettings=new MailSettings();

    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", mailSettings.isAuth());
        mailProperties.put("mail.smtp.starttls.enable", mailSettings.isStarttlsEnable());
        mailProperties.put("mail.smtp.socketFactory.class", mailSettings.getSocketFactoryClass());
        mailSender.setJavaMailProperties(mailProperties);
        mailSender.setHost(mailSettings.getHost());
        mailSender.setPort(mailSettings.getPort());
        mailSender.setUsername(mailSettings.getUsername());
        mailSender.setPassword(mailSettings.getPassword());
        mailSender.setJavaMailProperties(mailProperties);
        return mailSender;
    }


}
