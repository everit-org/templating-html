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

public class CommentNode extends Node {
    public CommentNode() {
    }

    public CommentNode(final int begin, final String name, final char[] template, final int start, final int end) {
        this.name = name;
        this.end = this.cEnd = end;
    }

    public CommentNode(final int begin, final String name, final char[] template, final int start, final int end,
            final Node next) {
        this.begin = begin;
        this.end = this.cEnd = end;
        this.next = next;
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    @Override
    public Object eval(final InlineRuntime runtime, final TemplateWriter appender, final Object ctx,
            final Map<String, Object> vars) {
        if (next != null) {
            return next.eval(runtime, appender, ctx, vars);
        } else {
            return null;
        }
    }
}
