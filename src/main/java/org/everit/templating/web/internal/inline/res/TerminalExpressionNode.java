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
package org.everit.templating.web.internal.inline.res;

import java.util.Map;

import org.everit.templating.web.TemplateWriter;
import org.everit.templating.web.el.CompiledExpression;
import org.everit.templating.web.el.ExpressionCompiler;
import org.everit.templating.web.internal.inline.InlineRuntime;

public class TerminalExpressionNode extends Node {
    private final CompiledExpression ce;

    public TerminalExpressionNode(final Node node, ExpressionCompiler expressionCompiler) {
        this.begin = node.begin;
        this.name = node.name;
        ce = expressionCompiler.compile(String.valueOf(node.contents, node.cStart, node.cEnd - node.cStart));
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    @Override
    public Object eval(final InlineRuntime runtime, final TemplateWriter appender, final Object ctx,
            final Map<String, Object> vars) {
        return ce.eval(vars);
    }
}