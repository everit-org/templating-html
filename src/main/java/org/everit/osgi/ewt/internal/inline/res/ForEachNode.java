/**
 * This file is part of Everit - Web Templating.
 *
 * Everit - Web Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Web Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Web Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.ewt.internal.inline.res;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.everit.osgi.ewt.RenderException;
import org.everit.osgi.ewt.TemplateWriter;
import org.everit.osgi.ewt.el.CompiledExpression;
import org.everit.osgi.ewt.el.ExpressionCompiler;
import org.everit.osgi.ewt.internal.InheritantMap;
import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.CompileException;
import org.mvel2.templates.util.ArrayIterator;
import org.mvel2.templates.util.CountIterator;
import org.mvel2.util.ParseTools;

public class ForEachNode extends Node {
    private CompiledExpression[] ce;

    private CompiledExpression cSepExpr;

    private final ExpressionCompiler expressionCompiler;
    private String[] item;

    public Node nestedNode;

    private char[] sepExpr;

    public ForEachNode(final int begin, final String name, final char[] template, final int start,
            final int end, final ExpressionCompiler expressionCompiler) {
        super(begin, name, template, start, end);
        this.expressionCompiler = expressionCompiler;
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
        for (String s : items) {
            item[i++] = s;
        }

        String[] expression;
        ce = new CompiledExpression[(expression = new String[expr.size()]).length];
        i = 0;
        for (String s : expr) {
            ce[i] = expressionCompiler.compile(expression[i++] = s);
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
            cSepExpr = expressionCompiler.compile(String.valueOf(sepExpr));
        }

        return false;
    }

    @Override
    public Object eval(final InlineRuntime runtime, final TemplateWriter appender, final Object ctx,
            final Map<String, Object> vars) {
        Iterator[] iters = new Iterator[item.length];

        Object o;
        for (int i = 0; i < iters.length; i++) {
            if ((o = ce[i].eval(vars)) instanceof Iterable) {
                iters[i] = ((Iterable) o).iterator();
            }
            else if (o instanceof Object[]) {
                iters[i] = new ArrayIterator((Object[]) o);
            }
            else if (o instanceof Integer) {
                iters[i] = new CountIterator((Integer) o);
            }
            else {
                throw new RenderException("cannot iterate object type: " + o.getClass().getName());
            }
        }

        Map<String, Object> localVars = new InheritantMap<String, Object>(vars);

        int iterate = iters.length;

        while (true) {
            for (int i = 0; i < iters.length; i++) {
                if (!iters[i].hasNext()) {
                    iterate--;
                    localVars.put(item[i], "");
                }
                else {
                    localVars.put(item[i], iters[i].next());
                }
            }
            if (iterate != 0) {
                nestedNode.eval(runtime, appender, ctx, localVars);

                if (sepExpr != null) {
                    for (Iterator it : iters) {
                        if (it.hasNext()) {
                            appender.append(String.valueOf(cSepExpr.eval(vars)));
                            break;
                        }
                    }
                }
            } else {
                break;
            }
        }

        return next != null ? next.eval(runtime, appender, ctx, vars) : null;
    }

    public Node getNestedNode() {
        return nestedNode;
    }

    public void setNestedNode(final Node nestedNode) {
        this.nestedNode = nestedNode;
    }
}
