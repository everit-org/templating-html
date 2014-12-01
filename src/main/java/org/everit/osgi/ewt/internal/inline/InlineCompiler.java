package org.everit.osgi.ewt.internal.inline;

import java.util.HashMap;
import java.util.Map;

import org.everit.osgi.ewt.internal.inline.res.CommentNode;
import org.everit.osgi.ewt.internal.inline.res.CompiledCodeNode;
import org.everit.osgi.ewt.internal.inline.res.CompiledEvalNode;
import org.everit.osgi.ewt.internal.inline.res.CompiledExpressionNode;
import org.everit.osgi.ewt.internal.inline.res.CompiledForEachNode;
import org.everit.osgi.ewt.internal.inline.res.CompiledIfNode;
import org.everit.osgi.ewt.internal.inline.res.CompiledTerminalExpressionNode;
import org.everit.osgi.ewt.internal.inline.res.EndNode;
import org.everit.osgi.ewt.internal.inline.res.ExpressionNode;
import org.everit.osgi.ewt.internal.inline.res.IfNode;
import org.everit.osgi.ewt.internal.inline.res.Node;
import org.everit.osgi.ewt.internal.inline.res.Opcodes;
import org.everit.osgi.ewt.internal.inline.res.TerminalNode;
import org.everit.osgi.ewt.internal.inline.res.TextNode;
import org.mvel2.CompileException;
import org.mvel2.ParserContext;
import org.mvel2.util.ExecutionStack;

public class InlineCompiler {
    private static final Map<String, Integer> OPCODES = new HashMap<String, Integer>();
    static {
        OPCODES.put("if", Opcodes.IF);
        OPCODES.put("else", Opcodes.ELSE);
        OPCODES.put("elseif", Opcodes.ELSE);
        OPCODES.put("end", Opcodes.END);
        OPCODES.put("foreach", Opcodes.FOREACH);

        OPCODES.put("comment", Opcodes.COMMENT);
        OPCODES.put("code", Opcodes.CODE);
        OPCODES.put("eval", Opcodes.EVAL);
    }

    private static int balancedCaptureWithLineAccounting(final char[] chars, int start, final int end, final char type,
            final ParserContext pCtx) {
        int depth = 1;
        int st = start;
        char term = type;
        switch (type) {
        case '[':
            term = ']';
            break;
        case '{':
            term = '}';
            break;
        case '(':
            term = ')';
            break;
        }

        if (type == term) {
            for (start++; start != end; start++) {
                if (chars[start] == type) {
                    return start;
                }
            }
        }
        else {
            int lines = 0;
            for (start++; start < end; start++) {
                if (isWhitespace(chars[start])) {
                    switch (chars[start]) {
                    case '\r':
                        continue;
                    case '\n':
                        if (pCtx != null)
                            pCtx.setLineOffset((short) start);
                        lines++;
                    }
                }
                else if (start < end && chars[start] == '/') {
                    if (start + 1 == end)
                        return start;
                    if (chars[start + 1] == '/') {
                        start++;
                        while (start < end && chars[start] != '\n')
                            start++;
                    }
                    else if (chars[start + 1] == '*') {
                        start += 2;
                        Skiploop: while (start != end) {
                            switch (chars[start]) {
                            case '*':
                                if (start + 1 < end && chars[start + 1] == '/') {
                                    break Skiploop;
                                }
                            case '\r':
                            case '\n':
                                if (pCtx != null)
                                    pCtx.setLineOffset((short) start);
                                lines++;
                                break;
                            }
                            start++;
                        }
                    }
                }
                if (start == end)
                    return start;
                if (chars[start] == '\'' || chars[start] == '"') {
                    start = captureStringLiteral(chars[start], chars, start, end);
                }
                else if (chars[start] == type) {
                    depth++;
                }
                else if (chars[start] == term && --depth == 0) {
                    if (pCtx != null)
                        pCtx.incrementLineCount(lines);
                    return start;
                }
            }
        }

        switch (type) {
        case '[':
            throw new CompileException("unbalanced braces [ ... ]", chars, st);
        case '{':
            throw new CompileException("unbalanced braces { ... }", chars, st);
        case '(':
            throw new CompileException("unbalanced braces ( ... )", chars, st);
        default:
            throw new CompileException("unterminated string literal", chars, st);
        }
    }

