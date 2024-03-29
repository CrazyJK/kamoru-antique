<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="shortcut icon" type="image/x-icon" href="<c:url value="/resources/video/video-favicon.ico" />">
<title>Overview[${video.opus}] - AV World</title>
<link rel="stylesheet" href="<c:url value="/resources/video/video.css" />" />
<style type="text/css">
.overviewTxt {width:100%; height:275px;}
</style>
<!--[if lt IE 9]>
<script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
<script type="text/javascript">
function overviewSave()
{
	var frm = document.forms['overviewFrm'];
	frm.submit();
/* 	if(opener) {
		opener.document.forms[0].submit();
	}
 */	
}
</script>
</head>
<body>
<form method="post" name="overviewFrm" action="<c:url value="/video/${video.opus}/overview" />">
<input type="hidden" name="opus" value="${video.opus}">
<textarea class="overviewTxt" name="overViewTxt">${video.overviewText}</textarea>
</form>
<div tabindex="1" style="position:absolute; right:0px; top:0px; margin:10px 5px 0px 0px; background-color:lightblue; cursor:pointer;" onclick="overviewSave()">SAVE</div>
</body>
</html>