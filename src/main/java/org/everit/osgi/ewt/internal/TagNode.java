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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.everit.osgi.ewt.el.CompiledExpression;

public class TagNode implements EWTNode {

    private final Map<String, CompiledExpression> attributeAppendExpressions = new LinkedHashMap<String, CompiledExpression>();

    private CompiledExpression attributeAppendMapExpression;
    private CompiledExpression attributeMapExpression;
    private final Map<String, CompiledExpression> attributePrependExpressions = new LinkedHashMap<String, CompiledExpression>();

    private CompiledExpression attributePrependMapExpression;

    private final Map<String, CompiledExpression> attributeExpressions = new LinkedHashMap<String, CompiledExpression>();

    private final List<EWTNode> children = new ArrayList<EWTNode>();

    private CompiledExpression foreachExpression = null;

    /**
     * Defaults to all.
     */
    private CompiledExpression renderExpression = null;

    private CompiledExpression textExpression = null;

    private boolean unescapeText = false;

    private CompiledExpression varExpression = null;

    public Map<String, CompiledExpression> getAttributeAppendExpressions() {
        return attributeAppendExpressions;
    }

    public CompiledExpression getAttributeAppendMapExpression() {
        return attributeAppendMapExpression;
    }

    public CompiledExpression getAttributeMapExpression() {
        return attributeMapExpression;
    }

    public Map<String, CompiledExpression> getAttributePrependExpressions() {
        return attributePrependExpressions;
    }

    public CompiledExpression getAttributePrependMapExpression() {
        return attributePrependMapExpression;
    }

    public Map<String, CompiledExpression> getAttributeExpressions() {
        return attributeExpressions;
    }

    public List<EWTNode> getChildren() {
        return children;
    }

    public CompiledExpression getForeachExpression() {
        return foreachExpression;
    }

    public CompiledExpression getRenderExpression() {
        return renderExpression;
    }

    public CompiledExpression getTextExpression() {
        return textExpression;
    }

    public CompiledExpression getVarExpression() {
        return varExpression;
    }

    public boolean isUnescapeText() {
        return unescapeText;
    }

    @Override
    public void render(StringBuilder sb, Map<String, Object> context) {
        // TODO Auto-generated method stub

    }

    public void setAttributeAppendMapExpression(CompiledExpression attributeAppendMapExpression) {
        this.attributeAppendMapExpression = attributeAppendMapExpression;
    }

    public void setAttributeMapExpression(CompiledExpression attributeMapExpression) {
        this.attributeMapExpression = attributeMapExpression;
    }

    public void setAttributePrependMapExpression(CompiledExpression attributePrependMapExpression) {
        this.attributePrependMapExpression = attributePrependMapExpression;
    }

    public void setForeachExpression(CompiledExpression foreachExpression) {
        this.foreachExpression = foreachExpression;
    }

    public void setRenderExpression(CompiledExpression renderExpression) {
        this.renderExpression = renderExpression;
    }

    public void setTextExpression(CompiledExpression textExpression) {
        this.textExpression = textExpression;
    }

    public void setUnescapeText(boolean unescapeText) {
        this.unescapeText = unescapeText;
    }

    public void setVarExpression(CompiledExpression varExpression) {
        this.varExpression = varExpression;
    }

}