    private static int captureStringLiteral(final char type, final char[] expr, int cursor, final int end) {
        while (++cursor < end && expr[cursor] != type) {
            if (expr[cursor] == '\\')
                cursor++;
        }

        if (cursor >= end || expr[cursor] != type) {
            throw new CompileException("unterminated string literal", expr, cursor);
        }

        return cursor;
    }

    public static CompiledInline compileTemplate(final CharSequence template) {
        return new InlineCompiler(template, ParserContext.create()).compile();
    }

    public static CompiledInline compileTemplate(final CharSequence template,
            final Map<String, Class<? extends Node>> customNodes) {
        return new InlineCompiler(template, customNodes, ParserContext.create()).compile();
    }

    public static CompiledInline compileTemplate(final CharSequence template,
            final Map<String, Class<? extends Node>> customNodes,
            final ParserContext context) {
        return new InlineCompiler(template, customNodes, context).compile();
    }

    public static CompiledInline compileTemplate(final CharSequence template, final ParserContext context) {
        return new InlineCompiler(template, context).compile();
    }

    public static CompiledInline compileTemplate(final String template) {
        return new InlineCompiler(template, ParserContext.create()).compile();
    }

    public static CompiledInline compileTemplate(final String template,
            final Map<String, Class<? extends Node>> customNodes) {
        return new InlineCompiler(template, customNodes, ParserContext.create()).compile();
    }

    public static CompiledInline compileTemplate(final String template,
            final Map<String, Class<? extends Node>> customNodes,
            final ParserContext context) {
        return new InlineCompiler(template, customNodes, context).compile();
    }

    public static CompiledInline compileTemplate(final String template, final ParserContext context) {
        return new InlineCompiler(template, context).compile();
    }

    public static boolean isIdentifierPart(final int c) {
        return ((c > 96 && c < 123)
                || (c > 64 && c < 91) || (c > 47 && c < 58) || (c == '_') || (c == '$')
                || Character.isJavaIdentifierPart(c));
    }

    private static boolean isWhitespace(final char c) {
        return c < '\u0020' + 1;
    }

    private static char[] subset(final char[] array, final int start, final int length) {

        char[] newArray = new char[length];

        for (int i = 0; i < newArray.length; i++) {
            newArray[i] = array[i + start];
        }

        return newArray;
    }

    private int colStart;

    private int cursor;

    private Map<String, Class<? extends Node>> customNodes;

    private int lastTextRangeEnding;

    private int length;

    private int line;

    private ParserContext parserContext;

    private int start;

    private char[] template;

    public InlineCompiler(final char[] template) {
        this.length = (this.template = template).length;
    }

    public InlineCompiler(final char[] template, final Map<String, Class<? extends Node>> customNodes) {
        this.length = (this.template = template).length;
        this.customNodes = customNodes;
    }

    public InlineCompiler(final char[] template, final Map<String, Class<? extends Node>> customNodes,
            final ParserContext context) {
        this.length = (this.template = template).length;
        this.customNodes = customNodes;
        this.parserContext = context;
    }

    public InlineCompiler(final char[] template, final ParserContext context) {
        this.length = (this.template = template).length;
        this.parserContext = context;
    }

    public InlineCompiler(final CharSequence sequence) {
        this.length = (this.template = sequence.toString().toCharArray()).length;
    }

    public InlineCompiler(final CharSequence sequence, final Map<String, Class<? extends Node>> customNodes) {
        this.length = (this.template = sequence.toString().toCharArray()).length;
        this.customNodes = customNodes;
    }

