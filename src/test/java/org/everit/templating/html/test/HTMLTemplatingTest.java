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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.everit.expression.ParserConfiguration;
import org.everit.expression.mvel.MvelExpressionCompiler;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.HTMLTemplateCompiler;
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testFull() {
        TemplateCompiler engine = new HTMLTemplateCompiler(new MvelExpressionCompiler());

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
        int n = 300000;
        for (int i = 0; i < n; i++) {
            compiledTemplate.render(writer, vars);
        }
        long endTime = System.nanoTime();
        System.out.println("Time: " + ((endTime - startTime) / 1000000) + "ms, "
                + ((double) n * 1000000 / (endTime - startTime)) + " db/ms");

        try {
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
