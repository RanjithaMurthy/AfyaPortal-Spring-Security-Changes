package com.afya.portal.util;

import com.afya.portal.application.PersistFacilityCommand;
import com.ibatis.common.jdbc.ScriptRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Mohan Sharma on 3/13/2015.
 */
public class SqlScriptRunner {

    public static String runScript(String host,String databaseName, String databaseUsername, String databasePassword, PersistFacilityCommand persistFacilityCommand, String sqlScript){
        return  runScript(host,databaseName, databaseUsername, databasePassword, persistFacilityCommand, sqlScript, null, false);
    }

    public static String runScript(String host,String databaseName, String databaseUsername, String databasePassword, PersistFacilityCommand persistFacilityCommand, String sqlScript, String delimiter, boolean skipLoginUserCreation){
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection(host, databaseName, databaseUsername, databasePassword);
            ScriptRunner runner = new ScriptRunner(connection, false, false);
            if(delimiter != null && delimiter.isEmpty() == false)
                runner.setDelimiter(delimiter, false);
            InputStream inputStream = SqlScriptRunner.class.getClassLoader().getResourceAsStream(sqlScript);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            runner.runScript(inputStreamReader);
            if(skipLoginUserCreation == false) {
                if (persistFacilityCommand.getFacilityType().toString().equals("PHARMACY")) {
                    DatabaseUtil.createUserLoginForTheSpecifiedCredentialsPharmacy(connection, persistFacilityCommand);
                } else {
                    DatabaseUtil.createUserLoginForTheSpecifiedCredentials(connection, persistFacilityCommand);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String runScriptNoAdditionalDataCreation(String host,String databaseName, String databaseUsername, String databasePassword, String sqlScript){
        Connection connection = null;
        try {
            connection = DatabaseUtil.getConnection(host, databaseName, databaseUsername, databasePassword);
            ScriptRunner runner = new ScriptRunner(connection, false, false);
            InputStream inputStream = SqlScriptRunner.class.getClassLoader().getResourceAsStream(sqlScript);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            runner.runScript(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
