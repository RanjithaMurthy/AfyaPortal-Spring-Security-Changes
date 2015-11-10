package com.afya.portal.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pradyumna on 12-06-2015.
 */
@Component
public class RestExceptionHandler extends AbstractHandlerExceptionResolver implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    private HttpMessageConverter<?>[] messageConverters = null;

    private List<HttpMessageConverter<?>> allMessageConverters = null;

    @Autowired
    private RestErrorResolver errorResolver;
    @Autowired
    private RestErrorConverter<?> errorConverter;

    public RestExceptionHandler() {
        this.errorResolver = new DefaultRestErrorResolver();
        this.errorConverter = new MapRestErrorConverter();
    }

    public void setMessageConverters(HttpMessageConverter<?>[] messageConverters) {
        this.messageConverters = messageConverters;
    }

    public void setErrorResolver(RestErrorResolver errorResolver) {
        this.errorResolver = errorResolver;
    }

    public RestErrorResolver getErrorResolver() {
        return this.errorResolver;
    }

    public RestErrorConverter<?> getErrorConverter() {
        return errorConverter;
    }

    public void setErrorConverter(RestErrorConverter<?> errorConverter) {
        this.errorConverter = errorConverter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ensureMessageConverters();
    }

    @SuppressWarnings("unchecked")
    private void ensureMessageConverters() {

        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();

        //user configured values take precedence:
        if (this.messageConverters != null && this.messageConverters.length > 0) {
            converters.addAll(CollectionUtils.arrayToList(this.messageConverters));
        }

        //defaults next:
        new HttpMessageConverterHelper().addDefaults(converters);

        this.allMessageConverters = converters;
    }

    //leverage Spring's existing default setup behavior:
    private static final class HttpMessageConverterHelper extends WebMvcConfigurationSupport {
        public void addDefaults(List<HttpMessageConverter<?>> converters) {
            addDefaultHttpMessageConverters(converters);
        }
    }

    /**
     * Actually resolve the given exception that got thrown during on handler execution, returning a ModelAndView that
     * represents a specific error page if appropriate.
     * <p/>
     * May be overridden in subclasses, in order to apply specific
     * exception checks. Note that this template method will be invoked <i>after</i> checking whether this resolved applies
     * ("mappedHandlers" etc), so an implementation may simply proceed with its actual exception handling.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  the executed handler, or <code>null</code> if none chosen at the time of the exception (for example,
     *                 if multipart resolution failed)
     * @param ex       the exception that got thrown during handler execution
     * @return a corresponding ModelAndView to forward to, or <code>null</code> for default processing
     */
    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        ServletWebRequest webRequest = new ServletWebRequest(request, response);

        RestErrorResolver resolver = getErrorResolver();

        RestError error = resolver.resolveError(webRequest, handler, ex);
        if (error == null) {
            return null;
        }

        ModelAndView mav = null;

        try {
            mav = getModelAndView(webRequest, handler, error);
        } catch (Exception invocationEx) {
            log.error("Acquiring ModelAndView for Exception [" + ex + "] resulted in an exception.", invocationEx);
        }

        return mav;
    }

    protected ModelAndView getModelAndView(ServletWebRequest webRequest, Object handler, RestError error) throws Exception {

        applyStatusIfPossible(webRequest, error);

        Object body = error; //default the error instance in case they don't configure an error converter

        RestErrorConverter converter = getErrorConverter();
        if (converter != null) {
            body = converter.convert(error);
        }

        return handleResponseBody(body, webRequest);
    }

    private void applyStatusIfPossible(ServletWebRequest webRequest, RestError error) {
        if (!WebUtils.isIncludeRequest(webRequest.getRequest())) {
            webRequest.getResponse().setStatus(error.getStatus().value());
        }
        //TODO support response.sendError ?
    }

    @SuppressWarnings("unchecked")
    private ModelAndView handleResponseBody(Object body, ServletWebRequest webRequest) throws ServletException, IOException {

        HttpInputMessage inputMessage = new ServletServerHttpRequest(webRequest.getRequest());

        List<MediaType> acceptedMediaTypes = inputMessage.getHeaders().getAccept();
        if (acceptedMediaTypes.isEmpty()) {
            acceptedMediaTypes = Collections.singletonList(MediaType.ALL);
        }

        MediaType.sortByQualityValue(acceptedMediaTypes);

        HttpOutputMessage outputMessage = new ServletServerHttpResponse(webRequest.getResponse());

        Class<?> bodyType = body.getClass();

        List<HttpMessageConverter<?>> converters = this.allMessageConverters;

        if (converters != null) {
            for (MediaType acceptedMediaType : acceptedMediaTypes) {
                for (HttpMessageConverter messageConverter : converters) {
                    if (messageConverter.canWrite(bodyType, acceptedMediaType)) {
                        messageConverter.write(body, acceptedMediaType, outputMessage);
                        //return empty model and view to short circuit the iteration and to let
                        //Spring know that we've rendered the view ourselves:
                        return new ModelAndView();
                    }
                }
            }
        }

        if (logger.isWarnEnabled()) {
            logger.warn("Could not find HttpMessageConverter that supports return type [" + bodyType +
                    "] and " + acceptedMediaTypes);
        }
        return null;
    }
}
