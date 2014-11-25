package org.everit.osgi.ewt.internal;

import org.htmlparser.lexer.PageAttribute;

public class RenderableAttribute {

    private String previousWhitespaces;

    private PageAttribute pageAttribute;

    private CompiledExpressionHolder expressionHolder;

    private CompiledExpressionHolder prependExpressionHolder;

    private CompiledExpressionHolder appendExpressionHolder;

    public String getPreviousWhitespaces() {
        return previousWhitespaces;
    }

    public void setPreviousWhitespaces(String previousWhitespaces) {
        this.previousWhitespaces = previousWhitespaces;
    }

    public PageAttribute getPageAttribute() {
        return pageAttribute;
    }

    public void setPageAttribute(PageAttribute pageAttribute) {
        this.pageAttribute = pageAttribute;
    }

    public CompiledExpressionHolder getExpressionHolder() {
        return expressionHolder;
    }

    public void setExpressionHolder(CompiledExpressionHolder expressionHolder) {
        this.expressionHolder = expressionHolder;
    }

    public CompiledExpressionHolder getPrependExpressionHolder() {
        return prependExpressionHolder;
    }

    public void setPrependExpressionHolder(CompiledExpressionHolder prependExpressionHolder) {
        this.prependExpressionHolder = prependExpressionHolder;
    }

    public CompiledExpressionHolder getAppendExpressionHolder() {
        return appendExpressionHolder;
    }

    public void setAppendExpressionHolder(CompiledExpressionHolder appendExpressionHolder) {
        this.appendExpressionHolder = appendExpressionHolder;
    }

}
