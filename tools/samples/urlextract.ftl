<#assign 
  url = message.getProperty("http.request.line")?split(" ")
  path = url[1]?split("/")
  last = path[6]?split("?")
  id = last[0]
>
{ "id": "${id}" }