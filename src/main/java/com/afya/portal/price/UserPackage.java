package com.afya.portal.price;

import com.afya.portal.domain.model.security.UserLogin;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Asus on 7/30/2015.
 */
@Entity
@Getter
@Setter
public class UserPackage implements ICrudEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    // additional fields
    private int activated;
    private int numberOfUsers;
    private String priceOption;
    private double amount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PACKAGE_ID")
    PricePackage pricePackage;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USERNAME")
    UserLogin user;
    // @Temporal(TemporalType.DATE)
    private Date subscriptionDate;

    public int getExpiryDays(boolean includeTrialPeriod){
        if(priceOption.equals("ANNUALLY"))
            return 365 + (includeTrialPeriod ? pricePackage.getTrialPeriodDays() : 0);
        else if(priceOption.equals("HALF_YEARLY"))
            return 180 + (includeTrialPeriod ? pricePackage.getTrialPeriodDays() : 0);
        else
            return 0;
    }

    public boolean isSubscriptionActive(){
        if(this.subscriptionDate == null || this.activated == 0)
            return false;
        Calendar c = Calendar.getInstance();
        /*c.setTime(this.subscriptionDate);
        c.add(Calendar.DATE, this.getExpiryDays());
        return c.getTime().compareTo(new Date()) >= 0;*/
        c.setTime(this.getUser().getExpiryDate());      // THIS SHOULD WORK, SINCE ONLY ONE USER PACKAGE CAN BE ACTIVE AT A TIME, CONSIDER MOVING THIS TO LoginUser CLASS
        return c.getTime().compareTo(new Date()) >= 0;
    }
}
