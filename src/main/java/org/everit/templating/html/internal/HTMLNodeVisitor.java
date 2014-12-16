/**
 * This file is part of Everit - HTML Templating.
 *
 * Everit - HTML Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTML Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTML Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.templating.html.internal;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.internal.util.HTMLTemplatingUtil;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

public class HTMLNodeVisitor extends NodeVisitor {

    private static class InlineContext {
        public int column;

        public int lineNumber;

        public TemplateCompiler templateCompiler;
    }

    private enum VisitMode {
        INLINE, NONE, NORMAL
    }

    private static class VisitorPathElement {

        public ParentNode ewtNode;

        public Tag tag;

        @Override
        public String toString() {
            if (tag == null) {
                return "*root*";
            }
            return tag.getTagName();
        }

        public VisitorPathElement withEwtNode(final ParentNode ewtNode) {
            this.ewtNode = ewtNode;
            return this;
        }

        public VisitorPathElement withTag(final Tag tag) {
            this.tag = tag;
            return this;
        }
    }

    private int column;

    private StringBuilder currentSB = new StringBuilder();

    private final String ewtAttributePrefix;

    private final ExpressionCompiler expressionCompiler;

    private final Map<String, TemplateCompiler> inlineCompilers;

    private InlineContext inlineContext = null;

    private int lineNumber;

    private ParentNode parentNode;

    private final ParserConfiguration parserConfiguration;

    private final RootNode rootNode;

    private int specialVisitDepth = 0;

    private VisitMode visitMode = VisitMode.NORMAL;

    private LinkedList<VisitorPathElement> visitorPath = new LinkedList<VisitorPathElement>();

    public HTMLNodeVisitor(final String ewtAttributePrefix, final ExpressionCompiler expressionCompiler,
            final Map<String, TemplateCompiler> inlineCompilers, final ParserConfiguration parserConfiguration) {
        this.ewtAttributePrefix = ewtAttributePrefix;
        this.inlineCompilers = inlineCompilers;
        this.parserConfiguration = parserConfiguration;
        this.rootNode = new RootNode();
        this.parentNode = rootNode;
        this.expressionCompiler = expressionCompiler;
        visitorPath.add(new VisitorPathElement().withEwtNode(rootNode));

        this.lineNumber = parserConfiguration.getLineNumber();
        this.column = parserConfiguration.getColumn();
    }

    private void appendCurrentSBAndClear() {
        if (currentSB.length() > 0) {
            parentNode.getChildren().add(new TextNode(currentSB.toString()));
            currentSB = new StringBuilder();
        }
    }

    private CompiledExpressionHolder compileExpression(final PageAttribute attribute) {
        try {
            String attributeValue = attribute.getValue();
            attributeValue = HTMLTemplatingUtil.unescape(attributeValue);

            ParserConfiguration currentParserConfig = new ParserConfiguration(parserConfiguration);
            // TODO

            return new CompiledExpressionHolder(expressionCompiler.compile(attributeValue, currentParserConfig),
                    attribute);
        } catch (RuntimeException e) {
            e.printStackTrace();
            // TODO throw nice exception
            return null;
        }
    }

    private void fillEwtTagNodeWithAttribute(final TagNode tagNode, final PageAttribute attribute,
            final String textBeforeAttribute) {
        String attributeName = attribute.getName();
        if (attributeName.startsWith(ewtAttributePrefix)) {
            String ewtAttributeName = attributeName.substring(ewtAttributePrefix.length());
            if (ewtAttributeName.equals("fragment")) {
                rootNode.addBookmark(HTMLTemplatingUtil.unescape(attribute.getValue()), tagNode);
            } else if (ewtAttributeName.equals("foreach")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getForeachExpressionHolder(), tagNode);
                tagNode.setForeachExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.equals("var")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getVarExpressionHolder(), tagNode);
                tagNode.setVarExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.equals("render")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getRenderExpressionHolder(), tagNode);
                tagNode.setRenderExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.equals("text")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder(), tagNode);
                tagNode.setTextExpressionHolder(compileExpression(attribute));
                tagNode.setEscapeText(true);
            } else if (ewtAttributeName.equals("utext")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder(), tagNode);
                tagNode.setTextExpressionHolder(compileExpression(attribute));
                tagNode.setEscapeText(false);
            } else if (ewtAttributeName.equals("attr")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeMapExpressionHolder(), tagNode);
                tagNode.setAttributeMapExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.equals("attrprepend")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributePrependMapExpressionHolder(), tagNode);
                tagNode.setAttributePrependMapExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.equals("attrappend")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeAppendMapExpressionHolder(), tagNode);
                tagNode.setAttributeAppendMapExpressionHolder(compileExpression(attribute));
            } else if (ewtAttributeName.startsWith("attr-")) {
                String attrName = ewtAttributeName.substring("attr-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getExpressionHolder(), tagNode);
                renderableAttribute.setExpressionHolder(compileExpression(attribute));
                renderableAttribute.setExpressionPageAttribute(attribute);
                if (renderableAttribute.getConstantValue() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (ewtAttributeName.startsWith("attrprepend-")) {
                String attrName = ewtAttributeName.substring("attrprepend-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getPrependExpressionHolder(), tagNode);
                renderableAttribute.setPrependExpressionHolder(compileExpression(attribute));
                renderableAttribute.setPrependPageAttribute(attribute);
                if (renderableAttribute.getExpressionHolder() == null
                        && renderableAttribute.getConstantValue() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (ewtAttributeName.startsWith("attrappend-")) {
                String attrName = ewtAttributeName.substring("attrappend-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getAppendExpressionHolder(), tagNode);
                renderableAttribute.setAppendExpressionHolder(compileExpression(attribute));
                renderableAttribute.setAppendPageAttribute(attribute);
                if (renderableAttribute.getPreviousText() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (!ewtAttributeName.equals("inline")) {
                throw new RuntimeException(ewtAttributeName);
                // TODO throw exception
            }
        } else {
            RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attributeName, tagNode, attribute);
            renderableAttribute.setPageAttribute(attribute);
            renderableAttribute.setPreviousText(textBeforeAttribute);
            if (renderableAttribute.getConstantValue() != null) {
                throw new RuntimeException();
                // TODO throw exception that tag is duplicated.
            }
            renderableAttribute.setConstantValue(attribute.getValue());
        }

    }

    @Override
    public void finishedParsing() {
        appendCurrentSBAndClear();
    }

    private RenderableAttribute getOrCreateRenderableAttribute(final String attrName, final TagNode tagNode,
            final PageAttribute attribute) {
        Map<String, RenderableAttribute> renderableAttributes = tagNode.getRenderableAttributes();
        RenderableAttribute renderableAttribute = renderableAttributes.get(attrName);
        if (renderableAttribute == null) {
            renderableAttribute = new RenderableAttribute();
            renderableAttributes.put(attrName, renderableAttribute);
        }
        return renderableAttribute;
    }

    public RootNode getRootNode() {
        return rootNode;
    }

    private void incrementLineAndColumn(final String text) {
        if (text == null) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                column = 1;
                lineNumber++;
            } else {
                column++;
            }
        }

    }

    private String inline(final Tag tag) {
        String renderAttributeName = ewtAttributePrefix + "inline";
        String renderValue = tag.getAttribute(renderAttributeName);
        return renderValue;
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

    private boolean renderNone(final Tag tag) {
        String renderAttributeName = ewtAttributePrefix + "render";
        String renderValue = tag.getAttribute(renderAttributeName);
        return HTMLTemplatingUtil.attributeConstantEquals("none", renderValue);
    }

    private void throwIfAttributeAlreadyDefined(final PageAttribute attribute,
            final CompiledExpressionHolder expression, final TagNode tagNode) {
        if (expression != null) {
            // TODO throw exception
        }
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

        if (visitMode == VisitMode.NONE) {
            int depth = visitorPath.size();
            if (depth <= specialVisitDepth) {
                visitMode = VisitMode.NORMAL;
            } else {
                return;
            }
            if (depth == specialVisitDepth) {
                return;
            }
        } else if (visitMode == VisitMode.INLINE) {
            int depth = visitorPath.size();
            if (depth > specialVisitDepth) {
                currentSB.append(tag.toHtml(true));
                return;
            }
            TemplateCompiler inlineCompiler = inlineCompilers.get(inlineContext);
            if (inlineCompiler == null) {
                throw new RuntimeException();
                // TODO throw nice exception
            }
            // TODO pass a parserConfiguration where position is actualized
            CompiledTemplate compiledInline = inlineCompiler.compile(currentSB.toString(),
                    parserConfiguration);
            parentNode.getChildren().add(new InlineNode(compiledInline));
            currentSB = new StringBuilder();
            visitMode = VisitMode.NORMAL;
        }

        if (found == null || found.ewtNode == null) {
            currentSB.append(tag.toHtml(true));
        } else {
            if (currentSB.length() > 0) {
                parentNode.getChildren().add(new TextNode(currentSB.toString()));
                currentSB = new StringBuilder();
            }

            ((TagNode) found.ewtNode).setEndTag(tag);

            boolean parentFound = false;
            while (!parentFound && iterator.hasPrevious()) {
                VisitorPathElement visitorPathElement = iterator.previous();
                if (visitorPathElement.ewtNode != null) {
                    parentFound = true;
                    parentNode = visitorPathElement.ewtNode;
                }
            }
        }
    }

    @Override
    public void visitRemarkNode(final Remark remark) {
        if (visitMode == VisitMode.NONE) {
            return;
        }

        if (visitMode == VisitMode.INLINE) {
            currentSB.append(remark.toHtml(true));
            return;
        }

        currentSB.append("<!--");
        String remarkText = remark.getText();
        Lexer lexer = new Lexer(remarkText);
        // lexer.setPosition(remark.getStartPosition() + 4);

        LinkedList<VisitorPathElement> previousVisitorPath = visitorPath;
        ParentNode previousParent = parentNode;

        visitorPath = new LinkedList<VisitorPathElement>();
        visitorPath.add(previousVisitorPath.getFirst());
        parentNode = new RootNode();
        try {
            for (Node node = lexer.nextNode(); node != null; node = lexer.nextNode()) {
                node.accept(this);
            }
        } catch (ParserException e) {
            e.printStackTrace();
            // TODO throw exception
        }
        visitorPath = previousVisitorPath;
        List<HTMLNode> remarkNodes = parentNode.getChildren();
        parentNode = previousParent;
        parentNode.getChildren().addAll(remarkNodes);

        currentSB.append("-->");
    }

    @Override
    public void visitStringNode(final Text text) {
        incrementLineAndColumn(text.getText());
        if (visitMode == VisitMode.NONE) {
            return;
        }
        currentSB.append(text.toPlainTextString());
    }

    @Override
    public void visitTag(final Tag tag) {
        if (visitMode == VisitMode.NONE || visitMode == VisitMode.INLINE) {
            if (!tag.isEmptyXmlTag()) {
                visitorPath.add(new VisitorPathElement().withTag(tag));
            }
            if (visitMode == VisitMode.INLINE) {
                currentSB.append(tag.toHtml(true));
            }
            return;
        }

        if (renderNone(tag)) {
            if (!tag.isEmptyXmlTag()) {
                visitorPath.add(new VisitorPathElement().withTag(tag));
                visitMode = VisitMode.NONE;
                specialVisitDepth = visitorPath.size();
            }
            return;
        }

        @SuppressWarnings("unchecked")
        Vector<PageAttribute> attributes = tag.getAttributesEx();

        if (isEwtNode(attributes)) {

            appendCurrentSBAndClear();

            TagNode tagNode = new TagNode(tag);
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

            String tmpInline = inline(tag);

            if (tmpInline != null && tagNode.getTextExpressionHolder() != null) {
                // TODO throw exception that the two attributes cannot be used together.
            }

            parentNode.getChildren().add(tagNode);
            if (!tag.isEmptyXmlTag()) {
                visitorPath.add(new VisitorPathElement().withEwtNode(tagNode).withTag(tag));
                parentNode = tagNode;
                if (tmpInline != null) {
                    visitMode = VisitMode.INLINE;
                    TemplateCompiler inlineCompiler = inlineCompilers.get(tmpInline);
                    if (inlineCompiler == null) {
                        throw new RuntimeException();
                        // TODO
                    }

                    InlineContext tmpInlineContext = new InlineContext();
                    tmpInlineContext.templateCompiler = inlineCompiler;
                    tmpInlineContext.column ;

                    this.inlineContext = tmpInline;
                    specialVisitDepth = visitorPath.size();
                }
            }
        } else {
            if (!tag.isEmptyXmlTag()) {
                visitorPath.add(new VisitorPathElement().withTag(tag));
            }
            currentSB.append(tag.toTagHtml());
        }
    }
}
