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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.everit.osgi.ewt.TemplateWriter;

public abstract class ParentNode implements EWTNode {

    private final List<EWTNode> children = new ArrayList<EWTNode>();

    public List<EWTNode> getChildren() {
        return children;
    }

    protected void renderChildren(TemplateWriter writer, Map<String, Object> vars) {
        for (EWTNode ewtNode : children) {
            ewtNode.render(writer, vars);
        }
    }
}
