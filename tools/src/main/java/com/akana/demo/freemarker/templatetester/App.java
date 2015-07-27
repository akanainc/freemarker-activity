package com.akana.demo.freemarker.templatetester;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
 
        final Options options = new Options();
      
        @SuppressWarnings("static-access")
		Option optionContentType = OptionBuilder.withArgName("contenttype")
        		.hasArg()
        		.withDescription("content type of model")
        		.create("content");
        Option optionUrlPath = OptionBuilder.withArgName("url")
        		.hasArg()
        		.withDescription("url path and parameters")
        		.create("url");
        
        Option optionHelp = new Option("help", "print this message");
        
        options.addOption(optionHelp);
        options.addOption(optionContentType);
        options.addOption(optionUrlPath);
        
        CommandLineParser parser = new DefaultParser();
       
        CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
			
			// Check for help flag
			if (cmd.hasOption("help")) {
				showHelp(options);
				return;
			}
			
			String[] remainingArguments = cmd.getArgs();
			if (remainingArguments.length < 2) {
				showHelp(options);
				return;
			}
			String ftlPath, dataPath = "none";
			
			ftlPath = remainingArguments[0];
			dataPath = remainingArguments[1];
			
			String contentType = "text/xml";
			// Discover content type from file extension
			String ext = FilenameUtils.getExtension(dataPath);
			if (ext.equals("json")) {
				contentType = "json";
			}
			// Override discovered content type
			if (cmd.hasOption("content")) {
				contentType = cmd.getOptionValue("content");
			} 
			
			System.out.println("Processing ftl   : " + ftlPath);
			System.out.println("  with data model: " + dataPath);
			System.out.println("with content-type: " + contentType);
			
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
			cfg.setDirectoryForTemplateLoading(new File("."));
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			
			/* Create a data-model */
			Map<String,Object> message = new HashMap<String,Object>();
			if (contentType.contains("json")) {
				message.put("contentAsString", 
						FileUtils.readFileToString(new File(dataPath),
						StandardCharsets.UTF_8));
			} else {
				message.put("contentAsXml", 
						freemarker.ext.dom.NodeModel.parse(new File(dataPath)));
			}

			Map root = new HashMap();
			root.put("message", message);

			/* Get the template (uses cache internally) */
			Template temp = cfg.getTemplate(ftlPath);

			/* Merge data-model with template */
			Writer out = new OutputStreamWriter(System.out);
			temp.process(root, out);
			
		} catch (ParseException e) {
			showHelp(options);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("Unable to parse ftl.");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("XML parsing issue.");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			System.out.println("Unable to configure parser.");
			e.printStackTrace();
		} catch (TemplateException e) {
			System.out.println("Unable to parse template.");
			e.printStackTrace();
		}
        
    }
    
    public static void showHelp(Options options) {
    	HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("template-tester <FTL_FILE> <MODEL_FILE>", options);
    }
}
