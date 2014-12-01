package org.everit.osgi.ewt.internal.inline.res;

import java.io.Serializable;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;
import org.mvel2.util.ParseTools;

public class CompiledIfNode extends IfNode {

    private Serializable ce;

    public CompiledIfNode(final int begin, final String name, final char[] template, final int start, final int end,
            final ParserContext context) {
        super(begin, name, template, start, end);
        while (cEnd > cStart && ParseTools.isWhitespace(template[cEnd]))
            cEnd--;
        if (cStart != cEnd) {
            ce = MVEL.compileExpression(template, cStart, cEnd - start, context);
        }
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        if (ce == null || MVEL.executeExpression(ce, ctx, factory, Boolean.class)) {
            return trueNode.eval(runtime, appender, ctx, factory);
        }
        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }
}
