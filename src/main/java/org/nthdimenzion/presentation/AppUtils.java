package org.nthdimenzion.presentation;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Locale;

import static org.nthdimenzion.common.AppConstants.DEFAULT_CURRENCY;

/**
 * Author: Nthdimenzion
 */
public class AppUtils {

    public static final String LOGGED_IN_USER = "LOGGED_IN_USER";

    private AppUtils() {
    }

    public static String stripCurrencyUnit(String money) {
        return money.substring(3);
    }

    public static String prependCurrencyUnit(String money) {
        return CurrencyUnit.getInstance(Locale.getDefault()).getCurrencyCode() + money;
    }

    public static DateTimeFormatter getDateTimeFormat() {
        String patternEnglish = DateTimeFormat.patternForStyle("S-", Locale.getDefault());
        patternEnglish = patternEnglish.replace("yy", "yyyy");
        return DateTimeFormat.forPattern(patternEnglish);
    }

    public static LocalDate toLocalDate(String date) {
        return getDateTimeFormat().parseLocalDate(date);
    }

    public static Money toMoney(String money) {
        return Money.of(DEFAULT_CURRENCY, new BigDecimal(money));
    }


    public static UserDetails getLoggedInUser(HttpServletRequest request) {
        Object loggedInUser = request.getSession().getAttribute(AppUtils.LOGGED_IN_USER);
        return loggedInUser != null ? (UserDetails) loggedInUser : null;
    }
}
