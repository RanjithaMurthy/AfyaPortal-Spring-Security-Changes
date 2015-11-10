package com.afya.portal.application;

import com.afya.portal.domain.model.Tenant;
import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.external.PropertiesLoader;
import com.afya.portal.util.DatabaseUtil;
import com.afya.portal.util.UtilValidator;
import com.nzion.dto.UserLoginDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.nthdimenzion.common.service.JpaRepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * Created by Mohan Sharma on 7/13/2015.
 */
@Component
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PortalFacilityCommandHandler {

    private PropertiesLoader propertiesLoader;
    private String databaseUsername;
    private String databasePassword;
    private String portalDatabaseName;
    private String hostPortal;
    private SimpleJpaRepository<UserLogin, String> userLoginRepository;
    private SimpleJpaRepository<Tenant, String> tenantRepository;

    @Autowired
    public PortalFacilityCommandHandler(PropertiesLoader propertiesLoader, JpaRepositoryFactory jpaRepositoryFactory){
        this.propertiesLoader = propertiesLoader;
        databaseUsername = propertiesLoader.getDatabaseUsername();
        databasePassword = propertiesLoader.getDatabasePassword();
        portalDatabaseName = propertiesLoader.getPortalDatabaseName();
        hostPortal = propertiesLoader.getHostForPortal();
        userLoginRepository = jpaRepositoryFactory.getCrudRepository(UserLogin.class);
        tenantRepository = jpaRepositoryFactory.getCrudRepository(Tenant.class);
    }

    @CommandHandler
    @Transactional
    public String persistUserLoginFacilityAssociation(PersistUserTenantAssocCommand persistUserTenantAssocCommand) throws SQLException {
        return DatabaseUtil.persistUserLoginFacilityAssociation(hostPortal, databaseUsername, databasePassword, portalDatabaseName, persistUserTenantAssocCommand);
    }

    @CommandHandler
    @Transactional
    public String persistUserLoginFromTenant(PersistUserLoginFromTenantCommand persistUserLoginFromTenantCommand) throws SQLException {
        return DatabaseUtil.persistUserLoginFromTenant(hostPortal, databaseUsername, databasePassword, portalDatabaseName, persistUserLoginFromTenantCommand.getUserLoginDto());
    }

    @CommandHandler
    @Transactional
    public String persistAuthorityForUserCommand(PersistAuthorityForUserCommand persistAuthorityForUserCommand) throws SQLException {
        DatabaseUtil.deleteExistingAuthorizationForUserLogin(hostPortal, databaseUsername, databasePassword, portalDatabaseName, persistAuthorityForUserCommand.getUserLoginDto());
        return DatabaseUtil.persistAuthorizationForUserLogin(hostPortal, databaseUsername, databasePassword, portalDatabaseName, persistAuthorityForUserCommand.getUserLoginDto(), persistAuthorityForUserCommand.getAuthority());
    }

    @CommandHandler
    @Transactional
    public boolean modifyUserLoginCommand(ModifyUserLoginCommand modifyUserLoginCommand) throws SQLException {
        UserLoginDto userLoginDto = modifyUserLoginCommand.getUserLoginDto();
        if(UtilValidator.isNotEmpty(userLoginDto)){
            UserLogin userLogin = userLoginRepository.findOne(userLoginDto.getUsername());
            if(UtilValidator.isNotEmpty(userLogin)){
                userLoginDto.setPropertiesToEntity(userLogin);
                userLoginRepository.saveAndFlush(userLogin);
                return true;
            }
        }
        return false;
    }

    @CommandHandler
    @Transactional
    public boolean updateSMSCountForGivenTenant(UpdateSmsCountTriggeredFromTenantCommand updateSmsCountTriggeredFromTenantCommand) throws SQLException {
        Tenant tenant =  tenantRepository.findOne(updateSmsCountTriggeredFromTenantCommand.getTenantId());
        if(UtilValidator.isNotEmpty(tenant)){
            tenant.setSentSmsCount(tenant.getSentSmsCount()+1);
            tenantRepository.saveAndFlush(tenant);
            return true;
        }
        return false;
    }

    @CommandHandler
    @Transactional
    public boolean updateSMSSenderInGivenTenantCommand(UpdateSMSSenderInGivenTenantCommand updateSMSSenderInGivenTenantCommand) throws SQLException {
        Tenant tenant =  tenantRepository.findOne(updateSMSSenderInGivenTenantCommand.getTenantId());
        if(UtilValidator.isNotEmpty(tenant)){
            if(UtilValidator.isNotEmpty(updateSMSSenderInGivenTenantCommand.getSmsSenderName())) {
                tenant.setSmsSenderName(updateSMSSenderInGivenTenantCommand.getSmsSenderName());
                tenantRepository.saveAndFlush(tenant);
                return true;
            }
        }
        return false;
    }

    @CommandHandler
    @Transactional
    public boolean updateSMSSenderVerifiedInGivenTenantCommand(UpdateSMSSenderVerifiedInGivenTenantCommand updateSMSSenderVerifiedInGivenTenantCommand) throws SQLException {
        Tenant tenant =  tenantRepository.findOne(updateSMSSenderVerifiedInGivenTenantCommand.getTenantId());
        if(UtilValidator.isNotEmpty(tenant)){
            tenant.setSmsSenderNameVerified(true);
            tenantRepository.saveAndFlush(tenant);
            return true;
        }
        return false;
    }
}
