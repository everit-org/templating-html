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

import java.util.HashMap;
import java.util.Map;

import org.everit.templating.html.internal.util.Coordinate;
import org.everit.templating.html.internal.util.HTMLTemplatingUtil;
import org.everit.templating.util.CompileException;
import org.htmlparser.Tag;
import org.htmlparser.lexer.PageAttribute;

/**
 * The main node of the HTML template.
 */
public class RootNode extends ParentNode {

  private final Map<String, TagNode> fragments = new HashMap<String, TagNode>();

  /**
   * Adds a fragment to the template that can be rendered directly.
   *
   * @param name
   *          The name of the fragment.
   * @param tagNode
   *          The fragment node.
   * @param attribute
   *          The attribute that contains the fragment definition.
   * @param templateStartCoordinate
   *          The beginning coordinates of the fragment.
   * @param tag
   *          The tag that contains the <code>data-eht-fragment</code> attribute.
   */
  public void addFragment(final String name, final TagNode tagNode, final PageAttribute attribute,
      final Coordinate templateStartCoordinate, final Tag tag) {
    if ("root".equals(name)) {
      HTMLTemplatingUtil.throwCompileExceptionForAttribute(
          "'root' cannot be used as a fragment id as it is reserved for the template itself", tag,
          attribute,
          true, templateStartCoordinate);
    }
    TagNode previous = fragments.get(name);
    if (previous != null) {
      TagInfo tagInfo = new TagInfo(tag);
      AttributeInfo attributeInfo = new AttributeInfo(attribute, tagInfo, templateStartCoordinate);

      CompileException e = new CompileException("Duplicate fragment: " + name,
          new TagInfo(tag).chars,
          attributeInfo.valueCursorInTag);
      e.setColumn(attributeInfo.valueStartCoordinate.column);
      e.setLineNumber(attributeInfo.valueStartCoordinate.row);
      throw e;
    }
    fragments.put(name, tagNode);
  }

  /**
   * Returns a fragment based on its name.
   *
   * @param name
   *          The name of the fragment.
   * @return the fragment node.
   */
  public ParentNode getFragment(final String name) {
    if ("root".equals(name)) {
      return this;
    }
    return fragments.get(name);
  }

  @Override
  public void render(final TemplateContextImpl templateContext) {
    renderChildren(templateContext);
  }
}
