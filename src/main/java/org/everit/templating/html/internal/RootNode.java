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

import org.everit.templating.html.internal.util.Coordinate;
import org.everit.templating.html.internal.util.HTMLTemplatingUtil;
import org.everit.templating.util.CompileException;
import org.htmlparser.Tag;
import org.htmlparser.lexer.PageAttribute;

public class RootNode extends ParentNode {

    private final Map<String, TagNode> fragments = new HashMap<String, TagNode>();

    public void addFragment(final String name, final TagNode tagNode, final PageAttribute attribute,
            final Coordinate templateStartCoordinate, final Tag tag) {
        if ("root".equals(name)) {
            HTMLTemplatingUtil.throwCompileExceptionForAttribute(
                    "'root' cannot be used as a fragment id as it is reserved for the template itself", tag, attribute,
                    true, templateStartCoordinate);
        }
        TagNode previous = fragments.get(name);
        if (previous != null) {
            TagInfo tagInfo = new TagInfo(tag);
            AttributeInfo attributeInfo = new AttributeInfo(attribute, tagInfo, templateStartCoordinate);

            CompileException e = new CompileException("Duplicate fragment: " + name, new TagInfo(tag).chars,
                    attributeInfo.valueCursorInTag);
            e.setColumn(attributeInfo.valueStartCoordinate.column);
            e.setLineNumber(attributeInfo.valueStartCoordinate.row);
            throw e;
        }
        fragments.put(name, tagNode);
    }

    public ParentNode getFragment(final String name) {
        if ("root".equals(name)) {
            return this;
        }
        return fragments.get(name);
    }

    @Override
    public void render(final TemplateContextImpl templateContext) {
        renderChildren(templateContext);
    }
}
