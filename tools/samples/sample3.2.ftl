<#assign m = message.contentAsString?eval>
{
  "name" : [
	<#list m as pet>
		"${pet.name}"<#sep>,
	</#list>
  ]
}
