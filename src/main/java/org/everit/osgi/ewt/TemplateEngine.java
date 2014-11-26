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
package org.everit.osgi.ewt;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.everit.osgi.ewt.el.ExpressionCompiler;
import org.everit.osgi.ewt.internal.EWTNodeVisitor;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

public class TemplateEngine {

    private final String ewtAttributeprefix;

    private final ExpressionCompiler expressionCompiler;

    public TemplateEngine(ExpressionCompiler expressionCompiler) {
        this("data-ewt-", expressionCompiler);
    }

    public TemplateEngine(String ewtAttributeprefix, ExpressionCompiler expressionCompiler) {
        this.ewtAttributeprefix = ewtAttributeprefix;
        this.expressionCompiler = expressionCompiler;
    }

    public CompiledTemplate compileTemplate(InputStream stream, String charset) throws ParserException,
            UnsupportedEncodingException {
        Page page = new Page(stream, charset);
        Lexer lexer = new Lexer(page);
        EWTNodeVisitor visitor = new EWTNodeVisitor(ewtAttributeprefix, expressionCompiler);
        visitor.beginParsing();
        for (Node node = lexer.nextNode(); node != null; node = lexer.nextNode()) {
            node.accept(visitor);
        }
        visitor.finishedParsing();
        return null;
    }
}
