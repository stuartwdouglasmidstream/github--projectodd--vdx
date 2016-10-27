/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.vdx.core;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18N {

    public enum Key {
        AND_N_MORE,

        ATTRIBUTE_DUPLICATED,
        ATTRIBUTE_DUPLICATED_FIRST_OCCURRENCE,
        ATTRIBUTE_DUPLICATED_NO_ELEMENT,
        ATTRIBUTE_IS_ALLOWED_ON,
        ATTRIBUTE_NOT_ALLOWED,
        ATTRIBUTES_ALLOWED_HERE,
        ATTRIBUTE_INVALID_VALUE,
        ATTRIBUTE_REQUIRED_MISSING,
        ATTRIBUTE_REQUIRED_MISSING_LIST,

        DID_YOU_MEAN,

        ELEMENT_DUPLICATED,
        ELEMENT_DUPLICATED_FIRST_OCCURRENCE,
        ELEMENT_HAS_NO_ATTRIBUTES,
        ELEMENT_WITH_ATTRIBUTE_DUPLICATED,
        ELEMENT_WITH_ATTRIBUTE_DUPLICATED_FIRST_OCCURRENCE,
        ELEMENT_IS_ALLOWED_ON,
        ELEMENT_NOT_ALLOWED,
        ELEMENT_REQUIRED_MISSING,
        ELEMENT_REQUIRED_MISSING_LIST,
        ELEMENT_UNSUPPORTED,
        ELEMENT_UNSUPPORTED_NO_ALT,
        ELEMENTS_ALLOWED_HERE,
        ELEMENTS_REQUIRED_MISSING,
        ELEMENTS_REQUIRED_MISSING_LIST,

        ORIGINAL_ERROR,
        PASSTHRU,
        PRINT_FAILURE,
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

    public static String validationErrorIn(final String docName) {
        return format(I18N.Key.VALIDATION_ERROR_IN, "OPVDX001", docName);
    }

    public static String failedToPrintError(final Throwable ex) {
        return format(Key.PRINT_FAILURE, "OPVDX002", ex.getMessage());
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
