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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.jboss.logging.BasicLogger;
import org.projectodd.vdx.core.ErrorPrinter;
import org.projectodd.vdx.core.ErrorType;
import org.projectodd.vdx.core.I18N;
import org.projectodd.vdx.core.Stringifier;
import org.projectodd.vdx.core.ValidationError;
import org.projectodd.vdx.core.XMLStreamValidationException;

public class ErrorReporter {
    public ErrorReporter(final File document, final BasicLogger logger) {
        this.document = document;
        this.logger = logger;
    }

    /**
     * Reports an error to VDX.
     * @param exception
     * @return true if the error was actually printed
     */
    public boolean report(final XMLStreamException exception) {
        boolean printed = false;
        try {
            final List<URL> schemas = findSchemas();

            if (!schemas.isEmpty()) {
                final ValidationError error;
                if (exception instanceof XMLStreamValidationException) {
                    error = ((XMLStreamValidationException) exception).getValidationError();
                } else {
                    final String message = exception.getMessage();

                    // detect duplicate attribute - this message comes from woodstox, and isn't i18n, so we don't have to
                    // worry about other languages
                    final Matcher dupMatcher = Pattern.compile("^Duplicate attribute '(.+?)'\\.").matcher(message);
                    if (dupMatcher.find()) {
                        error = ValidationError.from(exception, ErrorType.DUPLICATE_ATTRIBUTE)
                                .attribute(QName.valueOf(dupMatcher.group(1)));
                    } else {
                        error = ValidationError.from(exception, ErrorType.UNKNOWN_ERROR);
                        // attempt to strip the message code
                        final Matcher m = Pattern.compile("Message: \"?([A-Z]+\\d+: )?(.*?)\"?$").matcher(message);
                        if (m.find()) {
                            error.fallbackMessage(m.group(2));
                        }
                    }
                }

                final List<Stringifier> stringifiers = new ArrayList<>();
                stringifiers.add(new SubsystemStringifier());

                SchemaDocRelationships rel = new SchemaDocRelationships();

                new ErrorPrinter(this.document.toURI().toURL(), schemas)
                        .printer(new LoggingPrinter(this.logger))
                        .stringifiers(stringifiers)
                        .pathGate(rel)
                        .prefixProvider(rel)
                        .print(error);
                printed = true;
            }
        } catch (Exception ex) {
            this.logger.info(I18N.failedToPrintError(ex));
        }

        return printed;
    }

    protected List<URL> findSchemas() {
        final SchemaProvider provider = schemaProvider();
        final List<URL> schemas = provider.schemas();

        if (schemas.isEmpty()) {
            this.logger.info(I18N.noSchemasAvailable(provider.schemaResource()));
        }

        return schemas;
    }

    protected SchemaProvider schemaProvider() {
        return new DefaultWildFlySchemaProvider();
    }

    private final File document;
    private final BasicLogger logger;
}
