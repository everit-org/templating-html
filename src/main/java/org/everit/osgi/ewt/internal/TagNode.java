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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.everit.osgi.ewt.el.CompiledExpression;
import org.htmlparser.Tag;

public class TagNode extends ParentNode {

    private CompiledExpressionHolder attributeAppendMapExpressionHolder;

    private CompiledExpressionHolder attributeMapExpressionHolder;

    private CompiledExpressionHolder attributePrependMapExpressionHolder;

    private Tag endTag = null;

    private boolean escapeText = false;

    private CompiledExpressionHolder foreachExpressionHolder = null;

    private Map<String, RenderableAttribute> renderableAttributes = new LinkedHashMap<String, RenderableAttribute>();

    /**
     * Defaults to all.
     */
    private CompiledExpressionHolder renderExpressionHolder = null;

    private Tag tag;

    private String tagName;

    private CompiledExpressionHolder textExpressionHolder = null;

    private CompiledExpressionHolder varExpressionHolder = null;

    private String escape(final String textString) {
        StringBuilder sb = new StringBuilder(textString.length());
        for (int i = 0, n = textString.length(); i < n; i++) {
            char charAt = textString.charAt(i);
            switch (charAt) {
            case '&':
                sb.append("&amp;");
                break;
            case '\'':
                sb.append("&apos;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            default:
                sb.append(charAt);
                break;
            }
        }
        return null;
    }

    private <R> R evaluateExpression(final CompiledExpressionHolder expressionHolder, final Map<String, Object> vars,
            final Class<R> clazz) {
        if (expressionHolder == null) {
            return null;
        }
        try {
            Object result = expressionHolder.getCompiledExpression().eval(vars);
            if (result == null) {
                return null;
            }

            if (!clazz.isAssignableFrom(result.getClass())) {
                // TODO throw nice exception
            }

            @SuppressWarnings("unchecked")
            R typedResult = (R) result;
            return typedResult;
        } catch (RuntimeException e) {
            // TODO write out nice exception
        }
        return null;
    }

    private Entry<String, Iterable<?>> evaluateForeach(final Map<String, Object> vars) {
        // TODO Auto-generated method stub
        return null;
    }

    private RenderScope evaluateRender(final Map<String, Object> vars) {
        String renderString = evaluateExpression(renderExpressionHolder, vars, String.class);
        if (renderString == null || renderString.equalsIgnoreCase(RenderScope.ALL.toString())) {
            return RenderScope.ALL;
        }
        if (renderString.equalsIgnoreCase(RenderScope.NONE.toString())) {
            return RenderScope.NONE;
        }
        if (renderString.equalsIgnoreCase(RenderScope.BODY.toString())) {
            return RenderScope.BODY;
        }
        if (renderString.equalsIgnoreCase(RenderScope.TAG.toString())) {
            return RenderScope.TAG;
        }
        // TODO throw exception
        return null;
    }

    private Map<String, Object> evaluateTagVariables(final Map<String, Object> vars) {
        if (varExpressionHolder == null) {
            return null;
        }
        CompiledExpression compiledExpression = varExpressionHolder.getCompiledExpression();
        Object result = compiledExpression.eval(vars);
        if (result == null) {
            return null;
        }
        if (!(result instanceof Map)) {
            // TODO throw nice exception
        }
        @SuppressWarnings("unchecked")
        Map<Object, Object> tagVarMap = (Map<Object, Object>) result;

        for (Object key : tagVarMap.keySet()) {
            if (key == null || !(key instanceof String)) {
                // TODO throw nice error
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> mapResult = (Map<String, Object>) result;
        return mapResult;
    }

    private String evaluateText(final StringBuilder sb, final Map<String, Object> vars) {
        if (textExpressionHolder == null) {
            return null;
        } else {
            try {
                Object text = textExpressionHolder.getCompiledExpression().eval(vars);
                if (text == null) {
                    return "";
                }
                String textString = text.toString();
                if (escapeText) {
                    textString = escape(textString);
                }
                return textString;
            } catch (RuntimeException e) {
                // TODO throw nice exception
            }
        }
        return null;
    }

    public CompiledExpressionHolder getAttributeAppendMapExpressionHolder() {
        return attributeAppendMapExpressionHolder;
    }

    public CompiledExpressionHolder getAttributeMapExpressionHolder() {
        return attributeMapExpressionHolder;
    }

    public CompiledExpressionHolder getAttributePrependMapExpressionHolder() {
        return attributePrependMapExpressionHolder;
    }

    public CompiledExpressionHolder getForeachExpressionHolder() {
        return foreachExpressionHolder;
    }

    public Map<String, RenderableAttribute> getRenderableAttributes() {
        return renderableAttributes;
    }

    public CompiledExpressionHolder getRenderExpressionHolder() {
        return renderExpressionHolder;
    }

    public String getTagName() {
        return tagName;
    }

    public CompiledExpressionHolder getTextExpressionHolder() {
        return textExpressionHolder;
    }

    public CompiledExpressionHolder getVarExpressionHolder() {
        return varExpressionHolder;
    }

    public boolean isEscapeText() {
        return escapeText;
    }

    @Override
    public void render(final StringBuilder sb, final Map<String, Object> vars) {
        Entry<String, Iterable<?>> foreachEntry = evaluateForeach(vars);

        if (foreachEntry == null || foreachEntry.getValue() == null) {
            if (foreachExpressionHolder != null) {
                // Can happen if the user returns null in forech expression
                return;
            }

            Map<String, Object> tagVars = evaluateTagVariables(vars);

            Map<String, Object> scopedVars = vars;
            if (tagVars != null && tagVars.size() > 0) {
                scopedVars = new InheritantMap<String, Object>(vars);
                scopedVars.putAll(tagVars);
            }
            renderItem(sb, scopedVars);
        } else {
            Iterable<?> iterable = foreachEntry.getValue();
            String key = foreachEntry.getKey();
            for (Object element : iterable) {
                Map<String, Object> scopedVars = new InheritantMap<String, Object>(vars);
                scopedVars.put(key, element);
                Map<String, Object> tagVars = evaluateTagVariables(scopedVars);
                if (tagVars != null) {
                    scopedVars.putAll(tagVars);
                }
                renderItem(sb, scopedVars);
            }
        }
    }

    private void renderAttribute(final String attributeName, final RenderableAttribute renderableAttribute) {

        // TODO
    }

    private void renderItem(final StringBuilder sb, final Map<String, Object> vars) {
        RenderScope render = evaluateRender(vars);
        if (render == RenderScope.NONE) {
            return;
        }

        String text = evaluateText(sb, vars);
        if (render == RenderScope.ALL || render == RenderScope.TAG) {
            renderTag(sb, vars, text);
        } else {
            if (text != null) {
                sb.append(text);
            } else {
                renderChildren(sb, vars);
            }
        }

    }

    private void renderTag(final StringBuilder sb, final Map<String, Object> vars, final String text) {
        sb.append("<").append(tagName);

        Iterator<Entry<String, RenderableAttribute>> iterator = renderableAttributes.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, RenderableAttribute> entry = iterator.next();
            String attributeName = entry.getKey();
            RenderableAttribute renderableAttribute = entry.getValue();
            renderAttribute(attributeName, renderableAttribute);
        }
        // TODO render end tag or closing
    }

    public void setAttributeAppendMapExpressionHolder(final CompiledExpressionHolder attributeAppendMapExpression) {
        this.attributeAppendMapExpressionHolder = attributeAppendMapExpression;
    }

    public void setAttributeMapExpressionHolder(final CompiledExpressionHolder attributeMapExpression) {
        this.attributeMapExpressionHolder = attributeMapExpression;
    }

    public void setAttributePrependMapExpressionHolder(final CompiledExpressionHolder attributePrependMapExpression) {
        this.attributePrependMapExpressionHolder = attributePrependMapExpression;
    }

    public void setEndTag(final Tag endTag) {
        this.endTag = endTag;
    }

    public void setEscapeText(final boolean unescapeText) {
        this.escapeText = unescapeText;
    }

    public void setForeachExpressionHolder(final CompiledExpressionHolder foreachExpressionHolder) {
        this.foreachExpressionHolder = foreachExpressionHolder;
    }

    public void setRenderExpressionHolder(final CompiledExpressionHolder renderExpressionHolder) {
        this.renderExpressionHolder = renderExpressionHolder;
    }

    public void setTag(final Tag tag) {
        this.tag = tag;
    }

    public void setTagName(final String tagName) {
        this.tagName = tagName;
    }

    public void setTextExpressionHolder(final CompiledExpressionHolder textExpressionHolder) {
        this.textExpressionHolder = textExpressionHolder;
    }

    public void setVarExpressionHolder(final CompiledExpressionHolder varExpressionHolder) {
        this.varExpressionHolder = varExpressionHolder;
    }

}
