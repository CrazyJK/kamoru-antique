<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*, kamoru.util.*"  %>
<%@ page errorPage="/common/error.jsp"%>
<%@ include file="class.jspf" %>
<%
String bbsid 	= RequestUtil.getParameter(request, "bbsid");
if(bbsid == null || bbsid.trim().length() == 0) return;


HashMap bbs = getBBS(bbsid);
if(bbs == null) return;

ArrayList tagList = getTagList();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> 글 수정 - bbs </title>
<link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/bbs/bbs.css">
<script type="text/javascript" src="<%= request.getContextPath() %>/js/kamoru.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/bbs/bbs.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/fckeditor/fckeditor.js"></script>
<script>
function windowOnload(){
	preTagselect('<%= bbs.get("TAGS") %>');
}
</script>
</head>
<body onload="windowOnload()">
<form action="action.jsp" method="post" target="action">
<input type="hidden" name="bbsid" value="<%= bbs.get("BBSID") %>" />
<input type="hidden" name="action" value="edit" />
<input type="hidden" id="selectTag" name="selectTag"/>
<textarea id="hiddenContent" style="display:none"><%= bbs.get("CONTENT") %></textarea>
<table width="100%">
	<tr>
		<td>
			<input type="text" name="title" class="inputtext" style="width:100%" value="<%= bbs.get("TITLE") %>"/>
		</td>
	</tr>
	<tr>
		<td>
			<span style="font: bold 12pt;color:Red;">Tags </span>
			<% for(int i=0; i<tagList.size() ; i++){ 
				HashMap tag = (HashMap)tagList.get(i); %>
			<span class="Pointer" 
				  onclick="tagSelect(this, 'MARK');" 
				  id="<%= tag.get("TAGID") %>" 
				  title="Count:<%= tag.get("STATUS") %>">
				  <%= tag.get("TAGNAME") %>
			</span>
			<% } %>
			<input type="text" name="newTag" class="inputtext"/>
		</td>
	</tr>
	<tr>
		<td>
		<script type="text/javascript">
		<!--
		// Automatically calculates the editor base path based on the _samples directory.
		// This is usefull only for these samples. A real application should use something like this:
		// oFCKeditor.BasePath = '/fckeditor/' ;	// '/fckeditor/' is the default value.
		//var sBasePath = document.location.href.substring(0,document.location.href.lastIndexOf('_samples')) ;
		
		var sBasePath = '/fckeditor/';
		var oFCKeditor = new FCKeditor( 'content' ) ;
		oFCKeditor.BasePath	= sBasePath ;
		oFCKeditor.Height	= 400 ;
		oFCKeditor.Value	= document.getElementById("hiddenContent").value ;
		oFCKeditor.Create() ;
		//-->
		</script>
		</td>
	</tr>
	<tr>
		<td>				
			<button onclick="writeAction()">저장</button>
		</td>
	</tr>
</table>
</form>
<iframe name="action" src="" width="100%" height="50"></iframe>
</body>
</html>