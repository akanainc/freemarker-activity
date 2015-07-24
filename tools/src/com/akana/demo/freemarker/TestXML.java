package com.akana.demo.freemarker;

import freemarker.template.*;
import java.util.*;
import java.io.*;

public class TestXML {

    public static void main(String[] args) throws Exception {
        
        /* ------------------------------------------------------------------------ */    
        /* You should do this ONLY ONCE in the whole application life-cycle:        */    
    
        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDirectoryForTemplateLoading(new File("/Users/ian.goldsmith/projects/freemarker"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        /* ------------------------------------------------------------------------ */    
        /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */    

        /* Create a data-model */
        Map message = new HashMap();
        message.put(
                "contentAsXml",
                freemarker.ext.dom.NodeModel.parse(new File("/Users/ian.goldsmith/projects/freemarker/test.xml")));

        Map root = new HashMap();
        root.put("message", message);
        
        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("testxml.ftl");

        /* Merge data-model with template */
        Writer out = new OutputStreamWriter(System.out);
        temp.process(root, out);
        // Note: Depending on what `out` is, you may need to call `out.close()`.
        // This is usually the case for file output, but not for servlet output.
    }
}
