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
package org.everit.templating.html.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.everit.expression.ParserConfiguration;
import org.everit.expression.mvel.MvelExpressionCompiler;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.HTMLTemplateCompiler;
import org.everit.templating.text.TextTemplateCompiler;
import org.everit.templating.util.CompileException;
import org.junit.Assert;
import org.junit.Test;

public class HTMLTemplatingTest {

    private static String readTemplate(final String templateName) {
        InputStream stream = HTMLTemplatingTest.class.getClassLoader().getResourceAsStream("META-INF/test1.html");
        try {
            InputStreamReader reader = new InputStreamReader(stream, "UTF8");
            StringBuilder sb = new StringBuilder();
            char[] cbuf = new char[1024];
            int r = reader.read(cbuf);
            while (r >= 0) {
                sb.append(cbuf, 0, r);
                r = reader.read(cbuf);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private TemplateCompiler createTestEngine() {
        Map<String, TemplateCompiler> inlineCompilers = new HashMap<String, TemplateCompiler>();
        MvelExpressionCompiler expressionCompiler = new MvelExpressionCompiler();
        inlineCompilers.put("text", new TextTemplateCompiler(expressionCompiler));
        TemplateCompiler engine = new HTMLTemplateCompiler(expressionCompiler, inlineCompilers);
        return engine;
    }

    private ParserConfiguration createTestParserConfiguration() {
        ParserConfiguration parserConfiguration = new ParserConfiguration(this.getClass().getClassLoader());
        parserConfiguration.setStartColumn(11);
        parserConfiguration.setStartRow(10);
        return parserConfiguration;
    }

    @Test
    public void testBookmark() {
        TemplateCompiler engine = new HTMLTemplateCompiler(new MvelExpressionCompiler());

        CompiledTemplate compiledTemplate = engine.compile(readTemplate("META-INF/test1.html"),
                new ParserConfiguration(this.getClass().getClassLoader()));
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        HashMap<String, Object> vars = new HashMap<String, Object>();

        List<User> users = new ArrayList<User>();
        users.add(new User(0, "Niels", "Holgerson"));
        users.add(new User(1, "B", "Zs"));

        vars.put("users", users);

        compiledTemplate.render(writer, vars, "bookmark1");

        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDuplicateAttribute() {
        try {
            createTestEngine().compile("<test id='' id='' data-eht-attr-id='' />",
                    createTestParserConfiguration());
            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(23, e.getColumn());
        }
    }

    @Test
    public void testDuplicateEHTAttribute() {
        try {
            createTestEngine().compile("<test data-eht-text='\"\"' data-eht-utext='\"\"' />",
                    createTestParserConfiguration());
            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(36, e.getColumn());
        }
    }

    @Test
    public void testDuplicateFragmentId() {
        try {
            createTestEngine().compile("<test data-eht-fragment='fragment1'  /><test data-eht-fragment='fragment1' />",
                    createTestParserConfiguration());
            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(75, e.getColumn());
        }
    }

    @Test
    public void testForeachInconsistentType() {
        try {
            createTestEngine().compile("<test data-eht-foreach=\"'not a Map'\"  />",
                    createTestParserConfiguration()).render(new StringWriter(), new HashMap<String, Object>());

            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(35, e.getColumn());
        }
    }

    @Test
    public void testForeachNullKey() {
        try {
            createTestEngine().compile("<test data-eht-foreach='[null : ({\"\"})]'  />",
                    createTestParserConfiguration()).render(new StringWriter(), new HashMap<String, Object>());

            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(35, e.getColumn());
        }
    }

    @Test
    public void testForeachWrongValueType() {
        try {
            createTestEngine().compile("<test data-eht-foreach='[\"test\" : \"test\"]'  />",
                    createTestParserConfiguration()).render(new StringWriter(), new HashMap<String, Object>());

            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            System.out.println(e.getMessage());
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(35, e.getColumn());
        }
    }

    @Test
    public void testFull() {
        MvelExpressionCompiler expressionCompiler = new MvelExpressionCompiler();

        Map<String, TemplateCompiler> inlineCompilers = new HashMap<String, TemplateCompiler>();
        inlineCompilers.put("text", new TextTemplateCompiler(expressionCompiler));

        TemplateCompiler engine = new HTMLTemplateCompiler(expressionCompiler, inlineCompilers);

        CompiledTemplate compiledTemplate = engine.compile(readTemplate("META-INF/test1.html"),
                new ParserConfiguration(this.getClass().getClassLoader()));
        // Writer writer = new OutputStreamWriter(System.out);
        Writer writer = new NullWriter();

        HashMap<String, Object> vars = new HashMap<String, Object>();

        List<User> users = new ArrayList<User>();
        users.add(new User(0, "Niels", "Holgerson"));
        users.add(new User(1, "B", "Zs"));

        vars.put("users", users);

        long startTime = System.nanoTime();
        int n = 200000;
        for (int i = 0; i < n; i++) {
            compiledTemplate.render(writer, vars);
        }
        long endTime = System.nanoTime();
        System.out.println("Time: " + ((endTime - startTime) / 1000000) + "ms, "
                + ((double) n * 1000000 / (endTime - startTime)) + " db/ms");

        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testInlineAndTextTogether() {
        try {
            createTestEngine().compile("<test data-eht-inline='text' data-eht-text=\"'someText'\" />",
                    createTestParserConfiguration());
            Assert.fail("Should throw an exception");
        } catch (CompileException e) {
            Assert.assertEquals(12, e.getColumn());
            Assert.assertEquals(10, e.getLineNumber());
        }
    }

    @Test
    public void testInlineException() {
        TemplateCompiler engine = createTestEngine();

        ParserConfiguration parserConfiguration = createTestParserConfiguration();
        try {
            engine.compile("<test data-eht-inline='noSuchInline'></test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (CompileException e) {
            Assert.assertEquals(34, e.getColumn());
            Assert.assertEquals(10, e.getLineNumber());
        }

        try {
            engine.compile("\n<test data-eht-inline='noSuchInline'></test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (CompileException e) {
            Assert.assertEquals(24, e.getColumn());
            Assert.assertEquals(11, e.getLineNumber());
        }

        try {
            engine.compile("\n<test data-eht-inline='text'>@{[[][}</test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (org.mvel2.CompileException e) {
            Assert.assertEquals(32, e.getColumn());
            Assert.assertEquals(11, e.getLineNumber());
        }

        try {
            engine.compile("\n<test data-eht-inline='text'>\n@{[[][}</test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (org.mvel2.CompileException e) {
            Assert.assertEquals(3, e.getColumn());
            Assert.assertEquals(12, e.getLineNumber());
        }

    }

    @Test
    public void testRootFragmentId() {
        try {
            createTestEngine().compile("<test data-eht-fragment='root' />",
                    createTestParserConfiguration());
            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(36, e.getColumn());
        }
    }

    @Test
    public void testUnrecognizedAttribute() {
        try {
            createTestEngine().compile("<test data-eht-nothing='' />",
                    createTestParserConfiguration());
            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(17, e.getColumn());
        }
    }
}
