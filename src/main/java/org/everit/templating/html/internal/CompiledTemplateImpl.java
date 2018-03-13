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

import java.io.Writer;
import java.util.Map;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.FragmentNotFoundException;
import org.everit.templating.TemplateConstants;
import org.everit.templating.util.InheritantMap;

/**
 * The compiled HTML/XML template that can be rendered.
 */
public class CompiledTemplateImpl implements CompiledTemplate {

  private final RootNode rootNode;

  public CompiledTemplateImpl(final RootNode rootNode) {
    this.rootNode = rootNode;
  }

  @Override
  public void render(final Writer writer, final Map<String, Object> vars) {
    render(writer, vars, null);
  }

  @Override
  public void render(final Writer writer, final Map<String, Object> vars, final String fragmentId) {
    ParentNode parentNode;
    String evaluatedBookmark = fragmentId;

    if (fragmentId == null) {
      parentNode = rootNode;
      evaluatedBookmark = TemplateConstants.FRAGMENT_ROOT;
    } else {
      parentNode = rootNode.getFragment(fragmentId);
      if (parentNode == null) {
        throw new FragmentNotFoundException(
            "Could not find fragment [" + fragmentId + "] in HTML template");
      }
    }

    InheritantMap<String, Object> scopedVars = new InheritantMap<>(vars, false);
    TemplateContextImpl templateContext = new TemplateContextImpl(this, evaluatedBookmark,
        scopedVars, writer);
    scopedVars.putWithoutChecks(TemplateConstants.VAR_TEMPLATE_CONTEXT, templateContext);
    parentNode.render(templateContext);
  }
}
