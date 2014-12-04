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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.everit.expression.CompiledExpression;
import org.everit.templating.html.internal.util.HTMLTemplatingUtil;
import org.htmlparser.Tag;
import org.htmlparser.lexer.PageAttribute;

public class TagNode extends ParentNode {

    private class ForeachItem {
        /**
         * Either an array or an iterable.
         */
        public Object collection;

        public String indexVarName;

        public String valueVarName;
    }

    private class TagAttributeRenderContext {

        public final Map<String, Object> appendValueMap;

        public final Map<String, Object> prependValueMap;

        public final TemplateContextImpl templateContext;

        public final Map<String, Object> valueMap;

        public TagAttributeRenderContext(final TemplateContextImpl templateContext) {

            @SuppressWarnings("unchecked")
            Map<String, Object> lam = evaluateExpression(attributeMapExpressionHolder, templateContext, Map.class);
            valueMap = createWrapperMap(lam);

            @SuppressWarnings("unchecked")
            Map<String, Object> lapm = evaluateExpression(attributePrependMapExpressionHolder, templateContext,
                    Map.class);
            prependValueMap = createWrapperMap(lapm);

            @SuppressWarnings("unchecked")
            Map<String, Object> laam = evaluateExpression(attributeAppendMapExpressionHolder, templateContext,
                    Map.class);
            appendValueMap = createWrapperMap(laam);

            this.templateContext = templateContext;
        }

        private Map<String, Object> createWrapperMap(final Map<String, Object> wrapped) {
            if (wrapped == null) {
                return new HashMap<String, Object>();
            }
            return new HashMap<String, Object>(wrapped);
        }
    }

    private CompiledExpressionHolder attributeAppendMapExpressionHolder;

    private CompiledExpressionHolder attributeMapExpressionHolder;

    private CompiledExpressionHolder attributePrependMapExpressionHolder;

    private Tag endTag = null;

    private boolean escapeText = false;

    private CompiledExpressionHolder foreachExpressionHolder = null;

    private final Map<String, RenderableAttribute> renderableAttributes = new LinkedHashMap<String, RenderableAttribute>();

    /**
     * Defaults to all.
     */
    private CompiledExpressionHolder renderExpressionHolder = null;

    private final Tag tag;

    private String tagName;

    private CompiledExpressionHolder textExpressionHolder = null;

    private CompiledExpressionHolder varExpressionHolder = null;

    public TagNode(final Tag tag) {
        this.tag = tag;
    }

    private void assignForEachVariables(final Map<String, Object> vars, final ForeachItem item, final int index,
            final Object value) {
        vars.put(item.valueVarName, value);
        if (item.indexVarName != null) {
            vars.put(item.indexVarName, index);
        }
    }

    private <R> R evaluateExpression(final CompiledExpressionHolder expressionHolder,
            final TemplateContextImpl templateContext, final Class<R> clazz) {
        if (expressionHolder == null) {
            return null;
        }
        try {
            Object result = expressionHolder.getCompiledExpression().eval(templateContext.getVars());
            if (result == null) {
                return null;
            }

            if (!clazz.isAssignableFrom(result.getClass())) {
                throw new RuntimeException();
                // TODO throw nice exception
                // throw new RenderException("The result type " + result.getClass()
                // + " cannot be assigned to the expected type " + clazz.getName()
                // + " after evaluating attribute: " + expressionHolder.getPageAttribute().toString());
            }

            @SuppressWarnings("unchecked")
            R typedResult = (R) result;
            return typedResult;
        } catch (RuntimeException e) {
            throw new RuntimeException();
            // TODO throw nice exception
            // throw new RenderException("Error during evaluating attribute: "
            // + expressionHolder.getPageAttribute().toString(), e);
        }
    }

