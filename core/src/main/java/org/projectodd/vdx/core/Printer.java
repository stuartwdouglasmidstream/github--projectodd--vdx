package org.projectodd.vdx.core;

/**
 * Created by tcrawley on 7/6/16.
 */
public interface Printer {
    Printer DEFAULT_PRINTER = new Printer() {};

    default void println(String msg) {
        System.err.println(msg);
    }

}
