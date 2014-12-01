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
package org.everit.osgi.ewt.internal.inline;

import java.util.Map;

import org.everit.osgi.ewt.TemplateWriter;
import org.everit.osgi.ewt.internal.inline.res.Node;

/**
 * This is the root of the template runtime, and contains various utility methods for executing templates.
 */
public class InlineRuntime {

    public static Object execute(final Node root, final char[] template,
            final TemplateWriter appender, final Object context,
            final Map<String, Object> vars) {

        return new InlineRuntime(template, root, ".").execute(appender, context, vars);
    }

    public static Object execute(final Node root, final char[] template,
            final TemplateWriter appender, final Object context,
            final Map<String, Object> vars, final String baseDir) {

        return new InlineRuntime(template, root, baseDir).execute(appender, context, vars);
    }

    private final String baseDir;

    private ExecutionStack relPath;

    private Node rootNode;

    private char[] template;

    public InlineRuntime(final char[] template, final Node rootNode, final String baseDir) {
        this.template = template;
        this.rootNode = rootNode;
        this.baseDir = baseDir;
    }

    public Object execute(final TemplateWriter stream, final Object context, final Map<String, Object> vars) {
        return rootNode.eval(this, stream, context, vars);
    }

    public ExecutionStack getRelPath() {
        if (relPath == null) {
            relPath = new ExecutionStack();
            relPath.push(baseDir);
        }
        return relPath;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public char[] getTemplate() {
        return template;
    }

    public void setRootNode(final Node rootNode) {
        this.rootNode = rootNode;
    }

    public void setTemplate(final char[] template) {
        this.template = template;
    }
}
