package com.infusion.relnotesgen.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infusion.relnotesgen.Configuration;

public class FileUtils {
    
    private final static Logger logger = LoggerFactory.getLogger(Configuration.LOGGER_NAME);

    public static String readStringFromFile(String filename) throws IOException {
        Reader fileReader = null;
        BufferedReader bufReader = null;
        try {
            fileReader = new FileReader(filename);
            bufReader = new BufferedReader(fileReader);
            StringBuilder builder = new StringBuilder();
            String line = bufReader.readLine();
            while (line != null) {
                builder.append(line).append("\n");
                line = bufReader.readLine();
            }
            return builder.toString();
        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
            return "";
        } finally {
            if(bufReader  != null){
                bufReader.close();
            }
            if(fileReader  != null){
                fileReader.close();
            }
        }
    }

    public static String generateSerializedObjectPath(final String reportDirectoryPath) throws IOException {
        File reportPath = getReportDirectory(reportDirectoryPath);
        String objectStorePath = reportPath.getParentFile() + "\\objects\\";
        File directory = new File(String.valueOf(objectStorePath));
        if(!directory.exists()){
            directory.mkdir();
        }        
        return directory.toString() + "\\";
    }

    private static File getReportDirectory(final String reportDirectoryPath) throws IOException {
        return StringUtils.isEmpty(reportDirectoryPath) ?
                Files.createTempDirectory("ReportDirectory").toFile() :
                new File(reportDirectoryPath);
    }

}
