package org.everit.osgi.ewt.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.everit.osgi.ewt.CompiledTemplate;

public class CompiledTemplateImpl implements CompiledTemplate {

    private final RootNode rootNode;

    public CompiledTemplateImpl(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public void render(Writer writer, Map<String, Object> vars) throws IOException {
        render(writer, vars, null);
    }

    @Override
    public void render(Writer writer, Map<String, Object> vars, String bookmark) throws IOException {
        if (bookmark == null) {
            StringBuilder sb = new StringBuilder();
            rootNode.render(sb, vars);
            writer.write(sb.toString());
        } else {
            // TODO Auto-generated method stub
        }
    }

}
