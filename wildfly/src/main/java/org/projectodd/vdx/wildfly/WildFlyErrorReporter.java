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

package org.projectodd.vdx.wildfly;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.logging.BasicLogger;
import org.projectodd.vdx.core.Printer;

public class WildFlyErrorReporter extends ErrorReporter {
    public WildFlyErrorReporter(final File document, final BasicLogger logger) {
        super(asURL(document));
        this.printer = new LoggingPrinter(logger);
    }

    @Override
    protected SchemaProvider schemaProvider() {
        return new WildFlySchemaProvider();
    }

    @Override
    protected Printer printer() {
        return this.printer;
    }

    private static URL asURL(final File f) {
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private final Printer printer;
}
