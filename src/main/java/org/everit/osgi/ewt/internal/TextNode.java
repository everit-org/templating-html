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
package org.everit.osgi.ewt.internal;

import java.util.Map;

import org.everit.osgi.ewt.TemplateWriter;
import org.everit.osgi.ewt.el.ExpressionCompiler;
import org.everit.osgi.ewt.internal.inline.CompiledInline;
import org.everit.osgi.ewt.internal.inline.InlineCompiler;

public class TextNode implements EWTNode {

    private final CompiledInline compiledInline;

    private final String text;

    public TextNode(String text, boolean inline, ExpressionCompiler expressionCompiler) {
        this.text = text;
        if (!inline) {
            this.compiledInline = null;
        } else {
            this.compiledInline = InlineCompiler.compileTemplate(text, expressionCompiler);
        }

    }

    @Override
    public void render(TemplateWriter writer, Map<String, Object> context) {
        if (compiledInline != null) {

        } else {
            writer.append(text);
        }
    }

}
