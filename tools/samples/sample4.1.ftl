<#assign m = doc.contentAsString?eval>
{
  "name" : [
	<#list m as pet>
		"<#if pet.name??>${pet.name}<#else>${pet.category.name}</#if>"<#sep>,
	</#list>
  ]
}
