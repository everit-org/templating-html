/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.templating.html.internal;

import java.util.Collections;
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
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * {@link NodeVisitor} implementation that builds the {@link HTMLNode} tree.
 */
public class HTMLNodeVisitor extends NodeVisitor {

  /**
   * Context of a tag that has <code>data-eht-inline</code> attribute.
   */
  private static class InlineContext {

    public Coordinate position;

    public TemplateCompiler templateCompiler;

    InlineContext(final TemplateCompiler templateCompiler, final Coordinate position) {
      this.templateCompiler = templateCompiler;
      this.position = position;
    }
  }

  /**
   * Enumeration of the current mode of the visitor.
   */
  private enum VisitMode {
    INLINE, NONE, NORMAL
  }

  /**
   * Class that helps holding the current path where the visitor is at.
   */
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

  private static final Map<String, Object> EMPTY_VAR_MAP = Collections.emptyMap();

  private StringBuilder currentSB = new StringBuilder();

  private final String ehtAttributePrefix;

  private final ExpressionCompiler expressionCompiler;

  private final Map<String, TemplateCompiler> inlineCompilers;

  private InlineContext inlineContext = null;

  private ParentNode parentNode;

  private final ParserConfiguration parserConfiguration;

  private final RootNode rootNode;

  private int specialVisitDepth = 0;

  private Coordinate startPosition;

  private VisitMode visitMode = VisitMode.NORMAL;

  private LinkedList<VisitorPathElement> visitorPath = new LinkedList<>();

  /**
   * Constructor.
   *
   * @param ewtAttributePrefix
   *          The prefix of EHT attributes. By default it is "data-eht-".
   * @param expressionCompiler
   *          The compiler that compiles the el-expressions within the template.
   * @param inlineCompilers
   *          {@link Map} of inline compilers where the key is the name of the compiler and the
   *          value is the compiler itself. Used when <code>data-eht-inline</code> attribute is
   *          found.
   * @param parserConfiguration
   *          The configuration of the parser.
   */
  public HTMLNodeVisitor(final String ewtAttributePrefix,
      final ExpressionCompiler expressionCompiler,
      final Map<String, TemplateCompiler> inlineCompilers,
      final ParserConfiguration parserConfiguration) {
    ehtAttributePrefix = ewtAttributePrefix;
    this.inlineCompilers = inlineCompilers;
    this.parserConfiguration = parserConfiguration;
    rootNode = new RootNode();
    parentNode = rootNode;
    this.expressionCompiler = expressionCompiler;
    visitorPath.add(new VisitorPathElement().withEwtNode(rootNode));

    startPosition = new Coordinate(parserConfiguration.getStartRow(),
        parserConfiguration.getStartColumn());

  }

  private void appendCurrentSBAndClear() {
    if (currentSB.length() > 0) {
      parentNode.getChildren().add(new TextNode(currentSB.toString()));
      currentSB = new StringBuilder();
    }
  }

  private CompiledExpressionHolder compileExpression(final PageAttribute attribute,
      final TagInfo tagInfo) {
    String attributeValue = attribute.getValue();
    attributeValue = HTMLTemplatingUtil.unescape(attributeValue);

    ParserConfiguration currentParserConfig = new ParserConfiguration(parserConfiguration);

    Coordinate position = HTMLTemplatingUtil.calculateCoordinate(
        attribute.getPage(), attribute.getValueStartPosition(), startPosition);

    currentParserConfig.setStartColumn(position.column);
    currentParserConfig.setStartRow(position.row);

    return new CompiledExpressionHolder(expressionCompiler.compile(attributeValue,
        currentParserConfig),
        new AttributeInfo(attribute, tagInfo, startPosition));

  }

  private boolean fakeTag(final Tag tag) {
    int endPosition = tag.getEndPosition();
    return !">".equals(tag.getPage().getText(endPosition - 1, endPosition));
  }

