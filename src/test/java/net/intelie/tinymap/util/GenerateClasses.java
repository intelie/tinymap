package net.intelie.tinymap.util;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;

public class GenerateClasses {
    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setTemplateLoader(new ClassTemplateLoader(GenerateClasses.class.getClassLoader(), "/templates"));

        runFor(configuration, "TinyMapGenerated.template", "TinyMapGenerated.java");

    }

    private static void runFor(Configuration configuration, String templateName, String fileName) throws IOException, TemplateException {
        File file = Paths.get(System.getProperty("user.dir"), "src/main/java/net/intelie/tinymap/" + fileName).toFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            Template template = configuration.getTemplate(templateName);
            template.process(Collections.emptyMap(), writer);
        }
    }
}
