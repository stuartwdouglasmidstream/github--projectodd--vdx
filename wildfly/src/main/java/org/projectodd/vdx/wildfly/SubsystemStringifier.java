package org.projectodd.vdx.wildfly;

import org.projectodd.vdx.core.ConditionalNamespacedElementStringifier;

class SubsystemStringifier extends ConditionalNamespacedElementStringifier {
    SubsystemStringifier() {
        super(e -> e.name().equals("subsystem"));
    }
}
