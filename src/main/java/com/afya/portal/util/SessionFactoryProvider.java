package com.afya.portal.util;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

import javax.sql.DataSource;

/**
 * Created by Administrator on 12/2/2014.
 */
@Configuration
public class SessionFactoryProvider {

    @Autowired
    private DataSource primaryDataSource;

    public SessionFactory getSessionFactory(Class<?> someClass) {
        LocalSessionFactoryBuilder localSessionFactoryBuilder = new LocalSessionFactoryBuilder(primaryDataSource);
        localSessionFactoryBuilder.addAnnotatedClasses(someClass);
        return localSessionFactoryBuilder.buildSessionFactory();
    }
}
