/**
 * This file is part of Everit - Web Templating.
 *
 * Everit - Web Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Web Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Web Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.ewt.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.everit.osgi.ewt.el.CompiledExpression;
import org.everit.osgi.ewt.el.ExpressionCompiler;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.visitors.NodeVisitor;

public class EWTNodeVisitor extends NodeVisitor {

    private final StringBuilder currentSB = new StringBuilder();

    private final String ewtAttributePrefix;

    private final ExpressionCompiler expressionCompiler;

    private final List<Tag> tagBreadcumb = new ArrayList<Tag>();

    public EWTNodeVisitor(String ewtAttributePrefix, ExpressionCompiler expressionCompiler) {
        this.ewtAttributePrefix = ewtAttributePrefix;
        this.expressionCompiler = expressionCompiler;

    }

    private CompiledExpression compileExpression(PageAttribute attribute) {
        try {
            return expressionCompiler.compile(attribute.getValue());
        } catch (RuntimeException e) {
            // TODO
            return null;
        }
    }

    private void fillTagNode(TagNode tagNode, PageAttribute attribute) {
        String ewtAttributeName = attribute.getName().substring(ewtAttributePrefix.length());
        if (ewtAttributeName.equals("each")) {
            throwIfAttributeDefined(attribute, tagNode.getForeachExpression());
            tagNode.setForeachExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("var")) {
            throwIfAttributeDefined(attribute, tagNode.getVarExpression());
            tagNode.setForeachExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("render")) {
            throwIfAttributeDefined(attribute, tagNode.getRenderExpression());
            tagNode.setRenderExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("text")) {
            throwIfAttributeDefined(attribute, tagNode.getTextExpression());
            tagNode.setTextExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("utext")) {
            throwIfAttributeDefined(attribute, tagNode.getTextExpression());
            tagNode.setTextExpression(compileExpression(attribute));
            tagNode.setUnescapeText(true);
        } else if (ewtAttributeName.equals("attr")) {
            throwIfAttributeDefined(attribute, tagNode.getAttributeMapExpression());
            tagNode.setAttributeMapExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("attrprepend")) {
            throwIfAttributeDefined(attribute, tagNode.getAttributePrependMapExpression());
            tagNode.setAttributePrependMapExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("attrappend")) {
            throwIfAttributeDefined(attribute, tagNode.getAttributeAppendMapExpression());
            tagNode.setAttributeAppendMapExpression(compileExpression(attribute));
        } else if (ewtAttributeName.startsWith("attr-")) {
            String attrName = ewtAttributeName.substring("attr-".length());
            tagNode.getAttributeExpressions().put(attrName, compileExpression(attribute));
        } else if (ewtAttributeName.startsWith("attrprepend-")) {
            String attrName = ewtAttributeName.substring("attrprepend-".length());
            tagNode.getAttributePrependExpressions().put(attrName, compileExpression(attribute));
        } else if (ewtAttributeName.startsWith("attrappend-")) {
            String attrName = ewtAttributeName.substring("attrappend-".length());
            tagNode.getAttributeAppendExpressions().put(attrName, compileExpression(attribute));
        }
    }

    private boolean processTag(Tag tag) {
        @SuppressWarnings("unchecked")
        Vector<PageAttribute> attributes = tag.getAttributesEx();

        TagNode tagNode = null;
        for (PageAttribute attribute : attributes) {
            String attributeName = attribute.getName();
            if (attributeName != null && attributeName.startsWith(ewtAttributePrefix)) {
                if (tagNode == null) {
                    tagNode = new TagNode();
                }
                fillTagNode(tagNode, attribute);
            }
        }
        if (tagNode != null) {
            // TODO validate if text and parseBody not used together
            return true;
        }
        return false;
    }

    private void throwIfAttributeDefined(PageAttribute attribute, CompiledExpression foreachExpression) {
        // TODO Auto-generated method stub

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
        boolean ewtProcessed = processTag(tag);
        if (!ewtProcessed) {
            currentSB.append(tag.toTagHtml());
        }
    }

}
