package com.afya.portal.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by USER on 29-Oct-15.
 */
@Entity
@Getter
@Setter
public class PaymentGatewayTransaction implements ICrudEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long paymentId;
    // TO DO - other fields to be added from the (existing) table
    private String transactionType;
    private BigDecimal transactionAmount;
    private Date transactionTimestamp;
    private String isysTrackingRef;
    private String isysMerchantRef;
    private String isysPaymentStatus;
    private Date isysPaymentStatusTimestamp;
    private String isysPaymentStatusHttpRespStatus;

    private String username;

    private String afyaId;
    private String appointmentClinicId;
    private String appointmentDoctorId;
    private String appointmentSlot;

    private String subscriptionId;
    private String pharmacyOrderId;
    private String packageId;

    private BigDecimal processingFees;
    private String payerType;
    private String paymentChannel;
    private BigDecimal masterProcessingPercentage;
    private BigDecimal masterProcessingFixedValue;
}
