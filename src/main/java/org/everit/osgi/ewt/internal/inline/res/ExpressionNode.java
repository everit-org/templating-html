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
package org.everit.osgi.ewt.internal.inline.res;

import java.util.Map;

import org.everit.osgi.ewt.TemplateWriter;
import org.everit.osgi.ewt.el.CompiledExpression;
import org.everit.osgi.ewt.el.ExpressionCompiler;
import org.everit.osgi.ewt.internal.inline.InlineRuntime;

public class ExpressionNode extends Node {
    private final CompiledExpression ce;

    public ExpressionNode(final int begin, final String name, final char[] template, final int start,
            final int end, final ExpressionCompiler compiler) {
        this.begin = begin;
        this.name = name;
        this.contents = template;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        ce = compiler.compile(String.valueOf(template, cStart, cEnd - cStart));
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    @Override
    public Object eval(final InlineRuntime runtime, final TemplateWriter appender, final Object ctx,
            final Map<String, Object> vars) {
        appender.append(String.valueOf(ce.eval(vars)));
        return next != null ? next.eval(runtime, appender, ctx, vars) : null;
    }

    @Override
    public String toString() {
        return "ExpressionNode:" + name + "{" + (contents == null ? "" : new String(contents)) + "} (start=" + begin
                + ";end=" + end + ")";
    }
}
