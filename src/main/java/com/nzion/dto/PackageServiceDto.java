package com.nzion.dto;

import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.price.PricePackage;
import com.afya.portal.price.PricePackageService;
import com.afya.portal.price.UserPackageService;
import lombok.Data;
import org.nthdimenzion.utils.SessionManager;

/**
 * Created by Mohan Sharma on 4/1/2015.
 */
@Data
public class PackageServiceDto {
    private String serviceId;
    private String serviceName;
    private String serviceType;
    private String serviceDisplayName;
    private int serviceSequence;
    private int numberOfHours;
    private int packs;
    private String currency;
    private double rate;
    private double amount;
    private int expireDays;
    private int activated;
    private int numberOfSMS;

    private UserPackageService getUserPackageService(UserLogin user,PackageDto packageDto) {
        for(UserPackageService userPackageService : user.getUserPackageServices()) {
            if (userPackageService.getPricePackage().getPackageId().equals(packageDto.getPackageId()) &&
                    (userPackageService.getService().getServiceId().equals(getServiceId()))) {
                return userPackageService;
            }
        }
        return null;
    }

    public void populateFromDomain(UserLogin user, PackageDto packageDto, PricePackageService service) {
      serviceId = service.getServiceId();
      serviceName = service.getServiceName();
      serviceType = service.getServiceType();
      serviceDisplayName = service.getServiceDisplayName();
      serviceSequence = service.getServiceSequence();
      currency = service.getCurrency();
      numberOfSMS = service.getNumberOfSMS();
      rate = service.getRate();
      expireDays = service.getExpireDays();
      UserPackageService userPackageService = getUserPackageService(user,packageDto);
      if (userPackageService != null) {
          amount = userPackageService.getAmount();
          rate = userPackageService.getRate();
          packs = userPackageService.getPacks();
          numberOfHours = userPackageService.getNumberOfHours();
          activated = userPackageService.getActivated();
      }
    }
}
