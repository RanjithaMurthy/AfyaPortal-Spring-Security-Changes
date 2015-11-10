package com.afya.portal.membercareprovider.domain;

import com.afya.portal.domain.model.clinic.Clinic;
import lombok.Getter;
import lombok.Setter;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.nthdimenzion.common.crud.ICrudEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/3/15
 * Time: 5:53 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Getter
@Setter
public class Network extends AbstractAnnotatedAggregateRoot implements ICrudEntity {

    @Id
    private String id;

    @OneToOne
    private Clinic clinic;

    @OneToOne
    private Clinic toClinic;

    @Temporal(TemporalType.DATE)
    private Date createdOn;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    private STATUS status;

    public Network(){}

    public Network(Clinic clinic,Clinic toClinic, String message){
        id = UUID.randomUUID().toString();
        this.clinic = clinic;
        this.toClinic = toClinic;
        this.message = message;
        this.createdOn = new Date();
        this.status = STATUS.PENDING;
    }


    public enum STATUS{
        PENDING("Pending"),ACCEPTED("Accepted"),REJECTED("Rejected"),BLOCKED("Blocked"),UNBLOCKED("Unblocked");
        private String name;
        STATUS(String name){
          this.name = name;
        }
        public String getName(){
            return this.name;
        }
    }


}