  private void fillEhtTagNodeWithAttribute(final Tag tag, final TagNode tagNode,
      final PageAttribute attribute, final String textBeforeAttribute) {

    String attributeName = attribute.getName();
    if (attributeName.startsWith(ehtAttributePrefix)) {
      processEhtAttribute(tag, tagNode, attribute, textBeforeAttribute, attributeName);
    } else {
      RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attributeName,
          tagNode);
      renderableAttribute.setPageAttribute(attribute);
      renderableAttribute.setPreviousText(textBeforeAttribute);
      if (renderableAttribute.getConstantValue() != null) {
        HTMLTemplatingUtil.throwCompileExceptionForAttribute(getTemplateFileName(),
            "Duplicate attribute: " + attribute.getName(), tag, attribute, false, startPosition);
      }
      renderableAttribute.setConstantValue(HTMLTemplatingUtil.unescape(attribute.getValue()));
    }
  }

  private VisitorPathElement findBeginningPathElementForEndTag(final String tagName,
      final ListIterator<VisitorPathElement> iterator) {

    VisitorPathElement beginTagPathElement = null;
    while ((beginTagPathElement == null) && iterator.hasPrevious()) {
      VisitorPathElement visitorPathElement = iterator.previous();
      if ((visitorPathElement.tag != null) && visitorPathElement.tag.getTagName().equals(tagName)) {
        beginTagPathElement = visitorPathElement;
        iterator.remove();
        while (iterator.hasNext()) {
          iterator.next();
          iterator.remove();
        }
      }
    }
    return beginTagPathElement;
  }

  private void findParentNodeOnIteratorAndSet(final ListIterator<VisitorPathElement> iterator) {
    boolean parentFound = false;
    while (!parentFound && iterator.hasPrevious()) {
      VisitorPathElement visitorPathElement = iterator.previous();
      if (visitorPathElement.ewtNode != null) {
        parentFound = true;
        parentNode = visitorPathElement.ewtNode;
      }
    }
  }

  @Override
  public void finishedParsing() {
    appendCurrentSBAndClear();
  }

  private RenderableAttribute getOrCreateRenderableAttribute(final String attrName,
      final TagNode tagNode) {
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

  private String getTemplateFileName() {
    return parserConfiguration.getName();
  }

  private void handleEWTNode(final Tag tag, final Vector<PageAttribute> attributes) {
    appendCurrentSBAndClear();

    TagNode tagNode = new TagNode(tag, getTemplateFileName());
    tagNode.setTagName(tag.getRawTagName());
    Iterator<PageAttribute> iterator = attributes.iterator();
    // First attribute is the name of the tag
    iterator.next();

    StringBuffer previousString = new StringBuffer();
    while (iterator.hasNext()) {
      PageAttribute attribute = iterator.next();

      if ((attribute.getName() == null) || (attribute.getAssignment() == null)
          || (attribute.getValue() == null)) {
        attribute.toString(previousString);
      } else {
        fillEhtTagNodeWithAttribute(tag, tagNode, attribute, previousString.toString());
        previousString = new StringBuffer();
      }
    }

    TemplateCompiler inlineCompiler = resolveInlineCompiler(tag);

    if ((inlineCompiler != null) && (tagNode.getTextExpressionHolder() != null)) {
      HTMLTemplatingUtil.throwCompileExceptionForAttribute(getTemplateFileName(),
          "Inline and text cannot be used together within the same tag", tag, null, false,
          startPosition);
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
        int column = (page.column(tagEndPosition)
            + (pageRow == 0 ? startPosition.column : 1)) - 1;
        int row = startPosition.row + pageRow;

        inlineContext = new InlineContext(inlineCompiler, new Coordinate(row, column));
        specialVisitDepth = visitorPath.size();
      }
    }
  }

  private void handleFakeTag(final Tag tag) {
    if (visitMode != VisitMode.NONE) {
      currentSB.append(tag.getPage().getText(tag.getStartPosition(), tag.getEndPosition()));
    }
  }

  private void handleNonEWTNode(final Tag tag) {
    if (!tag.isEmptyXmlTag()) {
      visitorPath.add(new VisitorPathElement().withTag(tag));
    }
    currentSB.append(tag.toTagHtml());
  }

  private boolean handleRenderNoneTag(final Tag tag) {
    boolean renderNoneTag = false;
    if (renderNone(tag)) {
      if (!tag.isEmptyXmlTag()) {
        visitorPath.add(new VisitorPathElement().withTag(tag));
        visitMode = VisitMode.NONE;
        specialVisitDepth = visitorPath.size();
      }
      renderNoneTag = true;
    }
    return renderNoneTag;
  }

  private boolean handleTageInNoneAndInlineMode(final Tag tag) {
    boolean noneOrInlineTag = false;
    if ((visitMode == VisitMode.NONE) || (visitMode == VisitMode.INLINE)) {
      if (!tag.isEmptyXmlTag()) {
        visitorPath.add(new VisitorPathElement().withTag(tag));
      }
      if (visitMode == VisitMode.INLINE) {
        currentSB.append(tag.toHtml(true));
      }
      noneOrInlineTag = true;
    }
    return noneOrInlineTag;
  }

  private boolean isEwtNode(final Vector<PageAttribute> attributes) {

    for (PageAttribute pageAttribute : attributes) {
      String attributeName = pageAttribute.getName();
      if ((attributeName != null) && attributeName.startsWith(ehtAttributePrefix)) {
        return true;
      }
    }

    return false;
  }

  private void processAttrAppendPrefixedAttribute(final Tag tag, final TagNode tagNode,
      final PageAttribute attribute, final String textBeforeAttribute, final TagInfo tagInfo,
      final String ewtAttributeName) {
    String attrName = ewtAttributeName.substring("attrappend-".length());
    RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode);
    throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getAppendExpressionHolder(),
        tag);
    renderableAttribute.setAppendExpressionHolder(compileExpression(attribute, tagInfo));
    renderableAttribute.setAppendPageAttribute(attribute);
    if (renderableAttribute.getPreviousText() == null) {
      renderableAttribute.setPreviousText(textBeforeAttribute);
    }
  }

  private void processAttrPrefixedAttribute(final Tag tag, final TagNode tagNode,
      final PageAttribute attribute, final String textBeforeAttribute, final TagInfo tagInfo,
      final String ewtAttributeName) {
    String attrName = ewtAttributeName.substring("attr-".length());
    RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode);
    throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getExpressionHolder(), tag);
    renderableAttribute.setExpressionHolder(compileExpression(attribute, tagInfo));
    renderableAttribute.setExpressionPageAttribute(attribute);
    if (renderableAttribute.getConstantValue() == null) {
      renderableAttribute.setPreviousText(textBeforeAttribute);
    }
  }

  private void processAttrPrependPrefixedAttribute(final Tag tag, final TagNode tagNode,
      final PageAttribute attribute, final String textBeforeAttribute, final TagInfo tagInfo,
      final String ewtAttributeName) {
    String attrName = ewtAttributeName.substring("attrprepend-".length());
    RenderableAttribute renderableAttribute = getOrCreateRenderableAttribute(attrName, tagNode);
    throwIfAttributeAlreadyDefined(attribute, renderableAttribute.getPrependExpressionHolder(),
        tag);
    renderableAttribute.setPrependExpressionHolder(compileExpression(attribute, tagInfo));
    renderableAttribute.setPrependPageAttribute(attribute);
    if ((renderableAttribute.getExpressionHolder() == null)
        && (renderableAttribute.getConstantValue() == null)) {
      renderableAttribute.setPreviousText(textBeforeAttribute);
    }
  }

  private void processEhtAttribute(final Tag tag, final TagNode tagNode,
      final PageAttribute attribute, final String textBeforeAttribute, final String attributeName) {
    TagInfo tagInfo = new TagInfo(tag);
    String ehtAttributeName = attributeName.substring(ehtAttributePrefix.length());

    switch (ehtAttributeName) {
      case "fragment":
        processFragmentAttribute(tag, tagNode, attribute, tagInfo);
        break;
      case "foreach":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getForeachExpressionHolder(), tag);
        tagNode.setForeachExpressionHolder(compileExpression(attribute, tagInfo));
        break;
      case "code":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getVarExpressionHolder(), tag);
        tagNode.setCodeExpressionHolder(compileExpression(attribute, tagInfo));
        break;
      case "var":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getVarExpressionHolder(), tag);
        tagNode.setVarExpressionHolder(compileExpression(attribute, tagInfo));
        break;
      case "render":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getRenderExpressionHolder(), tag);
        tagNode.setRenderExpressionHolder(compileExpression(attribute, tagInfo));
        break;
      case "text":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder(), tag);
        tagNode.setTextExpressionHolder(compileExpression(attribute, tagInfo));
        tagNode.setEscapeText(true);
        break;
      case "utext":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getTextExpressionHolder(), tag);
        tagNode.setTextExpressionHolder(compileExpression(attribute, tagInfo));
        tagNode.setEscapeText(false);
        break;
      case "attr":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeMapExpressionHolder(), tag);
        tagNode.setAttributeMapExpressionHolder(compileExpression(attribute, tagInfo));
        break;
      case "attrprepend":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributePrependMapExpressionHolder(),
            tag);
        tagNode.setAttributePrependMapExpressionHolder(compileExpression(attribute, tagInfo));
        break;
      case "attrappend":
        throwIfAttributeAlreadyDefined(attribute, tagNode.getAttributeAppendMapExpressionHolder(),
            tag);
        tagNode.setAttributeAppendMapExpressionHolder(compileExpression(attribute, tagInfo));
        break;
      default:
        processNonFixedLengthEhtAttribute(tag, tagNode, attribute, textBeforeAttribute, tagInfo,
            ehtAttributeName);
        break;
    }
  }

  private void processFragmentAttribute(final Tag tag, final TagNode tagNode,
      final PageAttribute attribute, final TagInfo tagInfo) {
    CompiledExpressionHolder compileExpression = compileExpression(attribute, tagInfo);
    Object fragmentNameObj = compileExpression.compiledExpression.eval(EMPTY_VAR_MAP);

    if (fragmentNameObj == null) {
      HTMLTemplatingUtil.throwCompileExceptionForAttribute(getTemplateFileName(),
          "Null value defined as fragmentId", tag, attribute, false, startPosition);
    }

    rootNode.addFragment(getTemplateFileName(), String.valueOf(fragmentNameObj), tagNode, attribute,
        startPosition, tag);
  }

  private void processNonFixedLengthEhtAttribute(final Tag tag, final TagNode tagNode,
      final PageAttribute attribute,
      final String textBeforeAttribute, final TagInfo tagInfo, final String ehtAttributeName) {
    if (ehtAttributeName.startsWith("attr-")) {
      processAttrPrefixedAttribute(tag, tagNode, attribute, textBeforeAttribute, tagInfo,
          ehtAttributeName);
    } else if (ehtAttributeName.startsWith("attrprepend-")) {
      processAttrPrependPrefixedAttribute(tag, tagNode, attribute, textBeforeAttribute, tagInfo,
          ehtAttributeName);
    } else if (ehtAttributeName.startsWith("attrappend-")) {
      processAttrAppendPrefixedAttribute(tag, tagNode, attribute, textBeforeAttribute, tagInfo,
          ehtAttributeName);
    } else if (!"inline".equals(ehtAttributeName)) {
      HTMLTemplatingUtil.throwCompileExceptionForAttribute(getTemplateFileName(),
          "Unrecognized attribute name: " + attribute.getName(), tag, attribute, false,
          startPosition);
    }
  }

  private boolean renderNone(final Tag tag) {
    String renderAttributeName = ehtAttributePrefix + "render";
    String renderValue = tag.getAttribute(renderAttributeName);

    if (renderValue == null) {
      return false;
    }

    return HTMLTemplatingUtil.attributeConstantEquals("none", renderValue.trim())
        || "false".equalsIgnoreCase(renderValue.trim());
  }

  private TemplateCompiler resolveInlineCompiler(final Tag tag) {
    String inlineAttributeName = ehtAttributePrefix + "inline";

    Attribute attribute = tag.getAttributeEx(inlineAttributeName);

    if (attribute == null) {
      return null;
    }
    if (!(attribute instanceof PageAttribute)) {
      throw new RuntimeException(
          getTemplateFileName() + "attribute must be an instance of PageAttribute");
    }

    PageAttribute inlineAttribute = (PageAttribute) attribute;

    CompiledExpression compiledExpression =
        compileExpression(inlineAttribute, new TagInfo(tag)).compiledExpression;
    Object evaluatedInline = compiledExpression.eval(EMPTY_VAR_MAP);

    if (evaluatedInline != null) {
      TemplateCompiler inlineCompiler = inlineCompilers.get(evaluatedInline);
      if (inlineCompiler == null) {
        HTMLTemplatingUtil.throwCompileExceptionForAttribute(getTemplateFileName(),
            "No compiler found for inline type: " + evaluatedInline, tag, inlineAttribute, true,
            startPosition);
      }
      return inlineCompiler;
    }

    return null;
  }

  private void throwIfAttributeAlreadyDefined(final PageAttribute attribute,
      final CompiledExpressionHolder expression, final Tag tag) {
    if (expression != null) {
      HTMLTemplatingUtil.throwCompileExceptionForAttribute(getTemplateFileName(),
          "Attribute is defined more than once",
          tag, attribute, false, startPosition);
    }
  }

  @Override
  public void visitEndTag(final Tag tag) {
    String tagName = tag.getTagName();
    ListIterator<VisitorPathElement> iterator = visitorPath.listIterator(visitorPath.size());
    VisitorPathElement beginTagPathElement = findBeginningPathElementForEndTag(tagName, iterator);

    if (visitMode == VisitMode.NONE) {
      int depth = visitorPath.size();
      if (depth < specialVisitDepth) {
        visitMode = VisitMode.NORMAL;
      } else {
        return;
      }
      if (depth == (specialVisitDepth - 1)) {
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

    if ((beginTagPathElement == null) || (beginTagPathElement.ewtNode == null)) {
      currentSB.append(tag.toHtml(true));
    } else {
      if (currentSB.length() > 0) {
        parentNode.getChildren().add(new TextNode(currentSB.toString()));
        currentSB = new StringBuilder();
      }

      ((TagNode) beginTagPathElement.ewtNode).setEndTag(tag.toHtml(true));

      findParentNodeOnIteratorAndSet(iterator);
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
    startPosition = HTMLTemplatingUtil.calculateCoordinate(remark.getPage(),
        remark.getStartPosition()
            + remarkOpenTagLength,
        previousStartPosition);
    parentNode = new RootNode();
    visitorPath = new LinkedList<>();
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
    if (fakeTag(tag)) {
      handleFakeTag(tag);
      return;
    } else if (handleTageInNoneAndInlineMode(tag)) {
      return;
    } else if (handleRenderNoneTag(tag)) {
      return;
    }

    @SuppressWarnings("unchecked")
    Vector<PageAttribute> attributes = tag.getAttributesEx();

    if (isEwtNode(attributes)) {
      handleEWTNode(tag, attributes);
    } else {
      handleNonEWTNode(tag);
    }
  }
}
