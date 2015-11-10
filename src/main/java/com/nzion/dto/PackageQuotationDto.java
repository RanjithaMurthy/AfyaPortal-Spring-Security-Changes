package com.nzion.dto;

import lombok.Data;

/**
 * Created by USER on 17-Sep-15.
 */
@Data
public class PackageQuotationDto {
    private String membershipId;
    private String memberName;
    private String memberType;
    private String referredBy;
    private String date;
    private String packName;
    private String packageId;
    private String subscriptionPeriod;
    private String paymentAmount;
    private String paymentCurrency;
    private String subscriptionStartDate;
    private String subscriptionEndDate;
    private String subscriptionDays;
    private String smartSolutions;
    private String smartServicesPatient;
    private String smartServicesCareProviders;
    private String notificationServicePacks;
    private String subscriptionStartDateFormated;
    private String subscriptionEndDateFormated;
    private String dateFormated;
}
