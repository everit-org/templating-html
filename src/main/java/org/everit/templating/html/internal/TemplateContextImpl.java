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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.util.AbstractTemplateContext;
import org.everit.templating.util.TemplateWriter;

/**
 * Template rendering context.
 */
public class TemplateContextImpl extends AbstractTemplateContext {

  private final CompiledTemplate compiledTemplate;

  private final TemplateWriter writer;

  /**
   * Constructor.
   *
   * @param compiledTemplate
   *          The compiled template that is rendered.
   * @param fragmentId
   *          The id of the fragment that is rendered or <code>null</code> if the full template is
   *          rendered.
   * @param vars
   *          The variables that is used to render the template.
   * @param writer
   *          The writer where the rendered template will be written to.
   */
  public TemplateContextImpl(final CompiledTemplate compiledTemplate, final String fragmentId,
      final Map<String, Object> vars, final Writer writer) {
    super(fragmentId, vars);
    this.compiledTemplate = compiledTemplate;
    this.writer = new TemplateWriter(writer);
  }

  public TemplateWriter getWriter() {
    return writer;
  }

  @Override
  public String renderFragmentInternal(final String fragmentId, final Map<String, Object> vars) {
    StringWriter stringWriter = new StringWriter();

    compiledTemplate.render(stringWriter, vars, fragmentId);

    return stringWriter.toString();
  }

}
