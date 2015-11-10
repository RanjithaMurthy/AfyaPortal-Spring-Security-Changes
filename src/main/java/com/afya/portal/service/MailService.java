package com.afya.portal.service;

import com.google.common.base.Preconditions;
import freemarker.template.Configuration;
import org.nthdimenzion.application.MailSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import javax.servlet.http.HttpServletRequest;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Locale;
import java.util.Map;

/**
 * Created by pradyumna on 10-06-2015.
 */
@Service
@EnableConfigurationProperties(MailSettings.class)
public class MailService {

    private JavaMailSender javaMailSender;
    private Configuration freemarkerConfiguration;
    private static final String template = "mail/confirmation.ftl";

    private static final String resetPasswordTemplate = "mail/resetPassword.ftl";
    private static final String packageQuotationTemplate = "mail/packageQuotation.ftl";

    Locale locale = null;

    String pdfFilePath = null;

    boolean attachementRequired = true;
    boolean verified = false;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    DocumentManupulationService documentManupulationService;

    @Autowired
    private MailSettings mailSettings = new MailSettings();

    @Autowired
    public MailService(JavaMailSender mailSender, Configuration freemarkerConfiguration) {
        this.javaMailSender = mailSender;
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    /**
     * Checks if the valid parameters required in the freemarker
     * are set into the model.
     *
     * @param mailModel
     */

    /*public void sendMailPostFacilityRegistration(final Map<String, Object> mailModel) {
        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        Preconditions.checkArgument(mailModel.get("confirmationCode") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);

        StringBuilder confirmationUrl = new StringBuilder(mailSettings.getConfirmationUrl());
        confirmationUrl.append("?").append("email=").append(mailModel.get("sendTo")).append("&confirmation_code=").append(mailModel.get("confirmationCode"))
                .append("&new_account=1");
        mailModel.put("confirmationUrl", confirmationUrl);
        mailModel.put("subject", mailSettings.getConfirmationSubject());

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setFrom(mailSettings.getSentFrom());
                message.setTo((String) mailModel.get("sendTo"));
                message.setSubject((String) mailModel.get("subject"));
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(template, "UTF-8"), mailModel);
                message.setText(text, true);
            }
        };
        javaMailSender.send(preparator);
    }*/

    public void sendMailPostPatientRegistration(final Map<String, Object> mailModel) {
        //locale  = Locale.getDefault();
        locale = LocaleContextHolder.getLocale();
        //locale = new Locale("ar");
        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        //Preconditions.checkArgument(mailModel.get("confirmationCode") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);

        if (mailModel.get("verified") !=null && ((Boolean)mailModel.get("verified")) == true) {

            verified = true;
        } else {
            verified = false;
        }

        StringBuilder confirmationUrl = new StringBuilder(mailSettings.getConfirmationUrl());
        confirmationUrl.append("?").append("email=").append(mailModel.get("sendTo")).append("&confirmation_code=").append(mailModel.get("confirmationCode"))
                .append("&new_account=1");
        mailModel.put("confirmationUrl", confirmationUrl);

        MimeMessagePreparator preparator = null;
/*        if(mailModel.get("facility") != null) {
            mailModel.put("subject", mailSettings.getProviderConfirmationSubject());
            preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                    message.setFrom(mailSettings.getSentFrom());
                    message.setTo((String) mailModel.get("sendTo"));
                    message.setSubject((String) mailModel.get("subject"));
                    String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.PROVIDER_REGISTRATION.name(), null, locale), "UTF-8"), mailModel);
                    message.setText(text, true);

                }
            };
        } else { */

            if (verified) {
                mailModel.put("subject", mailSettings.getPatientRegistrationSubject());
                pdfFilePath = documentManupulationService.returnPdfFilePath(messageSource.getMessage(TemplateNames.WELCOME_ATTACHMENT_PATIENT.name(),null,locale), mailModel);
                if (pdfFilePath.startsWith("Failed"))
                {
                    System.out.println("Failed to generate PDF file. status: " + pdfFilePath);
                    attachementRequired = false;
                } else {
                    attachementRequired = true;
                }
            }else{
                mailModel.put("subject", mailSettings.getPatientConfirmationSubject());
            }
            preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage,true, "UTF-8");
                    message.setFrom(mailSettings.getSentFrom());
                    message.setTo((String) mailModel.get("sendTo"));
                    message.setSubject((String) mailModel.get("subject"));
                    String text = "";
                    if(verified){
                         text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.PATIENT_REGISTRATION.name(), null, locale), "UTF-8"), mailModel);
                    }
                    else{
                         text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.PATIENT_CONFIRMATION.name(), null, locale), "UTF-8"), mailModel);
                    }

                    message.setText(text, true);
                    if (attachementRequired && verified) {
                        message.addAttachment("Welcome Letter.pdf", new File(pdfFilePath));
                    }
                }
            };
        javaMailSender.send(preparator);

    }

    public void sendMailPostPayerRegistration(final Map<String, Object> mailModel) {
        //locale  = Locale.getDefault();
        locale = LocaleContextHolder.getLocale();
        //locale = new Locale("ar");
        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        //Preconditions.checkArgument(mailModel.get("confirmationCode") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);

        StringBuilder confirmationUrl = new StringBuilder(mailSettings.getConfirmationUrl());
        confirmationUrl.append("?").append("email=").append(mailModel.get("sendTo")).append("&confirmation_code=").append(mailModel.get("confirmationCode"))
                .append("&new_account=1");
        mailModel.put("confirmationUrl", confirmationUrl);

        MimeMessagePreparator preparator = null;
        mailModel.put("subject", mailSettings.getPayerConfirmationSubject());
        preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage,true, "UTF-8");
                message.setFrom(mailSettings.getSentFrom());
                message.setTo((String) mailModel.get("sendTo"));
                message.setSubject((String) mailModel.get("subject"));
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.PAYER_CONFIRMATION.name(), null, locale), "UTF-8"), mailModel);
                message.setText(text, true);
            }
        };
        javaMailSender.send(preparator);

    }

    public void sendMailPostFacilityRegistration(final Map<String, Object> mailModel) {
        //locale  = Locale.getDefault();
        locale = LocaleContextHolder.getLocale();
        //locale = new Locale("ar");
        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        //Preconditions.checkArgument(mailModel.get("confirmationCode") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);

        /*if (mailModel.get("verified") !=null && ((Boolean)mailModel.get("verified")) == true) {
            verified = true;
        } else {
            verified = false;
        }*/

        StringBuilder confirmationUrl = new StringBuilder(mailSettings.getConfirmationUrl());
        confirmationUrl.append("?").append("email=").append(mailModel.get("sendTo")).append("&confirmation_code=").append(mailModel.get("confirmationCode"))
                .append("&new_account=1");
        mailModel.put("confirmationUrl", confirmationUrl);
        MimeMessagePreparator preparator = null;

        mailModel.put("subject", mailSettings.getProviderConfirmationSubject());
        preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setFrom(mailSettings.getSentFrom());
                message.setTo((String) mailModel.get("sendTo"));
                message.setSubject((String) mailModel.get("subject"));
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.PROVIDER_REGISTRATION.name(), null, locale), "UTF-8"), mailModel);
                message.setText(text, true);

            }
        };

        javaMailSender.send(preparator);

    }


    public void sendMailResetPWD(final Map<String, Object> mailModel){
        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        /*Preconditions.checkArgument(mailModel.get("confirmationCode") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);*/

        StringBuilder confirmationUrl = new StringBuilder(mailSettings.getResetUrl());
        confirmationUrl.append("?").append("email=").append(mailModel.get("sendTo"))
                .append("&user=").append(mailModel.get("user"));
        mailModel.put("confirmationUrl", confirmationUrl);
        mailModel.put("subject", mailSettings.getResetSubject());

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setFrom(mailSettings.getSentFrom());
                message.setTo((String) mailModel.get("sendTo"));
                message.setSubject((String) mailModel.get("subject"));
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(resetPasswordTemplate, "UTF-8"), mailModel);
                message.setText(text, true);
            }
        };
        javaMailSender.send(preparator);
    }

    public void sendMailPackageQuotation(final Map<String, Object> mailModel) {
        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);

        /*StringBuilder confirmationUrl = new StringBuilder(mailSettings.getConfirmationUrl());
        confirmationUrl.append("?").append("email=").append(mailModel.get("sendTo")).append("&confirmation_code=").append(mailModel.get("confirmationCode"))
                .append("&new_account=1");
        mailModel.put("confirmationUrl", confirmationUrl);*/
        mailModel.put("subject", mailSettings.getPackageQuotationSubject());

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setFrom(mailSettings.getSentFrom());
                message.setTo((String) mailModel.get("sendTo"));
                message.setSubject((String) mailModel.get("subject"));
                String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(packageQuotationTemplate, "UTF-8"), mailModel);
                message.setText(text, true);
            }
        };
        javaMailSender.send(preparator);
    }

    public void sendMailPostSubscription(final Map<String, Object> mailModel) {

        locale = LocaleContextHolder.getLocale();

        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        //Preconditions.checkArgument(mailModel.get("confirmationCode") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);

        StringBuilder confirmationUrl = new StringBuilder(mailSettings.getConfirmationUrl());
        confirmationUrl.append("?").append("email=").append(mailModel.get("sendTo")).append("&confirmation_code=").append(mailModel.get("confirmationCode"))
                .append("&new_account=1");
        mailModel.put("confirmationUrl", confirmationUrl);

        mailModel.put("facility", mailModel.get("facility"));

        if (mailModel.get("trialSubscription") != null) {
            mailModel.put("subject", mailSettings.getTrialSubscriptionSubject());
        } else {
            mailModel.put("subject", mailSettings.getSubscriptionSubject());
        }


        MimeMessagePreparator preparator = null;

        if (mailModel.get("trialSubscription") != null) {
            pdfFilePath = documentManupulationService.returnPdfFilePath(messageSource.getMessage(TemplateNames.WELCOME_ATTACHMENT_PROVIDER_TRIAL.name(),null,locale), mailModel);
            if (pdfFilePath.startsWith("Failed"))
            {
                System.out.println("Failed to generate PDF file. status: " + pdfFilePath);
                attachementRequired = false;
            }

            preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage,true, "UTF-8");
                    message.setFrom(mailSettings.getSentFrom());
                    message.setTo((String) mailModel.get("sendTo"));
                    message.setSubject((String) mailModel.get("subject"));
                    String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.PROVIDER_SUBSCRIPTION_TRIAL.name(), null, locale), "UTF-8"), mailModel);
                    message.setText(text, true);
                    //InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(pdfFilePath);
                    if (attachementRequired) {
                        message.addAttachment("Welcome Letter.pdf", new File(pdfFilePath));
                    }

                }
            };
        } else {

            pdfFilePath = documentManupulationService.returnPdfFilePath(messageSource.getMessage(TemplateNames.WELCOME_ATTACHMENT_PROVIDER_SUBSCRIPTION.name(),null,locale), mailModel);
            if (pdfFilePath.startsWith("Failed"))
            {
                System.out.println("Failed to generate PDF file. status: "+pdfFilePath);
                attachementRequired = false;
            }
            preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage,true, "UTF-8");
                    message.setFrom(mailSettings.getSentFrom());
                    message.setTo((String) mailModel.get("sendTo"));
                    message.setSubject((String) mailModel.get("subject"));
                    String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.PROVIDER_SUBSCRIPTION.name(), null, locale), "UTF-8"), mailModel);
                    message.setText(text, true);
                    if (attachementRequired) {
                        message.addAttachment("Welcome Letter.pdf", new File(pdfFilePath));
                    }
                }
            };

        }
        javaMailSender.send(preparator);
    }

    public void sendAutoRegisteredPatientPassword(final Map<String, Object> mailModel) {

        locale = LocaleContextHolder.getLocale();

        Preconditions.checkArgument(mailModel.get("sendTo") != null);
        Preconditions.checkArgument(mailModel.get("firstName") != null);
        Preconditions.checkArgument(mailModel.get("lastName") != null);

        mailModel.put("subject", mailSettings.getAutoRegisteredPatientPasswordSubject());

        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage,true, "UTF-8");
            message.setFrom(mailSettings.getSentFrom());
            message.setTo((String) mailModel.get("sendTo"));
            message.setSubject((String) mailModel.get("subject"));
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerConfiguration.getTemplate(messageSource.getMessage(TemplateNames.AUTO_REGISTERED_PATIENT_PASSWORD.name(), null, locale), "UTF-8"), mailModel);
            message.setText(text, true);
            }
        };

        javaMailSender.send(preparator);
    }
}
