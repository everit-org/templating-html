/**
 * This file is part of Everit - HTML Templating.
 *
 * Everit - HTML Templating is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTML Templating is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTML Templating.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.templating.html;

import java.util.HashMap;
import java.util.Map;

import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.internal.CompiledTemplateImpl;
import org.everit.templating.html.internal.HTMLNodeVisitor;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.Source;
import org.htmlparser.lexer.StringSource;
import org.htmlparser.util.ParserException;

/**
 * This is the entry class that can compile templates.
 *
 */
public class HTMLTemplateCompiler implements TemplateCompiler {

    /**
     * Default prefix for HTML templating attributes.
     */
    public static final String DEFAULT_ATTRIBUTE_PREFIX = "data-eht-";

    /**
     * The prefix of the Web Templating attributes. By default it is "data-ewt-".
     */
    private final String ehtAttributeprefix;

    /**
     * The compiler for expressions.
     */
    private final ExpressionCompiler expressionCompiler;

    /**
     * Map of inline compilers that can be used as part of the HTML template.
     */
    private final Map<String, TemplateCompiler> inlineCompilers;

    /**
     * Creates a new TemplateCompiler with the {@value #DEFAULT_ATTRIBUTE_PREFIX} default attribute prefix.
     *
     * @param expressionCompiler
     *            The compiler for expressions.
     */
    public HTMLTemplateCompiler(final ExpressionCompiler expressionCompiler) {
        this(DEFAULT_ATTRIBUTE_PREFIX, expressionCompiler, new HashMap<String, TemplateCompiler>());
    }

    /**
     * Creates a new TemplateCompiler with the {@value #DEFAULT_ATTRIBUTE_PREFIX} default attribute prefix.
     *
     * @param expressionCompiler
     *            The compiler for expressions.
     * @param inlineCompilers
     *            Inline compilers can be used to put fragments into the HTML template with different syntax. E.g.: A
     *            Javascript should be templated with different syntax.
     */
    public HTMLTemplateCompiler(final ExpressionCompiler expressionCompiler,
            final Map<String, TemplateCompiler> inlineCompilers) {
        this(DEFAULT_ATTRIBUTE_PREFIX, expressionCompiler, inlineCompilers);
    }

    /**
     * Creates a new TemplateCompiler with the {@value #DEFAULT_ATTRIBUTE_PREFIX} default attribute prefix.
     *
     * @param expressionCompiler
     *            The compiler for expressions.
     * @param inlineCompilers
     *            Inline compilers can be used to put fragments into the HTML template with different syntax. E.g.: A
     *            Javascript should be templated with different syntax.
     * @param ehtAttributeprefix
     *            The prefix of the attributes of HTML templating.
     */
    public HTMLTemplateCompiler(final String ehtAttributeprefix, final ExpressionCompiler expressionCompiler,
            final Map<String, TemplateCompiler> inlineCompilers) {
        this.ehtAttributeprefix = ehtAttributeprefix;
        this.expressionCompiler = expressionCompiler;
        this.inlineCompilers = new HashMap<String, TemplateCompiler>(inlineCompilers);
    }

    @Override
    public CompiledTemplate compile(final char[] document, final int templateStart, final int templateLength,
            final ParserConfiguration parserConfiguration) {
        return compile(String.valueOf(document, templateStart, templateLength), parserConfiguration);
    }

    @Override
    public CompiledTemplate compile(final String template, final ParserConfiguration parserConfiguration) {
        Source source = new StringSource(template);
        Page page = new Page(source);
        Lexer lexer = new Lexer(page);
        HTMLNodeVisitor visitor = new HTMLNodeVisitor(ehtAttributeprefix, expressionCompiler, inlineCompilers,
                parserConfiguration);
        visitor.beginParsing();
        try {
            for (Node node = lexer.nextNode(); node != null; node = lexer.nextNode()) {
                node.accept(visitor);
            }
        } catch (ParserException e) {
            throw new RuntimeException(e);
        }
        visitor.finishedParsing();
        return new CompiledTemplateImpl(visitor.getRootNode());
    }

}