    private Map<Object, Object> evaluateForeachMap(final TemplateContextImpl templateContext) {
        if (foreachExpressionHolder == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<Object, Object> result = evaluateExpression(foreachExpressionHolder, templateContext, Map.class);

        return result;
    }

    private RenderScope evaluateRender(final TemplateContextImpl templateContext) {
        Object renderValue = evaluateExpression(renderExpressionHolder, templateContext, Object.class);
        if (renderValue == null) {
            return RenderScope.ALL;
        }

        if (renderValue instanceof RenderScope) {
            return (RenderScope) renderValue;
        }

        if (!(renderValue instanceof String)) {
            // TODO throw nice exception
            // throw new RenderException("Unrecognized evaluated type '" + renderValue.getClass().getName()
            // + "' of attribute: " + renderExpressionHolder.getPageAttribute().toString());
        }
        String renderString = (String) renderValue;
        if (renderString.equalsIgnoreCase(RenderScope.ALL.toString())) {
            return RenderScope.ALL;
        }
        if (renderString.equalsIgnoreCase(RenderScope.NONE.toString())) {
            return RenderScope.NONE;
        }
        if (renderString.equalsIgnoreCase(RenderScope.CONTENT.toString())) {
            return RenderScope.CONTENT;
        }
        if (renderString.equalsIgnoreCase(RenderScope.TAG.toString())) {
            return RenderScope.TAG;
        }
        throw new RuntimeException();
        // TODO throw nice exception.
        // throw new RenderException("Unrecognized evaluated value '" + renderString
        // + "' of attribute: " + renderExpressionHolder.getPageAttribute().toString());
    }

    private Map<String, Object> evaluateTagVariables(final TemplateContextImpl templateContext) {
        if (varExpressionHolder == null) {
            return null;
        }
        CompiledExpression compiledExpression = varExpressionHolder.getCompiledExpression();
        Object result = compiledExpression.eval(templateContext.getVars());
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

    private String evaluateText(final Map<String, Object> vars) {
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
                    textString = HTMLTemplatingUtil.escape(textString);
                }
                return textString;
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
                // TODO throw nice exception
                // throw new RenderException("Error during evaluating attribute: "
                // + textExpressionHolder.getPageAttribute().toString(), e);
            }
        }
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

