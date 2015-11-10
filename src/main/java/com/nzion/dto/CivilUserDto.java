package com.nzion.dto;

import com.google.gson.*;
import lombok.Getter;
import lombok.ToString;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mohan Sharma on 4/2/2015.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class CivilUserDto{
    private String civilId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String endMostName;
    private String nationality;
    private String gender;
    private Date dateOfBirth;
    private String bloodGroup;
    private String rh;
    private String cardType;
    private Date cardIssuedDate;
    private Date cardExpiryDate;
    private String emailId;
    private String mobileNumber;
    private String telephoneNumber;
    private String address;
    private String city;
    private String state;
    private String country;

    public CivilUserDto(){}

    public CivilUserDto(String civilId, String firstName, String middleName, String lastName, String endMostName, String nationality, String gender, Date dateOfBirth, String bloodGroup, String rh, String emailId, String mobileNumber, String telephoneNumber, String address, String city, String state, String country) {
        this.civilId = civilId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.endMostName = endMostName;
        this.nationality = nationality;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.bloodGroup = bloodGroup;
        this.rh = rh;
        this.emailId = emailId;
        this.mobileNumber = mobileNumber;
        this.telephoneNumber = telephoneNumber;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    public void setCivilId(String civilId) {
        this.civilId = civilId;
    }

    public class CustomJsonDeserializer implements JsonDeserializer<CivilUserDto> {

        @Override
        public CivilUserDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            CivilUserDto civilUserDto = new CivilUserDto();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if(jsonObject.get("CivilID") != null && !jsonObject.get("CivilID").isJsonNull())
                civilUserDto.civilId = jsonObject.get("CivilID").getAsString();
            if(jsonObject.get("LatinNamePart1") != null && !jsonObject.get("LatinNamePart1").isJsonNull())
                civilUserDto.firstName = jsonObject.get("LatinNamePart1").getAsString();
            if(jsonObject.get("LatinNamePart2") != null && !jsonObject.get("LatinNamePart2").isJsonNull())
                civilUserDto.middleName = jsonObject.get("LatinNamePart2").getAsString();
            if(jsonObject.get("LatinNamePart3") != null && !jsonObject.get("LatinNamePart3").isJsonNull())
                civilUserDto.lastName = jsonObject.get("LatinNamePart3").getAsString();
            if(jsonObject.get("LatinNamePart4") != null && !jsonObject.get("LatinNamePart4").isJsonNull())
                civilUserDto.endMostName = jsonObject.get("LatinNamePart4").getAsString();
            if(jsonObject.get("Sex") != null && !jsonObject.get("Sex").isJsonNull())
                civilUserDto.gender = jsonObject.get("Sex").getAsString();
            if(jsonObject.get("Nationality") != null && !jsonObject.get("Nationality").isJsonNull())
                civilUserDto.nationality = jsonObject.get("Nationality").getAsString();
            try {
                if(!(jsonObject.get("DateOfBirth").equals("")) && jsonObject.get("DateOfBirth") != null && !jsonObject.get("DateOfBirth").isJsonNull())
                    civilUserDto.dateOfBirth = dateFormat.parse(jsonObject.get("DateOfBirth").getAsString());
                if(jsonObject.get("CardIssued") != null && !jsonObject.get("CardIssued").isJsonNull())
                    civilUserDto.cardIssuedDate = dateFormat.parse(jsonObject.get("CardIssued").getAsString());
                if(jsonObject.get("CardExpires") != null && !jsonObject.get("CardExpires").isJsonNull())
                    civilUserDto.cardExpiryDate = dateFormat.parse(jsonObject.get("CardExpires").getAsString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(jsonObject.get("CardType") != null && !jsonObject.get("CardType").isJsonNull())
                civilUserDto.cardType = jsonObject.get("CardType").getAsString();
            if(jsonObject.get("BloodGroup") != null && !jsonObject.get("BloodGroup").isJsonNull())
                civilUserDto.bloodGroup = jsonObject.get("BloodGroup").getAsString();
            if(jsonObject.get("RH") != null && !jsonObject.get("RH").isJsonNull())
                civilUserDto.rh = jsonObject.get("RH").getAsString();
            if(jsonObject.get("Country") != null && !jsonObject.get("Country").isJsonNull())
                civilUserDto.country = jsonObject.get("Country").getAsString();
            if(jsonObject.get("Governorate") != null && !jsonObject.get("Governorate").isJsonNull())
                civilUserDto.state = jsonObject.get("Governorate").getAsString();
            if(jsonObject.get("City") != null && !jsonObject.get("City").isJsonNull())
                civilUserDto.city = jsonObject.get("City").getAsString();
            if(jsonObject.get("Address") != null && !jsonObject.get("Address").isJsonNull())
                civilUserDto.address = jsonObject.get("Address").getAsString();
            if(jsonObject.get("MobileNo") != null && !jsonObject.get("MobileNo").isJsonNull())
                civilUserDto.mobileNumber = jsonObject.get("MobileNo").getAsString();
            if(jsonObject.get("TelNo") != null && !jsonObject.get("TelNo").isJsonNull())
                civilUserDto.telephoneNumber = jsonObject.get("TelNo").getAsString();
            if(jsonObject.get("Email") != null && !jsonObject.get("Email").isJsonNull())
                civilUserDto.emailId = jsonObject.get("Email").getAsString();
            return civilUserDto;
        }
    }
}
