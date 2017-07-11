package com.infusion.relnotesgen;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class ReleaseNotesReportGenerator {
    private static final String LINUX_PATH_INDICATOR = "/";
    private static final String RELATIVE_PATH_INDICATOR = "./";
    private static final String WINDOWS_PATH_INDICATOR = ":";
    private final static Logger logger = LoggerFactory.getLogger(Configuration.LOGGER_NAME);
    static final ClassLoader loader = ReleaseNotesReportGenerator.class.getClassLoader();

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
        String errorHint = null;
        
        if(isNotEmpty(templateFilename)) {

            logger.info("Using template {}", templateFilename);
            if (templateFilename.contains(WINDOWS_PATH_INDICATOR)) {
                errorHint = "A windows absolute path was detected [" + templateFilename + "]";

            } else if (templateFilename.startsWith(RELATIVE_PATH_INDICATOR)) {
                String root = System.getProperty("user.dir");
                errorHint = "A relative path from [" + root + "] was detected [" + templateFilename + "]";
                templateFilename = templateFilename.substring(2);

            } else if (templateFilename.startsWith(LINUX_PATH_INDICATOR)) {
                errorHint = "A linux/mac absolute path was detected [" + templateFilename + "]";

            } else {
                errorHint = "A resource path was detected [" + templateFilename + "]";
                URL resource = loader.getResource(templateFilename);
                try {
                    templateFilename = resource.getPath();
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Failed to load template file as resource", npe);
                }
            }

            File template = new File(templateFilename);
            templateFilename = template.getName();

            try {
                File templateParent = template.getParentFile();
                freemarkerConf.setDirectoryForTemplateLoading(templateParent);
            } catch (IOException e) {
                if (errorHint!=null) {
                    logger.warn(errorHint);
                }
                throw new RuntimeException("Failed to set template directory", e);
            }
        } else {
            logger.info("Using default template.");
            templateFilename = DEFAULT_TEMPLATE;
            freemarkerConf.setClassForTemplateLoading(ReleaseNotesReportGenerator.class, LINUX_PATH_INDICATOR);
        }

        return templateFilename;
    }
}
