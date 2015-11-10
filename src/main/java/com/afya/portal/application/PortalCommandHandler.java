package com.afya.portal.application;

import com.afya.portal.domain.model.*;
import com.afya.portal.domain.model.clinic.Clinic;
import com.afya.portal.domain.model.doctor.Doctor;
import com.afya.portal.domain.model.doctor.DoctorId;
import com.afya.portal.domain.model.doctor.Speciality;
import com.afya.portal.domain.model.lab.Laboratory;
import com.afya.portal.domain.model.patient.Patient;
import com.afya.portal.domain.model.pharmacy.Pharmacy;
import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.domain.model.security.UserType;
import com.afya.portal.external.PropertiesLoader;
import com.afya.portal.price.*;
import com.afya.portal.query.PortalFinder;
import com.afya.portal.service.MailService;
import com.afya.portal.service.SmsService;
import com.afya.portal.service.TemplateNames;
import com.afya.portal.util.DatabaseUtil;
import com.afya.portal.util.SmsUtil;
import com.afya.portal.util.SqlScriptRunner;
import com.afya.portal.util.UtilValidator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.nzion.dto.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.nthdimenzion.common.service.JpaRepositoryFactory;
import org.nthdimenzion.object.utils.IIdGenerator;
import org.nthdimenzion.utils.UtilDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import javax.servlet.http.HttpServletRequest;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Mohan Sharma on 3/12/2015.
 */
