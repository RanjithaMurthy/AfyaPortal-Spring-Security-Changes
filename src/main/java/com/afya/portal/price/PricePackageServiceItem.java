package com.afya.portal.price;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Asus on 7/30/2015.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "serviceItemId")
public class PricePackageServiceItem implements ICrudEntity{
    @Id
    String serviceItemId;
    String serviceItemName;
}
