package org.everit.osgi.ewt.internal.inline.res;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public class CodeNode extends Node {
    private int offset;

    private int start;

    public CodeNode() {
    }

    public CodeNode(final int begin, final String name, final char[] template, final int start, final int end) {
        this.begin = begin;
        this.name = name;
        this.contents = template;
        this.start = start;
        this.offset = end - start - 1;

        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
    }

    public CodeNode(final int begin, final String name, final char[] template, final int start, final int end,
            final Node next) {
        this.name = name;
        this.begin = begin;
        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
        this.next = next;
        this.start = start;
        this.offset = end - start - 1;
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        MVEL.eval(contents, start, offset, ctx, factory);
        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }

    @Override
    public String toString() {
        return "CodeNode:" + name + "{" + (contents == null ? "" : new String(contents)) + "} (start=" + begin
                + ";end=" + end + ")";
    }
}
