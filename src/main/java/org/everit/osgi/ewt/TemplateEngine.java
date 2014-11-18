package org.everit.osgi.ewt;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.everit.osgi.ewt.internal.EWTNodeVisitor;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

public class TemplateEngine {

    private String ewtAttributeprefix = "data-ewt-";

    public CompiledTemplate compileTemplate(InputStream stream, String charset) throws ParserException,
            UnsupportedEncodingException {
        Page page = new Page(stream, charset);
        Lexer lexer = new Lexer(page);
        NodeVisitor visitor = new EWTNodeVisitor(ewtAttributeprefix);
        for (Node node = lexer.nextNode(); node != null; node = lexer.nextNode()) {
            node.accept(visitor);
        }
        return null;
    }

    public void setEwtAttributeprefix(String prefix) {
        this.ewtAttributeprefix = prefix;
    }
}
