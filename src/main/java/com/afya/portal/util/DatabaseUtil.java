package com.afya.portal.util;

import com.afya.portal.application.*;
import com.afya.portal.domain.model.TenantType;
import com.afya.portal.domain.model.security.UserType;
import com.nzion.dto.CivilUserDto;
import com.nzion.dto.InsuredPatientDto;
import com.nzion.dto.UserLoginDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Set;

/**
 * Created by Mohan Sharma on 3/13/2015.
 * Modified by Naren for Pharmacy DB creation
 */
public class DatabaseUtil {
    static PreparedStatement statement;

    public static Connection getConnection(String host, String databaseName, String databaseUsername, String databasePassword){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://"+host+"/"+databaseName+"?useUnicode=true",databaseUsername,databasePassword);
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getNewConnection(String host, String databaseName, String databaseUsername, String databasePassword){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://"+host+"/"+databaseName+"?useUnicode=true",databaseUsername,databasePassword);
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createDynamicDatabase(String host, String databaseName, String databaseUsername, String databasePassword) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + "/?user=" + databaseUsername + "&password=" + databasePassword);
            statement = connection.prepareStatement("CREATE database IF NOT EXISTS " + databaseName);
            statement.executeUpdate();
        } finally {
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String persistUserLoginFacilityAssociation(String hostPortal, String databaseUsername, String databasePassword, String portalDatabaseName, String userName, String tanentId, String facilityType) throws SQLException {
        String result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String QUERY_TO_PERSIST_USER_TENANT_ASSOC = "INSERT INTO user_tenant_assoc(facility_type, user_name, tenant_id) VALUES(?,?,?)";
        try {
            connection = getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
            preparedStatement = connection.prepareStatement(QUERY_TO_PERSIST_USER_TENANT_ASSOC);
            preparedStatement.setString(1, facilityType);
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, tanentId);
            int rowEffected = preparedStatement.executeUpdate();
            if (rowEffected > 0)
                result = "success";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            assert preparedStatement != null;
            try {
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void createTenantDatabasePharmacy(String host, String databaseName, String databaseUsername, String databasePassword, PersistFacilityCommand persistFacilityCommand) throws SQLException {
        Connection connection = getNewConnection(host, databaseName, databaseUsername, databasePassword);
        String queryToInsertToTenant = "INSERT INTO tenant(TENANT_ID,TENANT_NAME,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES(?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertToTenantDataSource = "INSERT INTO tenant_data_source (TENANT_ID,ENTITY_GROUP_NAME,JDBC_URI,JDBC_USERNAME,JDBC_PASSWORD,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES(?,?,?,?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(queryToInsertToTenant);
            statement.setString(1, persistFacilityCommand.getTenantId());
            statement.setString(2, persistFacilityCommand.getFacilityName());
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToTenantDataSource);
            String url = "jdbc:mysql://" + host + "/" + persistFacilityCommand.getTenantId() + "?rewriteBatchedStatements=TRUE&amp;createDatabaseIfNotExist=TRUE&amp;characterEncoding=UTF-8&amp;characterSetResults=UTF-8";
            statement.setString(1, persistFacilityCommand.getTenantId());
            statement.setString(2, "org.ofbiz");
            statement.setString(3, url);
            statement.setString(4, databaseUsername);
            statement.setString(5, databasePassword);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToTenantDataSource);
            url = "jdbc:mysql://" + host + "/" + persistFacilityCommand.getTenantId() + "_olap?rewriteBatchedStatements=TRUE&amp;createDatabaseIfNotExist=TRUE&amp;characterEncoding=UTF-8&amp;characterSetResults=UTF-8";
            statement.setString(1, persistFacilityCommand.getTenantId());
            statement.setString(2, "org.ofbiz.olap");
            statement.setString(3, url);
            statement.setString(4, databaseUsername);
            statement.setString(5, databasePassword);
            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*

        INSERT INTO
        `party`
        (`PARTY_ID`,`PARTY_TYPE_ID`)
        VALUES ('adminpharma','PERSON');

        INSERT INTO
        `person`(`PARTY_ID`,`FIRST_NAME`,`MIDDLE_NAME`,`LAST_NAME`)
        VALUES ('adminpharma','<FIRST_NAME>','<MIDDLE_NAME>','<LAST_NAME>');

        INSERT INTO
        `user_login`(`USER_LOGIN_ID`,`CURRENT_PASSWORD`,`ENABLED`,`PARTY_ID`)
        VALUES ('<USER_LOGIN_ID>','<CURRENT_PASSWORD (Eg. {SHA}5945ad9c89751d9a6615726c2f515907156678d5)>','Y','adminpharma');

        INSERT INTO
        `user_login_security_group`(`USER_LOGIN_ID`,`GROUP_ID`,`FROM_DATE`)
        VALUES ('adminpharma','FULLADMIN','<FROM_DATE (Eg. 2001-01-01 12:00:00)>');

        INSERT INTO
        `contact_mech`(`CONTACT_MECH_ID`, `CONTACT_MECH_TYPE_ID`, `INFO_STRING`)
        VALUES ('10000','EMAIL_ADDRESS','<INFO_STRING (Eg. abc@xyz.com)>'),('10001','TELECOM_NUMBER',NULL),('10002','TELECOM_NUMBER',NULL);

        INSERT INTO
        `telecom_number`(`CONTACT_MECH_ID`, `CONTACT_NUMBER`)
        VALUES ('10001','<MOBILE_NUMBER (Eg. 8888888888)>'),('10002','<OFFICE_NUMBER (Eg. 9999999999)>');

        INSERT INTO
        `party_contact_mech`(`PARTY_ID`, `CONTACT_MECH_ID`, `FROM_DATE`)
        VALUES ('adminpharma','10000','<FROM_DATE (Eg. 2015-08-01 13:04:51)>'),('adminpharma','10001','<FROM_DATE (Eg. 2015-08-01 13:04:51)>')('adminpharma','10002','<FROM_DATE (Eg. 2015-08-01 13:04:51)>');

        INSERT INTO
        `party_contact_mech_purpose`(`PARTY_ID`, `CONTACT_MECH_ID`, `CONTACT_MECH_PURPOSE_TYPE_ID`, `FROM_DATE`)
        VALUES ('adminpharma','10000','PRIMARY_EMAIL','<FROM_DATE (Eg. 2015-08-01 13:04:51)>'),('adminpharma','10001','PRIMARY_PHONE','<FROM_DATE (Eg. 2015-08-01 13:04:51)>'),('adminpharma','10002','PHONE_WORK','<FROM_DATE (Eg. 2015-08-01 13:04:51)>');

    */
    public static void createUserLoginForTheSpecifiedCredentialsPharmacy(Connection connection, PersistFacilityCommand persistFacilityCommand) throws SQLException {
        String queryToInsertCompanyToParty = "INSERT INTO party(PARTY_ID,PARTY_TYPE_ID,PREFERRED_CURRENCY_UOM_ID,STATUS_ID,CREATED_DATE,CREATED_BY_USER_LOGIN,SETUP_COMPLETE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company','PARTY_GROUP','KWD','PARTY_ENABLED',CURRENT_DATE(),'admin','Y',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToPartyRole = "INSERT INTO party_role(PARTY_ID,ROLE_TYPE_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company',?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToPartyStatus = "INSERT INTO party_status(STATUS_ID,PARTY_ID,STATUS_DATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('PARTY_ENABLED','Company',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToPartyGroup = "INSERT INTO party_group(PARTY_ID,GROUP_NAME,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company',?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToPartyAcctgPreference = "INSERT INTO party_acctg_preference(PARTY_ID,FISCAL_YEAR_START_MONTH,FISCAL_YEAR_START_DAY,COGS_METHOD_ID,BASE_CURRENCY_UOM_ID,LANGUAGE,COUNTRY,INVOICE_SEQUENCE_ENUM_ID,QUOTE_SEQUENCE_ENUM_ID,ORDER_SEQUENCE_ENUM_ID,EXCISE_ENABLED,INTERSTATE_DUTIES_REQ,MAINTAIN_MULTIPLE_GODOWNS,AUTO_POSTING,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company',4,1,'COGS_AVG_COST','KWD','en','KWT','INVSQ_ENF_SEQ','QTESQ_ENF_SEQ','ODRSQ_ENF_SEQ','Y','Y','Y','Y',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToContactMech = "INSERT INTO contact_mech(CONTACT_MECH_ID,CONTACT_MECH_TYPE_ID,INFO_STRING,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES (?,?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToPostalAddress = "INSERT INTO postal_address(CONTACT_MECH_ID,TO_NAME,ADDRESS1,CITY,POSTAL_CODE,COUNTRY_GEO_ID,STATE_PROVINCE_GEO_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES (?,?,?,?,?,?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToTelecomNumber = "INSERT INTO telecom_number(CONTACT_MECH_ID,COUNTRY_CODE,CONTACT_NUMBER,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES (?,?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToPartyContactMech = "INSERT INTO party_contact_mech(PARTY_ID,CONTACT_MECH_ID,FROM_DATE,ROLE_TYPE_ID,ALLOW_SOLICITATION,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company',?,CURRENT_DATE(),?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToPartyContactMechPurpose = "INSERT INTO party_contact_mech_purpose(PARTY_ID,CONTACT_MECH_ID,CONTACT_MECH_PURPOSE_TYPE_ID,FROM_DATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company',?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToFacility = "INSERT INTO facility(FACILITY_ID,FACILITY_TYPE_ID,OWNER_PARTY_ID,DEFAULT_INVENTORY_ITEM_TYPE_ID,FACILITY_NAME,DEFAULT_DAYS_TO_SHIP,DESCRIPTION,DEFAULT_WEIGHT_UOM_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company','WAREHOUSE','Company','NON_SERIAL_INV_ITEM','Stores',1,'Stores','OTH_box',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToFacilityContactMech = "INSERT INTO facility_contact_mech(FACILITY_ID,CONTACT_MECH_ID,FROM_DATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company',?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToFacilityContactMechPurpose = "INSERT INTO facility_contact_mech_purpose(FACILITY_ID,CONTACT_MECH_ID,CONTACT_MECH_PURPOSE_TYPE_ID,FROM_DATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('Company',?,?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";

        String queryToInsertAfyaSalesChannelToEnumeration = "INSERT INTO enumeration(ENUM_ID,ENUM_TYPE_ID,ENUM_CODE,SEQUENCE_ID,DESCRIPTION,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) values ('AFYA_SALES_CHANNEL','ORDER_SALES_CHANNEL','AFYA_SALES_CHANNEL','9','Afya Clinic Sales Channel',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";

        //String queryToInsertCompanyToProductStore = "INSERT INTO product_store(PRODUCT_STORE_ID,STORE_NAME,COMPANY_NAME,PAY_TO_PARTY_ID,DAYS_TO_CANCEL_NON_PAY,MANUAL_AUTH_IS_CAPTURE,PRORATE_SHIPPING,PRORATE_TAXES,VIEW_CART_ON_ADD,AUTO_SAVE_CART,AUTO_APPROVE_REVIEWS,IS_DEMO_STORE,IS_IMMEDIATELY_FULFILLED,INVENTORY_FACILITY_ID,ONE_INVENTORY_FACILITY,CHECK_INVENTORY,RESERVE_INVENTORY,RESERVE_ORDER_ENUM_ID,REQUIRE_INVENTORY,BALANCE_RES_ON_ORDER_CREATION,ORDER_NUMBER_PREFIX,DEFAULT_LOCALE_STRING,DEFAULT_CURRENCY_UOM_ID,DEFAULT_SALES_CHANNEL_ENUM_ID,ALLOW_PASSWORD,EXPLODE_ORDER_ITEMS,CHECK_GC_BALANCE,RETRY_FAILED_AUTHS,HEADER_APPROVED_STATUS,ITEM_APPROVED_STATUS,DIGITAL_ITEM_APPROVED_STATUS,HEADER_DECLINED_STATUS,ITEM_DECLINED_STATUS,HEADER_CANCEL_STATUS,ITEM_CANCEL_STATUS,AUTH_DECLINED_MESSAGE,AUTH_FRAUD_MESSAGE,AUTH_ERROR_MESSAGE,VISUAL_THEME_ID,STORE_CREDIT_ACCOUNT_ENUM_ID,USE_PRIMARY_EMAIL_USERNAME,REQUIRE_CUSTOMER_ROLE,AUTO_INVOICE_DIGITAL_ITEMS,REQ_SHIP_ADDR_FOR_DIG_ITEMS,SHOW_CHECKOUT_GIFT_OPTIONS,SELECT_PAYMENT_TYPE_PER_ITEM,SHOW_PRICES_WITH_VAT_TAX,SHOW_TAX_IS_EXEMPT,ENABLE_DIG_PROD_UPLOAD,PROD_SEARCH_EXCLUDE_VARIANTS,AUTO_ORDER_CC_TRY_EXP,AUTO_ORDER_CC_TRY_OTHER_CARDS,AUTO_ORDER_CC_TRY_LATER_NSF,STORE_CREDIT_VALID_DAYS,AUTO_APPROVE_INVOICE,AUTO_APPROVE_ORDER,SHIP_IF_CAPTURE_FAILS,REQ_RETURN_INVENTORY_RECEIVE,SHOW_OUT_OF_STOCK_PRODUCTS,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','Store','Company','Company',30,'N','Y','Y','N','N','N','Y','N','Company','Y','Y','Y','INVRO_FIFO_REC','N','Y','WS','en_US','KWD','WEB_SALES_CHANNEL','Y','N','N','Y','ORDER_APPROVED','ITEM_APPROVED','ITEM_APPROVED','ORDER_REJECTED','ITEM_REJECTED','ORDER_CANCELLED','ITEM_CANCELLED','There has been a problem with your method of payment. Please try a different method or call customer service.','Your order has been rejected and your account has been disabled due to fraud.','Problem connecting to payment processor; we will continue to retry and notify you by email.','EC_DEFAULT','FIN_ACCOUNT','N','N','Y','Y','Y','N','N','Y','N','Y','Y','Y','Y',90,'Y','N','Y','N','Y',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToProductStore = "INSERT INTO product_store(PRODUCT_STORE_ID,STORE_NAME,COMPANY_NAME,PAY_TO_PARTY_ID,IS_DEMO_STORE,IS_IMMEDIATELY_FULFILLED,INVENTORY_FACILITY_ID,ONE_INVENTORY_FACILITY,CHECK_INVENTORY,RESERVE_INVENTORY,REQUIRE_INVENTORY,BALANCE_RES_ON_ORDER_CREATION,ORDER_NUMBER_PREFIX,DEFAULT_LOCALE_STRING,DEFAULT_CURRENCY_UOM_ID,DEFAULT_SALES_CHANNEL_ENUM_ID,CHECK_GC_BALANCE,RETRY_FAILED_AUTHS,HEADER_APPROVED_STATUS,ITEM_APPROVED_STATUS,DIGITAL_ITEM_APPROVED_STATUS,HEADER_DECLINED_STATUS,ITEM_DECLINED_STATUS,HEADER_CANCEL_STATUS,ITEM_CANCEL_STATUS,AUTH_DECLINED_MESSAGE,AUTH_FRAUD_MESSAGE,AUTH_ERROR_MESSAGE,AUTO_ORDER_CC_TRY_EXP,AUTO_ORDER_CC_TRY_OTHER_CARDS,AUTO_ORDER_CC_TRY_LATER_NSF,AUTO_APPROVE_INVOICE,AUTO_APPROVE_ORDER,PRICES_INCLUDE_TAX,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','Store','Company','Company','N','N','Company','Y','N','Y','N','N','Order-','en_US','KWD','AFYA_SALES_CHANNEL','N','Y','ORDER_APPROVED','ITEM_APPROVED','ITEM_APPROVED','ORDER_REJECTED','ITEM_REJECTED','ORDER_CANCELLED','ITEM_CANCELLED','There has been a problem with your method of payment. Please try a different method or call customer service.','Your order has been rejected and your account has been disabled due to fraud.','Problem connecting to payment processor; we will continue to retry and notify you by email.','Y','Y','Y','Y','N','N',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE());";
        String queryToInsertCompanyToProductStoreEmailSetting = "INSERT INTO product_store_email_setting(PRODUCT_STORE_ID,EMAIL_TYPE,BODY_SCREEN_LOCATION,XSLFO_ATTACH_SCREEN_LOCATION,FROM_ADDRESS,CC_ADDRESS,BCC_ADDRESS,SUBJECT,CONTENT_TYPE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES (?,?,?,NULL,?,NULL,?,?,NULL,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToProductStoreFacility = "INSERT INTO product_store_facility(PRODUCT_STORE_ID,FACILITY_ID,FROM_DATE,THRU_DATE,SEQUENCE_NUM,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','Company',CURRENT_DATE(),NULL,NULL,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToProductStorePaymentSetting = "INSERT INTO product_store_payment_setting(PRODUCT_STORE_ID,PAYMENT_METHOD_TYPE_ID,PAYMENT_SERVICE_TYPE_ENUM_ID,APPLY_TO_ALL_PRODUCTS,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000',?,'PRDS_PAY_EXTERNAL','Y',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToProductStoreShipmentMeth = "INSERT INTO product_store_shipment_meth(PRODUCT_STORE_SHIP_METH_ID,PRODUCT_STORE_ID,SHIPMENT_METHOD_TYPE_ID,PARTY_ID,ROLE_TYPE_ID,ALLOW_USPS_ADDR,REQUIRE_USPS_ADDR,INCLUDE_NO_CHARGE_ITEMS,SEQUENCE_NUMBER,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES (?,?,?,'_NA_','CARRIER','N','N','Y',?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertCompanyToShipmentCostEstimate = "INSERT INTO shipment_cost_estimate(SHIPMENT_COST_ESTIMATE_ID,SHIPMENT_METHOD_TYPE_ID,CARRIER_PARTY_ID,CARRIER_ROLE_TYPE_ID,PRODUCT_STORE_ID,ORDER_FLAT_PRICE,ORDER_PRICE_PERCENT,ORDER_ITEM_FLAT_PRICE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES (?,?,'_NA_','CARRIER','10000',0.000,0.000,0.000,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";


        String queryToInsertAfyaSalesChannelToParty = "INSERT INTO party(PARTY_ID,PARTY_TYPE_ID,PREFERRED_CURRENCY_UOM_ID,STATUS_ID,CREATED_DATE,CREATED_BY_USER_LOGIN,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','PARTY_GROUP','KWD','PARTY_ENABLED',CURRENT_DATE(),'admin',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPartyRole = "INSERT INTO party_role(PARTY_ID,ROLE_TYPE_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000',?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPartyStatus = "INSERT INTO party_status(STATUS_ID,PARTY_ID,STATUS_DATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('PARTY_ENABLED','10000',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPartyRelationship = "INSERT INTO party_relationship(PARTY_ID_FROM,PARTY_ID_TO,ROLE_TYPE_ID_FROM,ROLE_TYPE_ID_TO,FROM_DATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) values('Company','10000','_NA_','BILL_TO_CUSTOMER',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPartyGroup = "INSERT INTO party_group(PARTY_ID,GROUP_NAME,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','AFYA Channel',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToContactMech = "INSERT INTO contact_mech(CONTACT_MECH_ID,CONTACT_MECH_TYPE_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10004','POSTAL_ADDRESS',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPostalAddress = "INSERT INTO postal_address(CONTACT_MECH_ID,TO_NAME,ADDRESS1,CITY,POSTAL_CODE,COUNTRY_GEO_ID,STATE_PROVINCE_GEO_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10004','AFYA Channel','AFYA Channel','Kuwait City','30000','KWT','KWT-HA',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPartyContactMech = "INSERT INTO party_contact_mech(PARTY_ID,CONTACT_MECH_ID,FROM_DATE,ROLE_TYPE_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','10004',CURRENT_DATE(),'PLACING_CUSTOMER',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPartyContactMechPurpose = "INSERT INTO party_contact_mech_purpose(PARTY_ID,CONTACT_MECH_ID,CONTACT_MECH_PURPOSE_TYPE_ID,FROM_DATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','10004','SHIPPING_LOCATION',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToPartyDataSource = "INSERT INTO party_data_source(PARTY_ID,DATA_SOURCE_ID,FROM_DATE,IS_CREATE,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10000','ECOMMERCE_SITE',CURRENT_DATE(),'Y',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertAfyaSalesChannelToProductStoreShipmentMeth = "INSERT INTO product_store_shipment_meth(PRODUCT_STORE_SHIP_METH_ID,PRODUCT_STORE_ID,SHIPMENT_METHOD_TYPE_ID,PARTY_ID,ROLE_TYPE_ID,ALLOW_USPS_ADDR,REQUIRE_USPS_ADDR,INCLUDE_NO_CHARGE_ITEMS,SEQUENCE_NUMBER,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('10004','10000','_NA_','10000','CARRIER','N','N','Y','5',CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";


        String queryToInsertToParty = "INSERT INTO party(PARTY_ID,PARTY_TYPE_ID) VALUES('adminpharma','PERSON')";
        String queryToInsertToPartyRole = "INSERT INTO party_role(PARTY_ID,ROLE_TYPE_ID,LAST_UPDATED_STAMP,LAST_UPDATED_TX_STAMP,CREATED_STAMP,CREATED_TX_STAMP) VALUES ('adminpharma',?,CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE(),CURRENT_DATE())";
        String queryToInsertToPerson = "INSERT INTO person(PARTY_ID, FIRST_NAME,MIDDLE_NAME, LAST_NAME) VALUES('adminpharma',?,?,?)";
        String queryToInsertToUserLogin = "INSERT INTO USER_LOGIN(USER_LOGIN_ID,CURRENT_PASSWORD,ENABLED,PARTY_ID) VALUES(?,?,'Y','adminpharma')";
        String queryToInsertToUserLoginSecurityGroup = "INSERT INTO user_login_security_group(USER_LOGIN_ID,GROUP_ID,FROM_DATE) VALUES (?,'FULLADMIN',CURRENT_DATE())";
        String queryToInsertToContactMech = "INSERT INTO contact_mech(CONTACT_MECH_ID, CONTACT_MECH_TYPE_ID, INFO_STRING) VALUES (?,?,?)";
        String queryToInsertToTelecomNumber = "INSERT INTO telecom_number(CONTACT_MECH_ID, CONTACT_NUMBER) VALUES (?,?)";
        String queryToInsertToPartyContactMech = "INSERT INTO party_contact_mech(PARTY_ID,CONTACT_MECH_ID, FROM_DATE) VALUES ('adminpharma',?,CURRENT_DATE())";
        String queryToInsertToPartyContactMechPurpose = "INSERT INTO party_contact_mech_purpose(PARTY_ID,CONTACT_MECH_ID, CONTACT_MECH_PURPOSE_TYPE_ID,FROM_DATE) VALUES ('adminpharma',?,?,CURRENT_DATE())";
        String queryToInsertToSequenceValueItem = "INSERT INTO sequence_value_item(SEQ_NAME,SEQ_ID) VALUES (?,?)";

        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(queryToInsertCompanyToParty);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyRole);
            statement.setString(1, "INTERNAL_ORGANIZATIO");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyRole);
            statement.setString(1, "BILL_FROM_VENDOR");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyRole);
            statement.setString(1, "BILL_TO_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyRole);
            statement.setString(1, "CARRIER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyRole);
            statement.setString(1, "_NA_");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyStatus);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyGroup);
            statement.setString(1, persistFacilityCommand.getFacilityName());
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyAcctgPreference);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToContactMech);
            statement.setString(1, "10000");
            statement.setString(2, "POSTAL_ADDRESS");
            statement.setString(3, null);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToContactMech);
            statement.setString(1, "10001");
            statement.setString(2, "TELECOM_NUMBER");
            statement.setString(3, null);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToContactMech);
            statement.setString(1, "10002");
            statement.setString(2, "EMAIL_ADDRESS");
            statement.setString(3, persistFacilityCommand.getEmail());
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToContactMech);
            statement.setString(1, "10003");
            statement.setString(2, "POSTAL_ADDRESS");
            statement.setString(3, null);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPostalAddress);
            statement.setString(1, "10000");
            statement.setString(2, persistFacilityCommand.getFacilityName());
            statement.setString(3, "dummy address");
            statement.setString(4, "Kuwait City");
            statement.setString(5, "30000");
            statement.setString(6, "KWT");
            statement.setString(7, "KWT-HA");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPostalAddress);
            statement.setString(1, "10003");
            statement.setString(2, persistFacilityCommand.getFacilityName());
            statement.setString(3, "dummy address");
            statement.setString(4, "Kuwait City");
            statement.setString(5, "30000");
            statement.setString(6, "KWT");
            statement.setString(7, "KWT-HA");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToTelecomNumber);
            statement.setString(1, "10001");
            statement.setString(2, "00965");
            statement.setString(3, persistFacilityCommand.getMobile());
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMech);
            statement.setString(1, "10000");
            statement.setString(2, "INTERNAL_ORGANIZATIO");
            statement.setString(3, "Y");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMech);
            statement.setString(1, "10001");
            statement.setString(2, "INTERNAL_ORGANIZATIO");
            statement.setString(3, "Y");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMech);
            statement.setString(1, "10002");
            statement.setString(2, "INTERNAL_ORGANIZATIO");
            statement.setString(3, "Y");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMechPurpose);
            statement.setString(1, "10000");
            statement.setString(2, "BILLING_LOCATION");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMechPurpose);
            statement.setString(1, "10000");
            statement.setString(2, "GENERAL_LOCATION");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMechPurpose);
            statement.setString(1, "10000");
            statement.setString(2, "PAYMENT_LOCATION");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMechPurpose);
            statement.setString(1, "10001");
            statement.setString(2, "PHONE_WORK");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToPartyContactMechPurpose);
            statement.setString(1, "10002");
            statement.setString(2, "PRIMARY_EMAIL");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToFacility);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToFacilityContactMech);
            statement.setString(1, "10003");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToFacilityContactMechPurpose);
            statement.setString(1, "10003");
            statement.setString(2, "SHIPPING_LOCATION");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToFacilityContactMechPurpose);
            statement.setString(1, "10003");
            statement.setString(2, "SHIP_ORIG_LOCATION");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToEnumeration);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStore);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStoreEmailSetting);
            String grnConfirmation = persistFacilityCommand.getFacilityName() + " - Goods Receive Note Confirmation";
            statement.setString(1, "10000");
            statement.setString(2, "GRN_CONFIRM");
            statement.setString(3, "component://product/widget/facility/FacilityScreens.xml#OrderConfirmNotice");
            statement.setString(4, "afyaarabia@nthdimenzion.com");
            statement.setString(5, "smesupport@nthdimenzion.com");
            statement.setString(6, grnConfirmation);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStoreEmailSetting);
            String orderConfirmation = persistFacilityCommand.getFacilityName() + " - Order Confirmation #${orderId}";
            statement.setString(1, "10000");
            statement.setString(2, "PRDS_ODR_CONFIRM");
            statement.setString(3, "component://order/widget/ordermgr/EmailOrderScreens.xml#OrderConfirmNotice");
            statement.setString(4, "afyaarabia@nthdimenzion.com");
            statement.setString(5, "smesupport@nthdimenzion.com");
            statement.setString(6, orderConfirmation);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStoreFacility);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStorePaymentSetting);
            statement.setString(1, "COMPANY_CHECK");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStorePaymentSetting);
            statement.setString(1, "EXT_COD");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStoreShipmentMeth);
            statement.setString(1, "10000");
            statement.setString(2, "10000");
            statement.setString(3, "HOME_DEVLIVERY");
            statement.setLong(4, 1);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStoreShipmentMeth);
            statement.setString(1, "10001");
            statement.setString(2, "10000");
            statement.setString(3, "NO_SHIPPING");
            statement.setLong(4, 2);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStoreShipmentMeth);
            statement.setString(1, "10002");
            statement.setString(2, "10000");
            statement.setString(3, "PICKUP");
            statement.setLong(4, 3);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToProductStoreShipmentMeth);
            statement.setString(1, "10003");
            statement.setString(2, "10000");
            statement.setString(3, "STANDARD");
            statement.setLong(4, 4);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToShipmentCostEstimate);
            statement.setString(1, "10000");
            statement.setString(2, "HOME_DEVLIVERY");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToShipmentCostEstimate);
            statement.setString(1, "10001");
            statement.setString(2, "NO_SHIPPING");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToShipmentCostEstimate);
            statement.setString(1, "10002");
            statement.setString(2, "PICKUP");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertCompanyToShipmentCostEstimate);
            statement.setString(1, "10003");
            statement.setString(2, "STANDARD");
            statement.executeUpdate();


            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToParty);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyRole);
            statement.setString(1, "BILL_TO_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyRole);
            statement.setString(1, "CARRIER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyRole);
            statement.setString(1, "CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyRole);
            statement.setString(1, "END_USER_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyRole);
            statement.setString(1, "PLACING_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyRole);
            statement.setString(1, "SHIP_TO_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyStatus);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyRelationship);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyGroup);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToContactMech);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPostalAddress);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyContactMech);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyContactMechPurpose);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToPartyDataSource);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertAfyaSalesChannelToProductStoreShipmentMeth);
            statement.executeUpdate();


            statement = connection.prepareStatement(queryToInsertToParty);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "BILL_FROM_VENDOR");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "BILL_TO_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "END_USER_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "GENERAL_MANAGER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "INVOICE_MANAGER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "MANAGER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "PACKER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "PLACING_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "QUOTE_MGR");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "REQ_APPROVER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "REQ_MANAGER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "REQ_MGR");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "REQ_PROPOSER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "REQ_TAKER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "SALES_MGR");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyRole);
            statement.setString(1, "SHIP_TO_CUSTOMER");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPerson);
            statement.setString(1, persistFacilityCommand.getFirstName());
            statement.setString(2, persistFacilityCommand.getMiddleName());
            statement.setString(3, persistFacilityCommand.getLastName());
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToUserLogin);
            statement.setString(1, persistFacilityCommand.username);
            statement.setString(2, persistFacilityCommand.password);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToUserLoginSecurityGroup);
            statement.setString(1, persistFacilityCommand.username);
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToContactMech);
            statement.setString(1, "10005");
            statement.setString(2, "EMAIL_ADDRESS");
            statement.setString(3, persistFacilityCommand.getEmail());
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToContactMech);
            statement.setString(1, "10006");
            statement.setString(2, "TELECOM_NUMBER");
            statement.setString(3, null);
            statement.executeUpdate();

            if (UtilValidator.isNotEmpty(persistFacilityCommand.getOfficePhoneNumber()) || persistFacilityCommand.getOfficePhoneNumber() != null) {
                statement = connection.prepareStatement(queryToInsertToContactMech);
                statement.setString(1, "10007");
                statement.setString(2, "TELECOM_NUMBER");
                statement.setString(3, null);
                statement.executeUpdate();
            }


            statement = connection.prepareStatement(queryToInsertToTelecomNumber);
            statement.setString(1, "10006");
            statement.setString(2, persistFacilityCommand.getMobile());
            statement.executeUpdate();

            if (UtilValidator.isNotEmpty(persistFacilityCommand.getOfficePhoneNumber()) || persistFacilityCommand.getOfficePhoneNumber() != null) {
                statement = connection.prepareStatement(queryToInsertToTelecomNumber);
                statement.setString(1, "10007");
                statement.setString(2, persistFacilityCommand.getOfficePhoneNumber());
                statement.executeUpdate();
            }

            statement = connection.prepareStatement(queryToInsertToPartyContactMech);
            statement.setString(1, "10005");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyContactMech);
            statement.setString(1, "10006");
            statement.executeUpdate();

            if (UtilValidator.isNotEmpty(persistFacilityCommand.getOfficePhoneNumber()) || persistFacilityCommand.getOfficePhoneNumber() != null) {
                statement = connection.prepareStatement(queryToInsertToPartyContactMech);
                statement.setString(1, "10007");
                statement.executeUpdate();
            }

            statement = connection.prepareStatement(queryToInsertToPartyContactMechPurpose);
            statement.setString(1, "10005");
            statement.setString(2, "PRIMARY_EMAIL");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToPartyContactMechPurpose);
            statement.setString(1, "10006");
            statement.setString(2, "PRIMARY_PHONE");
            statement.executeUpdate();

            if (UtilValidator.isNotEmpty(persistFacilityCommand.getOfficePhoneNumber()) || persistFacilityCommand.getOfficePhoneNumber() != null) {
                statement = connection.prepareStatement(queryToInsertToPartyContactMechPurpose);
                statement.setString(1, "10007");
                statement.setString(2, "PHONE_WORK");
                statement.executeUpdate();
            }

            statement = connection.prepareStatement(queryToInsertToSequenceValueItem);
            statement.setString(1, "ContactMech");
            if (UtilValidator.isNotEmpty(persistFacilityCommand.getOfficePhoneNumber()) || persistFacilityCommand.getOfficePhoneNumber() != null)
                statement.setString(2, "10008");
            else
                statement.setString(2, "10007");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToSequenceValueItem);
            statement.setString(1, "Party");
            statement.setString(2, "10001");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToSequenceValueItem);
            statement.setString(1, "ProductStore");
            statement.setString(2, "10001");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToSequenceValueItem);
            statement.setString(1, "ProductStoreShipmentMeth");
            statement.setString(2, "10005");
            statement.executeUpdate();

            statement = connection.prepareStatement(queryToInsertToSequenceValueItem);
            statement.setString(1, "ShipmentCostEstimate");
            statement.setString(2, "10004");
            statement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void createUserLoginForTheSpecifiedCredentials(Connection connection, PersistFacilityCommand persistFacilityCommand) throws SQLException {
        String queryToInsertToPerson = "INSERT INTO person(PARTY_TYPE, ACCOUNT_NUMBER, FIRST_NAME, LAST_NAME) VALUES('EMPLOYEE',FLOOR(10+(RAND()*90)),?,?)";
        String queryToInsertToPersonLocations = "INSERT INTO `person_locations`(`PERSON`, `LOCATIONS`) VALUES(LAST_INSERT_ID(), 10001);";
        String queryToInsertToUserLogin = "INSERT INTO USER_LOGIN(SUCCESSIVE_FAILED_LOGINS,CREATED_BY,ROLES,IS_ACTIVE, " +
                " ACCEPTED_TERMS_AND_CONDITIONS, ACCOUNT_NON_EXPIRED" +
                ", ACCOUNT_NON_LOCKED, CREDENTIALS_NON_EXPIRED, LOCKED,USERNAME," +
                " PASSWORD, IS_REQUIRE_PASSWORD_CHANGE, IMPERSONATED, PERSON_ID) VALUES(0,'superadmin','2251799813685248',?,?,?,?,?,?,?,?,?,?, LAST_INSERT_ID()) ";
        String queryToInsertIntoPractice = "INSERT INTO practice(PRACTICE_NAME,OFFICE_PHONE, FAX_NUMBER,\n" +
                " SERVICE_TAX_NUMBER, PAN_NUMBER, DRUG_LICENCE, VALID_FROM, VALID_THRU, ADDRESS1, ADDRESS2, COUNTRY_GEO, STATE_PROVINCE_GEO\n" +
                " , POSTAL_CODE,IS_ACTIVE, ACCR_NUMBER, ADMIN_USER_LOGIN_ID,TENANT_ID) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,LAST_INSERT_ID(),?)";
        PreparedStatement statement = null;
        try {
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(queryToInsertToPerson);
            statement.setString(1, persistFacilityCommand.getFirstName());
            statement.setString(2, persistFacilityCommand.getLastName());
            statement.executeUpdate();
            statement = connection.prepareStatement(queryToInsertToPersonLocations);
            statement.executeUpdate();
            statement = connection.prepareStatement(queryToInsertToUserLogin);
            statement.setBoolean(1, true);
            statement.setBoolean(2, true);
            statement.setBoolean(3, true);
            statement.setBoolean(4, true);
            statement.setBoolean(5, true);
            statement.setBoolean(6, false);
            statement.setString(7, persistFacilityCommand.username);
            statement.setString(8, persistFacilityCommand.password);
            statement.setBoolean(9, false);
            statement.setBoolean(10, false);
            statement.executeUpdate();
            statement = connection.prepareStatement(queryToInsertIntoPractice);
            statement.setString(1, persistFacilityCommand.facilityName);
            statement.setString(2, persistFacilityCommand.officePhoneNumber);
            statement.setString(3, persistFacilityCommand.faxNumber);
            statement.setString(4, persistFacilityCommand.serviceTaxNumber);
            statement.setString(5, persistFacilityCommand.panNumber);
            statement.setString(6, persistFacilityCommand.drugLicence);
            statement.setDate(7, convertUtilDateToSqlDate(persistFacilityCommand.validFrom));
            statement.setDate(8, convertUtilDateToSqlDate(persistFacilityCommand.validTo));
            statement.setString(9, persistFacilityCommand.address);
            statement.setString(10, persistFacilityCommand.addressAdditional);
            statement.setString(11, persistFacilityCommand.selectedCountry);
            statement.setString(12, persistFacilityCommand.selectedState);
            statement.setString(13, persistFacilityCommand.postalCode);
            statement.setBoolean(14, true);
            statement.setString(15, persistFacilityCommand.accrNumber);
            statement.setString(16, persistFacilityCommand.getTenantId());
            statement.executeUpdate();
            connection.commit();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String persistCivilData(String hostPortal, String portalDatabaseName, String databaseUsername, String databasePassword, PersistCivilIdDtoCommand persistCivilIdDtoCommand) throws SQLException {
        Connection connection = getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
        CivilUserDto civilUserDto = persistCivilIdDtoCommand.civilUserDto;
        String QUERY_TO_INSERT_CIVIL_DATA = "INSERT INTO civil_data(civil_id, first_name, middle_name, last_name, end_most_name, nationality," +
                "gender, date_of_birth, blood_group, rh, card_type, card_issued_date, card_expired_date, email_id, mobile_number," +
                "telephone_number, address, city, state, country) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                "first_name=VALUES(first_name), " +
                "middle_name=VALUES(middle_name), " +
                "last_name=VALUES(last_name), " +
                "end_most_name=VALUES(end_most_name), " +
                "nationality=VALUES(nationality), " +
                "gender=VALUES(gender), " +
                "date_of_birth=VALUES(date_of_birth), " +
                "blood_group=VALUES(blood_group), " +
                "rh=VALUES(rh), " +
                "card_type=VALUES(card_type), " +
                "card_issued_date=VALUES(card_issued_date), " +
                "card_expired_date=VALUES(card_expired_date), " +
                "email_id=VALUES(email_id), " +
                "mobile_number=VALUES(mobile_number), " +
                "telephone_number=VALUES(telephone_number), " +
                "address=VALUES(address), " +
                "city=VALUES(city), " +
                "state=VALUES(state), " +
                "country=VALUES(country) ";
        String result = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(QUERY_TO_INSERT_CIVIL_DATA);
            statement.setString(1, civilUserDto.getCivilId());
            statement.setObject(2, civilUserDto.getFirstName());
            statement.setString(3, civilUserDto.getMiddleName());
            statement.setString(4, civilUserDto.getLastName());
            statement.setString(5, civilUserDto.getEndMostName());
            statement.setString(6, civilUserDto.getNationality());
            statement.setString(7, civilUserDto.getGender());
            statement.setDate(8, convertUtilDateToSqlDate(civilUserDto.getDateOfBirth()));
            statement.setString(9, civilUserDto.getBloodGroup());
            statement.setString(10, civilUserDto.getRh());
            statement.setString(11, civilUserDto.getCardType());
            statement.setDate(12, convertUtilDateToSqlDate(civilUserDto.getCardIssuedDate()));
            statement.setDate(13, convertUtilDateToSqlDate(civilUserDto.getCardExpiryDate()));
            statement.setString(14, civilUserDto.getEmailId());
            statement.setString(15, civilUserDto.getMobileNumber());
            statement.setString(16, civilUserDto.getTelephoneNumber());
            statement.setString(17, civilUserDto.getAddress());
            statement.setString(18, civilUserDto.getCity());
            statement.setString(19, civilUserDto.getState());
            statement.setString(20, civilUserDto.getCountry());
            int rowUpdated = statement.executeUpdate();
            if (rowUpdated > 0)
                result = "success";
        } finally {
            try {
                assert statement != null;
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Date convertUtilDateToSqlDate(java.util.Date date) {
        if (date != null && !(date.equals(""))) {
            Date sqlDate = new Date(date.getTime());
            return sqlDate;
        }
        return null;
    }

    public static String deleteAllPatientInsuranceDetailsForGivenAfyaId(Connection connection, String afyaId) throws SQLException {
        String QUERY_TO_DELETE_ALL_INSURANCE_DETAILS_OF_A_PATIENT = "DELETE FROM patient_insurance WHERE afya_id = ?";
        PreparedStatement statement = null;
        String result = null;
        try {
            statement = connection.prepareStatement(QUERY_TO_DELETE_ALL_INSURANCE_DETAILS_OF_A_PATIENT);
            statement.setString(1, afyaId);
            int rowEffected = statement.executeUpdate();
            if (rowEffected > 0)
                result = "success";
        } finally {
            assert statement != null;
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String insertPatientInsuranceDetailsForGivenAfyaId(Connection connection, Set<InsuredPatientDto> insuredPatientDtos, String afyaId) throws SQLException {
        PreparedStatement statement = null;
        String result = "Nothing To Update";
        String INSERT_PATIENT_INSURANCE_DETAILS = "INSERT INTO patient_insurance(insurance_provider_id, insurance_plan_id, plan_name, patient_plan_id, patient_registration_no, afya_id)" +
                "VALUES (?,?,?,?,?,?)";
        try {
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(INSERT_PATIENT_INSURANCE_DETAILS);
            for (InsuredPatientDto insuredPatientDto : insuredPatientDtos) {
                statement.setString(1, insuredPatientDto.getInsuranceProviderId());
                statement.setString(2, insuredPatientDto.getInsurancePlanId());
                statement.setString(3, insuredPatientDto.getPlanName());
                statement.setString(4, insuredPatientDto.getPatientPlanId());
                statement.setString(5, insuredPatientDto.getPatientRegistrationNo());
                statement.setString(6, afyaId);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
            result = "success";
        } finally {
            assert statement != null;
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String insertCorporatePatientDetails(Connection connection, PersistCorporatePatientCommand persistCorporatePatientCommand) throws SQLException {
        PreparedStatement statement = null;
        String result = "Nothing To Update";
        String INSERT_CORPORATE_PATIENT_DETAILS = "INSERT INTO patient_corporate(corporate_id, corporate_plan_id, corporate_name, corporate_plan_name, contact_name, landline, employee_id, employee_role, afya_id)" +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            statement = connection.prepareStatement(INSERT_CORPORATE_PATIENT_DETAILS);
            statement.setString(1, persistCorporatePatientCommand.getCorporateId());
            statement.setString(2, persistCorporatePatientCommand.getCorporatePlanId());
            statement.setString(3, persistCorporatePatientCommand.getCorporateName());
            statement.setString(4, persistCorporatePatientCommand.getCorporatePlanName());
            statement.setString(5, persistCorporatePatientCommand.getContactName());
            statement.setString(6, persistCorporatePatientCommand.getLandline());
            statement.setString(7, persistCorporatePatientCommand.getEmployeeId());
            statement.setString(8, persistCorporatePatientCommand.getEmployeeRole());
            statement.setString(9, persistCorporatePatientCommand.getAfyaId());
            int rowsAffected = statement.executeUpdate();
            result = "success";
        } finally {
            assert statement != null;
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String deleteCorporatePatientDetailsForGivenAfyaId(Connection connection, String afyaId) throws SQLException {
        String QUERY_TO_DELETE_CORPORATE_DETAILS_OF_A_PATIENT = "DELETE FROM patient_corporate WHERE afya_id = ?";
        PreparedStatement statement = null;
        String result = null;
        try {
            statement = connection.prepareStatement(QUERY_TO_DELETE_CORPORATE_DETAILS_OF_A_PATIENT);
            statement.setString(1, afyaId);
            int rowEffected = statement.executeUpdate();
            if (rowEffected > 0)
                result = "success";
        } finally {
            assert statement != null;
            try {
                statement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String persistImageOfCivilData(String hostPortal, String portalDatabaseName, String databaseUsername, String databasePassword, PersistCivilDataImageCommand persistCivilDataImageCommand) throws SQLException, IOException {
        final String QUERY_TO_PERSIST_CIVIL_DATA_IMAGE = "INSERT INTO civil_data(civil_id, image) VALUES(?,?) ON DUPLICATE KEY UPDATE image=VALUES(image)";
        Connection connection = getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
        String civilId = persistCivilDataImageCommand.getCivilId();
        PreparedStatement preparedStatement = null;
        String result = null;
        try {
            byte[] image = persistCivilDataImageCommand.getImage().getBytes();
            preparedStatement = connection.prepareStatement(QUERY_TO_PERSIST_CIVIL_DATA_IMAGE);
            preparedStatement.setString(1, civilId);
            preparedStatement.setBlob(2, new ByteArrayInputStream(image));
            int rowEffected = preparedStatement.executeUpdate();
            if (rowEffected > 0)
                result = "success";
        } finally {
            assert preparedStatement != null;
            try {
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String persistUserLoginFacilityAssociation(String hostPortal, String databaseUsername, String databasePassword, String portalDatabaseName, PersistUserTenantAssocCommand persistUserTenantAssocCommand) throws SQLException {
        String result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String QUERY_TO_PERSIST_USER_TENANT_ASSOC = "INSERT INTO user_tenant_assoc(facility_type, user_name, tenant_id) VALUES(?,?,?) ON DUPLICATE KEY UPDATE facility_type=VALUES(facility_type), user_name=VALUES(user_name), tenant_id=VALUES(tenant_id)";
        try {
            connection = getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
            preparedStatement = connection.prepareStatement(QUERY_TO_PERSIST_USER_TENANT_ASSOC);
            preparedStatement.setString(1, TenantType.valueOf(persistUserTenantAssocCommand.facilityType.toUpperCase()).toString());
            preparedStatement.setString(2, persistUserTenantAssocCommand.userName);
            preparedStatement.setString(3, persistUserTenantAssocCommand.tenantId);
            int rowEffected = preparedStatement.executeUpdate();
            if (rowEffected > 0)
                result = "success";
        } finally {
            assert preparedStatement != null;
            try {
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String persistUserLoginFromTenant(String hostPortal, String databaseUsername, String databasePassword, String portalDatabaseName, UserLoginDto userLoginDto) throws SQLException {
        String result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String QUERY_TO_PERSIST_USER_TENANT_ASSOC = "INSERT INTO users(username, password, first_name, middle_name, last_name, is_verified, email_id, mobile_number, login_preference, tenant_id,trail) VALUES(?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE username=VALUES(username), password=VALUES(password), first_name=VALUES(first_name), middle_name=VALUES(middle_name), last_name=VALUES(last_name), is_verified=VALUES(is_verified), email_id=VALUES(email_id), mobile_number=VALUES(mobile_number), login_preference=VALUES(login_preference), tenant_id=VALUES(tenant_id)";
        try {
            connection = getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
            preparedStatement = connection.prepareStatement(QUERY_TO_PERSIST_USER_TENANT_ASSOC);
            preparedStatement.setString(1, userLoginDto.getUsername());
            preparedStatement.setString(2, userLoginDto.getPassword());
            preparedStatement.setString(3, userLoginDto.getFirstName());
            preparedStatement.setString(4, userLoginDto.getMiddleName());
            preparedStatement.setString(5, userLoginDto.getLastName());
            preparedStatement.setBoolean(6, userLoginDto.isVerified());
            preparedStatement.setString(7, userLoginDto.getEmailId());
            preparedStatement.setString(8, userLoginDto.getMobileNumber());
            preparedStatement.setString(9, userLoginDto.getLoginPreference());
            preparedStatement.setString(10, userLoginDto.getTenantId());
            preparedStatement.setBoolean(11, false);
            int rowEffected = preparedStatement.executeUpdate();
            if (rowEffected > 0)
                result = "success";
        } finally {
            assert preparedStatement != null;
            try {
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String persistAuthorizationForUserLogin(String hostPortal, String databaseUsername, String databasePassword, String portalDatabaseName, UserLoginDto userLoginDto, Set<String> roles) throws SQLException {
        String result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String QUERY_TO_PERSIST_USER_TENANT_ASSOC = "INSERT INTO authorities(username, authority, default_role) VALUES(?, ?, ?)";
        try {
            connection = getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
            preparedStatement = connection.prepareStatement(QUERY_TO_PERSIST_USER_TENANT_ASSOC);
            connection.setAutoCommit(false);
            for (String authority : roles) {
                preparedStatement.setString(1, userLoginDto.getUsername());
                preparedStatement.setString(2, authority);
                preparedStatement.setString(3, userLoginDto.getDefaultRole() != null ? UserType.valueOf(userLoginDto.getDefaultRole()).toString() : null);
                preparedStatement.addBatch();
            }
            int rowEffected[] = preparedStatement.executeBatch();
            connection.commit();
            result = "success";
        } catch (SQLException e) {
            connection.rollback();
            result = e.getMessage();
        } finally {
            assert preparedStatement != null;
            if (connection != null) {
                preparedStatement.close();
                connection.close();
            }
        }
        return result;
    }

    public static String deleteExistingAuthorizationForUserLogin(String hostPortal, String databaseUsername, String databasePassword, String portalDatabaseName, UserLoginDto userLoginDto) throws SQLException {
        String result = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        final String QUERY_TO_DELETE_EXISTING_ROLES_OF_USER = "DELETE FROM authorities WHERE username=?  AND authority <> ?";
        try {
            connection = getConnection(hostPortal, portalDatabaseName, databaseUsername, databasePassword);
            preparedStatement = connection.prepareStatement(QUERY_TO_DELETE_EXISTING_ROLES_OF_USER);
            preparedStatement.setString(1, userLoginDto.getUsername());
            preparedStatement.setString(2, "ROLE_FACILITY_ADMIN");
            int rowEffected = preparedStatement.executeUpdate();
            if (rowEffected > 0)
                result = "success";
        } catch (SQLException e) {
            result = e.getMessage();
        } finally {
            assert preparedStatement != null;
            if (connection != null) {
                preparedStatement.close();
                connection.close();
            }
        }
        return result;
    }

}
