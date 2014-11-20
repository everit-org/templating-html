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
import java.util.Map.Entry;

import org.everit.osgi.ewt.el.CompiledExpression;
import org.htmlparser.Tag;
import org.htmlparser.lexer.PageAttribute;

public class TagNode extends ParentNode {

    private final Map<String, CompiledExpression> attributeAppendExpressions = new LinkedHashMap<String, CompiledExpression>();

    private CompiledExpression attributeAppendMapExpression;

    private final Map<String, CompiledExpression> attributeExpressions = new LinkedHashMap<String, CompiledExpression>();
    private CompiledExpression attributeMapExpression;
    private final Map<String, CompiledExpression> attributePrependExpressions = new LinkedHashMap<String, CompiledExpression>();

    private CompiledExpression attributePrependMapExpression;

    private Tag endTag = null;

    private boolean escapeText = false;

    private CompiledExpressionHolder foreachExpressionHolder = null;

    private final List<PageAttribute> pageAttributes = new ArrayList<PageAttribute>();

    /**
     * Defaults to all.
     */
    private CompiledExpressionHolder renderExpressionHolder = null;

    private CompiledExpressionHolder textExpressionHolder = null;

    private CompiledExpressionHolder varExpressionHolder = null;

    private String escape(String textString) {
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

    private <R> R evaluateExpression(CompiledExpressionHolder expressionHolder, Map<String, Object> vars,
            Class<R> clazz) {
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

    private Entry<String, Iterable<?>> evaluateForeach(Map<String, Object> vars) {
        // TODO Auto-generated method stub
        return null;
    }

    private RenderScope evaluateRender(Map<String, Object> vars) {
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

    private Map<String, Object> evaluateTagVariables(Map<String, Object> vars) {
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

    private String evaluateText(StringBuilder sb, Map<String, Object> vars) {
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
    }

    public Map<String, CompiledExpression> getAttributeAppendExpressions() {
        return attributeAppendExpressions;
    }

    public CompiledExpression getAttributeAppendMapExpression() {
        return attributeAppendMapExpression;
    }

    public Map<String, CompiledExpression> getAttributeExpressions() {
        return attributeExpressions;
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

    public CompiledExpressionHolder getForeachExpressionHolder() {
        return foreachExpressionHolder;
    }

    public List<PageAttribute> getPageAttributes() {
        return pageAttributes;
    }

    public CompiledExpressionHolder getRenderExpressionHolder() {
        return renderExpressionHolder;
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
    public void render(StringBuilder sb, Map<String, Object> vars) {
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

    private void renderItem(StringBuilder sb, Map<String, Object> vars) {
        RenderScope render = evaluateRender(vars);
        if (render == RenderScope.NONE) {
            return;
        }

        if (render == RenderScope.ALL || render == RenderScope.TAG) {
            sb.append('<');
            for (PageAttribute pageAttribute : pageAttributes) {
                // TODO render attributes
            }

            // TODO if there is a body, render it, otherwise close the tag if it is self-closed or render the endTag
            // renderBody(sb, vars);
        } else {
            evaluateText(sb, vars);
        }

        // TODO

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

    public void setEndTag(Tag endTag) {
        this.endTag = endTag;
    }

    public void setEscapeText(boolean unescapeText) {
        this.escapeText = unescapeText;
    }

    public void setForeachExpressionHolder(CompiledExpressionHolder foreachExpressionHolder) {
        this.foreachExpressionHolder = foreachExpressionHolder;
    }

    public void setRenderExpressionHolder(CompiledExpressionHolder renderExpressionHolder) {
        this.renderExpressionHolder = renderExpressionHolder;
    }

    public void setTextExpressionHolder(CompiledExpressionHolder textExpressionHolder) {
        this.textExpressionHolder = textExpressionHolder;
    }

    public void setVarExpressionHolder(CompiledExpressionHolder varExpressionHolder) {
        this.varExpressionHolder = varExpressionHolder;
    }

}