@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PortalCommandHandler {

    private SimpleJpaRepository<Clinic, String> clinicalRepository;
    private SimpleJpaRepository<Patient, String> patientRepository;
    private SimpleJpaRepository<Laboratory, String> laboratoryRepository;
    private SimpleJpaRepository<Pharmacy, String> pharmacyRepository;
    private SimpleJpaRepository<Doctor, DoctorId> doctorRepository;
    private SimpleJpaRepository<DataResource, Long> dataResourceRepository;
    private SimpleJpaRepository<Speciality, String> specialityRepository;
    private SimpleJpaRepository<UserLogin, String> userLoginRepository;
    private SimpleJpaRepository<UserTenantAssoc, String> userTanentAssocRepository;
    private SimpleJpaRepository<PricePackage, String> pricePackageRepository;
    private SimpleJpaRepository<PricePackageService, String> pricePackageServiceRepository;
    private SimpleJpaRepository<UserPackage, String> userPackageRepository;
    private SimpleJpaRepository<UserPackageService, String> userPackageServiceRepository;
    private SimpleJpaRepository<Tenant, String> tenantRepository;
    private SimpleJpaRepository<UserNotificationSubscriptionHistory, String> userSubscriptionHistoryRepository;
    private SimpleJpaRepository<PaymentGatewayTransaction, Long> paymentGatewayTransactionRepository;
    private SimpleJpaRepository<UserPackageHistory, Long> userPackageHistoryRepository;
    private IIdGenerator iIdGenerator;
    private PropertiesLoader propertiesLoader;
    private String databaseUsername;
    private MailService mailService;
    private String databasePassword;
    private String portalDatabaseName;
    private String hostPortal;
    private String pharmacyTenantDatabase;
    @Autowired
    private PortalFinder portalFinder;

    @Autowired
    private SmsService smsService;

    @Autowired
    public PortalCommandHandler(IIdGenerator iIdGenerator, PropertiesLoader propertiesLoader, JpaRepositoryFactory jpaRepositoryFactory, MailService mailService) {
        this.iIdGenerator = iIdGenerator;
        this.propertiesLoader = propertiesLoader;
        this.mailService = mailService;
        clinicalRepository = jpaRepositoryFactory.getCrudRepository(Clinic.class);
        patientRepository = jpaRepositoryFactory.getCrudRepository(Patient.class);
        laboratoryRepository = jpaRepositoryFactory.getCrudRepository(Laboratory.class);
        pharmacyRepository = jpaRepositoryFactory.getCrudRepository(Pharmacy.class);
        doctorRepository = jpaRepositoryFactory.getCrudRepository(Doctor.class);
        dataResourceRepository = jpaRepositoryFactory.getCrudRepository(DataResource.class);
        specialityRepository = jpaRepositoryFactory.getCrudRepository(Speciality.class);
        databaseUsername = propertiesLoader.getDatabaseUsername();
        databasePassword = propertiesLoader.getDatabasePassword();
        portalDatabaseName = propertiesLoader.getPortalDatabaseName();
        pharmacyTenantDatabase = propertiesLoader.getPharmacyTenantDatabase();
        hostPortal = propertiesLoader.getHostForPortal();
        userLoginRepository = jpaRepositoryFactory.getCrudRepository(UserLogin.class);
        userTanentAssocRepository = jpaRepositoryFactory.getCrudRepository(UserTenantAssoc.class);
        pricePackageRepository = jpaRepositoryFactory.getCrudRepository(PricePackage.class);
        pricePackageServiceRepository = jpaRepositoryFactory.getCrudRepository(PricePackageService.class);
        userPackageRepository = jpaRepositoryFactory.getCrudRepository(UserPackage.class);
        userPackageServiceRepository = jpaRepositoryFactory.getCrudRepository(UserPackageService.class);
        userSubscriptionHistoryRepository = jpaRepositoryFactory.getCrudRepository(UserNotificationSubscriptionHistory.class);
        tenantRepository = jpaRepositoryFactory.getCrudRepository(Tenant.class);
        paymentGatewayTransactionRepository = jpaRepositoryFactory.getCrudRepository(PaymentGatewayTransaction.class);
        userPackageHistoryRepository = jpaRepositoryFactory.getCrudRepository(UserPackageHistory.class);
    }

    @CommandHandler
    public void persistFacility(PersistFacilityCommand cmd) throws SQLException, ClassNotFoundException {
        String identifier = null;
        String username = cmd.getLoginPreference() == LoginPreference.EMAIL ? cmd.getEmail() : cmd.getMobile();

        // get and updated, Governorate and Country from City (kannan)
        Map<String,Object> stateAndCountryMap = portalFinder.getStateCountryBasedOnCity(cmd.getSelectedCity());
        cmd.setSelectedState(stateAndCountryMap.get("state").toString());
        cmd.setSelectedCountry(stateAndCountryMap.get("country").toString());

        // delete if the user had another registration just before this step
        final String oldRegUsername = cmd.getOldRegUsername();
        if(oldRegUsername != null){
            // retrieve user to delete
            UserLogin userToDelete = userLoginRepository.findOne(oldRegUsername);
            // if user is not verified, then delete
            if(userToDelete.isVerified() == false){
                // delete Clinic, Pharmacy, Laboratory
                Clinic clinicToDelete = null;
                Pharmacy pharmacyToDelete = null;
                Laboratory labToDelete = null;
                boolean  deleteRecord = false;
                if (cmd.facilityType.equals(ProviderType.CLINIC)) {
                    clinicToDelete = clinicalRepository.getOne(userToDelete.getFacilityId());
                    if(!clinicToDelete.getClinicName().equals(cmd.getFacilityName())
                            || !clinicToDelete.getOfficePhoneNumber().equals(cmd.getOfficePhoneNumber())) {
                        deleteRecord = true;
                    }
                }else if (cmd.facilityType.equals(ProviderType.PHARMACY)) {
                    pharmacyToDelete =  pharmacyRepository.getOne(userToDelete.getFacilityId());
                    if(!pharmacyToDelete.getPharmacyName().equals(cmd.getFacilityName())
                            || !pharmacyToDelete.getOfficePhoneNumber().equals(cmd.getOfficePhoneNumber())) {
                        deleteRecord = true;
                    }
                }else if (cmd.facilityType.equals(ProviderType.LABORATORY)) {
                    labToDelete = laboratoryRepository.getOne(userToDelete.getFacilityId());
                    if(!labToDelete.getLabName().equals(cmd.getFacilityName())
                            || !labToDelete.getOfficePhoneNumber().equals(cmd.getOfficePhoneNumber())) {
                        deleteRecord = true;
                    }
                }

                if(!userToDelete.getFirstName().equals(cmd.getFirstName())
                    || !(userToDelete.getMiddleName() == null ? cmd.getMiddleName() == null : userToDelete.getMiddleName().equals(cmd.getMiddleName()))
                    || !userToDelete.getLastName().equals(cmd.getLastName())
                    || !userToDelete.getEmailId().equals(cmd.getEmail())
                    || !userToDelete.getMobileNumber().equals(cmd.getMobile())
                    || !userToDelete.getProviderCity().equals(cmd.getSelectedCity())
                    || !userToDelete.getProviderRegistrationNo().equals(cmd.getProviderRegistrationNo())
                    || !userToDelete.getPassword().equals(cmd.getPassword())
                    || !userToDelete.getProviderType().equals(cmd.getFacilityType())
                    || !userToDelete.getLoginPreference().equals(cmd.getLoginPreference().toString())
                    || deleteRecord == true){

                    // delete facility
                    if(clinicToDelete != null)
                        clinicalRepository.delete(clinicToDelete);
                    if(pharmacyToDelete != null)
                        pharmacyRepository.delete(pharmacyToDelete);
                    if(labToDelete != null)
                        laboratoryRepository.delete(labToDelete);
                    // delete the user
                    userLoginRepository.delete(userToDelete);
                    userLoginRepository.flush();
                }
            }
        }

        UserLogin fetchedUserLogin = userLoginRepository.findOne(username);

        if(fetchedUserLogin != null && !fetchedUserLogin.getVerified()) {
            if (fetchedUserLogin.getLoginPreference().equals(LoginPreference.MOBILE.toString())) {
                Map<String,Object> smsMap =  new HashMap<String, Object>();
                smsMap.put("firstName", fetchedUserLogin.getFirstName());
                smsMap.put("lastName", fetchedUserLogin.getLastName());
                smsMap.put("mobileNumber", fetchedUserLogin.getMobileNumber());
                smsMap.put("token", fetchedUserLogin.getOtpToken());
                //smsMap.put("isdCode", fetchedUserLogin.getIsdCode());
                SmsUtil.sendOTPSMS(smsMap);
            }
            else if (fetchedUserLogin.getLoginPreference().equals(LoginPreference.EMAIL.toString())) {
                sendAccountConfirmationMailForFacility(fetchedUserLogin.getFirstName(), fetchedUserLogin.getLastName(), fetchedUserLogin.getEmailId(), fetchedUserLogin.getToken(), fetchedUserLogin.getFacilityId(), false);
            }
            Preconditions.checkState((fetchedUserLogin.getVerified()) == true, "NotVerified");
        }

        if (cmd.getLoginPreference() == LoginPreference.EMAIL) {
            Preconditions.checkState((fetchedUserLogin == null), "This email Id " + username + " already exists");
        } else {
            Preconditions.checkState((fetchedUserLogin == null), "This mobile number " + username + " already exists");
        }
        final UserLogin userLogin = new UserLogin(username, cmd.getPassword(), UserType.ADMIN, identifier, cmd.facilityType);
        cmd.setUsername(username);

        /*  kannan - commented, Tenant creation to be moved to Pricing-Package Subscription
        if (cmd.facilityType.equals(ProviderType.CLINIC)) {
            identifier = createClinic(cmd, databaseUsername, databasePassword);
        }
        if (cmd.facilityType.equals(ProviderType.PHARMACY)) {
            identifier = createPharmacy(cmd, databaseUsername, databasePassword);
        }
        if (cmd.facilityType.equals(ProviderType.LABORATORY)) {
            identifier = createLaboratory(cmd, databaseUsername, databasePassword);
        }*/

        // kannan - create Clinic, Pharmacy, Laboratory without tenant DB
        if (cmd.facilityType.equals(ProviderType.CLINIC)) {
            identifier = createClinic(cmd);
        }
        if (cmd.facilityType.equals(ProviderType.PHARMACY)) {
            identifier = createPharmacy(cmd);
        }
        if (cmd.facilityType.equals(ProviderType.LABORATORY)) {
            identifier = createLaboratory(cmd);
        }

        if(UtilValidator.isEmpty(userLogin.getFirstName()))
            userLogin.setFirstName(cmd.getFirstName());
        if(UtilValidator.isEmpty(userLogin.getMiddleName()))
            userLogin.setMiddleName(cmd.getMiddleName());
        if(UtilValidator.isEmpty(userLogin.getLastName()))
            userLogin.setLastName(cmd.getLastName());
        if(UtilValidator.isEmpty(userLogin.getMobileNumber()))
            userLogin.setMobileNumber(cmd.getMobile());
        if(UtilValidator.isEmpty(userLogin.getEmailId()))
            userLogin.setEmailId(cmd.getEmail());
        userLogin.setVerified(false);
        if(UtilValidator.isEmpty(userLogin.getLoginPreference()))
            userLogin.setLoginPreference(cmd.getLoginPreference().name());

        userLogin.setTenantId(cmd.getTenantId());
        userLogin.setFacilityId(cmd.getTenantId());

        // start of add (kannan) - to store City, Governerate and Country
        userLogin.setProviderCity(cmd.getSelectedCity());
        // Map<String,Object> stateAndCountryMap = portalFinder.getStateCountryBasedOnCity(cmd.getSelectedCity());
        userLogin.setProviderGovernorate(stateAndCountryMap.get("state").toString());
        userLogin.setProviderCountry(stateAndCountryMap.get("country").toString());
        userLogin.setProviderRegistrationNo(cmd.getProviderRegistrationNo());
        // end of add


        if (userLogin.getLoginPreference().equals(LoginPreference.MOBILE.toString())) {
            Map<String,Object> smsMap =  new HashMap<String, Object>();
            smsMap.put("firstName", userLogin.getFirstName());
            smsMap.put("lastName", userLogin.getLastName());
            smsMap.put("mobileNumber", userLogin.getMobileNumber());
            smsMap.put("token", userLogin.getOtpToken());
            //smsMap.put("isdCode", null);
            SmsUtil.sendOTPSMS(smsMap);
        }

        userLoginRepository.save(userLogin);

        /*
        kannan - commented, Tenant creation to be moved to Pricing-Package Subscription
        UserTenantAssoc tenantAssoc = new UserTenantAssoc(cmd.getTenantId(), username, cmd.getFacilityType().toString());
        userTanentAssocRepository.save(tenantAssoc);
        */

        if (cmd.getLoginPreference() == LoginPreference.EMAIL) {
            sendAccountConfirmationMailForFacility(cmd.getFirstName(), cmd.getLastName(), cmd.getEmail(), userLogin.getToken(), userLogin.getFacilityId(), false);
        }
    }

    // method to create Tenant DB (Kannan - separated Clinic, Pharamacy and Laboratory Tenant DB creation into two parts, Provider creation and Tenant DB creation, this function will perfor the Tenant DB creation)
    private void createTenantDBIfRequired(UserLogin user, String smsSenderId) throws SQLException, ClassNotFoundException {
        // check if we need to create DB for the Tenant Provider-Type
        if(user.getProviderType() == ProviderType.CLINIC || user.getProviderType() == ProviderType.PHARMACY){

            // check if Tenant is already created
            Tenant tenant = tenantRepository.findOne(user.getTenantId());
            if(tenant != null)
                return;

            final PersistFacilityCommand cmd = new PersistFacilityCommand();

            cmd.setAddress(user.getProviderAddress1());
            cmd.setAddressAdditional(user.getProviderAddress2());
            cmd.setEmail(user.getEmailId());
            cmd.setFacilityType(user.getProviderType());
            cmd.setFirstName(user.getFirstName());
            cmd.setLastName(user.getLastName());
            cmd.setMiddleName(user.getMiddleName());
            cmd.setMobile(user.getMobileNumber());
            cmd.setPostalCode(user.getProviderPostalCode());
            cmd.setProviderRegistrationNo(user.getProviderRegistrationNo());
            cmd.setLoginPreference(LoginPreference.valueOf(user.getLoginPreference()));
            cmd.setOfficePhoneNumber(user.getOfficeNumber());
            cmd.setSelectedCity(user.getProviderCity());
            cmd.setSelectedState(user.getProviderGovernorate());
            cmd.setSelectedCountry(user.getProviderCountry());
            cmd.setUsername(user.getUsername());
            cmd.setPassword(user.getPassword());
            cmd.setSmsSenderId(smsSenderId);

            if(user.getProviderType() == ProviderType.CLINIC) {
                Clinic clinic = clinicalRepository.getOne(user.getTenantId());
                cmd.setAccrNumber(clinic.getAccrNumber());
                cmd.setDrugLicence(clinic.getDrugLicence());
                cmd.setFacilityName(clinic.getClinicName());
                cmd.setFaxNumber(clinic.getFaxNumber());
                cmd.setLocation(clinic.getLocation());
                cmd.setPanNumber(clinic.getPanNumber());
                cmd.setServiceTaxNumber(clinic.getServiceTaxNumber());
                createClinicTenantDB(cmd, databaseUsername, databasePassword);
            }else if (cmd.facilityType.equals(ProviderType.PHARMACY)) {
                Pharmacy pharmacy = pharmacyRepository.getOne(cmd.getTenantId());
                cmd.setAccrNumber(pharmacy.getAccrNumber());
                cmd.setDrugLicence(pharmacy.getDrugLicence());
                cmd.setFacilityName(pharmacy.getPharmacyName());
                cmd.setFaxNumber(pharmacy.getFaxNumber());
                cmd.setLocation(pharmacy.getLocation());
                cmd.setPanNumber(pharmacy.getPanNumber());
                cmd.setServiceTaxNumber(pharmacy.getServiceTaxNumber());
                createPharmacyTenantDB(cmd, databaseUsername, databasePassword);
            }/*else if (cmd.facilityType.equals(ProviderType.LABORATORY)) {
                Laboratory laboratory = laboratoryRepository.getOne(cmd.getTenantId());
                cmd.setAccrNumber(laboratory.getAccrNumber());
                cmd.setDrugLicence(laboratory.getDrugLicence());
                cmd.setFacilityName(laboratory.getLabName());
                cmd.setFaxNumber(laboratory.getFaxNumber());
                cmd.setLocation(laboratory.getLocation());
                cmd.setPanNumber(laboratory.getPanNumber());
                cmd.setServiceTaxNumber(laboratory.getServiceTaxNumber());
                createLaboratoryTenantDB(cmd, databaseUsername, databasePassword);
            }*/

            UserTenantAssoc tenantAssoc = new UserTenantAssoc(cmd.getTenantId(), cmd.getUsername(), cmd.getFacilityType().toString());
            userTanentAssocRepository.save(tenantAssoc);
        }
    }

    @CommandHandler
    public String persistPatient(PersistPatientCommand persistPatientCommand) {
        Patient patient = patientRepository.findOne(persistPatientCommand.getPatientDto().getAfyaId()) != null ? patientRepository.findOne(persistPatientCommand.getPatientDto().getAfyaId()) : new Patient();
        ProviderType facilityType = persistPatientCommand.getFacilityType();
        patient.setPropertiesToPatientEntity(persistPatientCommand.getPatientDto());
        if (ProviderType.CLINIC == facilityType) {
            Clinic clinic = clinicalRepository.findOne(persistPatientCommand.getTenantId());
            patient.addClinics(clinic);
        }
        if (ProviderType.PHARMACY == facilityType) {
            Pharmacy pharmacy = pharmacyRepository.findOne(persistPatientCommand.getTenantId());
            patient.addPharmacy(pharmacy);
        }
        patientRepository.saveAndFlush(patient);
        return patient.toString();
    }


    @CommandHandler
    public String registerPatient(RegisterPatientCommand cmd) {

        ServletRequestAttributes sra = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = sra.getRequest();
        StringBuffer url = req.getRequestURL();
        final String baseUrl = url.substring(0, url.length() - req.getRequestURI().length() + req.getContextPath().length());

        Patient patient = new Patient();
        PatientDto dto = cmd.getPatientDto();
        dto.setAfyaId("" + portalFinder.getNextAfyaId());
        patient.setPropertiesToPatientEntity(dto);
        // patientRepository.saveAndFlush(patient);

        String username = cmd.getPatientDto().getLoginPreference() == LoginPreference.EMAIL ? cmd.getPatientDto().getEmailId() : cmd.getPatientDto().getMobileNumber();

        // delete if the user had another registration just before this step
        String oldRegUsername = cmd.getPatientDto().getOldRegUsername();
        if(oldRegUsername != null){
            UserLogin userToDelete = userLoginRepository.findOne(oldRegUsername);
            if(userToDelete.isVerified() == false
                    && (!userToDelete.getFirstName().equals(dto.getFirstName()))
                        || !(userToDelete.getMiddleName() == null ? dto.getMiddleName() == null :  userToDelete.getMiddleName().equals(dto.getMiddleName()))
                        || !userToDelete.getLastName().equals(dto.getLastName())
                        || !userToDelete.getEmailId().equals(dto.getEmailId())
                        || !userToDelete.getMobileNumber().equals(dto.getMobileNumber())
                        || !userToDelete.getPassword().equals(dto.getPassword())
                        || !userToDelete.getLoginPreference().equals(dto.getLoginPreference().toString())){
                userLoginRepository.delete(userToDelete);
                userLoginRepository.flush();
            }
        }

        UserLogin fetchedUserLogin = userLoginRepository.findOne(username);
        if(fetchedUserLogin != null && !fetchedUserLogin.getVerified()) {
            if (fetchedUserLogin.getLoginPreference().equals(LoginPreference.MOBILE.toString())) {
                Map<String,Object> smsMap =  new HashMap<String, Object>();
                smsMap.put("firstName", fetchedUserLogin.getFirstName());
                smsMap.put("lastName", fetchedUserLogin.getLastName());
                smsMap.put("mobileNumber", fetchedUserLogin.getMobileNumber());
                smsMap.put("token", fetchedUserLogin.getOtpToken());
                //smsMap.put("isdCode", fetchedUserLogin.getIsdCode());
                SmsUtil.sendOTPSMS(smsMap);
            }
            else if (fetchedUserLogin.getLoginPreference().equals(LoginPreference.EMAIL.toString())) {
                sendAccountConfirmationMailForPatient(fetchedUserLogin.getFirstName(), fetchedUserLogin.getLastName(), fetchedUserLogin.getEmailId(), fetchedUserLogin.getToken(), false, baseUrl);
            }
            Preconditions.checkState((fetchedUserLogin.getVerified()) == true, "NotVerified");
        }

        if (cmd.getPatientDto().getLoginPreference() == LoginPreference.EMAIL) {
            Preconditions.checkState((fetchedUserLogin == null), " This email Id  " + username + " already exists");
        } else {
            Preconditions.checkState((fetchedUserLogin == null), " This mobile number " + username + " already exists");
        }
        final UserLogin userLogin = new UserLogin(username, cmd.getPatientDto().getPassword(), UserType.PATIENT, null, null);
        userLogin.setFirstName(dto.getFirstName());
        userLogin.setMiddleName(dto.getMiddleName());
        userLogin.setLastName(dto.getLastName());
        userLogin.setEmailId(dto.getEmailId());
        userLogin.setMobileNumber(dto.getMobileNumber());
        userLogin.setLoginPreference(dto.getLoginPreference().toString());
        userLogin.setCreatedTxTimestamp(new java.util.Date());
        userLogin.setVerified(false);

        if(!cmd.isSkipRegistrationNotification()) {     // kannan (2015-11-01)
            if (userLogin.getLoginPreference().equals(LoginPreference.MOBILE.toString())) {
                Map<String, Object> smsMap = new HashMap<String, Object>();
                smsMap.put("firstName", userLogin.getFirstName());
                smsMap.put("lastName", userLogin.getLastName());
                smsMap.put("mobileNumber", userLogin.getMobileNumber());
                smsMap.put("token", userLogin.getOtpToken());
                smsMap.put("isdCode", dto.getIsdCode());
                SmsUtil.sendOTPSMS(smsMap);
            } else if (cmd.getPatientDto().getLoginPreference() == LoginPreference.EMAIL) {
                sendAccountConfirmationMailForPatient(dto.getFirstName(), dto.getLastName(), dto.getEmailId(), userLogin.getToken(), false, baseUrl);
            }
        }

        userLoginRepository.save(userLogin);
        //if (cmd.getPatientDto().getLoginPreference() == LoginPreference.EMAIL)

        return patient.toString();
    }

    private UserPackage getUserPackage(UserLogin user, PackageDto packageDto) {
        for (UserPackage userPackage : user.getUserPackages()) {
            System.out.println("User Package ID=" + userPackage.getPricePackage().getPackageId() + ", DTO package ID=" + packageDto.getPackageId());
            if (userPackage.getPricePackage().getPackageId().equals(packageDto.getPackageId())) {
                return userPackage;
            }
        }
        return null;
    }

    private UserPackageService getUserPackageService(UserLogin user, PackageDto packageDto, PackageServiceDto dto) {
        for (UserPackageService userPackageService : user.getUserPackageServices()) {
            if (userPackageService.getPricePackage().getPackageId().equals(packageDto.getPackageId()) &&
                    (userPackageService.getService().getServiceId().equals(dto.getServiceId()))) {
                return userPackageService;
            }
        }
        return null;
    }

    @CommandHandler
    public void subscribePackage(SubscribePackageCommand cmd) throws SQLException, ClassNotFoundException {
        UserLogin user = userLoginRepository.findOne(cmd.getUserName());
        int availableSMSCount = user.getAvailableSMSCount();
        boolean sendSubscriptionEmail = false;  // kannan - 2015-11-03 - using this to check, if we need send Subscription Emails
        // boolean _isNewSubscription = false;
        boolean trail = false;
        // get Payment Gatewya Transaction if specified in command
        PaymentGatewayTransaction pgTransaction = null;
        if(cmd.getPaymentId() != null)
            pgTransaction = paymentGatewayTransactionRepository.findOne(cmd.getPaymentId());
        // current Datetime
        Date currentDateTime = pgTransaction != null ? pgTransaction.getTransactionTimestamp() : new Date();
        // int  expiryMonth =  0;  // kannan - commented
        for (PackageDto packageDto : cmd.getPackageList()) {
            if (packageDto.getTrail() != null && packageDto.getTrail().equals("Y"))
                trail = true;
                // trail = packageDto.getTrail();

            // Transaction time (either current DateTime / Payment Gateway Transaction DateTime / existing Package Subscription DateTime
            Date packageSubscriptionDatetime = null;
            // prepare User Package object
            UserPackage _userPackage = getUserPackage(user, packageDto);
            if (_userPackage == null) {
                _userPackage = new UserPackage();
                _userPackage.setUser(user);
                PricePackage pricePackage = pricePackageRepository.findOne(packageDto.getPackageId());
                _userPackage.setPricePackage(pricePackage);
            }else{
                // use the existing subscription Date if applicable
                if(_userPackage.isSubscriptionActive()) // THIS WOULD WORK FOR MOVING FROM TRIAL TO PAID SUBSCRIPTION (NEED TO WORK OUT LOGIC FOR UPGRADES - MOVING FROM ONE PRICING-PLAN TO ANOTHER)
                    packageSubscriptionDatetime = _userPackage.getSubscriptionDate();
            }
            if(packageSubscriptionDatetime == null)
                packageSubscriptionDatetime = currentDateTime;
            // TO DO - relook at the flow
            // determine if this is a new subscription - its a new Subscription, if the subscription is NOT active and the trial status is NOT same as existing (not upgrading from Trial to Paid)
            boolean _isNewSubscription = (_userPackage.isSubscriptionActive() == true && _userPackage.getUser().isTrail() == trail) ? false : true;

            if(_isNewSubscription == true) { // kannan - do the following only if this is NOT a Package that is Currently under Active Subscription
                // if (packageDto.getActivated() == 1) {
                if(packageDto.getActivated() == 1) {
                    /*if ("SERVICE".equals(packageDto.getPackageCategory())) {
                        if ("HALF_YEARLY".equals(packageDto.getPriceOption())) {
                            expiryMonth = 6;
                        }
                        if ("ANNUALLY".equals(packageDto.getPriceOption())) {
                            expiryMonth = 12;
                        }
                    }*/
                    _userPackage.setSubscriptionDate(packageSubscriptionDatetime);   // kannan - set the Subscription-Date to today's date, only if the package is activated
                }
                _userPackage.setActivated(packageDto.getActivated());
                _userPackage.setNumberOfUsers(packageDto.getNumberOfUsers());
                _userPackage.setPriceOption(packageDto.getPriceOption());
                _userPackage.setAmount(packageDto.getAmount());
                userPackageRepository.save(_userPackage);
            }
            for (PackageServiceDto serviceDto : packageDto.getServices()) {
                UserPackageService userPackageService = getUserPackageService(user, packageDto, serviceDto);
                if (userPackageService == null) {
                    userPackageService = new UserPackageService();
                    userPackageService.setUser(user);
                    PricePackage pricePackage = pricePackageRepository.findOne(packageDto.getPackageId());
                    userPackageService.setPricePackage(pricePackage);
                    PricePackageService pricePackageService = pricePackageServiceRepository.findOne(serviceDto.getServiceId());
                    userPackageService.setService(pricePackageService);
                }
                userPackageService.setActivated(serviceDto.getActivated());
                userPackageService.setNumberOfHours(serviceDto.getNumberOfHours());
                userPackageService.setRate(serviceDto.getRate());
                userPackageService.setPacks(serviceDto.getPacks());
                userPackageService.setNumberOfSMS(userPackageService.getPacks()*serviceDto.getNumberOfSMS());
                userPackageService.setExpiryDays(serviceDto.getExpireDays());
                userPackageService.setAmount(serviceDto.getAmount());

                if ((userPackageService.getService().getServiceType().equals("NOTIFICATION") || userPackageService.getService().getServiceType().equals("TRAINING")) &&
                        (serviceDto.getActivated() == 1)) {
                    userPackageService.setSubscriptionDate(currentDateTime);    // Notification & Training to use Current Timestamp, since we will come here only when user selects Notification/Training and either Subscribes / takes Trial
                    Calendar c = Calendar.getInstance();
                    c.setTime(userPackageService.getSubscriptionDate());
                    c.add(Calendar.DATE, userPackageService.getService().getExpireDays()); // Adding 5 days
                    userPackageService.setExpiryDate(c.getTime());
                    int numberOfSMS = userPackageService.getService().getNumberOfSMS();
                    availableSMSCount = availableSMSCount + (userPackageService.getPacks() * numberOfSMS);
                    UserNotificationSubscriptionHistory history = new UserNotificationSubscriptionHistory();
                    history.populate(userPackageService, currentDateTime);
                    //history.setPaymentTrackingRef(cmd.getPaymentTrackingRef()); // payment tracking ref
                    // update Paymentgateway transction in history object
                    if(cmd.getPaymentId() != null)
                        history.setPaymentGatewayTransaction(paymentGatewayTransactionRepository.findOne(cmd.getPaymentId()));
                    userSubscriptionHistoryRepository.save(history);
                }

                userPackageServiceRepository.save(userPackageService);
            }
            // retrieve User object
            user = userLoginRepository.findOne(cmd.getUserName());
            // update total SMS count in for the user
            user.setAvailableSMSCount(availableSMSCount);

            // user.setSubscriptionDate(new Date());
            /*if(_userPackage.getActivated() == 1 && "SERVICE".equals(packageDto.getPackageCategory())) {
                user.setSubscriptionDate(_userPackage.getSubscriptionDate());   // kannan - changed to updated Package Subscription Date
                user.setTrail(trail.equals("Y") ? true : false);
            } kannan - moving this to below block - to execute this only for new-subscriptions
            */

            if(_isNewSubscription == true && _userPackage.getActivated() == 1 && "SERVICE".equals(packageDto.getPackageCategory())) {          // kannan - update trial and expiry dates only if the package is active
                // set Subscription Date in users table
                user.setSubscriptionDate(_userPackage.getSubscriptionDate());
                // update subscription users fields based on Trial/Paid
                if (trail == true) {
                    Calendar c = Calendar.getInstance();
                    // c.setTime(new Date()); // Now use today date.
                    // c.add(Calendar.DATE, 30); // Adding 20 days
                    c.setTime(_userPackage.getSubscriptionDate());
                    c.add(Calendar.DATE, _userPackage.getPricePackage().getTrialPeriodDays());  // kannan Trial Period from Price Package
                    user.setExpiryDate(c.getTime());
                    user.setTrial(true);
                    // add User Package History
                    userPackageHistoryRepository.save(UserPackageHistory.create(_userPackage, null, currentDateTime));
                } else {
                    //if (expiryMonth > 0) {        // kannan - commented this condition - since update will be done only if Package is active and it is a SERVICE package
                    Calendar c = Calendar.getInstance();
                    //c.setTime(new Date()); // Now use today date.
                    //c.add(Calendar.MONTH, expiryMonth); // Adding 20 days
                    boolean isTrialConsumed = isTrialConsumed(user, packageSubscriptionDatetime);
                    c.setTime(_userPackage.getSubscriptionDate());
                    c.add(Calendar.DATE, _userPackage.getExpiryDays(isTrialConsumed ? false : true));
                    user.setExpiryDate(c.getTime());
                    user.setTrial(false);
                    // update payment tracking reference for un-piad notification subscription
                    final UserLogin finalUser = user;
                    List<UserNotificationSubscriptionHistory> unpaidSubscriptions = userSubscriptionHistoryRepository.findAll(new Specification<UserNotificationSubscriptionHistory>() {
                        @Override
                        public Predicate toPredicate(Root<UserNotificationSubscriptionHistory> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                            Join<UserNotificationSubscriptionHistory, UserLogin> userLoginJoin = root.join("user");
                            return criteriaBuilder.and(criteriaBuilder.equal(userLoginJoin.get("username"), finalUser.getUsername()),
                                    criteriaBuilder.isNull(root.get("paymentGatewayTransaction")));
                        }
                    });
                    for(UserNotificationSubscriptionHistory history : unpaidSubscriptions){
                        Preconditions.checkArgument(history.getPaymentGatewayTransaction() == null, "a Notification/Training history record already has Payment ID. UserNotificationSubscriptionHistory Record ID (" + history.getId() + ")");
                        history.setPaymentGatewayTransaction(pgTransaction);
                        history.setUpdatedTxTimestamp(currentDateTime);
                    }
                    userSubscriptionHistoryRepository.save(unpaidSubscriptions);
                    //}
                    // add User Package History
                    userPackageHistoryRepository.save(UserPackageHistory.create(_userPackage, pgTransaction, currentDateTime));
                }
                // create tenant DB (kannan)
                createTenantDBIfRequired(user, cmd.getSmsSenderId());
                // set flag to send Subscription Email
                sendSubscriptionEmail = true;
            }
            userLoginRepository.saveAndFlush(user);
        }

        final UserLogin userFinal = user;
        final boolean trailFinal = trail;
        // The email and sms notifications will be sent to the tenant upon
        // trial subscription, actual subscription or subscription during or after trial period
        if (sendSubscriptionEmail) {
            ServletRequestAttributes sra = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = sra.getRequest();
            StringBuffer url = req.getRequestURL();
            final String baseUrl = url.substring(0, url.length() - req.getRequestURI().length() + req.getContextPath().length());
            new Thread(new Runnable() {
                @Override
                public void run() {

                    System.out.println("Subscription Email ID " + userFinal.getEmailId());
                    // Raghu Bandi: Sending out e-mail upon subscription for trial period or not
                    sendMailPostSubscription(userFinal.getFirstName(), userFinal.getLastName(), userFinal.getEmailId(), userFinal.getToken(), userFinal.getFacilityId(), trailFinal, baseUrl);

                    Map<String,Object> smsMap =  new HashMap<String, Object>();
                    if (trailFinal){
                        smsMap.put("trial", new Boolean(true));
                        smsMap.put("key",TemplateNames.PROVIDER_SUBSCRIPTION_TRIAL_SMS.name());
                        smsMap.put("expiryDate", UtilDateTime.format(userFinal.getExpiryDate()));
                    } else {
                        smsMap.put("trial", new Boolean(false));
                        smsMap.put("key",TemplateNames.PROVIDER_SUBSCRIPTION_SMS.name());
                    }

                    smsMap.put("facility", userFinal.getFacilityId());
                    smsMap.put("firstName", userFinal.getFirstName());
                    smsMap.put("lastName", userFinal.getLastName());
                    smsMap.put("mobileNumber", userFinal.getMobileNumber());
                    smsMap.put("token", userFinal.getOtpToken());
                    //smsMap.put("isdCode", user.getIsdCode());
                    smsService.sendOTPSMS(smsMap);
                }
            }).start();

        }

    }

    private boolean isTrialConsumed(final UserLogin user, final Date asOnDate){
        // find from userPackageHistory for the User, having any expired Trial Subscriptions
        long trialConsumedCount = userPackageHistoryRepository.count(new Specification<UserPackageHistory>(){
            @Override
            public Predicate toPredicate(Root<UserPackageHistory> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder){
                Join<UserPackageHistory, UserLogin> userLoginJoin = root.join("user");
                return criteriaBuilder.and(criteriaBuilder.equal(userLoginJoin.get("username"), user.getUsername()),
                        criteriaBuilder.lessThan(root.<Date>get("expiryDate"),  asOnDate),
                        criteriaBuilder.equal(root.<Boolean>get("isTrial"), Boolean.TRUE));
            }
        });
        return trialConsumedCount > 0;
    }

    /*private String createClinic(final PersistFacilityCommand cmd, String databaseUsername, String databasePassword) throws ClassNotFoundException, SQLException {

        cmd.setTenantId(getTenantId(cmd));

        Person adminPerson = new Person(cmd.getFirstName(), cmd.getMiddleName(), cmd.getLastName(),
                cmd.getEmail(), cmd.getMobile());
         //Instead of cmd.location I have hardcode the location ass 111
        Clinic clinic = Clinic.createClinic(cmd.getTenantId(), cmd.facilityName, "111"
                , cmd.officePhoneNumber, cmd.faxNumber,
                cmd.serviceTaxNumber, cmd.panNumber,
                cmd.drugLicence, cmd.accrNumber,
                cmd.validFrom, cmd.validTo, adminPerson.getFirstName(), adminPerson.getLastName());

        clinic.setAdminDetail(adminPerson);


        Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        tenant.setAdminUsername(cmd.getUsername());
        Address address = Address.MakeAddressObject(cmd.address, cmd.addressAdditional, cmd.selectedCountry, cmd.selectedState, cmd.selectedCity, cmd.postalCode);
        clinic.setTenant(tenant);
        clinic.setAddress(address);
        clinicalRepository.saveAndFlush(clinic);
        String hostForClinic = propertiesLoader.getHostForClinic();
        createDatabaseAndSeed(cmd, hostForClinic, "seed/afya_clinic.sql", "seed/afya_clinic_function.sql", "$$");


        return clinic.getClinicId();
    }*/

    // kannan - to create Clinic without Tenant DB
    private String createClinic(final PersistFacilityCommand cmd) throws ClassNotFoundException, SQLException {

        cmd.setTenantId(getTenantId(cmd));

        Person adminPerson = new Person(cmd.getFirstName(), cmd.getMiddleName(), cmd.getLastName(),
                cmd.getEmail(), cmd.getMobile());
        //Instead of cmd.location I have hardcode the location ass 111
        Clinic clinic = Clinic.createClinic(cmd.getTenantId(), cmd.facilityName, "111"
                , cmd.officePhoneNumber, cmd.faxNumber,
                cmd.serviceTaxNumber, cmd.panNumber,
                cmd.drugLicence, cmd.accrNumber,
                cmd.validFrom, cmd.validTo, adminPerson.getFirstName(), adminPerson.getLastName());

        clinic.setAdminDetail(adminPerson);


        // Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        // tenant.setAdminUsername(cmd.getUsername());
        Address address = Address.MakeAddressObject(cmd.address, cmd.addressAdditional, cmd.selectedCountry, cmd.selectedState, cmd.selectedCity, cmd.postalCode);
        // clinic.setTenant(tenant);
        clinic.setAddress(address);
        clinicalRepository.saveAndFlush(clinic);
        // String hostForClinic = propertiesLoader.getHostForClinic();
        // createDatabaseAndSeed(cmd, hostForClinic, "seed/afya_clinic.sql", "seed/afya_clinic_function.sql", "$$");
        return clinic.getClinicId();
    }

    // kannan - to create Clinic Tenant DB
    private String createClinicTenantDB(final PersistFacilityCommand cmd, String databaseUsername, String databasePassword) throws ClassNotFoundException, SQLException {

        cmd.setTenantId(getTenantId(cmd));

        Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        tenant.setAdminUsername(cmd.getUsername());
        tenant.setSmsSenderName(cmd.getSmsSenderId());

        Clinic clinic = clinicalRepository.getOne(cmd.getTenantId());
        clinic.setTenant(tenant);
        clinicalRepository.saveAndFlush(clinic);

        String hostForClinic = propertiesLoader.getHostForClinic();
        createDatabaseAndSeed(cmd, hostForClinic, "seed/afya_clinic.sql", "seed/afya_clinic_function.sql", "$$");
        return clinic.getClinicId();
    }

    private String getTenantId(PersistFacilityCommand cmd) {
        if (cmd.getTenantId() != null) {
            return cmd.getTenantId();
        }
        if (cmd.getFacilityName() == null)
            return null;

        return cmd.getFacilityName().replaceAll("\\s", "").toLowerCase();

    }

    /*private String createLaboratory(PersistFacilityCommand cmd, String databaseUsername, String databasePassword) throws SQLException, ClassNotFoundException {
        cmd.setTenantId(getTenantId(cmd));
        Person adminPerson = new Person(cmd.getFirstName(), cmd.getMiddleName(), cmd.getLastName(),
                cmd.getEmail(), cmd.getMobile());


        Laboratory laboratory = Laboratory.CreateLaboratoryObject(iIdGenerator.nextId(), cmd.facilityName, cmd.location
                , cmd.officePhoneNumber, cmd.faxNumber,
                cmd.serviceTaxNumber, cmd.panNumber,
                cmd.drugLicence, cmd.accrNumber,
                cmd.validFrom, cmd.validTo, cmd.getFirstName(), cmd.getLastName());

        laboratory.setAdminDetail(adminPerson);

        Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        tenant.setAdminUsername(cmd.getUsername());
        Address address = Address.MakeAddressObject(cmd.address, cmd.addressAdditional, cmd.selectedCountry, cmd.selectedState, "-", cmd.postalCode);
        laboratory.setTenant(tenant);
        laboratory.setAddress(address);
        laboratoryRepository.saveAndFlush(laboratory);
        String hostForLab = propertiesLoader.getHostForLab();
        createDatabaseAndSeed(cmd, hostForLab, "seed/afya_lab.sql");
        return laboratory.getLabId();
    }*/

    private String createLaboratory(PersistFacilityCommand cmd) throws SQLException, ClassNotFoundException {
        cmd.setTenantId(getTenantId(cmd));
        Person adminPerson = new Person(cmd.getFirstName(), cmd.getMiddleName(), cmd.getLastName(),
                cmd.getEmail(), cmd.getMobile());


        Laboratory laboratory = Laboratory.CreateLaboratoryObject(iIdGenerator.nextId(), cmd.facilityName, cmd.location
                , cmd.officePhoneNumber, cmd.faxNumber,
                cmd.serviceTaxNumber, cmd.panNumber,
                cmd.drugLicence, cmd.accrNumber,
                cmd.validFrom, cmd.validTo, cmd.getFirstName(), cmd.getLastName());

        laboratory.setAdminDetail(adminPerson);

        // Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        // tenant.setAdminUsername(cmd.getUsername());
        Address address = Address.MakeAddressObject(cmd.address, cmd.addressAdditional, cmd.selectedCountry, cmd.selectedState, "-", cmd.postalCode);
        // laboratory.setTenant(tenant);
        laboratory.setAddress(address);
        laboratoryRepository.saveAndFlush(laboratory);
        // String hostForLab = propertiesLoader.getHostForLab();
        // createDatabaseAndSeed(cmd, hostForLab, "seed/afya_lab.sql");
        return laboratory.getLabId();
    }

    private String createLaboratoryTenantDB(PersistFacilityCommand cmd, String databaseUsername, String databasePassword) throws SQLException, ClassNotFoundException {
        cmd.setTenantId(getTenantId(cmd));

        Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        tenant.setSmsSenderName(cmd.getSmsSenderId());
        Laboratory laboratory = laboratoryRepository.getOne(cmd.getTenantId());
        laboratory.setTenant(tenant);
        laboratoryRepository.saveAndFlush(laboratory);
        String hostForLab = propertiesLoader.getHostForLab();
        createDatabaseAndSeed(cmd, hostForLab, "seed/afya_lab.sql");
        return laboratory.getLabId();
    }

    /*private String createPharmacy(PersistFacilityCommand cmd, String databaseUsername, String databasePassword) throws SQLException, ClassNotFoundException {
        cmd.setTenantId(getTenantId(cmd));
        Person adminPerson = new Person(cmd.getFirstName(), cmd.getMiddleName(), cmd.getLastName(),
                cmd.getEmail(), cmd.getMobile());

        Pharmacy pharmacy = Pharmacy.CreatePharmacyObject(cmd.getTenantId(), cmd.facilityName, cmd.location
                , cmd.officePhoneNumber, cmd.faxNumber,
                cmd.serviceTaxNumber, cmd.panNumber,
                cmd.drugLicence, cmd.accrNumber,
                cmd.validFrom, cmd.validTo, adminPerson.getFirstName(),
                adminPerson.getLastName());

        pharmacy.setAdminDetail(adminPerson);

        Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        tenant.setAdminUsername(cmd.getUsername());
        Address address = Address.MakeAddressObject(cmd.address, cmd.addressAdditional, cmd.selectedCountry, cmd.selectedState, "-", cmd.postalCode);
        pharmacy.setTenant(tenant);
        pharmacy.setAddress(address);
        pharmacyRepository.saveAndFlush(pharmacy);
        String hostForPharmacy = propertiesLoader.getHostForPharmacy();
        createDatabaseAndSeed(cmd, hostForPharmacy, "seed/afya_pharmacy.sql");
        return pharmacy.getPharmacyId();
    }*/

    private String createPharmacy(PersistFacilityCommand cmd) throws SQLException, ClassNotFoundException {
        cmd.setTenantId(getTenantId(cmd));
        Person adminPerson = new Person(cmd.getFirstName(), cmd.getMiddleName(), cmd.getLastName(),
                cmd.getEmail(), cmd.getMobile());

        Pharmacy pharmacy = Pharmacy.CreatePharmacyObject(cmd.getTenantId(), cmd.facilityName, cmd.location
                , cmd.officePhoneNumber, cmd.faxNumber,
                cmd.serviceTaxNumber, cmd.panNumber,
                cmd.drugLicence, cmd.accrNumber,
                cmd.validFrom, cmd.validTo, adminPerson.getFirstName(),
                adminPerson.getLastName());

        pharmacy.setAdminDetail(adminPerson);

        // Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        // tenant.setAdminUsername(cmd.getUsername());
        Address address = Address.MakeAddressObject(cmd.address, cmd.addressAdditional, cmd.selectedCountry, cmd.selectedState, "-", cmd.postalCode);
        // pharmacy.setTenant(tenant);
        pharmacy.setAddress(address);
        pharmacyRepository.saveAndFlush(pharmacy);
        // String hostForPharmacy = propertiesLoader.getHostForPharmacy();
        // createDatabaseAndSeed(cmd, hostForPharmacy, "seed/afya_pharmacy.sql");
        return pharmacy.getPharmacyId();
    }

    private String createPharmacyTenantDB(PersistFacilityCommand cmd, String databaseUsername, String databasePassword) throws SQLException, ClassNotFoundException {
        cmd.setTenantId(getTenantId(cmd));

        Tenant tenant = Tenant.MakeTenant(cmd.getTenantId(), databaseUsername, databasePassword);
        tenant.setAdminUsername(cmd.getUsername());
        tenant.setSmsSenderName(cmd.getSmsSenderId());
        Pharmacy pharmacy = pharmacyRepository.getOne(cmd.getTenantId());
        pharmacy.setTenant(tenant);
        pharmacyRepository.saveAndFlush(pharmacy);
        String hostForPharmacy = propertiesLoader.getHostForPharmacy();
        createDatabaseAndSeed(cmd, hostForPharmacy, "seed/afya_pharmacy.sql");
        return pharmacy.getPharmacyId();
    }

    public void createDatabaseAndSeedPharmacyOlap(final PersistFacilityCommand persistFacilityCommand, final String host, final String sqlScript) throws SQLException, ClassNotFoundException {
        String databaseName = getTenantId(persistFacilityCommand);
        databaseName = databaseName+"_olap";
        try {
            DatabaseUtil.createDynamicDatabase(host, databaseName, databaseUsername, databasePassword);
            SqlScriptRunner.runScriptNoAdditionalDataCreation(host, databaseName, databaseUsername, databasePassword,sqlScript);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createDatabaseAndSeed(final PersistFacilityCommand persistFacilityCommand, final String host, final String sqlScript) throws SQLException, ClassNotFoundException {
        createDatabaseAndSeed(persistFacilityCommand, host, sqlScript, null, null);
    }

    public void createDatabaseAndSeed(final PersistFacilityCommand persistFacilityCommand, final String host, final String sqlScript,
                                      final String sqlScripWithDelimiter, final String delimiter) throws SQLException, ClassNotFoundException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String databaseName = getTenantId(persistFacilityCommand);
                try {
                    DatabaseUtil.createDynamicDatabase(host, databaseName, databaseUsername, databasePassword);
                    SqlScriptRunner.runScript(host, databaseName, databaseUsername, databasePassword, persistFacilityCommand, sqlScript);
                    if(sqlScripWithDelimiter != null && delimiter != null)
                        SqlScriptRunner.runScript(host, databaseName, databaseUsername, databasePassword, persistFacilityCommand, sqlScripWithDelimiter, delimiter, true);
                    if (persistFacilityCommand.getFacilityType().toString().equals("PHARMACY")) {
                        createDatabaseAndSeedPharmacyOlap(persistFacilityCommand, host, "seed/afya_pharmacy_olap.sql");
                        DatabaseUtil.createTenantDatabasePharmacy(host, pharmacyTenantDatabase, databaseUsername, databasePassword, persistFacilityCommand);
                    }
                    if(persistFacilityCommand.getFacilityType().toString().equals("CLINIC")){
                        updatePracticeDetails(persistFacilityCommand);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
        return;
    }

    @Value("${CLINIC_BASE_URL}")
    private String CLINIC_BASE_URL;

    // Update provider details in DB
    private void updatePracticeDetails(PersistFacilityCommand cmd) {
        PersistPracticeCommand practiceCommand = new PersistPracticeCommand(cmd);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(mediaTypes);
        HttpEntity<?> requestEntity = new HttpEntity<Object>(httpHeaders);

        String responseEntity = restTemplate.postForObject(CLINIC_BASE_URL + "ospedale/clinicMaster/updatePracticeDetails", practiceCommand,String.class);
    }

    private void getPackageData(String token) {
        //String username = cmd.getPatientDto().getLoginPreference() == LoginPreference.EMAIL ? cmd.getPatientDto().getEmailId() : cmd.getPatientDto().getMobileNumber();
        //UserLogin fetchedUserLogin = userLoginRepository.findOne(username);

    }

    private void sendAccountConfirmationMailForPatient(String firstName, String lastName, String username, String token, boolean verified, String baseUrl) {
        if(baseUrl == null) {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = sra.getRequest();
            StringBuffer url = req.getRequestURL();
            baseUrl = url.substring(0, url.length() - req.getRequestURI().length() + req.getContextPath().length());
        }
        Map model = new HashMap<String, Object>();
        model.put("sentFrom", "admin@aafya.com");
        model.put("firstName", firstName);
        model.put("lastName", lastName);
        model.put("sendTo", username);
        model.put("confirmationCode", token);
        //if (verified) {
        model.put("verified", new Boolean(verified));
        model.put("baseUrl", baseUrl);
        //}
        mailService.sendMailPostPatientRegistration(model);
    }

    private void sendAccountConfirmationMailForPayer(String firstName, String lastName, String username, String token, boolean verified) {
        /*if(baseUrl == null) {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = sra.getRequest();
            StringBuffer url = req.getRequestURL();
            baseUrl = url.substring(0, url.length() - req.getRequestURI().length() + req.getContextPath().length());
        }*/
        Map model = new HashMap<String, Object>();
        model.put("sentFrom", "admin@aafya.com");
        model.put("firstName", firstName);
        model.put("lastName", lastName);
        model.put("sendTo", username);
        model.put("confirmationCode", token);
        //if (verified) {
        model.put("verified", new Boolean(verified));
        //model.put("baseUrl", baseUrl);
        //}

        mailService.sendMailPostPayerRegistration(model);

    }

    private void sendAccountConfirmationMailForFacility(String firstName, String lastName, String username, String token, String facilityId, boolean verified) {
        Map model = new HashMap<String, Object>();
        model.put("sentFrom", "admin@aafya.com");
        model.put("firstName", firstName);
        model.put("lastName", lastName);
        model.put("sendTo", username);
        model.put("confirmationCode", token);
        model.put("facility", facilityId);
        //if (verified) {
        model.put("verified", new Boolean(verified));
        //}
        mailService.sendMailPostFacilityRegistration(model);
    }

    private void sendMailPostSubscription(String firstName, String lastName, String username, String token, String facility, boolean trialSubscription, String baseUrl) {
        Map model = new HashMap<String, Object>();
        model.put("sentFrom", "admin@aafya.com");
        model.put("firstName", firstName);
        model.put("lastName", lastName);
        model.put("sendTo", username);
        model.put("confirmationCode", token);
        model.put("baseUrl",baseUrl);

        if (facility != null)
        {
            model.put("facility", facility);
        }
        if (trialSubscription)
        {
            model.put("trialSubscription", new Boolean(true));
        }
        mailService.sendMailPostSubscription(model);
    }

    @CommandHandler
    public String persistCivilIdData(PersistCivilIdDtoCommand persistCivilIdDtoCommand) throws SQLException {
        return DatabaseUtil.persistCivilData(hostPortal, portalDatabaseName, databaseUsername, databasePassword, persistCivilIdDtoCommand);
    }

    @CommandHandler
    public String persistPatientInsuranceDetails(PersistPatientInsuranceCommand persistPatientInsuranceCommand) throws SQLException {
        Connection connection = DatabaseUtil.getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
        DatabaseUtil.deleteAllPatientInsuranceDetailsForGivenAfyaId(connection, persistPatientInsuranceCommand.getAfyaId());
        connection = DatabaseUtil.getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
        return DatabaseUtil.insertPatientInsuranceDetailsForGivenAfyaId(connection, persistPatientInsuranceCommand.getInsuredPatientDtos(), persistPatientInsuranceCommand.getAfyaId());
    }

    @CommandHandler
    public String persistCorporatePatientDetails(PersistCorporatePatientCommand persistCorporatePatientCommand) throws SQLException {
        Connection connection = DatabaseUtil.getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
        DatabaseUtil.deleteCorporatePatientDetailsForGivenAfyaId(connection, persistCorporatePatientCommand.getAfyaId());
        connection = DatabaseUtil.getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
        return DatabaseUtil.insertCorporatePatientDetails(connection, persistCorporatePatientCommand);
    }

    @CommandHandler
    public String persistDoctor(PersistDoctorCommand persistDoctorCommand) throws SQLException {
        DoctorDto doctorDto = persistDoctorCommand.getDoctorDto();
        Set<Speciality> specialities = Sets.newHashSet();
        for (DoctorDto.SpecialityDto specialityDto : doctorDto.getSpecialities()) {
            Speciality speciality = getSpecilityByCode(specialityDto.specialityCode);
            if (speciality != null)
                specialities.add(speciality);
        }
        Doctor doctor = DoctorDto.setPropertiesFromDoctorDtoToDoctor(persistDoctorCommand.getDoctorDto(), specialities, persistDoctorCommand.getClinicId(), doctorRepository);
        doctor = doctorRepository.saveAndFlush(doctor);
        return doctor.getDoctorId().toString();
    }

    @CommandHandler
    @Transactional
    public boolean updateMemberSettings(UpdateMemberDetailsCommand updateMemberDetailsCommand) {
        PatientDto patientDto = updateMemberDetailsCommand.getPatientDto();
        Patient patient = patientRepository.findOne(patientDto.getAfyaId());
        if(UtilValidator.isEmpty(patient))
            return false;
        patient.setPropertiesToPatientEntity(patientDto);
        patientRepository.saveAndFlush(patient);
        return true;
    }

    @CommandHandler(commandName = "ACCOUNT_CONFIRMATION")
    public void confirmRegistration(GenericCommandMessage<String> commandMessage) {
        String username = commandMessage.getPayload();
        String confirmationCode = (String) commandMessage.getMetaData().get("confirmationCode");
        Preconditions.checkArgument(username != null, "Username cannot be empty.");
        Preconditions.checkArgument(confirmationCode != null, "Confirmation Code cannot be empty.");
        UserLogin userLogin = userLoginRepository.findOne(username);
        if (userLogin != null) {
            Preconditions.checkState(confirmationCode.equals(userLogin.getToken()), "Confirmation code does not match.");
            if (!userLogin.isVerified()) {
                userLogin.setVerified(true);
                userLogin.setToken(null);
                userLoginRepository.save(userLogin);

                if(userLogin.getFacilityId() == null){

                    ServletRequestAttributes sra = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
                    HttpServletRequest req = sra.getRequest();
                    StringBuffer url = req.getRequestURL();
                    final String baseUrl = url.substring(0, url.length() - req.getRequestURI().length() + req.getContextPath().length());

                   sendAccountConfirmationMailForPatient(userLogin.getFirstName(), userLogin.getLastName(), userLogin.getEmailId(), userLogin.getToken(), true, baseUrl);
                    Map<String,Object> smsMap =  new HashMap<String, Object>();
                    smsMap.put("firstName", userLogin.getFirstName());
                    smsMap.put("lastName", userLogin.getLastName());
                    smsMap.put("mobileNumber", userLogin.getMobileNumber());
                    smsMap.put("token", userLogin.getOtpToken());
                    //smsMap.put("isdCode", dto.getIsdCode());
                    smsMap.put("key", TemplateNames.PATIENT_REGISTRATION_SMS.name());
                    smsService.sendOTPSMS(smsMap);

                }
            }
        }

    }


    @CommandHandler(commandName = "GET_USER_TYPE_COMMAND")
    public UserLogin getUserType(GenericCommandMessage<String> commandMessage) {
        System.out.println(commandMessage);
        final String username = commandMessage.getPayload();
        String type = "";


        //UserLogin userLogin = userLoginRepository.findOne(username);

        UserLogin userLogin = userLoginRepository.findOne(new Specification<UserLogin>() {
            @Override
            public Predicate toPredicate(Root<UserLogin> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.<String>get("username"), username);
                // return null;
            }
        });

        if (userLogin == null) {
            return null;
        }
        return userLogin;
    }

    @CommandHandler(commandName = "API_LOGIN_COMMAND")
    public UserLogin apiLogin(GenericCommandMessage<String> commandMessage) {
        System.out.println(commandMessage);
        String username = commandMessage.getPayload();
        String password = (String) commandMessage.getMetaData().get("password");
        Preconditions.checkArgument(username != null, "Username cannot be empty.");
        Preconditions.checkArgument(password != null, "Password cannot be empty.");
        UserLogin userLogin = userLoginRepository.findOne(username);
        if (userLogin != null) {
            Preconditions.checkState(password.equals(userLogin.getPassword()), "Password does not match.");
            /*if (!userLogin.isVerified()) {
                userLogin.setVerified(true);
                userLogin.setToken(null);
                userLoginRepository.save(userLogin);
            }*/
        }
        return userLogin;
    }

    @CommandHandler(commandName = "OTP_CONFIRMATION")
    public String confirmOTP(GenericCommandMessage<String> commandMessage) {
        System.out.println(commandMessage);
        String username = commandMessage.getPayload();
        String otp_token = (String) commandMessage.getMetaData().get("otp_token");
        Preconditions.checkArgument(username != null, "Username cannot be empty.");
        Preconditions.checkArgument(otp_token != null, "Confirmation Code cannot be empty.");
        UserLogin userLogin = userLoginRepository.findOne(username);
        if (userLogin != null) {
            Preconditions.checkState(otp_token.equals(userLogin.getOtpToken()), "OTP code does not match.");
            if (!userLogin.isVerified()) {
                userLogin.setVerified(true);
                userLogin.setToken(null);
                userLoginRepository.save(userLogin);

                if (userLogin.getFacilityId() == null) {
                   sendAccountConfirmationMailForPatient(userLogin.getFirstName(), userLogin.getLastName(), userLogin.getEmailId(), userLogin.getToken(), true, null);
                    Map<String, Object> smsMap = new HashMap<String, Object>();
                    smsMap.put("firstName", userLogin.getFirstName());
                    smsMap.put("lastName", userLogin.getLastName());
                    smsMap.put("mobileNumber", userLogin.getMobileNumber());
                    smsMap.put("token", userLogin.getOtpToken());
                    //smsMap.put("isdCode", userLogin.getIsdCode());
                    smsMap.put("key", TemplateNames.PATIENT_REGISTRATION_SMS.name());
                    smsService.sendOTPSMS(smsMap);
                }
            }
            return "success";
        }
        return "success";
    }
    @CommandHandler(commandName = "REGENERATE_OTP")
    public String regenerateOTP(GenericCommandMessage<String> commandMessage) {
        System.out.println(commandMessage);
        String username = commandMessage.getPayload();
        Preconditions.checkArgument(username != null, "Username cannot be empty.");
        UserLogin userLogin = userLoginRepository.findOne(username);
        if (userLogin != null) {
            userLogin.reGenerateOTP();
            Map<String,Object> smsMap =  new HashMap<String, Object>();
            smsMap.put("firstName", userLogin.getFirstName());
            smsMap.put("lastName", userLogin.getLastName());
            smsMap.put("mobileNumber", userLogin.getMobileNumber());
            smsMap.put("token", userLogin.getOtpToken());
            //smsMap.put("isdCode", fetchedUserLogin.getIsdCode());
            SmsUtil.sendOTPSMS(smsMap);
            userLoginRepository.save(userLogin);
        }
        return "success";
    }

    private Speciality getSpecilityByCode(String specialityCode) {
        if (specialityCode == null)
            return null;
        return specialityRepository.findOne(specialityCode);
    }


    @CommandHandler(commandName = "GET_PACKAGES")
    public List<PackageDto> getPackagesForUser(GenericCommandMessage<String> commandMessage) {
        List<PackageDto> packageDtoList = new ArrayList<>();
        System.out.println(commandMessage);
        String username = commandMessage.getPayload();
        List<PricePackage> packagesList = pricePackageRepository.findAll();
        UserLogin user = userLoginRepository.findOne(username);
        Set<UserPackage> userPackages = user.getUserPackages();
        Date currentDatetime = new Date();
        for (PricePackage Package : packagesList) {
           PackageDto dto = new PackageDto();
            dto.populateFromDomain(user, Package);
            UserPackage userPackage = getUserPackage(user, dto);
            if (userPackage != null) {
                dto.setNumberOfUsers(userPackage.getNumberOfUsers());
                Date expiryDate = null;
                if(userPackage.getPriceOption() != null && userPackage.getSubscriptionDate() != null) {
                    boolean trialConsumed = isTrialConsumed(user, currentDatetime);
                    if(userPackage.getPricePackage().getPackageCategory().equals("SERVICE"))
                        expiryDate =  userPackage.getUser().getExpiryDate();    // KANNAN - 2015-11-05 - SINCE only one Service Package can be active at a time
                    else
                        expiryDate = addDays(userPackage.getSubscriptionDate(), userPackage.getExpiryDays(trialConsumed ? false : true));  // kannan - to force activated to 0 if expiry date crosses the limit
                    // set In-Trial-Period flag
                    if(trialConsumed == false){
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(userPackage.getSubscriptionDate());
                        cal.add(Calendar.DATE, userPackage.getPricePackage().getTrialPeriodDays()); //minus number would decrement the days
                        Date trialEndDate = cal.getTime();
                        dto.setIsTrialExpired(trialEndDate.compareTo(currentDatetime) <= 0 ? true : false);
                    }else {
                        dto.setIsTrialExpired(true);
                    }
                }
                if(expiryDate != null && expiryDate.compareTo(currentDatetime) < 0)
                    dto.setActivated(0);
                else
                    dto.setActivated(userPackage.getActivated());
                if(dto.getActivated() == 1){
                    // get unpaid notifications/training services from history
                    final UserLogin finalUser = user;
                    List<UserNotificationSubscriptionHistory> unpaidSubscriptions = userSubscriptionHistoryRepository.findAll(new Specification<UserNotificationSubscriptionHistory>() {
                        @Override
                        public Predicate toPredicate(Root<UserNotificationSubscriptionHistory> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                            Join<UserNotificationSubscriptionHistory, UserLogin> userLoginJoin = root.join("user");
                            //Join<UserNotificationSubscriptionHistory, PaymentGatewayTransaction> paymentGatewayTransactionJoin = root.join("paymentGatewayTransaction");
                            return criteriaBuilder.and(criteriaBuilder.equal(userLoginJoin.get("username"), finalUser.getUsername()),
                                    criteriaBuilder.isNull(root.get("paymentGatewayTransaction")));
                        }
                    });
                    if(dto.getPendingPayment() == null)
                        dto.setPendingPayment(new BigDecimal(0));
                    BigDecimal pendingPayment = dto.getPendingPayment();
                    for(UserNotificationSubscriptionHistory sub : unpaidSubscriptions){
                        pendingPayment = pendingPayment.add(new BigDecimal( sub.getAmount()));
                    }
                    dto.setPendingPayment(pendingPayment);
                }
                dto.setPriceOption(userPackage.getPriceOption());
                dto.setAmount(userPackage.getAmount());
                dto.setSubscriptionDate(userPackage.getSubscriptionDate()); // kannan -- setting to userPackage.subscriptionDate instead of user.subscriptionDate
            }
            dto.setTotalServices(Package.getTotalServices());
            // dto.setSubscriptionDate(user.getSubscriptionDate());     // kannan -- setting to userPackage.subscriptionDate instead of user.subscriptionDate
            packageDtoList.add(dto);
        }
        return packageDtoList;
    }

    public static Date addDays(Date d, int days)
    {
        if(d != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            c.add(Calendar.DATE, days);
            // Date dt = new Date(d.getTime());
            // dt.setTime(dt.getTime() + days * 1000 * 60 * 60 * 24);
            return  c.getTime();
        }
        return d;
    }

    @CommandHandler
    @Transactional
    public String registerCarePayerCommand(RegisterCarePayerCommand registerCarePayerCommand) throws SQLException {
        UserLogin payerUserLogin = new UserLogin();
        UserLoginDto userLoginDto = registerCarePayerCommand.getUserLoginDto();
        userLoginDto.setPropertiesToEntity(payerUserLogin);
        // patientRepository.saveAndFlush(patient);

        String username = userLoginDto.getLoginPreference().equals(LoginPreference.EMAIL.toString()) ? userLoginDto.getEmailId() : userLoginDto.getMobileNumber();

        // delete if the user had another registration just before this step
        String oldRegUsername = registerCarePayerCommand.getUserLoginDto().getOldRegUsername();
        if(oldRegUsername != null){
            UserLogin userToDelete = userLoginRepository.findOne(oldRegUsername);
            if(userToDelete.isVerified() == false
                    && (!userToDelete.getFirstName().equals(userLoginDto.getFirstName()))
                    || !(userToDelete.getMiddleName() == null ? userLoginDto.getMiddleName() == null :  userToDelete.getMiddleName().equals(userLoginDto.getMiddleName()))
                    || !userToDelete.getLastName().equals(userLoginDto.getLastName())
                    || !userToDelete.getEmailId().equals(userLoginDto.getEmailId())
                    || !userToDelete.getMobileNumber().equals(userLoginDto.getMobileNumber())
                    || !userToDelete.getPassword().equals(userLoginDto.getPassword())
                    || !userToDelete.getLoginPreference().equals(userLoginDto.getLoginPreference())
                    || !userToDelete.getPayerOrganizationName().equals(userLoginDto.getPayerOrganizationName())
                    || !userToDelete.getPayerOfficeNumber().equals(userLoginDto.getPayerOfficeNumber())
                    || !userToDelete.getPayerType().equals(userLoginDto.getPayerType())){
                userLoginRepository.delete(userToDelete);
                userLoginRepository.flush();
            }
        }

        UserLogin fetchedUserLogin = userLoginRepository.findOne(username);

        if(fetchedUserLogin != null && !fetchedUserLogin.getVerified()) {
            if (fetchedUserLogin.getLoginPreference().equals(LoginPreference.MOBILE.toString())) {
                Map<String,Object> smsMap =  new HashMap<String, Object>();
                smsMap.put("firstName", fetchedUserLogin.getFirstName());
                smsMap.put("lastName", fetchedUserLogin.getLastName());
                smsMap.put("mobileNumber", fetchedUserLogin.getMobileNumber());
                smsMap.put("token", fetchedUserLogin.getOtpToken());
                //smsMap.put("isdCode", fetchedUserLogin.getIsdCode());
                SmsUtil.sendOTPSMS(smsMap);
            }
            else if (fetchedUserLogin.getLoginPreference().equals(LoginPreference.EMAIL.toString())) {
                //sendAccountConfirmationMailForFacility(fetchedUserLogin.getFirstName(), fetchedUserLogin.getLastName(), fetchedUserLogin.getEmailId(), fetchedUserLogin.getToken(), fetchedUserLogin.getTenantId(), fetchedUserLogin.isVerified());
            sendAccountConfirmationMailForPayer(fetchedUserLogin.getFirstName(), fetchedUserLogin.getLastName(), fetchedUserLogin.getEmailId(), fetchedUserLogin.getToken(), fetchedUserLogin.isVerified());
            }
            Preconditions.checkState((fetchedUserLogin.getVerified()) == true, "NotVerified");
        }

        if (userLoginDto.getLoginPreference().equals(LoginPreference.EMAIL.toString())) {
            Preconditions.checkState((fetchedUserLogin == null), " This email Id  " + username + " already exists");
        } else {
            Preconditions.checkState((fetchedUserLogin == null), " This mobile number " + username + " already exists");
        }
        final UserLogin userLogin = new UserLogin(username, userLoginDto.getPassword(), UserType.PAYER, null, null);
        userLogin.setFirstName(userLoginDto.getFirstName());
        userLogin.setMiddleName(userLoginDto.getMiddleName());
        userLogin.setLastName(userLoginDto.getLastName());
        userLogin.setEmailId(userLoginDto.getEmailId());
        userLogin.setMobileNumber(userLoginDto.getMobileNumber());
        userLogin.setLoginPreference(userLoginDto.getLoginPreference().toString());
        userLogin.setPayerType(userLoginDto.getPayerType());
        userLogin.setPayerOrganizationName(userLoginDto.getPayerOrganizationName());
        userLogin.setPayerOfficeNumber(userLoginDto.getPayerOfficeNumber());
        userLogin.setVerified(false);

        if (userLogin.getLoginPreference().equals(LoginPreference.MOBILE.toString())) {
            Map<String,Object> smsMap =  new HashMap<String, Object>();
            smsMap.put("firstName", userLogin.getFirstName());
            smsMap.put("lastName", userLogin.getLastName());
            smsMap.put("mobileNumber", userLogin.getMobileNumber());
            smsMap.put("token", userLogin.getOtpToken());
            //smsMap.put("isdCode", null);
            SmsUtil.sendOTPSMS(smsMap);
        }

        userLoginRepository.saveAndFlush(userLogin);
        if (userLoginDto.getLoginPreference().equals(LoginPreference.EMAIL.toString())) {
            //sendAccountConfirmationMailForFacility(userLoginDto.getFirstName(), userLoginDto.getLastName(), userLoginDto.getEmailId(), userLogin.getToken(),userLogin.getTenantId(),userLogin.isVerified());
            sendAccountConfirmationMailForPayer(userLoginDto.getFirstName(), userLoginDto.getLastName(), userLoginDto.getEmailId(), userLogin.getToken(), userLogin.isVerified());
        }
        return userLogin.toString();
    }

    @CommandHandler(commandName = "RESEND_EMAIL")
    public String resendEmail(GenericCommandMessage<String> commandMessage) {
        System.out.println(commandMessage);
        String username = commandMessage.getPayload();
        Preconditions.checkArgument(username != null, "Username cannot be empty.");
        UserLogin userLogin = userLoginRepository.findOne(username);
        if (userLogin != null) {
            if(userLogin.isVerified() == false) {

                if(userLogin.getTenantId() == null && userLogin.getPayerType() == null) {
                    sendAccountConfirmationMailForPatient(userLogin.getFirstName(), userLogin.getLastName(), userLogin.getEmailId(), userLogin.getToken(),userLogin.isVerified(), null);
                } else if(userLogin.getTenantId() == null && userLogin.getPayerType() != null){
                    sendAccountConfirmationMailForPayer(userLogin.getFirstName(), userLogin.getLastName(), userLogin.getEmailId(), userLogin.getToken(), userLogin.isVerified());
                } else {
                    sendAccountConfirmationMailForFacility(userLogin.getFirstName(), userLogin.getLastName(), userLogin.getEmailId(), userLogin.getToken(),userLogin.getTenantId(),userLogin.isVerified());
                }
                return "success";
            }else{
                return "verified";
            }
        }
        return "error";
    }

    @CommandHandler(commandName = "UPDATE_LOGGED_IN_TIMESTAMP")
    public String updateloggedInTimestamp(GenericCommandMessage<String> commandMessage) {
        System.out.println(commandMessage);
        String username = commandMessage.getPayload();
        Preconditions.checkArgument(username != null, "Username cannot be empty.");
        UserLogin userLogin = userLoginRepository.findOne(username);
        if (userLogin != null) {
            if(userLogin.isVerified() == true) {
                Date dtNow = new Date();
                userLogin.setLastLoggedInTimestamp(dtNow);
                userLoginRepository.save(userLogin);
                return "success";
            }
        }
        return "error";
    }
}
