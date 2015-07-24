<#ftl ns_prefixes={"D":"http://demo.akana.com/borrower"}>
<#assign xmlmsg = message.contentAsXml>
<br:borrower xmlns:br="http://demo.akana.com/borrower">
	<br:last>${xmlmsg.add.borrower.last}</br:last>
</br:borrower>
