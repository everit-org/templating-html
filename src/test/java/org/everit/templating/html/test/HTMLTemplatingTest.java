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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.everit.expression.ParserConfiguration;
import org.everit.expression.mvel.MvelExpressionCompiler;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.TemplateContext;
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

    private void runFullInternal(final CompiledTemplate compiledTemplate, final Writer writer,
            final HashMap<String, Object> vars, final int threadNum, final int cycle) {
        final AtomicInteger processingThreads = new AtomicInteger(threadNum);

        for (int i = 0; i < threadNum; i++) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    for (int j = 0; j < cycle; j++) {
                        compiledTemplate.render(writer, vars);
                    }
                    int runningThreads = processingThreads.decrementAndGet();
                    if (runningThreads == 0) {
                        synchronized (processingThreads) {
                            processingThreads.notify();
                        }
                    }
                }
            }).start();
        }

        synchronized (processingThreads) {
            if (processingThreads.get() > 0) {
                try {
                    processingThreads.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testBookmark() {
        TemplateCompiler engine = createTestEngine();

        CompiledTemplate compiledTemplate = engine.compile(readTemplate("META-INF/test1.html"),
                new ParserConfiguration(this.getClass().getClassLoader()));
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        HashMap<String, Object> vars = new HashMap<String, Object>();

        List<User> users = new LinkedList<User>();
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
            createTestEngine().compile(
                    "<test data-eht-fragment=\"'fragment1'\"  /><test data-eht-fragment=\"'fragment1'\" />",
                    createTestParserConfiguration());
            Assert.fail("Exception should have been thrown");
        } catch (CompileException e) {
            Assert.assertEquals(10, e.getLineNumber());
            Assert.assertEquals(77, e.getColumn());
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

        ParserConfiguration parserConfiguration = new ParserConfiguration(this.getClass().getClassLoader());
        Map<String, Class<?>> variableTypes = new HashMap<String, Class<?>>();
        variableTypes.put("users", List.class);
        variableTypes.put("user", User.class);
        variableTypes.put("rowId", Integer.class);
        variableTypes.put("hello_world", String.class);
        variableTypes.put("template_ctx", TemplateContext.class);

        parserConfiguration.setVariableTypes(variableTypes);
        final CompiledTemplate compiledTemplate = engine.compile(readTemplate("META-INF/test1.html"),
                parserConfiguration);
        // Writer writer = new OutputStreamWriter(System.out);
        final Writer writer = new NullWriter();

        final HashMap<String, Object> vars = new HashMap<String, Object>();

        List<User> users = new LinkedList<User>();
        users.add(new User(0, "Niels", "Holgerson"));
        users.add(new User(1, "B", "Zs"));

        vars.put("users", users);

        // runFullInternal(compiledTemplate, writer, vars, 1, 20000);

        final int threadNum = 1;
        final int cycle = 1;

        // long startTime = System.nanoTime();

        runFullInternal(compiledTemplate, writer, vars, threadNum, cycle);

        // long endTime = System.nanoTime();
        // System.out.println("Time: " + ((endTime - startTime) / 1000000) + "ms, "
        // + ((double) cycle * threadNum * 1000000 / (endTime - startTime)) + " db/ms");

        try {
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testInlineAndTextTogether() {
        try {
            createTestEngine().compile("<test data-eht-inline='\"text\"' data-eht-text=\"'someText'\" />",
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
            engine.compile("<test data-eht-inline=\"'noSuchInline'\"></test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (CompileException e) {
            Assert.assertEquals(34, e.getColumn());
            Assert.assertEquals(10, e.getLineNumber());
        }

        try {
            engine.compile("\n<test data-eht-inline=\"'noSuchInline'\"></test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (CompileException e) {
            Assert.assertEquals(24, e.getColumn());
            Assert.assertEquals(11, e.getLineNumber());
        }

        try {
            engine.compile("\n<test data-eht-inline=\"'text'\">@{[[][}</test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (org.mvel2.CompileException e) {
            Assert.assertEquals(34, e.getColumn());
            Assert.assertEquals(11, e.getLineNumber());
        }

        try {
            engine.compile("\n<test data-eht-inline=\"'text'\">\n@{[[][}</test>", parserConfiguration);
            Assert.fail("Should throw an exception");
        } catch (org.mvel2.CompileException e) {
            Assert.assertEquals(3, e.getColumn());
            Assert.assertEquals(12, e.getLineNumber());
        }

    }

    @Test
    public void testRootFragmentId() {
        try {
            createTestEngine().compile("<test data-eht-fragment=\"'root'\" />",
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

    @Test
    public void testWriterUsageWithText() {
        String template = "<a><b data-eht-text=\"appender.run()\">someContent</b></a>";
        TemplateCompiler engine = createTestEngine();
        CompiledTemplate compiledTemplate = engine.compile(template, new ParserConfiguration(
            HTMLTemplateCompiler.class.getClassLoader()));

        final StringWriter stringWriter = new StringWriter();
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("appender", new Runnable() {
          
            @Override
            public void run() {
                stringWriter.write("Hello world");
            }
        });

        compiledTemplate.render(stringWriter, vars);

        Assert.assertEquals("<a><b>Hello world</b></a>", stringWriter.toString());
    }
}
