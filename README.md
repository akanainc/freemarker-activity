# FreeMarker Process Activity
The FreeMarker process activity allows you to create message content using FreeMarker templates.  It provides a powerful content creation mechanism that can address any element in the process and message contexts, including headers, parameters, content, and more.  It includes helper methods for returning message context as an XML document structure or a string for easy addressing using the FreeMarker template syntax.

## Activity Overview
The activity is very easy to use, you simply provide a freemarker template, and specify the message variable the content of which you will replace with the results of processing the template, and the Content-Type of the resulting message content.

The activity populates the FreeMarker data model with all the variables named in the processContext, most notably the default message variable.  For details on the processContext and messageContext see the [script api documentation](http://docs.akana.com/ag/assets/scriptDocs/index.html).

There are two added helper methods in each message context:

* contentAsString - returns the message content as a string
* contentAsXml - returns the message content as an XML DOM object

You can access any of the properties in each context using a dot notation that skips the get prefix on the propery, e.g. a method in the script API of ```getTransportHeaders()``` can be accessed as a property using ```message.transportHeaders```.  This will access the transportHeaders property of the default message context.  Note: while many properties will directly return a scalar value, this example will return a Headers map and to use this in a template you will need to find a specific instance of a header using something like ```${message.transportHeaders.get("User-Agent").value}```.

A simple template for processing a JSON document might look like:

```
<#assign m = message.contentAsString?eval>
<name>${m.pet.name}</name>
```

With an input of this JSON document:

```
{ 
	"pet" : 
	{
		"name" : "fido",
		"type" : "dog" 
	}
}
```

This will return a result of:

```
<name>fido</name>
```
## FreeMarker Introduction
FreeMarker is very powerful and I am not going to attempt to cover the template language here.  For a comprehensive description of the template language please visit [FreeMarker.org](http://freemarker.org/docs/index.html). 

The important thing with FreeMarker is to understand the concept.  Essentially what FreeMarker does is to provide a means of creating a template that outputs text with the ability to insert content in the template based on extracting information from an input data model (in our case the data model is built from the source message and process context).  FreeMarker provides a language for specifying how to insert content from the data model, and it provides a set of control directives for looping through content and using conditionals etc.

At its core a FreeMarker template is just the text that is going to be output, for example this template:

```
<html>
	<body>
		<p>Hello World</p>
	</body>
</html>
```

Would simply output that piece of static html.

Of course, that would be pretty useless, so FreeMarker provides a language for inserting values from the data model into the template, e.g.

```
<html>
	<body>
		<p>Hello ${user}</p>
	</body>
</html>
```

Extracts the user value from the data model and uses it to generate a personalized piece of html - this is, of course, assuming that your data model contains a scalar value stored as user.

The data model is heirarchical, so if your user value is actually an object, then you would access the inner values using a dot notation, like ```${user.name.lastname}```.  This is a very important point, the value that you try and insert MUST be a scalar value, for example if your ```user``` value was a complex object, and you tried ```${user}``` the template rendering would fail with an error like: 
> "${...}" content: Expected a string or something automatically convertible to string (number, date or boolean), but this has evaluated to an extended_hash

The FreeMarker process activity **replaces** the content of the specified message variable with the result of running a template over the data model. 

This guide provides a couple of examples showing ways you can use this activity to deliver some common use-cases. 

## Extracting data from XML
FreeMarker provides an easy way to extract some elements from an XML document with a complex structure and use their values to generate JSON, or even HTML without having to become an XSLT expert.  FreeMarker doesn't eliminate the need for XSLT, but it does provide a simpler alternative for many common uses.

We'll work from a fairly simply document, this will be enough to show a lot of the common approaches and pitfalls:

```
<br:add xmlns:br="http://demo.akana.com/borrower">
   <br:borrower>
      <br:id>111-11-1111</br:id>
      <br:ssn>111-11-1111</br:ssn>
      <br:city>Los Angeles</br:city>
      <br:first>John</br:first>
      <br:last>Smith</br:last>
      <br:line1>12100 Wilshire Blvd</br:line1>
      <br:phone>310-000-0000</br:phone>
      <br:state>CA</br:state>
      <br:zip>90025</br:zip>
   </br:borrower>
</br:add>
```

The basic idea is that we are going to access the XML document structure using the ```contentAsXml``` helper method, and then we should be able to address the elements.  For any XML document using namespaces we must first define the namespaces using the freemarker ```ns_prefixes``` tag.  We can then address elements using array notation (not dot notation), e.g.:

```
<#ftl ns_prefixes={"br":"http://demo.akana.com/borrower"}>
{ "result" : "${message.contentAsXml["br:add"]["br:borrower"]["br:last"]}" }
```

Will output:

```
{ "result" : "Smith" }
```

I prefer using the special "D" prefix to define a default namespace so that I can use dot notation.  I also like to assign the result of the helper function to a variable so that I can use it more efficiently later on.  In this case:

```
<#ftl ns_prefixes={"D":"http://demo.akana.com/borrower"}>
<#assign xmlmsg = message.contentAsXml>
{ "result" : "${xmlmsg.add.borrower.last}" }
```

Will have the same effect as the first template, but is much more readable.

> NOTE: if you have multiple namespaces in your message, pick the one you will need to access the most for your default, and then remember that you will have to use array notation with the prefixes to address elements in other namespaces.

## Creating an XML Document
Remember that the FreeMarker template is essentially just the text you will output, so if you want to create an XML document, you are going to simply write your document.  Rewriting the example above to output XML instead of JSON we might end up with something like this:

```
<#ftl ns_prefixes={"D":"http://demo.akana.com/borrower"}>
<#assign xmlmsg = message.contentAsXml>
<br:borrower xmlns:br="http://demo.akana.com/borrower">
	<br:last>${xmlmsg.add.borrower.last}</br:last>
</br:borrower>
```

> NOTE: you have to manually structure your document and make you produce valid XML complete with namespace definitions and prefixes (of course if you want to, you can create invalid XML, the product won't complain unless you then ask it to mediate it.

## Extracting Data from JSON
JSON data is structured remarkably similarly to a FreeMarker data model.  FreeMarker provides a simple ```eval``` utility for evaluating a string containing a JSON document and mapping it to a data model.

The following examples will all work with this fairly complex JSON document:

```
[
    {
        "category": {
            "id": 100,
            "name": "Dinosaur"
        },
        "name": "Tyrannosaurus",
        "photoUrls": [
            "http://en.wikipedia.org/wiki/Tyrannosaurus#/media/File:Tyrannosaurus_rex_mmartyniuk.png"
        ],
        "tags": [
            {
                "id": 100,
                "name": "reptile"
            },
            {
                "id": 101,
                "name": "dinosaur"
            }
        ],
        "status": "available",
        "id": "100",
        "href": "http://localhost:9900/things/petstore/100"
    },
    {
        "category": {
            "id": 200,
            "name": "Dog"
        },
        "name": "Rover",
        "photoUrls": [
            "http://en.wikipedia.org/wiki/Tyrannosaurus#/media/File:Tyrannosaurus_rex_mmartyniuk.png"
        ],
        "tags": [
            {
                "id": 102,
                "name": "mammal"
            },
            {
                "id": 103,
                "name": "dog"
            }
        ],
        "status": "available",
        "id": "101",
        "href": "http://localhost:9900/things/petstore/101"
    },
    {
        "category": {
            "id": 200,
            "name": "Dog"
        },
        "name": "Fido",
        "photoUrls": [],
        "tags": [
            {
                "id": 102,
                "name": "mammal"
            },
            {
                "id": 103,
                "name": "dog"
            }
        ],
        "status": "available",
        "id": "102",
        "href": "http://localhost:9900/things/petstore/102"
    },
    {
        "id": "1433943941767",
        "category": {
            "id": 100,
            "name": "Dinosaur"
        },
        "name": "Brontosuarus",
        "photoUrls": [],
        "tags": [
            {
                "id": 100,
                "name": "reptile"
            },
            {
                "id": 101,
                "name": "dinosaur"
            }
        ],
        "status": "sold",
        "href": "http://localhost:9900/things/petstore/1433943941767"
    }
]
```

One of the most complex aspects of this document is that it has no top-level named objects, it's just an array of objects.  So if we want to extract information from the first item in the array we need to address it appropriately, e.g.:

```
<#assign m = message.contentAsString?eval>
{ "name" : "${m[0].name}" }
```

Will return:

```
{ "name" : "Tyrannosaurus" }
```

> NOTE: the <#assign> directive uses our helper function to return the string value of the message content and the FreeMarker eval function to map the JSON document to the data model

Of course it's really not that useful to be be forced to manually select which element in the array you want to work with, and this is where the ```<#list>``` directive comes in:

```
<#assign m = message.contentAsString?eval>
{
  "name" : [
	<#list m as pet>
		"${pet.name}"<#sep>,
	</#list>
  ]
}
```

This template iterates through the elements in the top-level array (assigned to m), and assigns each element to a new variable we are calling pet.  We can then work on each element in the pet array separately.  This template also introduces the ```<#sep>``` directive which is used when you have to display something between each item (but not before the first item or after the last item).

## Error Handling
When the template fails in the activity it will output any exceptions to a debug entry in the message log.  The most common failures once you have a template working will be when an element doesn't exist.  Take our JSON document example above.  If one of the pet objects didn't have a name, then our listing template would fail.  To get around this, we can use conditionals around elements that may or may not be present.  For example:

```
<#assign m = message.contentAsString?eval>
{
  "name" : [
	<#list m as pet>
		"<#if pet.name??>${pet.name}<#else>${pet.category.name}</#if>"<#sep>,
	</#list>
  ]
}
```

Uses the ``??`` operator in a conditional ```<#if pet.name??>....</#if>``` to detect if pet.name exists.  In this case I am including an ```<#else>``` directive to pick another value to use, ```${pet.category.name}```, in the event that my pet object doesn't have a name.

  		
