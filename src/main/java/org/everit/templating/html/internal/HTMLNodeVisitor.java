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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import org.everit.expression.CompiledExpression;
import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.internal.util.Coordinate;
import org.everit.templating.html.internal.util.HTMLTemplatingUtil;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

public class HTMLNodeVisitor extends NodeVisitor {

    private static class InlineContext {

        public Coordinate position;

        public TemplateCompiler templateCompiler;

        public InlineContext(final TemplateCompiler templateCompiler, final Coordinate position) {
            this.templateCompiler = templateCompiler;
            this.position = position;
        }
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

    private StringBuilder currentSB = new StringBuilder();

    private final String ewtAttributePrefix;

    private final ExpressionCompiler expressionCompiler;

    private final Map<String, TemplateCompiler> inlineCompilers;

    private InlineContext inlineContext = null;

    private ParentNode parentNode;

    private final ParserConfiguration parserConfiguration;

    private final RootNode rootNode;

    private int specialVisitDepth = 0;

    private Coordinate startPosition;

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

        this.startPosition = new Coordinate(parserConfiguration.getStartRow(), parserConfiguration.getStartColumn());

    }

    private void appendCurrentSBAndClear() {
        if (currentSB.length() > 0) {
            parentNode.getChildren().add(new TextNode(currentSB.toString()));
            currentSB = new StringBuilder();
        }
    }

    private CompiledExpressionHolder compileExpression(final PageAttribute attribute, final TagInfo tagInfo) {
        String attributeValue = attribute.getValue();
        attributeValue = HTMLTemplatingUtil.unescape(attributeValue);

        ParserConfiguration currentParserConfig = new ParserConfiguration(parserConfiguration);

        Coordinate position = HTMLTemplatingUtil.calculateCoordinate(
                attribute.getPage(), attribute.getValueStartPosition(), startPosition);

        currentParserConfig.setStartColumn(position.column);
        currentParserConfig.setStartRow(position.row);

        return new CompiledExpressionHolder(expressionCompiler.compile(attributeValue, currentParserConfig),
                new AttributeInfo(attribute, tagInfo, startPosition));

    }

