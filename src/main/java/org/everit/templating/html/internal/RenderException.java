package org.everit.templating.html.internal;

import org.everit.templating.util.CompileException;

public class RenderException extends CompileException {

    public RenderException(final String message, final AttributeInfo attributeInfo) {
        super(message, attributeInfo.tagInfo.chars, attributeInfo.valueCursorInTag);
        setColumn(attributeInfo.valueStartCoordinate.column);
        setLineNumber(attributeInfo.valueStartCoordinate.row);
    }
}
