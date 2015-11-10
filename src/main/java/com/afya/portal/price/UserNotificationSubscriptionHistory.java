package com.afya.portal.price;

import com.afya.portal.domain.model.PaymentGatewayTransaction;
import com.afya.portal.domain.model.security.UserLogin;
import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Asus on 7/30/2015.
 */
@Entity
@Getter
@Setter
public class UserNotificationSubscriptionHistory implements ICrudEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    // additional fields
    private double rate;
    private double amount;
    private int packs;
    private int numberOfSMS;
    private boolean expired;
    private int expiryDays;
    private int numberOfHours;      // added kannan
    // @Temporal(TemporalType.DATE)
    private Date expiryDate;
    // @Temporal(TemporalType.DATE)
    private Date subscriptionDate;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PACKAGE_ID")
    PricePackage pricePackage;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USERNAME")
    UserLogin user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SERVICE_ID")
    PricePackageService service;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    PaymentGatewayTransaction paymentGatewayTransaction;

    private Date createdTxTimestamp;
    private Date updatedTxTimestamp;


    public void populate(UserPackageService service, Date currentDatetime) {
        rate = service.getRate();
        amount = service.getAmount();
        packs = service.getPacks();
        numberOfSMS = service.getNumberOfSMS();
        numberOfHours = service.getNumberOfHours(); // added kannan
        expiryDays = service.getExpiryDays();
        expiryDate = service.getExpiryDate();
        subscriptionDate = service.getSubscriptionDate();
        setPricePackage(service.getPricePackage());
        setUser(service.getUser());
        setService(service.getService());
        setCreatedTxTimestamp(currentDatetime);
        if(getUpdatedTxTimestamp() == null)
            setUpdatedTxTimestamp(currentDatetime);
    }
}
