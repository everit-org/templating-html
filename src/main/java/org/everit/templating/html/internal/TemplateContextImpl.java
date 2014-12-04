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

import java.io.Writer;
import java.util.Map;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateContext;

public class TemplateContextImpl implements TemplateContext {

    private final String bookmark;

    private final CompiledTemplate compiledTemplate;

    private Map<String, Object> vars;

    private final TemplateWriter writer;

    public TemplateContextImpl(final CompiledTemplate compiledTemplate, final String bookmark,
            final Map<String, Object> vars, final Writer writer) {
        this.bookmark = bookmark;
        this.compiledTemplate = compiledTemplate;
        this.vars = vars;
        this.writer = new TemplateWriter(writer);
    }

    @Override
    public String getBookmark() {
        return bookmark;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public TemplateWriter getWriter() {
        return writer;
    }

    @Override
    public void renderBookmark(final String bookmark) {
        compiledTemplate.render(writer.getWrapped(), vars, bookmark);
    }

    public void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }

}
