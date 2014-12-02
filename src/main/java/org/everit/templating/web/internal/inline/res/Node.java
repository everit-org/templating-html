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
import org.everit.templating.web.internal.inline.InlineRuntime;
import org.everit.templating.web.internal.util.EWTUtil;

public abstract class Node {
    protected int begin;
    protected int cEnd;
    protected char[] contents;
    protected int cStart;
    protected int end;
    protected String name;
    public Node next;
    protected Node terminus;

    public Node() {
    }

    public Node(final int begin, final String name, final char[] template, final int start, final int end) {
        this.begin = begin;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        this.name = name;
        this.contents = template;
        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
    }

    public Node(final int begin, final String name, final char[] template, final int start, final int end,
            final Node next) {
        this.name = name;
        this.begin = begin;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        this.contents = template;
        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
        this.next = next;
    }

    public void calculateContents(final char[] template) {
        this.contents = EWTUtil.subset(template, cStart, end - cStart);
    }

    public abstract boolean demarcate(Node terminatingNode, char[] template);

    public abstract Object eval(InlineRuntime runtime, TemplateWriter appender, Object ctx,
            Map<String, Object> vars);

    public int getBegin() {
        return begin;
    }

    public int getCEnd() {
        return cEnd;
    }

    public char[] getContents() {
        return contents;
    }

    public int getCStart() {
        return cStart;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return this.end - this.begin;
    }

    public String getName() {
        return name;
    }

    public Node getNext() {
        return next;
    }

    public Node getTerminus() {
        return terminus;
    }

    public boolean isOpenNode() {
        return false;
    }

    public void setBegin(final int begin) {
        this.begin = begin;
    }

    public void setCEnd(final int cEnd) {
        this.cEnd = cEnd;
    }

    public void setContents(final char[] contents) {
        this.contents = contents;
    }

    public void setCStart(final int cStart) {
        this.cStart = cStart;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Node setNext(final Node next) {
        return this.next = next;
    }

    public void setTerminus(final Node terminus) {
        this.terminus = terminus;
    }
}
