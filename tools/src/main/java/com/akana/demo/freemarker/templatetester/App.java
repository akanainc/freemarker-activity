/*
 * Copyright 2015 Akana, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Template tester app, with simulation of Akana API Gateway Message object
 * methods.
 */
public class App 
{
    public static void main( String[] args )
    {
 
        final Options options = new Options();
      
        @SuppressWarnings("static-access")
		Option optionContentType = OptionBuilder.withArgName("content-type")
        		.hasArg()
        		.withDescription("content type of model")
        		.create("content");
        @SuppressWarnings("static-access")
        Option optionUrlPath = OptionBuilder.withArgName("httpRequestLine")
        		.hasArg()
        		.withDescription("url path and parameters in HTTP Request Line format")
        		.create("url");
        @SuppressWarnings("static-access")
        Option optionRootMessageName = OptionBuilder.withArgName("messageName")
        		.hasArg()
        		.withDescription("root data object name, defaults to 'message'")
        		.create("root");
        @SuppressWarnings("static-access")
        Option optionAdditionalMessages = OptionBuilder.withArgName("dataModelPaths")
        		.hasArgs(Option.UNLIMITED_VALUES)
        		.withDescription("additional message object data sources")
        		.create("messages");
        @SuppressWarnings("static-access")
        Option optionDebugMessages = OptionBuilder
        		.hasArg(false)
        		.withDescription("Shows debug information about template processing")
        		.create("debug");
        
        Option optionHelp = new Option("help", "print this message");
        
        options.addOption(optionHelp);
        options.addOption(optionContentType);
        options.addOption(optionUrlPath);
        options.addOption(optionRootMessageName);
        //options.addOption(optionAdditionalMessages);
        options.addOption(optionDebugMessages);
        
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
			} else if (ext.equals("txt")) {
				contentType = "txt";
			}
			// Override discovered content type
			if (cmd.hasOption("content")) {
				contentType = cmd.getOptionValue("content");
			}
			// Root data model name
			String rootMessageName = "message";
			if (cmd.hasOption("root")) {
				rootMessageName = cmd.getOptionValue("root");
			}
			// Additional data models
			String[] additionalModels = new String[0];
			if (cmd.hasOption("messages")) {
				additionalModels = cmd.getOptionValues("messages");
			}
			// Debug Info
			if (cmd.hasOption("debug")) {
				System.out.println(" Processing ftl   : " + ftlPath);
				System.out.println("   with data model: " + dataPath);
				System.out.println(" with content-type: " + contentType);
				System.out.println(" data model object: " + rootMessageName);
				if (cmd.hasOption("messages")) {
					System.out.println("additional models: " + additionalModels.length);
				}
			}
			
			Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
			cfg.setDirectoryForTemplateLoading(new File("."));
			cfg.setDefaultEncoding("UTF-8");
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			
			/* Create the primary data-model */
			Map<String,Object> message = new HashMap<String,Object>();
			if (contentType.contains("json") || contentType.contains("txt")) {
				message.put("contentAsString", 
						FileUtils.readFileToString(new File(dataPath), StandardCharsets.UTF_8));
			} else {
				message.put("contentAsXml", 
						freemarker.ext.dom.NodeModel.parse(new File(dataPath)));
			}

			if (cmd.hasOption("url")) {
				message.put("getProperty", new AkanaGetProperty(cmd.getOptionValue("url")));
			}
			
			Map<String, Object> root = new HashMap<String, Object>();
			root.put(rootMessageName, message);

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
		formatter.printHelp("template-tester [OPTIONS] <FTL_FILE> <MODEL_FILE>", options);
    }
}
