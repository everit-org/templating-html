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
package org.everit.templating.web;

import java.util.Map;

/**
 * A HTML / XML template that is compiled and can render output based on specified variables.
 *
 */
public interface CompiledTemplate {

    /**
     * Renders the output of a compiled template.
     *
     * @param writer
     *            The output of the template will be written to the writer.
     * @param vars
     *            The variables that will be used during the template rendering.
     */
    void render(TemplateWriter writer, Map<String, Object> vars);

    /**
     * Renders a part of a compiled template.
     *
     * @param writer
     *            The output of the template will be written to the writer.
     * @param vars
     *            The variables that will be used during the template rendering.
     * @param bookmark
     *            Identifies the part of the template that should be rendered. In case the bookmark is null, the full
     *            template will be processed.
     */
    void render(TemplateWriter writer, Map<String, Object> vars, String bookmark);

}
