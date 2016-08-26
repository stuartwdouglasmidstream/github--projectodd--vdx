package org.projectodd.vdx.wildfly;

import org.jboss.logging.BasicLogger;
import org.projectodd.vdx.core.Printer;

class LoggingPrinter implements Printer {
    LoggingPrinter(final BasicLogger logger) {
        this.logger = logger;
    }

    @Override
    public void println(final String msg) {
        logger.error("\n" + msg);
    }

    private final BasicLogger logger;
}
