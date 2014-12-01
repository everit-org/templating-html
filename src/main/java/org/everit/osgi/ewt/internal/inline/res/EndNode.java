package org.everit.osgi.ewt.internal.inline.res;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public class EndNode extends Node {
    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    public Object eval(final InlineRuntime runtie, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        return appender.toString();
    }

    @Override
    public String toString() {
        return "EndNode";
    }
}
