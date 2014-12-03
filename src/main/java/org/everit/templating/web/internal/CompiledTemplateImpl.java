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
package org.everit.templating.web.internal;

import java.io.Writer;
import java.util.Map;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateConstants;

public class CompiledTemplateImpl implements CompiledTemplate {

    private final RootNode rootNode;

    public CompiledTemplateImpl(final RootNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public void render(final Writer writer, final Map<String, Object> vars) {
        render(writer, vars, null);
    }

    @Override
    public void render(final Writer writer, final Map<String, Object> vars, final String bookmark) {
        ParentNode parentNode;
        String evaluatedBookmark = bookmark;

        if (bookmark == null) {
            parentNode = rootNode;
            evaluatedBookmark = TemplateConstants.BOOKMARK_ROOT;
        } else {
            parentNode = rootNode.getBookmark(bookmark);
            if (parentNode == null) {
                return;
            }
        }

        InheritantMap<String, Object> scopedVars = new InheritantMap<String, Object>(vars);
        TemplateContextImpl templateContext = new TemplateContextImpl(this, evaluatedBookmark, scopedVars, writer);
        scopedVars.putInternal(TemplateConstants.VAR_TEMPLATE_CONTEXT,
                templateContext);
        parentNode.render(templateContext);
    }
}