    private void fillEwtTagNodeWithAttribute(final Tag tag, final TagNode tagNode, final PageAttribute attribute,
            final String textBeforeAttribute) {
        String attributeName = attribute.getName();
        if (attributeName.startsWith(ewtAttributePrefix)) {
            TagInfo tagInfo = new TagInfo(tag);
            String ewtAttributeName = attributeName.substring(ewtAttributePrefix.length());
            if (ewtAttributeName.equals("fragment")) {
                CompiledExpressionHolder compileExpression = compileExpression(attribute, tagInfo);
                Object fragmentNameObj = compileExpression.compiledExpression.eval(new HashMap<String, Object>());

                if (fragmentNameObj == null) {
                    HTMLTemplatingUtil.throwCompileExceptionForAttribute("Null value defined as fragmentId", tag,
                            attribute, false, startPosition);
                }

                rootNode.addFragment(String.valueOf(fragmentNameObj), tagNode, attribute,
                        startPosition, tag);
            } else if (ewtAttributeName.equals("foreach")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getForeachExpressionHolder(), tag);
                tagNode.setForeachExpressionHolder(compileExpression(attribute, tagInfo));
            } else if (ewtAttributeName.equals("code")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getVarExpressionHolder(), tag);
                tagNode.setCodeExpressionHolder(compileExpression(attribute, tagInfo));
            } else if (ewtAttributeName.equals("var")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getVarExpressionHolder(), tag);
                tagNode.setVarExpressionHolder(compileExpression(attribute, tagInfo));
            } else if (ewtAttributeName.equals("render")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getRenderExpressionHolder(), tag);
                tagNode.setRenderExpressionHolder(compileExpression(attribute, tagInfo));
            } else if (ewtAttributeName.equals("text")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder(), tag);
                tagNode.setTextExpressionHolder(compileExpression(attribute, tagInfo));
                tagNode.setEscapeText(true);
            } else if (ewtAttributeName.equals("utext")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder(), tag);
                tagNode.setTextExpressionHolder(compileExpression(attribute, tagInfo));
                tagNode.setEscapeText(false);
            } else if (ewtAttributeName.equals("attr")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeMapExpressionHolder(), tag);
                tagNode.setAttributeMapExpressionHolder(compileExpression(attribute, tagInfo));
            } else if (ewtAttributeName.equals("attrprepend")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributePrependMapExpressionHolder(), tag);
                tagNode.setAttributePrependMapExpressionHolder(compileExpression(attribute, tagInfo));
            } else if (ewtAttributeName.equals("attrappend")) {
                throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeAppendMapExpressionHolder(), tag);
                tagNode.setAttributeAppendMapExpressionHolder(compileExpression(attribute, tagInfo));
            } else if (ewtAttributeName.startsWith("attr-")) {
                String attrName = ewtAttributeName.substring("attr-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getExpressionHolder(), tag);
                renderableAttribute.setExpressionHolder(compileExpression(attribute, tagInfo));
                renderableAttribute.setExpressionPageAttribute(attribute);
                if (renderableAttribute.getConstantValue() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (ewtAttributeName.startsWith("attrprepend-")) {
                String attrName = ewtAttributeName.substring("attrprepend-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getPrependExpressionHolder(), tag);
                renderableAttribute.setPrependExpressionHolder(compileExpression(attribute, tagInfo));
                renderableAttribute.setPrependPageAttribute(attribute);
                if (renderableAttribute.getExpressionHolder() == null
                        && renderableAttribute.getConstantValue() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (ewtAttributeName.startsWith("attrappend-")) {
                String attrName = ewtAttributeName.substring("attrappend-".length());
                RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode, attribute);
                throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getAppendExpressionHolder(), tag);
                renderableAttribute.setAppendExpressionHolder(compileExpression(attribute, tagInfo));
                renderableAttribute.setAppendPageAttribute(attribute);
                if (renderableAttribute.getPreviousText() == null) {
                    renderableAttribute.setPreviousText(textBeforeAttribute);
                }
            } else if (!ewtAttributeName.equals("inline")) {
                HTMLTemplatingUtil.throwCompileExceptionForAttribute(
                        "Unrecognized attribute name: " + attribute.getName(), tag, attribute, false, startPosition);
            }
        } else {
            RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attributeName, tagNode, attribute);
            renderableAttribute.setPageAttribute(attribute);
            renderableAttribute.setPreviousText(textBeforeAttribute);
            if (renderableAttribute.getConstantValue() != null) {
                HTMLTemplatingUtil.throwCompileExceptionForAttribute(
                        "Duplicate attribute: " + attribute.getName(), tag, attribute, false, startPosition);
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

        if (renderValue == null) {
            return false;
        }

        return HTMLTemplatingUtil.attributeConstantEquals("none", renderValue.trim())
                || "false".equalsIgnoreCase(renderValue.trim());
    }

    private TemplateCompiler resolveInlineCompiler(final Tag tag) {
        String inlineAttributeName = ewtAttributePrefix + "inline";
        PageAttribute inlineAttribute = (PageAttribute) tag.getAttributeEx(inlineAttributeName);

        if (inlineAttribute != null) {
            CompiledExpression compiledExpression =
                    compileExpression(inlineAttribute, new TagInfo(tag)).compiledExpression;
            Object evaluatedInline = compiledExpression.eval(new HashMap<String, Object>());

            if (evaluatedInline != null) {
                TemplateCompiler inlineCompiler = inlineCompilers.get(evaluatedInline);
                if (inlineCompiler == null) {
                    HTMLTemplatingUtil.throwCompileExceptionForAttribute("No compiler found for inline type: "
                            + evaluatedInline, tag, inlineAttribute, true, startPosition);
                }
                return inlineCompiler;
            }

        }

        return null;
    }

    private void throwIfAttributeAlreadyDefined(final PageAttribute attribute,
            final CompiledExpressionHolder expression, final Tag tag) {
        if (expression != null) {
            HTMLTemplatingUtil.throwCompileExceptionForAttribute("Attribute is defined more than once", tag, attribute,
                    false, startPosition);
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
            if (depth < specialVisitDepth) {
                visitMode = VisitMode.NORMAL;
            } else {
                return;
            }
            if (depth == specialVisitDepth - 1) {
                return;
            }
        } else if (visitMode == VisitMode.INLINE) {
            int depth = visitorPath.size();
            if (depth > specialVisitDepth) {
                currentSB.append(tag.toHtml(true));
                return;
            }

            TemplateCompiler inlineCompiler = inlineContext.templateCompiler;
            ParserConfiguration inlinePC = new ParserConfiguration(parserConfiguration.getClassLoader());
            inlinePC.setStartColumn(inlineContext.position.column);
            inlinePC.setStartRow(inlineContext.position.row);
            CompiledTemplate compiledInline = inlineCompiler.compile(currentSB.toString(), inlinePC);
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

            ((TagNode) found.ewtNode).setEndTag(tag.toHtml(true));

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

        String remarkOpenTag = "<!--";
        int remarkOpenTagLength = remarkOpenTag.length();
        currentSB.append(remarkOpenTag);
        String remarkText = remark.getText();
        Lexer lexer = new Lexer(remarkText);
        // lexer.setPosition(remark.getStartPosition() + 4);

        LinkedList<VisitorPathElement> previousVisitorPath = visitorPath;
        ParentNode previousParent = parentNode;

        Coordinate previousStartPosition = startPosition;
        startPosition = HTMLTemplatingUtil.calculateCoordinate(remark.getPage(), remark.getStartPosition()
                + remarkOpenTagLength, previousStartPosition);
        parentNode = new RootNode();
        visitorPath = new LinkedList<VisitorPathElement>();
        visitorPath.add(new VisitorPathElement().withEwtNode(parentNode));

        try {
            for (Node node = lexer.nextNode(); node != null; node = lexer.nextNode()) {
                node.accept(this);
            }
        } catch (ParserException e) {
            throw new UncheckedParserException(e);
        }
        visitorPath = previousVisitorPath;
        List<HTMLNode> remarkNodes = parentNode.getChildren();
        parentNode = previousParent;
        parentNode.getChildren().addAll(remarkNodes);
        startPosition = previousStartPosition;

        currentSB.append("-->");
    }

    @Override
    public void visitStringNode(final Text text) {
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
            // First attribute is the name of the tag
            iterator.next();

            StringBuffer previousString = new StringBuffer();
            while (iterator.hasNext()) {
                PageAttribute attribute = iterator.next();

                if (attribute.getName() == null || attribute.getAssignment() == null || attribute.getValue() == null) {
                    attribute.toString(previousString);
                } else {
                    fillEwtTagNodeWithAttribute(tag, tagNode, attribute, previousString.toString());
                    previousString = new StringBuffer();
                }
            }

            TemplateCompiler inlineCompiler = resolveInlineCompiler(tag);

            if (inlineCompiler != null && tagNode.getTextExpressionHolder() != null) {
                HTMLTemplatingUtil.throwCompileExceptionForAttribute(
                        "Inline and text cannot be used together within the same tag", tag, null, false, startPosition);
            }

            parentNode.getChildren().add(tagNode);
            if (!tag.isEmptyXmlTag()) {
                visitorPath.add(new VisitorPathElement().withEwtNode(tagNode).withTag(tag));
                parentNode = tagNode;
                if (inlineCompiler != null) {
                    visitMode = VisitMode.INLINE;

                    int tagEndPosition = tag.getEndPosition() + 1;
                    Page page = tag.getPage();

                    int pageRow = page.row(tagEndPosition);
                    int column = page.column(tagEndPosition) + (pageRow == 0 ? startPosition.column : 1) - 1;
                    int row = startPosition.row + pageRow;

                    this.inlineContext = new InlineContext(inlineCompiler, new Coordinate(row, column));
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
