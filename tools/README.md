# Template Test Utilities

This provides couple of simple utilities for quickly testing a template against content you provide.

You will need to download the [freemarker jar](http://freemarker.org/freemarkerdownload.html), and [Apache Commons IO](https://commons.apache.org/proper/commons-io/download_io.cgi) and make sure these jars are in your project build path.

The tools will read templates and files from a specified directory - you will need to change this.

TestXML.java will read a template called testxml.ftl and a source file called test.xml and will output the results.

TestJSON.java will read a template called testjson.ftl and a source file called test.json and will output the results.

Both tools mimic the helper methods creating a top-level object in the data model called message and adding contentAsXml and contentAsString methods appropriately.

