package org.everit.osgi.ewt.internal.inline.res;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public class TextNode extends Node {
    public TextNode(final int begin, final int end) {
        this.begin = begin;
        this.end = end;
    }

    public TextNode(final int begin, final int end, final ExpressionNode next) {
        this.begin = begin;
        this.end = end;
        this.next = next;
    }

    @Override
    public void calculateContents(final char[] template) {
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    @Override
    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        int len = end - begin;
        if (len != 0) {
            appender.append(new String(runtime.getTemplate(), begin, len));
        }
        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }

    @Override
    public String toString() {
        return "TextNode(" + begin + "," + end + ")";
    }
}
