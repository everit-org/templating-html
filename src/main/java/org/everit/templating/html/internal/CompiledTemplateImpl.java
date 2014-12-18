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

import java.io.Writer;
import java.util.Map;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateConstants;
import org.everit.templating.util.InheritantMap;

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
    public void render(final Writer writer, final Map<String, Object> vars, final String fragmentId) {
        ParentNode parentNode;
        String evaluatedBookmark = fragmentId;

        if (fragmentId == null) {
            parentNode = rootNode;
            evaluatedBookmark = TemplateConstants.FRAGMENT_ROOT;
        } else {
            parentNode = rootNode.getFragment(fragmentId);
            if (parentNode == null) {
                return;
            }
        }

        InheritantMap<String, Object> scopedVars = new InheritantMap<String, Object>(vars, false);
        TemplateContextImpl templateContext = new TemplateContextImpl(this, evaluatedBookmark, scopedVars, writer);
        scopedVars.putWithoutChecks(TemplateConstants.VAR_TEMPLATE_CONTEXT, templateContext);
        parentNode.render(templateContext);
    }
}
