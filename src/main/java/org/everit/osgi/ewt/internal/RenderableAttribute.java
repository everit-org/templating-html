package org.everit.osgi.ewt.internal;

import org.htmlparser.lexer.PageAttribute;

public class RenderableAttribute {

    private CompiledExpressionHolder appendExpressionHolder;

    private String constantValue;

    private CompiledExpressionHolder expressionHolder;

    private PageAttribute pageAttribute;

    private CompiledExpressionHolder prependExpressionHolder;

    private String previousText;

    public CompiledExpressionHolder getAppendExpressionHolder() {
        return appendExpressionHolder;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public CompiledExpressionHolder getExpressionHolder() {
        return expressionHolder;
    }

    public PageAttribute getPageAttribute() {
        return pageAttribute;
    }

    public CompiledExpressionHolder getPrependExpressionHolder() {
        return prependExpressionHolder;
    }

    public String getPreviousText() {
        return previousText;
    }

    public void setAppendExpressionHolder(CompiledExpressionHolder appendExpressionHolder) {
        this.appendExpressionHolder = appendExpressionHolder;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

    public void setExpressionHolder(CompiledExpressionHolder expressionHolder) {
        this.expressionHolder = expressionHolder;
    }

    public void setPageAttribute(PageAttribute pageAttribute) {
        this.pageAttribute = pageAttribute;
    }

    public void setPrependExpressionHolder(CompiledExpressionHolder prependExpressionHolder) {
        this.prependExpressionHolder = prependExpressionHolder;
    }

    public void setPreviousText(String previousWhitespaces) {
        this.previousText = previousWhitespaces;
    }

}