    public InlineCompiler(final CharSequence sequence, final Map<String, Class<? extends Node>> customNodes,
            final ParserContext context) {
        this.length = (this.template = sequence.toString().toCharArray()).length;
        this.customNodes = customNodes;
        this.parserContext = context;
    }

    public InlineCompiler(final CharSequence sequence, final ParserContext context) {
        this.length = (this.template = sequence.toString().toCharArray()).length;
        this.parserContext = context;
    }

    public InlineCompiler(final String template) {
        this.length = (this.template = template.toCharArray()).length;
    }

    public InlineCompiler(final String template, final Map<String, Class<? extends Node>> customNodes) {
        this.length = (this.template = template.toCharArray()).length;
        this.customNodes = customNodes;
    }

    public InlineCompiler(final String template, final Map<String, Class<? extends Node>> customNodes,
            final ParserContext context) {
        this.length = (this.template = template.toCharArray()).length;
        this.customNodes = customNodes;
        this.parserContext = context;
    }

    private char[] capture() {
        char[] newChar = new char[cursor - start];
        for (int i = 0; i < newChar.length; i++) {
            newChar[i] = template[i + start];
        }
        return newChar;
    }

    private int captureOrbInternal() {
        try {
            ParserContext pCtx = new ParserContext();
            cursor = balancedCaptureWithLineAccounting(template, start = cursor, length, '{', pCtx);
            line += pCtx.getLineCount();
            int ret = start + 1;
            start = cursor + 1;
            return ret;
        } catch (CompileException e) {
            e.setLineNumber(line);
            e.setColumn((cursor - colStart) + 1);
            throw e;
        }
    }

    private int captureOrbToken() {
        int newStart = ++cursor;
        while ((cursor != length) && isIdentifierPart(template[cursor]))
            cursor++;
        if (cursor != length && template[cursor] == '{')
            return newStart;
        return -1;
    }

    public CompiledInline compile() {
        return new CompiledInline(template, compileFrom(null, new ExecutionStack()));
    }

