package org.everit.osgi.ewt.internal;

import org.htmlparser.lexer.PageAttribute;

public class RenderableAttribute {

    private CompiledExpressionHolder appendExpressionHolder = null;

    private PageAttribute appendPageAttribute = null;

    private String constantValue = null;

    private CompiledExpressionHolder expressionHolder = null;

    private PageAttribute expressionPageAttribute = null;

    private PageAttribute pageAttribute = null;

    private CompiledExpressionHolder prependExpressionHolder = null;

    private PageAttribute prependPageAttribute = null;

    private String previousText = null;

    public CompiledExpressionHolder getAppendExpressionHolder() {
        return appendExpressionHolder;
    }

    public PageAttribute getAppendPageAttribute() {
        return appendPageAttribute;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public CompiledExpressionHolder getExpressionHolder() {
        return expressionHolder;
    }

    public PageAttribute getExpressionPageAttribute() {
        return expressionPageAttribute;
    }

    public PageAttribute getPageAttribute() {
        return pageAttribute;
    }

    public CompiledExpressionHolder getPrependExpressionHolder() {
        return prependExpressionHolder;
    }

    public PageAttribute getPrependPageAttribute() {
        return prependPageAttribute;
    }

    public String getPreviousText() {
        return previousText;
    }

    public void setAppendExpressionHolder(CompiledExpressionHolder appendExpressionHolder) {
        this.appendExpressionHolder = appendExpressionHolder;
    }

    public void setAppendPageAttribute(PageAttribute appendPageAttribute) {
        this.appendPageAttribute = appendPageAttribute;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

    public void setExpressionHolder(CompiledExpressionHolder expressionHolder) {
        this.expressionHolder = expressionHolder;
    }

    public void setExpressionPageAttribute(PageAttribute expressionPageAttribute) {
        this.expressionPageAttribute = expressionPageAttribute;
    }

    public void setPageAttribute(PageAttribute pageAttribute) {
        this.pageAttribute = pageAttribute;
    }

    public void setPrependExpressionHolder(CompiledExpressionHolder prependExpressionHolder) {
        this.prependExpressionHolder = prependExpressionHolder;
    }

    public void setPrependPageAttribute(PageAttribute prependPageAttribute) {
        this.prependPageAttribute = prependPageAttribute;
    }

    public void setPreviousText(String previousWhitespaces) {
        this.previousText = previousWhitespaces;
    }

}
