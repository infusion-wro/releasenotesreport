package com.infusion.relnotesgen.util;

import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infusion.relnotesgen.Configuration;
import com.thoughtworks.xstream.XStream;

public class XstreamSerializer<T> {

    private final static Logger logger = LoggerFactory.getLogger(Configuration.LOGGER_NAME);

    public void serialize(String filename, T inputVal) throws IOException {
        PrintWriter out = null;
        try {
            XStream xstream = new XStream();
            String xml = xstream.toXML(inputVal);
            out = new PrintWriter(filename);
            out.print(xml);
        } catch (Exception e) {
            logger.error("{}", e.getMessage(), e);
        } finally {
            if(out  != null){
                out.close();
            } 
        }
    }

    @SuppressWarnings("unchecked")
    public T deserialize(String filename) throws IOException {
        XStream xstream = new XStream();
        T output = (T)xstream.fromXML(FileUtils.readStringFromFile(filename));
        return output;
    }
    

}
