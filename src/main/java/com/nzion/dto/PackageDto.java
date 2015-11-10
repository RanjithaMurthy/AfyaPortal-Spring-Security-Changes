package com.nzion.dto;

import com.afya.portal.domain.model.security.UserLogin;
import com.afya.portal.price.PricePackage;
import com.afya.portal.price.PricePackageService;
import com.google.common.collect.Sets;
import lombok.Data;

import javax.print.attribute.standard.PrinterIsAcceptingJobs;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mohan Sharma on 4/1/2015.
 */
@Data
public class PackageDto {
    private String packageId;
    private String packageName;
    private String solutionName;
    private String packageType;
    private String packageCategory;
    private int   totalServices;
    private double halfYearlyPricePerUom;
    private double annuallyPricePerUom;
    private double amount;
    private String paymentFrequency;
    private int  numberOfUsers;
    private String priceOption;
    private int activated;
    private String trail;
    private Date subscriptionDate;
    private int trialPeriodDays;
    private BigDecimal pendingPayment;
    private Boolean isTrialExpired;

    private Set<PackageServiceDto> services = Sets.newHashSet();;

    public void populateFromDomain(UserLogin user,PricePackage Package) {
        setPackageId(Package.getPackageId());
        setPackageName(Package.getPackageName());
        setSolutionName(Package.getSolutionName());
        setPackageType(Package.getPackageType());
        setPackageCategory(Package.getPackageCategory());
        setTrialPeriodDays(Package.getTrialPeriodDays());
        if (Package.getHalfYearlyPricePerUOM() != null)
            setHalfYearlyPricePerUom(Package.getHalfYearlyPricePerUOM());
        if (Package.getAnnuallyPricePerUOM() != null)
            setAnnuallyPricePerUom(Package.getAnnuallyPricePerUOM());
        if (Package.getServices() != null) {
            int total_services = 0;
            for(PricePackageService service : Package.getServices()) {
                // total_services += service.getServiceItems().size();
                String serviceType = service.getServiceType();
                if(serviceType.equals("TOP_LEVEL") || serviceType.equals("PROVIDER") || serviceType.equals("PATIENT"))
                    total_services += 1;
                            PackageServiceDto serviceDto = new PackageServiceDto();
                serviceDto.populateFromDomain(user,this,service);
                services.add(serviceDto);
            }
            if(Package.getPackageCategory().equals("SERVICE")){
                total_services += 2;    // Accounting for Training and Notification Packages
            }
            Package.setTotalServices(total_services);
        }
    }
}
