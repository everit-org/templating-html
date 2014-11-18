package org.everit.osgi.ewt.internal;

import java.util.Map;

public class TextNode implements EWTNode {

    private final String text;

    public TextNode(String text) {
        this.text = text;
    }

    @Override
    public void render(StringBuilder sb, Map<String, Object> context) {
        sb.append(text);
    }

}
