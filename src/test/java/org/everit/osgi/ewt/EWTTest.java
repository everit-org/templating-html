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

import org.htmlparser.util.ParserException;
import org.junit.Test;

public class EWTTest {

    @Test
    public void test1() {
        TemplateEngine engine = new TemplateEngine(new MvelExpressionCompiler());

        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("META-INF/test1.html");

        try {
            engine.compileTemplate(stream, "UTF8");
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
