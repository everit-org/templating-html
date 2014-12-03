package org.everit.templating.web.internal;

import java.io.Writer;
import java.util.Map;

import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateContext;

public class TemplateContextImpl implements TemplateContext {

    private final String bookmark;

    private final CompiledTemplate compiledTemplate;

    private Map<String, Object> vars;

    private final TemplateWriter writer;

    public TemplateContextImpl(final CompiledTemplate compiledTemplate, final String bookmark,
            final Map<String, Object> vars, final Writer writer) {
        this.bookmark = bookmark;
        this.compiledTemplate = compiledTemplate;
        this.vars = vars;
        this.writer = new TemplateWriter(writer);
    }

    @Override
    public String getBookmark() {
        return bookmark;
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public TemplateWriter getWriter() {
        return writer;
    }

    @Override
    public void renderBookmark(final String bookmark) {
        compiledTemplate.render(writer.getWrapped(), vars, bookmark);
    }

    public void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }

}
