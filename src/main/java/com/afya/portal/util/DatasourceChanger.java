package com.afya.portal.util;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.spring.DynamicDataSourceProxy;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by Mohan Sharma on 3/13/2015.
 */
public class DatasourceChanger {
    public static DataSource changeDatasource(DataSource dataSource){
        try {
            BoneCPConfig oldConfig = ((BoneCPDataSource) dataSource).getConfig();
            DynamicDataSourceProxy dynamicDataSourceProxy = new DynamicDataSourceProxy(dataSource);
            BoneCPConfig newConfig = oldConfig.clone();
            newConfig.setJdbcUrl("jdbc:mysql://127.0.0.1/mohan_domain?rewriteBatchedStatements=true&amp;createDatabaseIfNotExist=true&amp;characterEncoding=UTF-8&amp;characterSetResults=UTF-8");
            dynamicDataSourceProxy.switchDataSource(newConfig);
            return dynamicDataSourceProxy.getTargetDataSource();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
