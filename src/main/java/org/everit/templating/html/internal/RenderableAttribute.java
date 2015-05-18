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

import org.htmlparser.lexer.PageAttribute;

/**
 * A tag attribute that will be rendered on the output of the HTML template.
 *
 */
public class RenderableAttribute {

  private CompiledExpressionHolder appendExpressionHolder = null;

  private PageAttribute appendPageAttribute = null;

  private String constantValue = null;

  private CompiledExpressionHolder expressionHolder = null;

  private PageAttribute expressionPageAttribute = null;

  private PageAttribute pageAttribute = null;

  private CompiledExpressionHolder prependExpressionHolder = null;

  private PageAttribute prependPageAttribute = null;

  private String previousText = null;

  public CompiledExpressionHolder getAppendExpressionHolder() {
    return appendExpressionHolder;
  }

  public PageAttribute getAppendPageAttribute() {
    return appendPageAttribute;
  }

  public String getConstantValue() {
    return constantValue;
  }

  public CompiledExpressionHolder getExpressionHolder() {
    return expressionHolder;
  }

  public PageAttribute getExpressionPageAttribute() {
    return expressionPageAttribute;
  }

  public PageAttribute getPageAttribute() {
    return pageAttribute;
  }

  public CompiledExpressionHolder getPrependExpressionHolder() {
    return prependExpressionHolder;
  }

  public PageAttribute getPrependPageAttribute() {
    return prependPageAttribute;
  }

  public String getPreviousText() {
    return previousText;
  }

  public void setAppendExpressionHolder(final CompiledExpressionHolder appendExpressionHolder) {
    this.appendExpressionHolder = appendExpressionHolder;
  }

  public void setAppendPageAttribute(final PageAttribute appendPageAttribute) {
    this.appendPageAttribute = appendPageAttribute;
  }

  public void setConstantValue(final String constantValue) {
    this.constantValue = constantValue;
  }

  public void setExpressionHolder(final CompiledExpressionHolder expressionHolder) {
    this.expressionHolder = expressionHolder;
  }

  public void setExpressionPageAttribute(final PageAttribute expressionPageAttribute) {
    this.expressionPageAttribute = expressionPageAttribute;
  }

  public void setPageAttribute(final PageAttribute pageAttribute) {
    this.pageAttribute = pageAttribute;
  }

  public void setPrependExpressionHolder(final CompiledExpressionHolder prependExpressionHolder) {
    this.prependExpressionHolder = prependExpressionHolder;
  }

  public void setPrependPageAttribute(final PageAttribute prependPageAttribute) {
    this.prependPageAttribute = prependPageAttribute;
  }

  public void setPreviousText(final String previousWhitespaces) {
    previousText = previousWhitespaces;
  }

}
