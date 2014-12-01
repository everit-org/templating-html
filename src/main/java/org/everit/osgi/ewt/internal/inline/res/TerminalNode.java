package org.everit.osgi.ewt.internal.inline.res;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public class TerminalNode extends Node {
    public TerminalNode() {
    }

    public TerminalNode(final int begin, final int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }
}
