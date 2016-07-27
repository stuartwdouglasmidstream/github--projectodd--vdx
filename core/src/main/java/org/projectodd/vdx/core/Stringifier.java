package org.projectodd.vdx.core;

public interface Stringifier {
    Stringifier DEFAULT_STRINGIFIER = new Stringifier() {
        @Override
        public Class handledClass() {
            return null;
        }

        @Override
        public String asString(Object value) {
            return value.toString();
        }
    };


    @SuppressWarnings("unchecked")
    default boolean handles(final Object value) {
        return handledClass().isAssignableFrom(value.getClass());
    }

    Class handledClass();

    String asString(Object value);
}
