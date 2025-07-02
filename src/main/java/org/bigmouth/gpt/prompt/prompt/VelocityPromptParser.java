package org.bigmouth.gpt.prompt.prompt;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.util.Map;

/**
 * @author Allen Hu
 * @date 2025/3/13
 */
public class VelocityPromptParser implements PromptParser {

    private static final VelocityEngine VELOCITY_ENGINE;
    private final VelocityContext context;
    private final String logTag = "DEMO_TEMPLATE";

    static {
        VELOCITY_ENGINE = new VelocityEngine();
        VELOCITY_ENGINE.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        VELOCITY_ENGINE.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        VELOCITY_ENGINE.init();
    }

    public static VelocityEngine getEngine() {
        return VELOCITY_ENGINE;
    }

    public VelocityPromptParser(Map<String, Object> variables) {
        context = new VelocityContext(variables);
    }

    @Override
    public String process(String prompt) throws IllegalArgumentException {
        StringWriter writer = new StringWriter();
        VelocityEngine engine = getEngine();
        engine.evaluate(context, writer, logTag, prompt);
        return writer.toString();
    }
}
