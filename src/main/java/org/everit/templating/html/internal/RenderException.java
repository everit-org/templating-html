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

import org.everit.templating.util.CompileException;

public class RenderException extends CompileException {

    public RenderException(final String message, final AttributeInfo attributeInfo) {
        super(message, attributeInfo.tagInfo.chars, attributeInfo.valueCursorInTag);
        setColumn(attributeInfo.valueStartCoordinate.column);
        setLineNumber(attributeInfo.valueStartCoordinate.row);
    }
}
