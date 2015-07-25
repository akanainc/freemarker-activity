# Template Test Utilities

The tools here provides couple of simple utilities for quickly testing a template against content you provide without installing the Akana FreeMarker Activity for the API Gateway.

The tools mimic the Akana FreeMarker Activity helper methods creating a top-level object in the data model called `message` and adding `contentAsXml` and `contentAsString` methods appropriately.

## Executable jar

The tools jar can be obtained from the releases section.

### Usage

	usage: template-tester <FTL_FILE> <MODEL_FILE>
	 -content <contenttype>   content type of model
	 -help                    print this message

Example

	java -jar template-tester-0.0.1.jar samples/sample2.1.ftl samples/sample2_data.xml

With the `-content` flag, you can specify the input type of the data model file.

	java -jar template-tester-0.0.1.jar -content json samples/sample4.ftl samples/sample4_data.json


Example run:

	java -jar template-tester-0.0.1.jar -content json samples/sample4.ftl samples/sample4_data.json

	Processing ftl   : samples/sample4.ftl
	  with data model: samples/sample4_data.json
	with content-type: json
	{
	  "name" : [
			"Dinosaur",
			"Rover",
			"Fido",
			"Brontosuarus"  ]
	}

### Build

To build an executable jar, use maven:

	mvn clean compile assembly:single

The output will show up in the `target` directory.

## Standalone classes

These java source files are useful when using in an IDE, like Eclipse.

You will need to download the [freemarker jar](http://freemarker.org/freemarkerdownload.html), and [Apache Commons IO](https://commons.apache.org/proper/commons-io/download_io.cgi) and make sure these jars are in your project build path.

The tools will read templates and files from a specified directory - you will need to change this.

* `TestXML.java` will read a template called `testxml.ftl` and a source file called `test.xml` and will output the results.

*  `TestJSON.java` will read a template called `testjson.ftl` and a source file called `test.json` and will output the results.

