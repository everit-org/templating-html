package org.everit.osgi.ewt.internal.inline.res;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;
import org.mvel2.util.ParseTools;

public class IfNode extends Node {
    protected Node elseNode;
    protected Node trueNode;

    public IfNode(final int begin, final String name, final char[] template, final int start, final int end) {
        super(begin, name, template, start, end);
        while (cEnd > cStart && ParseTools.isWhitespace(template[cEnd]))
            cEnd--;
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        trueNode = next;
        next = terminus;
        return true;
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        if (cEnd == cStart || MVEL.eval(contents, cStart, cEnd - cStart, ctx, factory, Boolean.class)) {
            return trueNode.eval(runtime, appender, ctx, factory);
        }
        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }

    public Node getElseNode() {
        return elseNode;
    }

    public Node getTrueNode() {
        return trueNode;
    }

    public void setElseNode(final ExpressionNode elseNode) {
        this.elseNode = elseNode;
    }

    public void setTrueNode(final ExpressionNode trueNode) {
        this.trueNode = trueNode;
    }
}
