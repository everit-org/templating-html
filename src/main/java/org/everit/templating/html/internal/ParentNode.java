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

import java.util.ArrayList;
import java.util.List;

/**
 * Any HTML tag that has children.
 */
public abstract class ParentNode implements HTMLNode {

  private final List<HTMLNode> children = new ArrayList<HTMLNode>();

  public List<HTMLNode> getChildren() {
    return children;
  }

  /**
   * Renders the children of the HTML tag.
   *
   * @param templateContext
   *          The current context of the rendering process.
   */
  protected void renderChildren(final TemplateContextImpl templateContext) {
    for (HTMLNode ewtNode : children) {
      ewtNode.render(templateContext);
    }
  }
}
