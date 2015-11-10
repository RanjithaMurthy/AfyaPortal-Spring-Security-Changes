/*
 * Copyright (c) 1/22/15 8:50 PM.Nth Dimenzion, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package org.nthdimenzion.security.configuration;

import org.nthdimenzion.security.service.AuthenticationFailureHandler;
import org.nthdimenzion.security.service.AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.dao.SystemWideSaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import javax.sql.DataSource;

/**
 * @author: Mohan Sharma
 * @since 1.0 23/01/2015
 */
@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource dataSource;
    @Autowired
    private UserDetailsService userService;
    @Autowired
    private DaoAuthenticationProvider daoAuthenticationProvider;
    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;


    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/", "/api_login", "/api_logout", "/regenerate_otp", "/provider_sign_up", "/patient_sign_up", "/otp_confirmation", "/logout", "/redirect", "/confirmation", "/password-reset", "/loginViaPopUp").permitAll()
                .anyRequest().authenticated()
                .and()
                    .formLogin()
                        .loginPage("/login")
                                //.failureUrl("/login?error=1")
                .usernameParameter("username").passwordParameter("password")
                        .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                        .permitAll()
                .and()
                    .logout()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .logoutSuccessHandler(authenticationSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .deleteCookies("username")
                        .deleteCookies("token")
                        .deleteCookies("role")
                .and()
                .rememberMe()
                        .key("pla_rem_srv")
                        .rememberMeServices(rememberMeServices())
                .and()
                    .csrf()
                        .disable();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/js/**","/anon/**", "/css/**", "/img/**", "/images/**","/fonts/**","/webjars/**", "/webjarsjs", "/webjarslocator/**","/anonV1/**",
                        "/sign_up","/**/*.html","/index","/static/**", "classpath:static/**");
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider);
    }

    @Bean
    public ShaPasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder();
    }

    @Bean
    public SystemWideSaltSource saltSource() {
        SystemWideSaltSource systemWideSaltSource = new SystemWideSaltSource();
        systemWideSaltSource.setSystemWideSalt("pla");
        return systemWideSaltSource;
    }

    @Bean
    public JdbcDaoImpl userValidationService() {
        JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        jdbcDao.setDataSource(dataSource);
        jdbcDao.setEnableAuthorities(true);
        return jdbcDao;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        return authenticationProvider;
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        // Key must be equal to rememberMe().key()
        CustomTokenBasedRememberMeService rememberMeServices = new CustomTokenBasedRememberMeService("pla_rem_srv", userService);
        rememberMeServices.setCookieName("remember_me_cookie");
        rememberMeServices.setParameter("remember_me_checkbox");
        rememberMeServices.setTokenValiditySeconds(2678400); // 1month
        return rememberMeServices;
    }
}
