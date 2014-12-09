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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.util.AbstractTemplateContext;
import org.everit.templating.util.TemplateWriter;

public class TemplateContextImpl extends AbstractTemplateContext {

    private final CompiledTemplate compiledTemplate;

    private final TemplateWriter writer;

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
