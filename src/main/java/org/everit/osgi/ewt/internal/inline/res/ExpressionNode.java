package org.everit.osgi.ewt.internal.inline.res;

import static java.lang.String.valueOf;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public class ExpressionNode extends Node {
    public ExpressionNode() {
    }

    public ExpressionNode(final int begin, final String name, final char[] template, final int start, final int end) {
        this.begin = begin;
        this.name = name;
        this.contents = template;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
    }

    public ExpressionNode(final int begin, final String name, final char[] template, final int start, final int end,
            final Node next) {
        this.name = name;
        this.begin = begin;
        this.contents = template;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
        this.next = next;
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        appender.append(valueOf(MVEL.eval(contents, cStart, cEnd - cStart, ctx, factory)));
        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }

    @Override
    public String toString() {
        return "ExpressionNode:" + name + "{" + (contents == null ? "" : new String(contents, cStart, cEnd - cStart))
                + "} (start=" + begin + ";end=" + end + ")";
    }
}
