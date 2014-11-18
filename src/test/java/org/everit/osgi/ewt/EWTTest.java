package org.everit.osgi.ewt;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.htmlparser.util.ParserException;
import org.junit.Test;

public class EWTTest {

    @Test
    public void test1() {
        TemplateEngine engine = new TemplateEngine();

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
