package org.everit.templating.html.internal;

import org.htmlparser.Tag;
import org.htmlparser.lexer.Page;

public class TagInfo {

    public final char[] chars;

    public final int startPosition;

    public TagInfo(final Tag tag) {
        Page page = tag.getPage();
        this.startPosition = tag.getStartPosition();
        int endPosition = tag.getEndPosition();
        chars = new char[endPosition - startPosition];
        page.getText(chars, 0, startPosition, endPosition);
    }
}
