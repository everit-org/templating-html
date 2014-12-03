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
package org.everit.templating.web.internal;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.everit.expression.CompileException;
import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserContext;
import org.everit.templating.CompiledTemplate;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

/**
 * This is the entry class that can compile templates.
 *
 */
public class TemplateCompilerImpl {

    /**
     * The prefix of the Web Templating attributes. By default it is "data-ewt-".
     */
    private final String ewtAttributeprefix;

    /**
     * The compiler for expressions.
     */
    private final ExpressionCompiler expressionCompiler;

    /**
     * Creates a new TemplateCompiler with the "data-ewt-" default attribute prefix.
     *
     * @param expressionCompiler
     *            The compiler of expressions.
     */
    public TemplateCompilerImpl(final ExpressionCompiler expressionCompiler) {
        this("data-ewt-", expressionCompiler);
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
    public TemplateCompilerImpl(final String ewtAttributeprefix, final ExpressionCompiler expressionCompiler) {
        this.ewtAttributeprefix = ewtAttributeprefix;
        this.expressionCompiler = expressionCompiler;
    }

    /**
     * Compiles a template.
     *
     * @param stream
     *            The stream where the template is read from.
     * @param charset
     *            The character encoding of the template.
     * @return The compiled template.
     */
    public CompiledTemplate compileTemplate(final InputStream stream, final String charset) {
        try {
            Page page = new Page(stream, charset);
            return compileTemplateInternal(page);
        } catch (UnsupportedEncodingException e) {
            throw new CompileException(e);
        } catch (ParserException e) {
            throw new CompileException(e);
        }

    }

    public CompiledTemplate compileTemplate(final String template) {
        this(template, null);
    }

    public CompiledTemplate compileTemplate(final String template, final ParserContext parserContext) {
        Page page = new Page(template);
        try {
            return compileTemplateInternal(page, new ParserContext(parserContext));
        } catch (ParserException e) {
            throw new CompileException(e);
        }
    }

    private CompiledTemplate compileTemplateInternal(final Page page, final ParserContext parserContext)
            throws ParserException {
        Lexer lexer = new Lexer(page);
        HTMLNodeVisitor visitor = new HTMLNodeVisitor(ewtAttributeprefix, expressionCompiler);
        visitor.beginParsing();
        for (Node node = lexer.nextNode(); node != null; node = lexer.nextNode()) {
            node.accept(visitor);
        }
        visitor.finishedParsing();
        return new CompiledTemplateImpl(visitor.getRootNode());
    }

}
