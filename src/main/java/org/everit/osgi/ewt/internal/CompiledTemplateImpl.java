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

import java.io.IOException;
import java.util.Map;

import org.everit.osgi.ewt.CompiledTemplate;
import org.everit.osgi.ewt.EWTConstants;
import org.everit.osgi.ewt.TemplateWriter;

public class CompiledTemplateImpl implements CompiledTemplate {

    private final RootNode rootNode;

    public CompiledTemplateImpl(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public void render(TemplateWriter writer, Map<String, Object> vars) throws IOException {
        render(writer, vars, null);
    }

    @Override
    public void render(TemplateWriter writer, Map<String, Object> vars, String bookmark) throws IOException {
        if (bookmark == null) {
            InheritantMap<String, Object> scopedVars = new InheritantMap<String, Object>(vars);
            scopedVars.putInternal(EWTConstants.EWT_CONTEXT, new EWTContext("root"));
            rootNode.render(writer, vars);
        } else {
            ParentNode parentNode = rootNode.getBookmark(bookmark);
            if (parentNode == null) {
                return;
            }
            InheritantMap<String, Object> scopedVars = new InheritantMap<String, Object>(vars);
            scopedVars.putInternal(EWTConstants.EWT_CONTEXT, new EWTContext(bookmark));
            parentNode.render(writer, scopedVars);
        }
    }
}
