package com.afya.portal.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by Mohan Sharma on 4/9/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext.xml")
public class PropertyLoaderTest {

    @Value("${jdbc.url}")
    private String DATABASE_URL;
    @Value("${jdbc.username}")
    private String USERNAME;
    @Value("${jdbc.password}")
    private String PASSWORD;

    @Test
    public void whenApplicationProperitesAreInjected_thenApplicationPropertiesMustNotBeNull(){
        assertNotNull(DATABASE_URL);
        assertNotNull(USERNAME);
        assertNotNull(PASSWORD);
        assertThat(DATABASE_URL, is("jdbc:mysql://127.0.0.1/afya_portal?rewriteBatchedStatements=true&amp;createDatabaseIfNotExist=true&amp;characterEncoding=UTF-8&amp;characterSetResults=UTF-8"));
        assertThat(USERNAME, is("root"));
        assertThat(PASSWORD, is("welcome"));
    }
}
