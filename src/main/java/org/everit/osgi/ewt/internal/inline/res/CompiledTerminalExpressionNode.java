package org.everit.osgi.ewt.internal.inline.res;

import java.io.Serializable;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public class CompiledTerminalExpressionNode extends TerminalExpressionNode {
    private Serializable ce;

    public CompiledTerminalExpressionNode(final Node node, final ParserContext context) {
        this.begin = node.begin;
        this.name = node.name;
        ce = MVEL.compileExpression(node.contents, node.cStart, node.cEnd - node.cStart, context);
    }

    @Override
    public boolean demarcate(final Node terminatingNode, final char[] template) {
        return false;
    }

    @Override
    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        return MVEL.executeExpression(ce, ctx, factory);
    }
}
