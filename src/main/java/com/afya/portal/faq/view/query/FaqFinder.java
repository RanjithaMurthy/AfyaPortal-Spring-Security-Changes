package com.afya.portal.faq.view.query;

import com.afya.portal.faq.view.dto.FaqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 8/2/15
 * Time: 6:01 PM
 * To change this template use File | Settings | File Templates.
 */

@Component
public class FaqFinder {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public FaqFinder(@Qualifier("primaryDataSource") DataSource dataSource){
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<FaqDto> getAllFaq(){
        SqlParameterSource parameterSource = new MapSqlParameterSource();
        return namedParameterJdbcTemplate.query("select * from faq",parameterSource,new BeanPropertyRowMapper<>(FaqDto.class));
    }


    /*private PatientDto fetchPatientByGivenCriteria(String firstName, String lastName, Date dateOfBirth, String mobileNumber) {
        SqlParameterSource parameterSource = new MapSqlParameterSource("firstName", firstName).addValue("lastName", lastName).addValue("dateOfBirth", dateOfBirth).addValue("mobileNumber", mobileNumber);
        try {
            List<PatientDto> patientDto = namedParameterJdbcTemplate.query(GET_PATIENT_BASED_ON_GIVEN_CRITERIA, parameterSource, new BeanPropertyRowMapper<>(PatientDto.class));
            if (patientDto.size() == 0)
                return null;
            else
                return patientDto.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }*/


}
