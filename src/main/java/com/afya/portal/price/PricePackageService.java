package com.afya.portal.price;

import com.afya.portal.domain.model.doctor.Speciality;
import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Asus on 7/30/2015.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "serviceId")
public class PricePackageService implements ICrudEntity{
    @Id
    String serviceId;
    String serviceName;
    String serviceDisplayName;
    String serviceType; // PATIENT/TOP_LEVEL/PROVIDER/TRAINING
    String currency;
    int    serviceSequence;
    int    numberOfSMS;
    double rate; // For Training service
    int expireDays;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "price_service_item_assoc", joinColumns = {
            @JoinColumn(name = "SERVICE_ID")},
            inverseJoinColumns = { @JoinColumn(name = "SERVICE_ITEM_ID") })
    private Set<PricePackageServiceItem> serviceItems = Sets.newHashSet();
}
