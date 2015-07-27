# Template Test Utilities

The tools here provides couple of simple utilities for quickly testing a template against content you provide without installing the Akana FreeMarker Activity for the Akana API Gateway.

The tools mimic the Akana FreeMarker Activity helper methods creating a top-level object in the data model called `message` and adding `contentAsXml` and `contentAsString` methods appropriately.

## Executable jar

The template-tester.jar can be obtained from the releases section.

### Usage

	usage: template-tester <FTL_FILE> <MODEL_FILE>
	 -content <contenttype>   content type of model
	 -help                    print this message
	 -url <url>               url path and parameters

Example usage

	java -jar template-tester.jar samples/sample2.1.ftl samples/sample2_data.xml

Note that the content type of the data file is inferred from the extension. This can be overridden with the `-content` flag.  The `-content` specifies the input type of the data model file.

	java -jar template-tester.jar -content json samples/sample4.ftl samples/sample4_data.json


Example run:

	java -jar template-tester.jar samples/sample4.ftl samples/sample4_data.json

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

Example with HTTP URL `-url` string (note, the HTTP URL must be in the [HTTP RFC7230 3.1.1 Request Line](https://tools.ietf.org/html/rfc7230#section-3.1.1) format):

	java -jar target/template-tester.jar -url "GET /test/fm/fm/aggregate/people/AC3?start=12 HTTP/1.1" samples/urlextract.ftl samples/sample4_data.json

	Processing ftl   : samples/urlextract.ftl
	  with data model: samples/sample4_data.json
	with content-type: json
	{ "id": "AC3" }

Note that the model data is still required, though the template in this example does not use it. Here's an example that does use the model (although in an HTTP `GET` there wouldn't be body data):

	java -jar target/template-tester.jar -url "GET /test/fm/fm/aggregate/people/AC3?start=12 HTTP/1.1" samples/urlextract2.ftl samples/sample4_data.json

	Processing ftl   : samples/urlextract2.ftl
	  with data model: samples/sample4_data.json
	with content-type: json
	{
	  "id": "AC3",
	  "pet": "Rover"
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

