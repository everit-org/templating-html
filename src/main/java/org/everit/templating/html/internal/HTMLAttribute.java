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

import org.everit.expression.CompiledExpression;
import org.htmlparser.lexer.PageAttribute;

/**
 * A {@link PageAttribute} together with the {@link CompiledExpression} it defines.
 */
public class HTMLAttribute {

  private final CompiledExpression expression;

  private final PageAttribute pageAttribute;

  public HTMLAttribute(final PageAttribute pageAttribute, final CompiledExpression expression) {
    this.pageAttribute = pageAttribute;
    this.expression = expression;
  }

  public CompiledExpression getExpression() {
    return expression;
  }

  public PageAttribute getPageAttribute() {
    return pageAttribute;
  }
}
