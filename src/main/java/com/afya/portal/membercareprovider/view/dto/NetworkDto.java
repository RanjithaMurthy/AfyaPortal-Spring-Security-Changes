package com.afya.portal.membercareprovider.view.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/4/15
 * Time: 12:00 PM
 * To change this template use File | Settings | File Templates.
 */

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class NetworkDto {
    private String networkId;
    private Date createdOn;
    private String message;
    private String status;
    private String facilityId;
    private String facilityName;
    private String address;
    private String firstName;
    private String lastName;
    private String keyPersonFirstName;
    private String keyPersonLastName;
    private String email;
}
