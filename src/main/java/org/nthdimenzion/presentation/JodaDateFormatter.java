package org.nthdimenzion.presentation;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.springframework.format.Formatter;

import java.text.ParseException;
import java.util.Locale;

/**
 * Not required now as the date format is picked based on the locale configured in CustomMVCConfiguration.java
 * Author: Nthdimenzion
 */

public class JodaDateFormatter implements Formatter<LocalDate> {

    @Override
    public LocalDate parse(String text, Locale locale) throws ParseException {
        return LocalDate.parse(text, DateTimeFormat.forPattern("dd/MM/yyyy"));
    }

    @Override
    public String print(LocalDate date, Locale locale) {
        return date.toString(DateTimeFormat.forPattern("dd/MM/yyyy"));
    }

    public static void main(String[] args) {
        System.out.println(LocalDate.now().toString(DateTimeFormat.forPattern("dd/MM/yyyy")));
    }
}
