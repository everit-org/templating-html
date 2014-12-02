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

import org.everit.templating.web.el.CompiledExpression;
import org.htmlparser.lexer.PageAttribute;

public class CompiledExpressionHolder {

    private final CompiledExpression compiledExpression;

    private final PageAttribute pageAttribute;

    public CompiledExpressionHolder(final CompiledExpression compiledExpression, final PageAttribute pageAttribute) {
        this.compiledExpression = compiledExpression;
        this.pageAttribute = pageAttribute;
    }

    public CompiledExpression getCompiledExpression() {
        return compiledExpression;
    }

    public PageAttribute getPageAttribute() {
        return pageAttribute;
    }

}
