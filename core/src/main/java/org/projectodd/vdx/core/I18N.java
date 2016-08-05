package org.projectodd.vdx.core;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18N {

    public enum Key {
        ATTRIBUTE_DUPLICATED,
        ATTRIBUTE_IS_ALLOWED_ON,
        ATTRIBUTE_NOT_ALLOWED,
        ATTRIBUTES_ALLOWED_HERE,
        ATTRIBUTE_INVALID_VALUE,
        ATTRIBUTE_REQUIRED_MISSING,
        ATTRIBUTE_REQUIRED_MISSING_LIST,

        BLANK,

        DID_YOU_MEAN,

        ELEMENT_DUPLICATED,
        ELEMENT_DUPLICATED_FIRST_OCCURRENCE,
        ELEMENT_IS_ALLOWED_ON,
        ELEMENT_NOT_ALLOWED,
        ELEMENTS_ALLOWED_HERE,
        ELEMENTS_REQUIRED_MISSING,
        ELEMENTS_REQUIRED_MISSING_LIST,

        ORIGINAL_ERROR,
        PASSTHRU,
        VALIDATION_ERROR_IN;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static String lookup(final Key key) {
        init();

        return bundle.getString(key.toString());
    }

    public static String format(final Key key, Object... args) {
        return String.format(lookup(key), args);
    }

    public static void setLocale(final Locale l) {
        locale = l;
    }

    private static Locale activeLocale() {
        return locale == null ? Locale.getDefault() : locale;
    }

    private I18N() {}

    private static void init() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("Messages", activeLocale());
        }
    }

    private static Locale locale;
    private static ResourceBundle bundle;

}
