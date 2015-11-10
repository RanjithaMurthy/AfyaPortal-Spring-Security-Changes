package com.afya.portal.price;

import com.afya.portal.domain.model.PaymentGatewayTransaction;
import com.afya.portal.domain.model.security.UserLogin;
import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by USER on 04-Nov-15.
 */
@Entity
@Getter
@Setter
public class UserPackageHistory implements ICrudEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    // additional fields
    private int numberOfUsers;
    private String priceOption;
    private double amount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PACKAGE_ID")
    private PricePackage pricePackage;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USERNAME")
    private UserLogin user;
    // @Temporal(TemporalType.DATE)
    private Date subscriptionDate;
    private Date expiryDate;
    private boolean isTrial;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private PaymentGatewayTransaction paymentGatewayTransaction;

    private Date createdTxTimestamp;
    private Date udpatedTxTimestamp;

    public static UserPackageHistory create(UserPackage userPackage, PaymentGatewayTransaction paymentGatewayTransaction, Date currentDatetime){
        UserPackageHistory uph = new UserPackageHistory();
        uph.setNumberOfUsers(userPackage.getNumberOfUsers());
        uph.setPriceOption(userPackage.getPriceOption());
        uph.setAmount(userPackage.getAmount());

        uph.setPricePackage(userPackage.getPricePackage());

        UserLogin userLogin = userPackage.getUser();
        uph.setUser(userLogin);
        uph.setSubscriptionDate(userLogin.getSubscriptionDate());
        uph.setExpiryDate(userLogin.getExpiryDate());
        uph.setTrial(userLogin.isTrail());

        uph.setPaymentGatewayTransaction(paymentGatewayTransaction);

        uph.setCreatedTxTimestamp(currentDatetime);
        uph.setUdpatedTxTimestamp(currentDatetime);

        return uph;
    }
}
