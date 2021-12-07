<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.commons.io.IOUtils"
         import="java.io.*"
         import="com.http.*"
         import="com.alibaba.fastjson.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page trimDirectiveWhitespaces="true" %>

<%
    String postdata =Common.getPostData(request.getInputStream(), request.getContentLength(), null);
    JSONObject getdata= JSON.parseObject(postdata);
    int status=new sqlControl().register(getdata);
    String toprint="";
    if(status==0){
        toprint="{\"status\":\"success\"}";
    }
    else if(status==1){
        toprint="{\"status\":\"username used\"}";
    }else{
        toprint="{\"status\":\"system error\"}";
    }
%>
<%=toprint%>
