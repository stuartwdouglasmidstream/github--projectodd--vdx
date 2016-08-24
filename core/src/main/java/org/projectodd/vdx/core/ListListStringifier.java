package org.projectodd.vdx.core;

import java.util.List;

public class ListListStringifier extends NonElementListStringifier {

    @Override
    public boolean handles(Object value) {
        return super.handles(value) &&
                List.class.isAssignableFrom(((List)value).get(0).getClass());
    }

    public ListListStringifier(final int limit) {
        super(0, limit);
    }
}
