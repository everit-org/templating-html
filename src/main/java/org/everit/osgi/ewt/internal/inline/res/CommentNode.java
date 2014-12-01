package org.everit.osgi.ewt.internal.inline.res;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public class CommentNode extends Node {
    public CommentNode() {
    }

    public CommentNode(final int begin, final String name, final char[] template, final int start, final int end) {
        this.name = name;
        this.end = this.cEnd = end;
    }

    public CommentNode(final int begin, final String name, final char[] template, final int start, final int end,
            final Node next) {
        this.begin = begin;
        this.end = this.cEnd = end;
        this.next = next;
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        if (next != null)
            return next.eval(runtime, appender, ctx, factory);
        else
            return null;
    }
}
