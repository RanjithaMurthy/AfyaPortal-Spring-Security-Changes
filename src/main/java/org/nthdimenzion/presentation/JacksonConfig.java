package org.nthdimenzion.presentation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.money.Money;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.nthdimenzion.common.AppConstants.MONEY_FORMATTER;
import static org.nthdimenzion.presentation.AppUtils.getDateTimeFormat;
import static org.nthdimenzion.presentation.AppUtils.prependCurrencyUnit;
import static org.nthdimenzion.presentation.AppUtils.stripCurrencyUnit;

/**
 * https://github.com/FasterXML/jackson-datatype-joda/issues/12
 * <p/>
 * Need to add custom serialiser and deserialiser to handle joda date formats
 * <p/>
 * Locale based formats hacks http://stackoverflow.com/questions/25551011/change-date-pattern-from-shortdate-format-in-jodatime
 */

@Component
public class JacksonConfig implements BeanPostProcessor {

    private static DateTimeFormatter formatter = getDateTimeFormat();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof MappingJackson2HttpMessageConverter) {
            MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) bean;
            ObjectMapper objectMapper = jsonConverter.getObjectMapper();
            final JodaModule jodaModule = new JodaModule();
            jodaModule.addSerializer(LocalDate.class, new LocalDateSerializer());
            jodaModule.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
            jodaModule.addSerializer(Money.class, new MoneySerializer());
            jodaModule.addDeserializer(Money.class, new MoneyDeSerializer());
            objectMapper.registerModule(jodaModule);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        }
        return bean;
    }

    private class LocalDateSerializer extends JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(formatter.print(value));
        }
    }

    private class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return formatter.parseLocalDate(jp.getValueAsString());
        }
    }

    private class MoneySerializer extends JsonSerializer<Money> {

        @Override
        public void serialize(Money value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(stripCurrencyUnit(MONEY_FORMATTER.print(value)));
        }
    }

    private class MoneyDeSerializer extends JsonDeserializer<Money> {

        @Override
        public Money deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return MONEY_FORMATTER.parseMoney(prependCurrencyUnit(jp.getValueAsString()));
        }
    }

}
