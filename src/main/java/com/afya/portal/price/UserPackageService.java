package com.afya.portal.price;

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
public class UserPackageService implements ICrudEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    // additional fields
    private int activated;
    private int numberOfHours;
    private int numberOfSMS;
    private double rate;
    private double amount;
    private int packs;
    private int expiryDays;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PACKAGE_ID")
    PricePackage pricePackage;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USERNAME")
    UserLogin user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SERVICE_ID")
    PricePackageService service;
    // @Temporal(TemporalType.DATE)
    private Date expiryDate;
    // @Temporal(TemporalType.DATE)
    private Date subscriptionDate;
}
