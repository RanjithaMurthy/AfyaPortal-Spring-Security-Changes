package com.afya.portal.price;

import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Asus on 7/30/2015.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "packageId")
public class PricePackage implements ICrudEntity{
    @Id
    private String packageId;
    private String packageName;
    private String solutionName;
    private String packageType;
    private String packageCategory;
    private String uom;
    private Double halfYearlyPricePerUOM;
    private Double annuallyPricePerUOM;
    private String currency;
    private int totalServices;
    @Column(columnDefinition =  "int default 30")
    private int trialPeriodDays;
   /* @OneToMany(mappedBy = "pricePackage")
    private Set<UserPackage> userPackages = Sets.newHashSet();
    public void addUserPackage(UserPackage userPackage) {
        this.userPackages.add(userPackage);
    }
    */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "price_package_service_assoc", joinColumns = {
            @JoinColumn(name = "PACKAGE_ID")},
            inverseJoinColumns = { @JoinColumn(name = "SERVICE_ID") })
    private Set<PricePackageService> services = Sets.newHashSet();



}
