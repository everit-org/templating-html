package org.everit.osgi.ewt.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.visitors.NodeVisitor;

public class EWTNodeVisitor extends NodeVisitor {

    private final StringBuilder currentSB = new StringBuilder();

    private final String ewtAttributePrefix;

    private final List<Tag> tagBreadcumb = new ArrayList<Tag>();

    public EWTNodeVisitor(String ewtAttributePrefix) {
        super();
        this.ewtAttributePrefix = ewtAttributePrefix;
    }

    @Override
    public void visitEndTag(Tag tag) {
        // TODO Auto-generated method stub
        super.visitEndTag(tag);
    }

    @Override
    public void visitRemarkNode(Remark remark) {
        // TODO Auto-generated method stub
        super.visitRemarkNode(remark);
    }

    @Override
    public void visitStringNode(Text string) {
        currentSB.append(string.toPlainTextString());
    }

    @Override
    public void visitTag(Tag tag) {

        @SuppressWarnings("unchecked")
        Vector<PageAttribute> attributes = tag.getAttributesEx();
        Iterator<PageAttribute> iterator = attributes.iterator();
        TagNode tagNode = null;
        while (iterator.hasNext()) {
            PageAttribute attribute = iterator.next();
            if (attribute.getName().startsWith(ewtAttributePrefix)) {
                tagNode = new TagNode();
            }
        }

        if (tagNode == null) {
            currentSB.append(tag.toTagHtml());
        }
    }

}
