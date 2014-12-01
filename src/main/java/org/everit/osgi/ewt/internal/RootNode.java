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

import java.util.HashMap;
import java.util.Map;

import org.everit.osgi.ewt.TemplateWriter;

public class RootNode extends ParentNode {

    private final Map<String, TagNode> bookmarks = new HashMap<String, TagNode>();

    public void addBookmark(String name, TagNode tagNode) {
        if ("root".equals(name)) {
            // TODO throw exception
        }
        TagNode previous = bookmarks.get(name);
        if (previous != null) {
            // TODO throw exception
        }
        bookmarks.put(name, tagNode);
    }

    public ParentNode getBookmark(String name) {
        if ("root".equals(name)) {
            return this;
        }
        return bookmarks.get(name);
    }

    @Override
    public void render(TemplateWriter sb, Map<String, Object> vars) {
        renderChildren(sb, vars);
    }
}
