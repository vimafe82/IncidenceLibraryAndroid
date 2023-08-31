package com.e510.commons.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtils {

    private static int NUMBER_OF_DECIMALS = 2;

    public static String getCorrectFormatValue(Double num, String concatenateValue) {
        String value = "";
        if (num != null) {
            num = NumberUtils.roundAvoid(num, NUMBER_OF_DECIMALS);

            if (isDecimal(num)) {
                value = NumberUtils.getValueWithTwoDecimals(num) + concatenateValue;
            } else {
                value = String.valueOf(num.intValue()).replace(".", ",") + concatenateValue;
            }
        }

        return value;
    }

    public static double roundAvoidTwoDecimals(double num) {
        return roundAvoid(num, NUMBER_OF_DECIMALS);
    }

    public static double roundAvoid(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    public static String getValueWithTwoDecimals(double num) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(num);
    }

    public static boolean isDecimal(double number) {
        if((number % 1) == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static String formatIntPointMillers(int d) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###");
        String str = formatter.format(d);
        str = str.replace(",", ".");
        return str;
    }
}
