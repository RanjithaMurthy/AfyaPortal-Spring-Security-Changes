package com.afya.portal.presentation;

import org.springframework.core.convert.converter.Converter;

/**
 * Created by pradyumna on 12-06-2015.
 */
public interface RestErrorConverter<T> extends Converter<RestError, T> {

    /**
     * Converts the RestError instance into an object that will then be used by an
     * {@link org.springframework.http.converter.HttpMessageConverter HttpMessageConverter} to render the response body.
     *
     * @param re the {@code RestError} instance to convert to another object instance 'understood' by other registered
     *           {@code HttpMessageConverter} instances.
     * @return an object suited for HTTP response rendering by an {@code HttpMessageConverter}
     */
    T convert(RestError re);
}
