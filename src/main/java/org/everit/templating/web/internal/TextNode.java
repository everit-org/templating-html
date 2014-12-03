/**
 * This file is part of Everit - Web Templating.
 *
 * Everit - Web Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Web Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Web Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.templating.web.internal;

import java.io.StringReader;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;

public class TextNode implements HTMLNode {

    private CompiledTemplate compiledInline;

    private final String text;

    public TextNode(final String text, final boolean inline, final TemplateCompiler templateCompiler) {
        this.text = text;
        if (!inline) {
            this.compiledInline = null;
        } else {
            this.compiledInline = templateCompiler.compile(new StringReader(text));
        }
    }

    @Override
    public void render(final TemplateContextImpl templateContext) {
        if (compiledInline != null) {
            compiledInline.render(templateContext.getWriter().getWrapped(), templateContext.getVars());
        } else {
            templateContext.getWriter().append(text);
        }
    }

}
