package org.everit.osgi.ewt.internal.inline.res;

import static java.lang.String.valueOf;

import java.io.Serializable;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.templates.util.TemplateOutputStream;

public class CompiledEvalNode extends Node {
    private Serializable ce;

    public CompiledEvalNode(final int begin, final String name, final char[] template, final int start, final int end,
            final ParserContext context) {
        this.begin = begin;
        this.name = name;
        this.contents = template;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        ce = MVEL.compileExpression(template, cStart, cEnd - cStart, context);
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        appender.append(String.valueOf(TemplateRuntime.eval(valueOf(MVEL.executeExpression(ce, ctx, factory)), ctx,
                factory)));
        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }

    @Override
    public String toString() {
        return "EvalNode:" + name + "{" + (contents == null ? "" : new String(contents)) + "} (start=" + begin
                + ";end=" + end + ")";
    }
}
