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
package org.everit.templating.web.internal.inline;

public class ExecutionStack {

    private StackElement element;

    private int size = 0;

    public int deepCount() {
        int count = 0;

        if (element == null) {
            return 0;
        }
        else {
            count++;
        }

        StackElement element = this.element;
        while ((element = element.next) != null) {
            count++;
        }
        return count;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Object peek() {
        if (size == 0) {
            return null;
        } else {
            return element.value;
        }
    }

    public Object pop() {
        if (size == 0) {
            return null;
        }
        try {
            size--;
            return element.value;
        } finally {
            element = element.next;
            assert size == deepCount();
        }
    }

    public void push(final Object o) {
        size++;
        element = new StackElement(element, o);
        assert size == deepCount();

    }

    @Override
    public String toString() {
        StackElement el = element;

        if (element == null) {
            return "<EMPTY>";
        }

        StringBuilder appender = new StringBuilder().append("[");
        do {
            appender.append(String.valueOf(el.value));
            if (el.next != null) {
                appender.append(", ");
            }
        } while ((el = el.next) != null);

        appender.append("]");

        return appender.toString();
    }
}