    public Node compileFrom(Node root, final ExecutionStack stack) {
        line = 1;

        Node n = root;
        if (root == null) {
            n = root = new TextNode(0, 0);
        }

        IfNode last;
        Integer opcode;
        String name;
        int x;

        try {
            while (cursor < length) {
                switch (template[cursor]) {
                case '\n':
                    line++;
                    colStart = cursor + 1;
                    break;
                case '@':
                case '$':
                    if (isNext(template[cursor])) {
                        start = ++cursor;
                        (n = markTextNode(n)).setEnd(n.getEnd() + 1);
                        start = lastTextRangeEnding = ++cursor;

                        continue;
                    }
                    if ((x = captureOrbToken()) != -1) {
                        start = x;
                        switch ((opcode = OPCODES.get(name = new String(capture()))) == null ? 0 : opcode) {
                        case Opcodes.IF:
                            /**
                             * Capture any residual text node, and push the if statement on the nesting stack.
                             */
                            stack.push(n = markTextNode(n).next =
                                    new CompiledIfNode(start, name, template, captureOrbInternal(), start,
                                            parserContext));

                            n.setTerminus(new TerminalNode());

                            break;

                        case Opcodes.ELSE:
                            if (!stack.isEmpty() && stack.peek() instanceof IfNode) {
                                markTextNode(n).next = (last = (IfNode) stack.pop()).getTerminus();

                                last.demarcate(last.getTerminus(), template);
                                last.next = n = new CompiledIfNode(start, name, template,
                                        captureOrbInternal(), start, parserContext);

                                stack.push(n);
                            }
                            break;

                        case Opcodes.FOREACH:
                            stack.push(
                                    n = markTextNode(n).next = new CompiledForEachNode(start, name,
                                            template, captureOrbInternal(), start, parserContext));

                            n.setTerminus(new TerminalNode());

                            break;

                        case Opcodes.CODE:
                            n = markTextNode(n)
                                    .next = new CompiledCodeNode(start, name, template,
                                            captureOrbInternal(), start = cursor + 1, parserContext);
                            break;

                        case Opcodes.EVAL:
                            n = markTextNode(n).next =
                                    new CompiledEvalNode(start, name, template, captureOrbInternal(),
                                            start = cursor + 1, parserContext);
                            break;

                        case Opcodes.COMMENT:
                            n = markTextNode(n)
                                    .next = new CommentNode(start, name, template, captureOrbInternal(),
                                            start = cursor + 1);

                            break;

                        case Opcodes.END:
                            n = markTextNode(n);

                            Node end = (Node) stack.pop();
                            Node terminal = end.getTerminus();

                            terminal.setCStart(captureOrbInternal());
                            terminal.setEnd((lastTextRangeEnding = start) - 1);
                            terminal.calculateContents(template);

                            if (end.demarcate(terminal, template))
                                n = n.next = terminal;
                            else
                                n = terminal;

                            break;

                        default:
                            if (name.length() == 0) {
                                n = markTextNode(n).next =
                                        new CompiledExpressionNode(start, name, template, captureOrbInternal(),
                                                start = cursor + 1, parserContext);
                            }
                            else if (customNodes != null && customNodes.containsKey(name)) {
                                Class<? extends Node> customNode = customNodes.get(name);

                                try {
                                    (n = markTextNode(n).next = (customNode.newInstance())).setBegin(start);
                                    n.setName(name);
                                    n.setCStart(captureOrbInternal());
                                    n.setCEnd(start = cursor + 1);
                                    n.setEnd(n.getCEnd());

                                    n.setContents(subset(template, n.getCStart(), n.getCEnd() - n.getCStart() - 1));

                                    if (n.isOpenNode()) {
                                        stack.push(n);
                                    }
                                } catch (InstantiationException e) {
                                    throw new RuntimeException("unable to instantiate custom node class: "
                                            + customNode.getName());
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException("unable to instantiate custom node class: "
                                            + customNode.getName());
                                }
                            }
                            else {
                                throw new RuntimeException("unknown token type: " + name);
                            }
                        }
                    }

                    break;
                }
                cursor++;
            }
        } catch (RuntimeException e) {
            CompileException ce = new CompileException(e.getMessage(), template, cursor, e);
            ce.setExpr(template);

            if (e instanceof CompileException) {
                CompileException ce2 = (CompileException) e;
                if (ce2.getCursor() != -1) {
                    ce.setCursor(ce2.getCursor());
                    if (ce2.getColumn() == -1)
                        ce.setColumn(ce.getCursor() - colStart);
                    else
                        ce.setColumn(ce2.getColumn());
                }
            }
            ce.setLineNumber(line);

            throw ce;
        }

        if (!stack.isEmpty()) {
            CompileException ce = new CompileException("unclosed @" + ((Node) stack.peek()).getName()
                    + "{} block. expected @end{}", template, cursor);
            ce.setColumn(cursor - colStart);
            ce.setLineNumber(line);
            throw ce;
        }

        if (start < template.length) {
            n = n.next = new TextNode(start, template.length);
        }
        n.next = new EndNode();

        n = root;
        do {
            if (n.getLength() != 0) {
                break;
            }
        } while ((n = n.getNext()) != null);

        if (n != null && n.getLength() == template.length - 1) {
            if (n instanceof ExpressionNode) {
                return new CompiledTerminalExpressionNode(n, parserContext);
            }
            else {
                return n;
            }
        }

        return root;
    }

    public ParserContext getParserContext() {
        return parserContext;
    }

    private boolean isNext(final char c) {
        return cursor != length && template[cursor + 1] == c;
    }

    private Node markTextNode(final Node n) {
        int s = (n.getEnd() > lastTextRangeEnding ? n.getEnd() : lastTextRangeEnding);

        if (s < start) {
            return n.next = new TextNode(s, lastTextRangeEnding = start - 1);
        }
        return n;
    }
}
