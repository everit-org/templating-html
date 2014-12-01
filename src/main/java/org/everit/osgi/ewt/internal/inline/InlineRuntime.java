package org.everit.osgi.ewt.internal.inline;

import java.util.Map;

import org.everit.osgi.ewt.internal.inline.res.Node;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.util.TemplateOutputStream;
import org.mvel2.templates.util.io.StringBuilderStream;
import org.mvel2.util.ExecutionStack;

/**
 * This is the root of the template runtime, and contains various utility methods for executing templates.
 */
public class InlineRuntime {

    public static Object execute(final CompiledInline compiled, final Map vars) {
        return execute(compiled.getRoot(), compiled.getTemplate(), new StringBuilder(), null,
                new MapVariableResolverFactory(vars));
    }

    public static Object execute(final Node root, final char[] template,
            final StringBuilder appender, final Object context,
            final VariableResolverFactory factory) {

        return new InlineRuntime(template, root, ".").execute(appender, context, factory);
    }

    public static Object execute(final Node root, final char[] template,
            final StringBuilder appender, final Object context,
            final VariableResolverFactory factory, final String baseDir) {

        return new InlineRuntime(template, root, baseDir).execute(appender, context, factory);
    }

    public static Object execute(final Node root, final char[] template,
            final TemplateOutputStream appender, final Object context,
            final VariableResolverFactory factory) {

        return new InlineRuntime(template, root, ".").execute(appender, context, factory);
    }

    public static Object execute(final Node root, final char[] template,
            final TemplateOutputStream appender, final Object context,
            final VariableResolverFactory factory, final String baseDir) {

        return new InlineRuntime(template, root, baseDir).execute(appender, context, factory);
    }

    private String baseDir;

    private ExecutionStack relPath;

    private Node rootNode;

    private char[] template;

    public InlineRuntime(final char[] template, final Node rootNode, final String baseDir) {
        this.template = template;
        this.rootNode = rootNode;
        this.baseDir = baseDir;
    }

    public Object execute(final StringBuilder appender, final Object context, final VariableResolverFactory factory) {
        return execute(new StringBuilderStream(appender), context, factory);
    }

    public Object execute(final TemplateOutputStream stream, final Object context, final VariableResolverFactory factory) {
        return rootNode.eval(this, stream, context, factory);
    }

    public ExecutionStack getRelPath() {
        if (relPath == null) {
            relPath = new ExecutionStack();
            relPath.push(baseDir);
        }
        return relPath;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public char[] getTemplate() {
        return template;
    }

    public void setRootNode(final Node rootNode) {
        this.rootNode = rootNode;
    }

    public void setTemplate(final char[] template) {
        this.template = template;
    }
}
