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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import org.everit.osgi.ewt.el.ExpressionCompiler;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.visitors.NodeVisitor;

public class EWTNodeVisitor extends NodeVisitor {

    private static class VisitorPathElement {

        public ParentNode ewtNode;

        public Tag tag;

        public VisitorPathElement withEwtNode(ParentNode ewtNode) {
            this.ewtNode = ewtNode;
            return this;
        }

        public VisitorPathElement withTag(Tag tag) {
            this.tag = tag;
            return this;
        }

    }

    private StringBuilder currentSB = new StringBuilder();

    private final String ewtAttributePrefix;

    private final ExpressionCompiler expressionCompiler;

    private ParentNode parentNode;

    private final ParentNode rootNode;

    private final LinkedList<VisitorPathElement> visitorPath = new LinkedList<VisitorPathElement>();

    public EWTNodeVisitor(final String ewtAttributePrefix, final ExpressionCompiler expressionCompiler) {
        this.ewtAttributePrefix = ewtAttributePrefix;
        this.expressionCompiler = expressionCompiler;
        this.rootNode = new RootNode();
        this.parentNode = rootNode;
        visitorPath.add(new VisitorPathElement().withEwtNode(rootNode));

    }

    private void appendCurrentSBAndClear() {
        if (currentSB.length() > 0) {
            parentNode.getChildren().add(new TextNode(currentSB.toString(), false));
            currentSB = new StringBuilder();
        }
    }

    private CompiledExpressionHolder compileExpression(final PageAttribute attribute) {
        try {
            return new CompiledExpressionHolder(expressionCompiler.compile(attribute.getValue()), attribute);
        } catch (RuntimeException e) {
            // TODO
            return null;
        }
    }

    private void fillEwtTagNodeWithAttribute(final TagNode tagNode, final PageAttribute attribute,
            String textBeforeAttribute) {
        String attributeName = attribute.getName();
        if (attributeName.startsWith(ewtAttributePrefix)) {
            String ewtAttributeName = attributeName.substring(ewtAttributePrefix.length());
            if (ewtAttributeName.equals("each")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getForeachExpressionHolder());
                tagNode.setForeachExpressionHolder(compileExpression(attribute));
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
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeMapExpressionHolder());
                tagNode.setAttributeMapExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.equals("attrprepend")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributePrependMapExpressionHolder());
                tagNode.setAttributePrependMapExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.equals("attrappend")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeAppendMapExpressionHolder());
                tagNode.setAttributeAppendMapExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.startsWith("attr-")) {
                String attrName = ewtAttributeName.substring("attr-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getExpressionHolder());
                renderableAttribute.setExpressionHolder(compileExpression(attribute));
                renderableAttribute.setExpressionPageAttribute(attribute);
                if (renderableAttribute.getConstantValue() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (ewtAttributeName.startsWith("attrprepend-")) {
                String attrName = ewtAttributeName.substring("attrprepend-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getPrependExpressionHolder());
                renderableAttribute.setPrependExpressionHolder(compileExpression(attribute));
                renderableAttribute.setPrependPageAttribute(attribute);
                if (renderableAttribute.getExpressionHolder() == null && renderableAttribute.getConstantValue() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (ewtAttributeName.startsWith("attrappend-")) {
                String attrName = ewtAttributeName.substring("attrappend-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getAppendExpressionHolder());
                renderableAttribute.setAppendExpressionHolder(compileExpression(attribute));
                renderableAttribute.setAppendPageAttribute(attribute);
                if (renderableAttribute.getPreviousText() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            }
        } else {
            RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attributeName, tagNode, attribute);
            renderableAttribute.setPageAttribute(attribute);
            renderableAttribute.setPreviousText(textBeforeAttribute);
            if (renderableAttribute.getConstantValue() != null) {
                // TODO throw nice exception
            }
            renderableAttribute.setConstantValue(attribute.getValue());

            // TODO
        }

    }

    @Override
    public void finishedParsing() {
        appendCurrentSBAndClear();
    }

    private RenderableAttribute getOrCreateRenderableAttribute(String attrName, TagNode tagNode, PageAttribute attribute) {
        Map<String, RenderableAttribute> renderableAttributes = tagNode.getRenderableAttributes();
        RenderableAttribute renderableAttribute = renderableAttributes.get(attrName);
        if (renderableAttribute == null) {
            renderableAttribute = new RenderableAttribute();
            renderableAttributes.put(attrName, renderableAttribute);
        }
        return renderableAttribute;
    }

    private boolean isEwtNode(final Vector<PageAttribute> attributes) {

        for (PageAttribute pageAttribute : attributes) {
            String attributeName = pageAttribute.getName();
            if (attributeName != null && attributeName.startsWith(ewtAttributePrefix)) {
                return true;
            }
        }

        return false;
    }

    private void throwIfAttributeAlreadyDefined(final PageAttribute attribute,
            final CompiledExpressionHolder expression) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitEndTag(final Tag tag) {
        String tagName = tag.getTagName();
        ListIterator<VisitorPathElement> iterator = visitorPath.listIterator(visitorPath.size());
        VisitorPathElement found = null;
        while (found == null && iterator.hasPrevious()) {
            VisitorPathElement visitorPathElement = iterator.previous();
            if (visitorPathElement.tag != null && visitorPathElement.tag.getTagName().equals(tagName)) {
                found = visitorPathElement;
                iterator.remove();
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
            }
        }

        if (found == null || found.ewtNode == null) {
            currentSB.append(tag.toHtml(true));
        } else {
            ((TagNode) found.ewtNode).setEndTag(tag);

            boolean parentFound = false;
            while (!parentFound && iterator.hasPrevious()) {
                VisitorPathElement visitorPathElement = iterator.previous();
                if (visitorPathElement.ewtNode != null) {
                    parentNode = visitorPathElement.ewtNode;
                }
            }
        }
    }

    @Override
    public void visitRemarkNode(final Remark remark) {
        // TODO Auto-generated method stub
        super.visitRemarkNode(remark);
    }

    @Override
    public void visitStringNode(final Text string) {
        currentSB.append(string.toPlainTextString());
    }

    @Override
    public void visitTag(final Tag tag) {
        // TODO if render has a constant none value, return do not handle the tag at all for performance reasons
        @SuppressWarnings("unchecked")
        Vector<PageAttribute> attributes = tag.getAttributesEx();

        if (isEwtNode(attributes)) {
            appendCurrentSBAndClear();

            TagNode tagNode = new TagNode();
            tagNode.setTagName(tag.getRawTagName());
            Iterator<PageAttribute> iterator = attributes.iterator();
            // First one is the name of the tag
            iterator.next();

            StringBuffer previousString = new StringBuffer();
            while (iterator.hasNext()) {
                PageAttribute attribute = iterator.next();

                if (attribute.getName() == null || attribute.getAssignment() == null || attribute.getValue() == null) {
                    attribute.toString(previousString);
                } else {
                    fillEwtTagNodeWithAttribute(tagNode, attribute, previousString.toString());
                    previousString = new StringBuffer();
                }
            }
            // TODO validate if text and parseBody not used together

            parentNode.getChildren().add(tagNode);
            if (!tag.isEmptyXmlTag()) {
                visitorPath.add(new VisitorPathElement().withEwtNode(tagNode).withTag(tag));
                parentNode = tagNode;
            }
        } else {
            visitorPath.add(new VisitorPathElement().withTag(tag));
            currentSB.append(tag.toTagHtml());
        }
    }

}
