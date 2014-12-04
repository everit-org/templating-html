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

import java.util.HashMap;
import java.util.Map;

public class RootNode extends ParentNode {

    private final Map<String, TagNode> bookmarks = new HashMap<String, TagNode>();

    public void addBookmark(final String name, final TagNode tagNode) {
        if ("root".equals(name)) {
            // TODO throw nice exception.
            // throw new CompileException("'root' cannot be used as a boomark name as it is reserved for the"
            // + " root element");
        }
        TagNode previous = bookmarks.get(name);
        if (previous != null) {
            // TODO throw nice exception
            // throw new CompileException("Duplicate bookmark: " + name);
        }
        bookmarks.put(name, tagNode);
    }

    public ParentNode getBookmark(final String name) {
        if ("root".equals(name)) {
            return this;
        }
        return bookmarks.get(name);
    }

    @Override
    public void render(final TemplateContextImpl templateContext) {
        renderChildren(templateContext);
    }
}
