/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.apache.commons.jexl2.JexlException;
import org.everit.expression.ParserConfiguration;
import org.everit.expression.jexl.JexlExpressionCompiler;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.TemplateContext;
import org.everit.templating.html.HTMLTemplateCompiler;
import org.everit.templating.text.TextTemplateCompiler;
import org.everit.templating.util.CompileException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class of HTML templating.
 */
public class HTMLTemplatingTest {

  private static final int INITIAL_START_COLUMN = 11;

  private static final int INITIAL_START_ROW = 10;

  private static String readTemplate(final String templateName) {
    InputStream stream = HTMLTemplatingTest.class.getClassLoader().getResourceAsStream(
        "META-INF/test1.html");
    try {
      InputStreamReader reader = new InputStreamReader(stream, "UTF8");
      StringBuilder sb = new StringBuilder();
      final int bufferSize = 1024;
      char[] cbuf = new char[bufferSize];
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
    Map<String, TemplateCompiler> inlineCompilers = new HashMap<>();
    JexlExpressionCompiler expressionCompiler = new JexlExpressionCompiler();
    inlineCompilers.put("text", new TextTemplateCompiler(expressionCompiler));
    TemplateCompiler engine = new HTMLTemplateCompiler(expressionCompiler, inlineCompilers);
    return engine;
  }

  private ParserConfiguration createTestParserConfiguration() {
    ParserConfiguration parserConfiguration = new ParserConfiguration(this.getClass()
        .getClassLoader());
    parserConfiguration.setStartColumn(INITIAL_START_COLUMN);
    parserConfiguration.setStartRow(INITIAL_START_ROW);
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
          throw new RuntimeException(e);
        }
      }
    }
  }

  @Test
  public void testBookmark() {
    TemplateCompiler engine = createTestEngine();

    CompiledTemplate compiledTemplate = engine.compile(
        HTMLTemplatingTest.readTemplate("META-INF/test1.html"),
        new ParserConfiguration(this.getClass().getClassLoader()));
    OutputStreamWriter writer = new OutputStreamWriter(System.out);
    HashMap<String, Object> vars = new HashMap<>();

    List<User> users = new LinkedList<>();
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
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());
      final int offsetOrErrorInText = 12;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
    }
  }

  @Test
  public void testDuplicateEHTAttribute() {
    try {
      createTestEngine().compile("<test data-eht-text='\"\"' data-eht-utext='\"\"' />",
          createTestParserConfiguration());
      Assert.fail("Exception should have been thrown");
    } catch (CompileException e) {
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());
      final int offsetOrErrorInText = 25;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
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
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());
      final int offsetOrErrorInText = 66;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
    }
  }

  @Test
  public void testForeachInconsistentType() {
    try {
      createTestEngine().compile("<test data-eht-foreach=\"'not a Map'\"  />",
          createTestParserConfiguration())
          .render(new StringWriter(), new HashMap<String, Object>());

      Assert.fail("Exception should have been thrown");
    } catch (CompileException e) {
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());

      final int offsetOrErrorInText = 24;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
    }
  }

  @Test
  public void testForeachNullKey() {
    try {
      createTestEngine().compile("<test data-eht-foreach='[null : ({\"\"})]'  />",
          createTestParserConfiguration())
          .render(new StringWriter(), new HashMap<String, Object>());

      Assert.fail("Exception should have been thrown");
    } catch (JexlException e) {
      System.out.println(e.getMessage());
      Assert.assertEquals("@1:7 parsing error near '... ull : ({\"\" ...'", e.getMessage());
    }
  }

  @Test
  public void testForeachWrongValueType() {
    try {
      createTestEngine().compile("<test data-eht-foreach='[\"test\" : \"test\"]'  />",
          createTestParserConfiguration())
          .render(new StringWriter(), new HashMap<String, Object>());

      Assert.fail("Exception should have been thrown");
    } catch (JexlException e) {
      Assert.assertEquals("@1:9 parsing error near '... st\" : \"tes ...'", e.getMessage());
    }
  }

  @Test
  public void testFull() {
    JexlExpressionCompiler expressionCompiler = new JexlExpressionCompiler();

    Map<String, TemplateCompiler> inlineCompilers = new HashMap<>();
    inlineCompilers.put("text", new TextTemplateCompiler(expressionCompiler));

    TemplateCompiler engine = new HTMLTemplateCompiler(expressionCompiler, inlineCompilers);

    ParserConfiguration parserConfiguration = new ParserConfiguration(this.getClass()
        .getClassLoader());
    Map<String, Class<?>> variableTypes = new HashMap<>();
    variableTypes.put("users", List.class);
    variableTypes.put("user", User.class);
    variableTypes.put("rowId", Integer.class);
    variableTypes.put("hello_world", String.class);
    variableTypes.put("template_ctx", TemplateContext.class);

    parserConfiguration.setVariableTypes(variableTypes);
    final CompiledTemplate compiledTemplate = engine.compile(
        HTMLTemplatingTest.readTemplate("META-INF/test1.html"),
        parserConfiguration);
    // Writer writer = new OutputStreamWriter(System.out);
    final Writer writer = new NullWriter();

    final HashMap<String, Object> vars = new HashMap<>();

    List<User> users = new LinkedList<>();
    users.add(new User(0, "Niels", "Holgerson"));
    users.add(new User(1, "B", "Zs"));

    vars.put("users", users);

    // runFullInternal(compiledTemplate, writer, vars, 1, 20000);

    final int threadNum = 1;
    final int cycle = 1;

    // long startTime = System.nanoTime();

    runFullInternal(compiledTemplate, writer, vars, threadNum, cycle);

    // long endTime = System.nanoTime();
    // System.xxout.println("Time: " + ((endTime - startTime) / 1000000) + "ms, "
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
      createTestEngine().compile(
          "<test data-eht-inline='\"text\"' data-eht-text=\"'someText'\" />",
          createTestParserConfiguration());
      Assert.fail("Should throw an exception");
    } catch (CompileException e) {
      final int offsetOrErrorInText = 1;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());
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
      final int offsetOrErrorInText = 23;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());
    }

    try {
      engine.compile("\n<test data-eht-inline=\"'noSuchInline'\"></test>", parserConfiguration);
      Assert.fail("Should throw an exception");
    } catch (CompileException e) {
      final int offsetOfErrorSinceLineBreak = 24;
      Assert.assertEquals(offsetOfErrorSinceLineBreak, e.getColumn());
      int initialRowPlusLineBreaks = INITIAL_START_ROW + 1;
      Assert.assertEquals(initialRowPlusLineBreaks, e.getLineNumber());
    }

    try {
      engine.compile("\n<test data-eht-inline=\"'text'\">@{[[][}</test>", parserConfiguration);
      Assert.fail("Should throw an exception");
    } catch (JexlException e) {
      Assert.assertEquals("@1:4 parsing error in '[[]['", e.getMessage());
    }

    try {
      engine.compile("\n<test data-eht-inline=\"'text'\">\n@{[[][}</test>", parserConfiguration);
      Assert.fail("Should throw an exception");
    } catch (JexlException e) {
      Assert.assertEquals("@1:4 parsing error in '[[]['", e.getMessage());
    }

  }

  @Test
  public void testLTInText() {
    StringWriter writer = new StringWriter();
    String template = "<b><a> some text with <in it</a> and <one more</b>";
    createTestEngine().compile(template, createTestParserConfiguration())
        .render(writer, new HashMap<String, Object>());

    Assert.assertEquals(template, writer.toString());
  }

  @Test
  public void testNotEscapingConstantAttributeInEHTNode() {
    CompiledTemplate compiledTemplate =
        createTestEngine().compile(
            "<e const=\"&lt;a&gt;\" data-eht-attr-dyn1=\"'&lt;b'\" data-eht-attr-dyn2=\"var1\" />",
            createTestParserConfiguration());

    StringWriter sw = new StringWriter();
    Map<String, Object> vars = new HashMap<>();
    vars.put("var1", "<c");
    compiledTemplate.render(sw, vars);
    Assert.assertEquals("<e const=\"&lt;a&gt;\" dyn1=\"&lt;b\" dyn2=\"&lt;c\" />", sw.toString());
  }

  @Test
  public void testRootFragmentId() {
    try {
      createTestEngine().compile("<test data-eht-fragment=\"'root'\" />",
          createTestParserConfiguration());
      Assert.fail("Exception should have been thrown");
    } catch (CompileException e) {
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());

      final int offsetOrErrorInText = 25;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
    }
  }

  @Test
  public void testUnrecognizedAttribute() {
    try {
      createTestEngine().compile("<test data-eht-nothing='' />",
          createTestParserConfiguration());
      Assert.fail("Exception should have been thrown");
    } catch (CompileException e) {
      Assert.assertEquals(INITIAL_START_ROW, e.getLineNumber());

      final int offsetOrErrorInText = 6;
      final int offsetOfErrorWithInitialColumn = INITIAL_START_COLUMN + offsetOrErrorInText;
      Assert.assertEquals(offsetOfErrorWithInitialColumn, e.getColumn());
    }
  }

  @Test
  public void testWriterUsageWithText() {
    String template = "<a><b data-eht-text=\"appender.run()\">someContent</b></a>";
    TemplateCompiler engine = createTestEngine();
    CompiledTemplate compiledTemplate = engine.compile(template, new ParserConfiguration(
        HTMLTemplateCompiler.class.getClassLoader()));

    final StringWriter stringWriter = new StringWriter();
    Map<String, Object> vars = new HashMap<>();
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
