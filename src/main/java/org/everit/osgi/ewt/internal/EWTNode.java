package org.everit.osgi.ewt.internal;

import java.util.Map;

public interface EWTNode {

    void render(StringBuilder sb, Map<String, Object> context);
}
