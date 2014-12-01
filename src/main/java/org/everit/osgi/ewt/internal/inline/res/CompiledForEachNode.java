package org.everit.osgi.ewt.internal.inline.res;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntimeError;
import org.mvel2.templates.util.ArrayIterator;
import org.mvel2.templates.util.CountIterator;
import org.mvel2.templates.util.TemplateOutputStream;
import org.mvel2.util.ParseTools;

public class CompiledForEachNode extends Node {
    private Serializable[] ce;
    private ParserContext context;

    private Serializable cSepExpr;

    private String[] item;
    public Node nestedNode;

    private char[] sepExpr;

    public CompiledForEachNode(final int begin, final String name, final char[] template, final int start,
            final int end, final ParserContext context) {
        super(begin, name, template, start, end);
        this.context = context;
        configure();
    }

    private void configure() {
        ArrayList<String> items = new ArrayList<String>();
        ArrayList<String> expr = new ArrayList<String>();

        int start = cStart;
        for (int i = start; i < cEnd; i++) {
            switch (contents[i]) {
            case '(':
            case '[':
            case '{':
            case '"':
            case '\'':
                i = ParseTools.balancedCapture(contents, i, contents[i]);
                break;
            // if (expr.size() < items.size()) {
            // start = i;
            // i = ParseTools.balancedCapture(contents, i, contents[i]);
            // expr.add(ParseTools.createStringTrimmed(contents, start, i - start + 1));
            // start = i + 1;
            // }
            // else {
            // throw new CompileException("unexpected character '" + contents[i] + "' in foreach tag", contents,cStart +
            // 1);
            // }
            // break;

            case ':':
                items.add(ParseTools.createStringTrimmed(contents, start, i - start));
                start = i + 1;
                break;
            case ',':
                if (expr.size() != (items.size() - 1)) {
                    throw new CompileException("unexpected character ',' in foreach tag", contents, cStart + i);
                }
                expr.add(ParseTools.createStringTrimmed(contents, start, i - start));
                start = i + 1;
                break;
            }
        }

        if (start < cEnd) {
            if (expr.size() != (items.size() - 1)) {
                throw new CompileException("expected character ':' in foreach tag", contents, cEnd);
            }
            expr.add(ParseTools.createStringTrimmed(contents, start, cEnd - start));
        }

        item = new String[items.size()];
        int i = 0;
        for (String s : items)
            item[i++] = s;

        String[] expression;
        ce = new Serializable[(expression = new String[expr.size()]).length];
        i = 0;
        for (String s : expr) {
            ce[i] = MVEL.compileExpression(expression[i++] = s, context);
        }
    }

    @Override
    public boolean demarcate(final Node terminatingnode, final char[] template) {
        nestedNode = next;
        next = terminus;

        sepExpr = terminatingnode.getContents();
        if (sepExpr.length == 0) {
            sepExpr = null;
        }
        else {
            cSepExpr = MVEL.compileExpression(sepExpr, context);
        }

        return false;
    }

    public Object eval(final InlineRuntime runtime, final TemplateOutputStream appender, final Object ctx,
            final VariableResolverFactory factory) {
        Iterator[] iters = new Iterator[item.length];

        Object o;
        for (int i = 0; i < iters.length; i++) {
            if ((o = MVEL.executeExpression(ce[i], ctx, factory)) instanceof Iterable) {
                iters[i] = ((Iterable) o).iterator();
            }
            else if (o instanceof Object[]) {
                iters[i] = new ArrayIterator((Object[]) o);
            }
            else if (o instanceof Integer) {
                iters[i] = new CountIterator((Integer) o);
            }
            else {
                throw new TemplateRuntimeError("cannot iterate object type: " + o.getClass().getName());
            }
        }

        Map<String, Object> locals = new HashMap<String, Object>();
        MapVariableResolverFactory localFactory = new MapVariableResolverFactory(locals, factory);

        int iterate = iters.length;

        while (true) {
            for (int i = 0; i < iters.length; i++) {
                if (!iters[i].hasNext()) {
                    iterate--;
                    locals.put(item[i], "");
                }
                else {
                    locals.put(item[i], iters[i].next());
                }
            }
            if (iterate != 0) {
                nestedNode.eval(runtime, appender, ctx, localFactory);

                if (sepExpr != null) {
                    for (Iterator it : iters) {
                        if (it.hasNext()) {
                            appender.append(String.valueOf(MVEL.executeExpression(cSepExpr, ctx, factory)));
                            break;
                        }
                    }
                }
            }
            else
                break;
        }

        return next != null ? next.eval(runtime, appender, ctx, factory) : null;
    }

    public Node getNestedNode() {
        return nestedNode;
    }

    public void setNestedNode(final Node nestedNode) {
        this.nestedNode = nestedNode;
    }
}
