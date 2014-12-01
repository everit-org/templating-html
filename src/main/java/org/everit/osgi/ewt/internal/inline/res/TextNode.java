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
import org.everit.osgi.ewt.internal.inline.InlineRuntime;

public class TextNode extends Node {
    public TextNode(final int begin, final int end) {
        this.begin = begin;
        this.end = end;
    }

    public TextNode(final int begin, final int end, final ExpressionNode next) {
        this.begin = begin;
        this.end = end;
        this.next = next;
    }

    @Override
    public void calculateContents(final char[] template) {
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    @Override
    public Object eval(final InlineRuntime runtime, final TemplateWriter appender, final Object ctx,
            final Map<String, Object> vars) {
        int len = end - begin;
        if (len != 0) {
            appender.append(new String(runtime.getTemplate(), begin, len));
        }
        return next != null ? next.eval(runtime, appender, ctx, vars) : null;
    }

    @Override
    public String toString() {
        return "TextNode(" + begin + "," + end + ")";
    }
}
