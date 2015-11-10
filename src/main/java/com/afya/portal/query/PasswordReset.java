package com.afya.portal.query;

/**
 * Created by User on 8/24/2015.
 */

import com.afya.portal.domain.model.security.UserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
public class PasswordReset {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public PasswordReset(@Qualifier("primaryDataSource") DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }
    public int resetPassword(String username, String password){
        int numberOfUser = namedParameterJdbcTemplate.update("UPDATE users SET PASSWORD = :password WHERE username= :username", new MapSqlParameterSource().addValue("password", password).addValue("username", username)) ;
        return numberOfUser;
    }
    public int validateCurrentPassword(String username, String password){
        List<Map<String, Object>> numberOfUser = namedParameterJdbcTemplate.queryForList("SELECT * FROM users WHERE username= :username AND PASSWORD =:password", new MapSqlParameterSource().addValue("username", username).addValue("password", password));
        int test = numberOfUser.size();
        return numberOfUser.size();
    }
}
