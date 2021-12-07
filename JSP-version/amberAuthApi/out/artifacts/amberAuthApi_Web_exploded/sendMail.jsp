<%@ page import="java.io.InputStream" %>
<%@ page import="org.apache.commons.io.IOUtils"
         import="java.io.*"
         import="com.http.*"
         import="com.alibaba.fastjson.*"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page trimDirectiveWhitespaces="true" %>

<%
    String postdata =Common.getPostData(request.getInputStream(), request.getContentLength(), null);
    JSONObject getdata= JSON.parseObject(postdata);
    JSONObject outdata=new sqlControl().sendEmail(getdata);
%>
<%=outdata.toJSONString()%>
