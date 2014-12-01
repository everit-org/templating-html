package org.everit.osgi.ewt.internal.inline.res;

import static org.mvel2.util.ParseTools.subset;

import org.everit.osgi.ewt.internal.inline.InlineRuntime;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;

public abstract class Node {
    protected int begin;
    protected int cEnd;
    protected char[] contents;
    protected int cStart;
    protected int end;
    protected String name;
    public Node next;
    protected Node terminus;

    public Node() {
    }

    public Node(final int begin, final String name, final char[] template, final int start, final int end) {
        this.begin = begin;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        this.name = name;
        this.contents = template;
        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
    }

    public Node(final int begin, final String name, final char[] template, final int start, final int end,
            final Node next) {
        this.name = name;
        this.begin = begin;
        this.cStart = start;
        this.cEnd = end - 1;
        this.end = end;
        this.contents = template;
        // this.contents = subset(template, this.cStart = start, (this.end = this.cEnd = end) - start - 1);
        this.next = next;
    }

    public void calculateContents(final char[] template) {
        this.contents = subset(template, cStart, end - cStart);
    }

    public abstract boolean demarcate(Node terminatingNode, char[] template);

    public abstract Object eval(InlineRuntime runtime, TemplateOutputStream appender, Object ctx,
            VariableResolverFactory factory);

    public int getBegin() {
        return begin;
    }

    public int getCEnd() {
        return cEnd;
    }

    public char[] getContents() {
        return contents;
    }

    public int getCStart() {
        return cStart;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return this.end - this.begin;
    }

    public String getName() {
        return name;
    }

    public Node getNext() {
        return next;
    }

    public Node getTerminus() {
        return terminus;
    }

    public boolean isOpenNode() {
        return false;
    }

    public void setBegin(final int begin) {
        this.begin = begin;
    }

    public void setCEnd(final int cEnd) {
        this.cEnd = cEnd;
    }

    public void setContents(final char[] contents) {
        this.contents = contents;
    }

    public void setCStart(final int cStart) {
        this.cStart = cStart;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Node setNext(final Node next) {
        return this.next = next;
    }

    public void setTerminus(final Node terminus) {
        this.terminus = terminus;
    }
}