    public Tag getTag() {
        return tag;
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
    public void render(final TemplateContextImpl templateContext) {
        Map<Object, Object> foreachMap = evaluateForeachMap(templateContext);

        if (foreachExpressionHolder != null && (foreachMap == null || foreachMap.size() == 0)) {
            return;
        }

        if (foreachMap != null) {
            Map<String, Object> originalVars = templateContext.getVars();
            templateContext.setVars(new InheritantMap<String, Object>(originalVars));
            renderEach(templateContext, foreachMap);
            templateContext.setVars(originalVars);
        } else {
            renderItem(templateContext);
        }
    }

    private void renderAttribute(final TemplateWriter writer, final String attributeName,
            final RenderableAttribute renderableAttribute, final TagAttributeRenderContext actx) {
        String attributeValue = renderableAttribute.getConstantValue();

        if (actx.valueMap != null && actx.valueMap.containsKey(attributeName)) {
            Object attributeValueObject = actx.valueMap.remove(attributeName);
            attributeValue = (attributeValueObject != null) ? attributeValueObject.toString() : null;
        } else {
            CompiledExpressionHolder expressionHolder = renderableAttribute.getExpressionHolder();
            if (expressionHolder != null) {
                Object attributeValueObject = evaluateExpression(expressionHolder, actx.templateContext,
                        Object.class);
                attributeValue = (attributeValueObject != null) ? attributeValueObject.toString() : null;
            }
        }

        String prependText = resolveXPend(attributeName, actx.prependValueMap,
                renderableAttribute.getPrependExpressionHolder(), actx.templateContext);
        if (prependText != null) {
            attributeValue = prependText + ((attributeValue != null) ? attributeValue : "");
        }

        String appendText = resolveXPend(attributeName, actx.appendValueMap,
                renderableAttribute.getAppendExpressionHolder(), actx.templateContext);
        if (appendText != null) {
            attributeValue = ((attributeValue != null) ? attributeValue : "") + appendText;
        }

        if (attributeValue != null) {
            String previousText = renderableAttribute.getPreviousText();
            if (previousText != null) {
                writer.append(previousText);
            }

            writer.append(attributeName);

            PageAttribute pageAttribute = renderableAttribute.getPageAttribute();
            if (pageAttribute == null) {
                pageAttribute = renderableAttribute.getExpressionPageAttribute();
                if (pageAttribute == null) {
                    pageAttribute = renderableAttribute.getPrependPageAttribute();
                }
                if (pageAttribute == null) {
                    pageAttribute = renderableAttribute.getAppendPageAttribute();
                }
            }

            String assigment = "=";
            char quote = '"';
            if (pageAttribute != null) {
                assigment = pageAttribute.getAssignment();
                quote = pageAttribute.getQuote();
            }
            String quoteString = String.valueOf(quote);
            writer.append(assigment).append(quoteString).append(HTMLTemplatingUtil.escape(attributeValue)).append(quoteString);
        }
    }

    private void renderEach(final TemplateContextImpl templateContext,
            final Map<Object, Object> foreachMap) {

        Set<Entry<Object, Object>> entrySet = foreachMap.entrySet();

        ForeachItem[] items = new ForeachItem[foreachMap.size()];

        int i = 0;
        for (Entry<Object, Object> entry : entrySet) {

            Object key = entry.getKey();
            if (key == null) {
                // TODO throw nice exception.
            }

            Object value = entry.getValue();

            if (value == null) {
                return;
            }

            if (!value.getClass().isArray() && !(value instanceof Iterable)) {
                // TODO throw nice exception
            }

            String valueVarName = null;
            String indexVarName = null;

            if (key instanceof String) {
                valueVarName = (String) key;
            } else if (key instanceof Object[]) {
                Object[] foreachKeyObjArray = (Object[]) key;
                if (foreachKeyObjArray.length == 0 || foreachKeyObjArray.length > 2) {
                    // TODO throw nice exception
                }
                valueVarName = String.valueOf(foreachKeyObjArray[0]);
                if (foreachKeyObjArray.length == 2) {
                    indexVarName = String.valueOf(foreachKeyObjArray[1]);
                }
            } else {
                // TODO throw nice exception
            }
            ForeachItem item = new ForeachItem();
            item.collection = value;
            item.indexVarName = indexVarName;
            item.valueVarName = valueVarName;
            items[i] = item;
            i++;
        }

        renderEachRecurse(templateContext, items, 0);

    }

    private void renderEachRecurse(final TemplateContextImpl templateContext,
            final ForeachItem[] items,
            final int mapEntryIndex) {

        if (mapEntryIndex == items.length) {
            renderItem(templateContext);
        } else {
            ForeachItem item = items[mapEntryIndex];
            Object collectionObject = item.collection;
            Map<String, Object> vars = templateContext.getVars();
            if (collectionObject instanceof Iterable) {
                Iterable<?> iterable = (Iterable<?>) collectionObject;
                Iterator<?> iterator = iterable.iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    Object value = iterator.next();
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                    i++;
                }
            } else if (collectionObject instanceof byte[]) {
                byte[] collectionObject2 = (byte[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    byte value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof boolean[]) {
                boolean[] collectionObject2 = (boolean[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    boolean value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof char[]) {
                char[] collectionObject2 = (char[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    char value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof double[]) {
                double[] collectionObject2 = (double[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    double value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof float[]) {
                float[] collectionObject2 = (float[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    float value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof int[]) {
                int[] collectionObject2 = (int[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    int value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof long[]) {
                long[] collectionObject2 = (long[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    long value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof short[]) {
                short[] collectionObject2 = (short[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    short value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else if (collectionObject instanceof Object[]) {
                Object[] collectionObject2 = (Object[]) collectionObject;
                for (int i = 0; i < collectionObject2.length; i++) {
                    Object value = collectionObject2[i];
                    assignForEachVariables(vars, item, i, value);
                    renderEachRecurse(templateContext, items, mapEntryIndex + 1);
                }
            } else {
                // TODO throw nice exception
            }
        }
    }

    private void renderItem(final TemplateContextImpl templateContext) {
        Map<String, Object> tagVars = evaluateTagVariables(templateContext);

        Map<String, Object> originalVars = templateContext.getVars();

        Map<String, Object> scopedVars = originalVars;
        if (tagVars != null && tagVars.size() > 0) {
            scopedVars = new InheritantMap<String, Object>(templateContext.getVars());
            templateContext.setVars(scopedVars);
            scopedVars.putAll(tagVars);
        }

        RenderScope render = evaluateRender(templateContext);
        if (render == RenderScope.NONE) {
            templateContext.setVars(originalVars);
            return;
        }

        String text = null;
        if (render == RenderScope.ALL || render == RenderScope.CONTENT) {
            text = evaluateText(scopedVars);
        }

        if (render == RenderScope.ALL || render == RenderScope.TAG) {
            renderTag(templateContext, text, render == RenderScope.ALL);
        } else {

            if (text != null) {
                templateContext.getWriter().append(text);
            } else {
                renderChildren(templateContext);
            }
        }

        templateContext.setVars(originalVars);

    }

    private void renderRemainingAttribute(final TemplateContextImpl templateContext, final String attributeName,
            final Object prepend,
            final Object attributeValue, final Object append) {

        TemplateWriter writer = templateContext.getWriter();

        if (attributeName == null || (prepend == null && attributeValue == null && append == null)) {
            return;
        }
        writer.append(" ").append(attributeName).append("=\"");

        StringBuilder attributeValueSB = new StringBuilder();

        if (prepend != null) {
            attributeValueSB.append(prepend);
        }
        if (attributeValue != null) {
            attributeValueSB.append(attributeValue);
        }
        if (append != null) {
            attributeValueSB.append(append);
        }
        writer.append(HTMLTemplatingUtil.escape(attributeValueSB.toString()));

        writer.append("\"");

    }

    /**
     * Render those attributes that are not listed directly but are available in the value, prepend or append map. The
     * attributes are appended by adding a space in front.
     *
     * @param sb
     *            The stringBuilder or the rendering process.
     * @param vars
     *            The context variables.
     * @param attributeCtx
     *            The context of the tag attributes.
     */
    private void renderRemainingAttributesFromMaps(final TemplateContextImpl templateContext,
            final TagAttributeRenderContext attributeCtx) {

        Map<String, Object> valueMap = attributeCtx.valueMap;
        Map<String, Object> prependMap = attributeCtx.prependValueMap;
        Map<String, Object> appendMap = attributeCtx.appendValueMap;

        Iterator<Entry<String, Object>> iterator = valueMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            iterator.remove();
            String attributeName = entry.getKey();
            Object attributeValue = entry.getValue();
            renderRemainingAttribute(templateContext, attributeName, prependMap.remove(attributeName), attributeValue,
                    appendMap.remove(attributeName));
        }

        iterator = prependMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            iterator.remove();
            String attributeName = entry.getKey();
            Object prepend = entry.getValue();

            renderRemainingAttribute(templateContext, attributeName, prepend, null, appendMap.remove(attributeName));
        }

        iterator = appendMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            iterator.remove();
            String attributeName = entry.getKey();
            Object append = entry.getValue();

            renderRemainingAttribute(templateContext, attributeName, null, null, append);
        }
    }

    private void renderTag(final TemplateContextImpl templateContext, final String text,
            final boolean renderBody) {
        TemplateWriter writer = templateContext.getWriter();
        writer.append("<").append(tagName);

        TagAttributeRenderContext attributeCtx = new TagAttributeRenderContext(templateContext);

        Iterator<Entry<String, RenderableAttribute>> iterator = renderableAttributes.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, RenderableAttribute> entry = iterator.next();
            String attributeName = entry.getKey();
            RenderableAttribute renderableAttribute = entry.getValue();
            renderAttribute(templateContext.getWriter(), attributeName, renderableAttribute, attributeCtx);
        }

        renderRemainingAttributesFromMaps(templateContext, attributeCtx);

        if (!renderBody || (text == null && getChildren().size() == 0)) {
            if (endTag != null) {
                writer.append(">").append(endTag.toHtml(true));
            } else {
                if (tag.isEmptyXmlTag()) {
                    writer.append(" /");
                }
                writer.append(">");
            }
        } else {
            writer.append(">");
            if (text != null) {
                writer.append(text);
            } else {
                renderChildren(templateContext);
            }
            if (tag.isEmptyXmlTag()) {
                writer.append("</").append(tagName).append(">");
            } else if (endTag != null) {
                writer.append(endTag.toHtml(true));
            }
        }

    }

    private String resolveXPend(final String attributeName, final Map<String, Object> xpendValueMap,
            final CompiledExpressionHolder xpendExpressionHolder, final TemplateContextImpl templateContext) {

        String xpend = null;

        if (xpendValueMap != null && xpendValueMap.containsKey(attributeName)) {
            Object xpendObject = xpendValueMap.remove(attributeName);
            xpend = (xpendObject != null) ? xpendObject.toString() : null;
        } else if (xpendExpressionHolder != null) {
            Object xpendObject = evaluateExpression(xpendExpressionHolder, templateContext, Object.class);
            xpend = (xpendObject != null) ? xpendObject.toString() : null;
        }
        return xpend;
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
