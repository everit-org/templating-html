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

    private final ParentNode parentNode;

    private final ParentNode rootNode;

    private final List<Tag> tagBreadcumb = new ArrayList<Tag>();

    public EWTNodeVisitor(String ewtAttributePrefix, ExpressionCompiler expressionCompiler) {
        this.ewtAttributePrefix = ewtAttributePrefix;
        this.expressionCompiler = expressionCompiler;
        this.rootNode = new RootNode();
        this.parentNode = this.rootNode;

    }

    private CompiledExpressionHolder compileExpression(PageAttribute attribute) {
        try {
            return new CompiledExpressionHolder(expressionCompiler.compile(attribute.getValue()), attribute);
        } catch (RuntimeException e) {
            // TODO
            return null;
        }
    }

    private void fillEwtTagNodeWithAttribute(TagNode tagNode, PageAttribute attribute) {
        String ewtAttributeName = attribute.getName().substring(ewtAttributePrefix.length());
        if (ewtAttributeName.equals("each")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getForeachExpressionHolder());
            tagNode.setForeachExpressionHolder(compileExpression(attribute), attribute);
        } else if (ewtAttributeName.equals("var")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getVarExpressionHolder());
            tagNode.setVarExpressionHolder(compileExpression(attribute));
        } else if (ewtAttributeName.equals("render")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getRenderExpressionHolder());
            tagNode.setRenderExpressionHolder(compileExpression(attribute));
        } else if (ewtAttributeName.equals("text")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder());
            tagNode.setTextExpressionHolder(compileExpression(attribute));
            tagNode.setEscapeText(true);
        } else if (ewtAttributeName.equals("utext")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder());
            tagNode.setTextExpressionHolder(compileExpression(attribute));
            tagNode.setEscapeText(false);
        } else if (ewtAttributeName.equals("attr")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeMapExpression());
            tagNode.setAttributeMapExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("attrprepend")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributePrependMapExpression());
            tagNode.setAttributePrependMapExpression(compileExpression(attribute));
        } else if (ewtAttributeName.equals("attrappend")) {
            throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeAppendMapExpression());
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

    private boolean isEwtNode(Vector<PageAttribute> attributes) {

        for (PageAttribute pageAttribute : attributes) {
            String attributeName = pageAttribute.getName();
            if (attributeName != null && attributeName.startsWith(ewtAttributePrefix)) {
                return true;
            }
        }

        return false;
    }

    private void throwIfAttributeAlreadyDefined(PageAttribute attribute, CompiledExpressionHolder foreachExpression) {
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
        // TODO if render has a constant node value, return do not handle the tag at all for performance reasons
        @SuppressWarnings("unchecked")
        Vector<PageAttribute> attributes = tag.getAttributesEx();

        if (isEwtNode(attributes)) {
            if (currentSB.length() > 0) {
                parentNode.getChildren().add(new TextNode(currentSB.toString(), false));
            }

            TagNode tagNode = new TagNode();
            for (PageAttribute attribute : attributes) {
                tagNode.getPageAttributes().add(attribute);
                fillEwtTagNodeWithAttribute(tagNode, attribute);
            }
            // TODO validate if text and parseBody not used together
        } else {
            currentSB.append(tag.toTagHtml());
        }
    }

}
