Here's a neat little piece of Freemarker code that comes in really useful for turning REST requests with multiple params into a valud SOAP request.  I didn't know that Freemarker was as good with XML as it is (not quite a good qith namespace management as XSL, but still, pretty darn good).  Here are a couple of links to the freemarker docs:  

* Imperative XML Processing - http://freemarker.org/docs/xgui_imperative_formal.html
* XML Handling & Namespaces - http://freemarker.org/docs/xgui_imperative_learn.html#autoid_73

The code below is quite simple.  It works on a fairly simple input XML doc, the complexity is that the REST request also includes a path param (called id).  The input doc was defined via CM as a model object for the API and as such is in the jsonSchema namespace. 

```
<resource xmlns="http://soa.com/jsonSchema">
    <city>My Town</city>
    <first>Me</first>
    <last>Too</last>
    <line1>Address</line1>
    <phone>555-5555</phone>
    <state>CA</state>
    <zip>90000</zip>
    <ID>555555555</ID>
    <SSN>555555555</SSN>
</resource>
```

Basically the Freemarker template does:

* Sets the default namespace to jsonSchema so that I can use dot notation to access the elements
* Assigns a variable using the contentAsXml helper function
* Creates a shell of the XML document
** NOTE: it uses un-prefixed XML so that everything in the generated message with no prefix will be in the defined namespace (http://soap.borrower.demo.soa.com)
* Accesses the id path param using getPart
* Accesses the child elements of the <resource> node using the dot notation and turns them all into text with @@markup

```
<#ftl ns_prefixes={"D":"http://soa.com/jsonSchema"}>
<#assign xmlmsg = message.contentAsXml>
<update xmlns="http://soap.borrower.demo.soa.com">
 <cin>${message.getPart("id")}</cin>
 <borrower>
 	${xmlmsg.resource.*.@@markup}
 </borrower>
</update>
```

This is a very nice, and VERY fast way to create a SOAP message from a multi-part REST input.  

NOTE: in order for any of this to work you must normalize the message before you run the freemarker template.  Just use the normalize activity.
