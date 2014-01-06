<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"     uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix='form'   uri='http://www.springframework.org/tags/form'%>
<!DOCTYPE html>
<html>
<head>
<title><s:message code="default.login"/></title>
<style type="text/css">
input[type=text],
input[type=password] {
	width:100%;
	height:30px;
	border:0 solid orange;
	border-radius: 5px;
	background-color: rgba(211, 211, 211, 0.5); /* lightgray; */
}
input[type=submit] {
	min-width:76px;
	height:32px;
	border:1px solid #3079ed;
	background-color:#4d90fe;
	color:#fff;
	text-shadow:0 1px rgba(0,0,0,.1);
	border-radius: 3px;
}
input[type=submit]:hover {
	background-color:#4787ed;
}
input[type=checkbox] {
	background-color:rgba(255,255,255,.5);
}
select, option {
	border-radius: 5px;
	background-color:rgba(255,255,255,.5);
	border:0;
}
div {
	margin:10px;
}
body {
	/* background-image:url('<s:url value="/resources/img/orgrimmar_horde_territory.jpg"/>'); */
	background-repeat: repeat;
	background-position: center center;
}
#sign-in {
	margin:20px 20px 20px;
	background-color: rgba(241, 241, 241, 0.5); 
	border:1px solid #e5e5e5;
	border-radius:10px;
	padding:20px 25px 15px; 
	width:300px;
	float:right;
}
#sign-in-box {
	background-image: url('<s:url value="/resources/favicon_kamoru.ico"/>');
	background-repeat: no-repeat;
	background-position: right top;
}
#lang-chooser {
	text-align:right;
}
#deco_section {
	background-color:rgba(255,255,255,0);
}
</style>
</head>
<body>
<div id="sign-in">
	<div id="sign-in-box">
		<h3><s:message code="default.login"/></h3>
		<form name='f' action='<c:url value="/j_spring_security_check"/>' method='POST'>
			<div>
				<label>
					<s:message code="default.identify"/>
		    		<input type='text' name='j_username' placeholder="<s:message code="default.identify"/>" autofocus/></label>
		    </div>
		    <div>
		    	<label>
		    		<s:message code="default.password"/>
		    		<input type='password' name='j_password' placeholder="<s:message code="default.password"/>"/></label>
		    </div>
		    <div>
		    	<input name="submit" type="submit" value="<s:message code="default.login"/>"/>
			    <label style="float:right;">
				    <input type="checkbox" name="_spring_security_remember_me">
				    <s:message code="default.rememberme"/>
			    </label>
		    </div>
		    <div style="text-align:center">
		    	<span style="color:red">${login_msg} ${access_msg}</span>
		    </div>
		</form>
	</div>
	<div id="lang-chooser">
		<form>
			<label>
				<s:message code="default.language"/> 
				<select name="lang" onchange="document.forms[1].submit();">
					<option value="ko" ${locale eq "ko" ? "selected" : "" }><s:message code="default.korean"/></option>
					<option value="en" ${locale eq "en" ? "selected" : "" }><s:message code="default.english"/></option>
					<option value="ja" ${locale eq "ja" ? "selected" : "" }><s:message code="default.japanese"/></option>
				</select>
			</label>
		</form>
	</div>
</div>
</body>
</html>