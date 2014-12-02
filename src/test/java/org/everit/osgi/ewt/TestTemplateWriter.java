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

import java.io.IOException;
import java.io.Writer;

import org.everit.templating.web.TemplateWriter;

public class TestTemplateWriter implements TemplateWriter {

    private final Writer writer;

    public TestTemplateWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public TemplateWriter append(String text) {
        if (writer != null) {
            try {
                writer.write(text);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return this;
    }

}
