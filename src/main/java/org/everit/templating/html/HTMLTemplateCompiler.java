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
     * The prefix of the Web Templating attributes. By default it is "data-ewt-".
     */
    private final String ewtAttributeprefix;

    /**
     * The compiler for expressions.
     */
    private final ExpressionCompiler expressionCompiler;

    private final Map<String, TemplateCompiler> inlineCompilers;

    /**
     * Creates a new TemplateCompiler with the "data-ewt-" default attribute prefix.
     *
     * @param expressionCompiler
     *            The compiler of expressions.
     */
    public HTMLTemplateCompiler(final ExpressionCompiler expressionCompiler) {
        this("data-ewt-", expressionCompiler, new HashMap<String, TemplateCompiler>());
    }

    /**
     * Creates a new template compiler.
     *
     * @param ewtAttributeprefix
     *            The prefix of ewt attributes. Dynamic attributes (each, bookmark, ...) will be searched with this
     *            prefix.
     * @param expressionCompiler
     *            The compiler of the expressions.
     */
    public HTMLTemplateCompiler(final String ewtAttributeprefix, final ExpressionCompiler expressionCompiler,
            final Map<String, TemplateCompiler> inlineCompilers) {
        this.ewtAttributeprefix = ewtAttributeprefix;
        this.expressionCompiler = expressionCompiler;
        this.inlineCompilers = new HashMap<String, TemplateCompiler>(inlineCompilers);
    }

    @Override
    public CompiledTemplate compile(final String template, final ParserConfiguration parserConfiguration) {
        Source source = new StringSource(template);
        Page page = new Page(source);
        Lexer lexer = new Lexer(page);
        HTMLNodeVisitor visitor = new HTMLNodeVisitor(ewtAttributeprefix, expressionCompiler, inlineCompilers,
                parserConfiguration);
        visitor.beginParsing();
        try {
            for (Node node = lexer.nextNode(); node != null; node = lexer.nextNode()) {
                node.accept(visitor);
            }
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        visitor.finishedParsing();
        return new CompiledTemplateImpl(visitor.getRootNode());
    }

}
