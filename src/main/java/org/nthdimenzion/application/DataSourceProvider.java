package org.nthdimenzion.application;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Created by Mohan Sharma on 3/16/2015.
 */
@Configuration
public class DataSourceProvider {
    @Bean(name = "primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource getPrimaryDatasource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "jobsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.jobs")
    public DataSource getJobsDatasource() {
        return DataSourceBuilder.create().build();
    }
}
