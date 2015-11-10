/*
 * Copyright (c) 1/24/15 8:17 AM.Nth Dimenzion, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package org.nthdimenzion.presentation;

import com.afya.portal.presentation.RestExceptionHandler;
import com.afya.portal.util.SimpleCORSFilter;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.servlet.Filter;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import static org.nthdimenzion.common.AppConstants.MONEY_FORMATTER;
import static org.nthdimenzion.presentation.AppUtils.prependCurrencyUnit;
import static org.nthdimenzion.presentation.AppUtils.stripCurrencyUnit;

/**
 * @author: Samir
 * @since 1.0 24/01/2015
 */
@Configuration
public class CustomMVCConfiguration extends WebMvcConfigurerAdapter {

    private static final String[] SERVLET_RESOURCE_LOCATIONS = {"/"};
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/", "classpath:/resources/",
            "classpath:/static/", "classpath:/public/"};
    private static final String[] RESOURCE_LOCATIONS;

    static {
        Locale.setDefault(Locale.UK);
    }

    static {
        RESOURCE_LOCATIONS = new String[CLASSPATH_RESOURCE_LOCATIONS.length
                + SERVLET_RESOURCE_LOCATIONS.length];
        System.arraycopy(SERVLET_RESOURCE_LOCATIONS, 0, RESOURCE_LOCATIONS, 0,
                SERVLET_RESOURCE_LOCATIONS.length);
        System.arraycopy(CLASSPATH_RESOURCE_LOCATIONS, 0, RESOURCE_LOCATIONS,
                SERVLET_RESOURCE_LOCATIONS.length, CLASSPATH_RESOURCE_LOCATIONS.length);
    }

    @Autowired
    RestExceptionHandler restExceptionHandler;

    @Bean
    public Filter simpleCORSFilter() {
        return new SimpleCORSFilter();
    }

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {

        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {
                ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/401.html");
                ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
                ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");
                container.addErrorPages(error401Page, error404Page, error500Page);
            }
        };
    }

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.getDefault());
        return slr;
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(restExceptionHandler);
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/home").setViewName("home");
//        registry.addViewController("/sign_up").setViewName("sign_up");
//        registry.addViewController("/clinic").setViewName("clinicForm");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/webjars/**")) {
            registry.addResourceHandler("/webjars/**")
                    .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }

        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**")
                    .addResourceLocations(RESOURCE_LOCATIONS);
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }


    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new JodaMoneyFormatter());
        super.addFormatters(registry);
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        UrlBasedViewResolver resolver = new UrlBasedViewResolver();
        resolver.setPrefix("/WEB-INF/classes/templates/");
        resolver.setSuffix(".html");
        resolver.setViewClass(InternalResourceView.class);
        registry.viewResolver(resolver);
    }

    private class JodaMoneyFormatter implements Formatter<Money> {

        @Override
        public Money parse(String text, Locale locale) throws ParseException {
            return MONEY_FORMATTER.parseMoney(prependCurrencyUnit(text));
        }

        @Override
        public String print(Money object, Locale locale) {
            return stripCurrencyUnit(MONEY_FORMATTER.print(object));
        }
    }
}
