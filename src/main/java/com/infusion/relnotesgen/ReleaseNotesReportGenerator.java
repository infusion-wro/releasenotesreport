package com.infusion.relnotesgen;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class ReleaseNotesReportGenerator {
    private final static Logger logger = LoggerFactory.getLogger(Configuration.LOGGER_NAME);

    private final String DEFAULT_TEMPLATE = "report.ftl";

    private final Template template;
    private boolean generateInternalViewLevelReport;

    public ReleaseNotesReportGenerator(com.infusion.relnotesgen.Configuration configuration, boolean generateInternalViewLevelReport) {
        this.generateInternalViewLevelReport = generateInternalViewLevelReport;
        this.template = getFreemarkerTemplate(configuration);
    }

    public String generate(ReleaseNotesModel reportModel) {
        StringWriter writer = new StringWriter();
        generate(reportModel, writer);
        return writer.toString();
    }

    public void generate(ReleaseNotesModel reportModel, Writer writer) {
        try {
            template.process(reportModel, writer);
        } catch (TemplateException|IOException e) {
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    private Template getFreemarkerTemplate(Configuration configuration) {
        freemarker.template.Configuration freemarkerConf = new freemarker.template.Configuration();

        String templateName = generateTemplateNameAndInitialize(configuration, freemarkerConf);

        freemarkerConf.setURLEscapingCharset("UTF-8");
        freemarkerConf.setIncompatibleImprovements(new Version(2, 3, 20));
        freemarkerConf.setDefaultEncoding("UTF-8");
        freemarkerConf.setLocale(Locale.getDefault());
        freemarkerConf.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        BeansWrapper beansWrapper = (BeansWrapper) ObjectWrapper.BEANS_WRAPPER;
        freemarkerConf.setObjectWrapper(beansWrapper);

        try {
            return freemarkerConf.getTemplate(templateName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load report template", e);
        }
    }

    private String generateTemplateNameAndInitialize(Configuration configuration, freemarker.template.Configuration freemarkerConf) {
        String templateFilename = (generateInternalViewLevelReport) ? configuration.getReportInternalTemplate() : configuration.getReportExternalTemplate();
        if (templateFilename==null || templateFilename.isEmpty()) {
            templateFilename = DEFAULT_TEMPLATE;
        }

        if(isNotEmpty(templateFilename)) {
            logger.info("Using template {}", templateFilename);
            File template = new File(templateFilename);
            templateFilename = template.getName();
            try {
                freemarkerConf.setDirectoryForTemplateLoading(template.getParentFile());
            } catch (IOException e) {
                throw new RuntimeException("Failed to set template directory", e);
            }
        } else {
            logger.info("Using default template.");
            freemarkerConf.setClassForTemplateLoading(ReleaseNotesReportGenerator.class, "/");
        }

        return templateFilename;
    }
}
