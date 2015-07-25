<#ftl ns_prefixes={"D":"http://demo.akana.com/borrower"}>
<#assign xmlmsg = message.contentAsXml>
{ "result" : "${xmlmsg.add.borrower.last}" }
