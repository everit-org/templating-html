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

import org.everit.templating.web.internal.inline.res.Node;

public class CompiledInline {
    private Node root;
    private char[] template;

    public CompiledInline(final char[] template, final Node root) {
        this.template = template;
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public char[] getTemplate() {
        return template;
    }

    public void setRoot(final Node root) {
        this.root = root;
    }

    public void setTemplate(final char[] template) {
        this.template = template;
    }
}
