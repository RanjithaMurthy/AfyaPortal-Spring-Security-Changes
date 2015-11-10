package org.nthdimenzion.common;

import org.joda.money.CurrencyUnit;
import org.joda.money.format.MoneyAmountStyle;
import org.joda.money.format.MoneyFormatter;
import org.joda.money.format.MoneyFormatterBuilder;

import java.util.Locale;

/**
 * Author: Nthdimenzion
 */

public interface AppConstants {

    /**
     * Not to used !!
     */
    CurrencyUnit DEFAULT_CURRENCY = CurrencyUnit.of(Locale.getDefault());

    MoneyFormatter MONEY_FORMATTER = new MoneyFormatterBuilder().appendCurrencyCode().appendAmount(MoneyAmountStyle.ASCII_DECIMAL_POINT_GROUP3_COMMA).toFormatter();

}
