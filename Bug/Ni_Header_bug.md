# nginx header 下划线BUG
nginx默认request的header内容参数下划线时会自动忽略掉，需要添加以下配置到http中  
`underscores_in_headers on; `  
在 HEADER字段名中使用下划线其实是合法的、符合 HTTP 标准的。    
服务器之所以要默认禁止使用是因为 CGI 历史遗留问题。   
下划线和中划线都为会被映射为 CGI 系统变量名中的下划线，这样容易引起混淆。   
