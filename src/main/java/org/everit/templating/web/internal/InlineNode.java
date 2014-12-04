package org.everit.templating.web.internal;

import org.everit.templating.CompiledTemplate;

public class InlineNode implements HTMLNode {

    private final CompiledTemplate compiledTemplate;

    public InlineNode(final org.everit.templating.CompiledTemplate compiledTemplate) {
        this.compiledTemplate = compiledTemplate;
    }

    @Override
    public void render(final TemplateContextImpl templateContext) {
        compiledTemplate.render(templateContext.getWriter().getWrapped(), templateContext.getVars());
    }

}
