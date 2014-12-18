package org.everit.templating.html.internal;

import org.everit.templating.html.internal.util.Coordinate;
import org.everit.templating.html.internal.util.HTMLTemplatingUtil;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.PageAttribute;

public class AttributeInfo {

    public final TagInfo tagInfo;

    /**
     * Starting cursor position of the value of the attribute within the template.
     */
    public final int valueCursorInTag;

    public final Coordinate valueStartCoordinate;

    public AttributeInfo(final PageAttribute pageAttribute, final TagInfo tagInfo,
            final Coordinate templateStartCoordinate) {
        this.tagInfo = tagInfo;

        int valueStartPosition = pageAttribute.getValueStartPosition();
        this.valueCursorInTag = valueStartPosition - tagInfo.startPosition;

        Page page = pageAttribute.getPage();
        valueStartCoordinate = HTMLTemplatingUtil
                .calculateCoordinate(page, valueStartPosition, templateStartCoordinate);

    }
}
