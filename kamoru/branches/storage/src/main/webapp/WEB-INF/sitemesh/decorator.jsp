<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"        uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s"        uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ page import="org.apache.commons.lang3.time.DateFormatUtils" %>
<%@ page import="org.springframework.web.servlet.support.RequestContext" %>
<%
String dateString = DateFormatUtils.format(new java.util.Date(), "yyyy-MM-dd");
%>
<%
String lang = "ko";
try {
	lang = new RequestContext(request).getLocale().getLanguage();
} catch(Exception e) {}
%>

<!DOCTYPE html>
<html lang="<%=lang%>">
<head>
<link rel="shortcut icon" type="image/x-icon" href="<c:url value="/resources/favicon_kamoru.ico"/>">
<title><sitemesh:write property='title'>Title goes here</sitemesh:write> - kAmOrU</title>
<link rel="stylesheet" href="<c:url value="/resources/deco.css" />" />
<!--[if lt IE 9]><script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
<script src="<c:url value="/resources/common.js" />" type="text/javascript"></script>
<script type="text/javascript">
var context = '<c:url value="/"/>';

$(document).ready(function(){
	$(window).bind("resize", resizeSectionHeight);
	$("nav#deco_nav section").bind("mouseover", function(){
		$(this).children("ul").show();
	}).bind("mouseout", function(){
		$(this).children("ul").hide();
	});

	setBackgroundImage();
	setInterval(function() {setBackgroundImage();},	60*1000);
	
	$("nav#deco_nav section ul").hide();

	resizeSectionHeight();
	showNav();

});
function setBackgroundImage() {
	var bgImgUrl = "<c:url value="/image/random"/>?t=" + new Date().getTime();
	$("body").css("background-image", "url(" + bgImgUrl + ")");
}
function resizeSectionHeight() {
	var windowHeight = $(window).height();
	var headerHeight = $("#deco_header").outerHeight();
	var navHeight    = $("#deco_nav").outerHeight();
	var footerHeight = $("#deco_footer").outerHeight();
	var resizeSectionHeight = windowHeight - headerHeight - navHeight - footerHeight -20; 
	$("#deco_section").height(resizeSectionHeight);
	try {
		resizeSecondDiv();
	} catch(e) {}
}
function showNav() {
	var found = false;
	$("nav#deco_nav section ul li a").each(function() {
		if ($(this).attr("href") == window.location.pathname) {
			found = true;
			$(this).parent().addClass("menu-selected");
			$(this).parents("ul").prev().addClass("menu-selected");
		}
		else {
			$(this).parent().addClass("menu");
		}
	});
//	if(!found)
//		$("#deco_nav").css("display", "none");
}
</script>
<sitemesh:write property="head" />
</head>
<body id="deco_body">
	<header id="deco_header">
		<h1 id="deco_h1">
			<a href="<c:url value="/"/>">kAmOrU&hellip;</a> <sitemesh:write property='title'/>

			<a href="mailto:<s:message code="default.mail.addr"/>" title="<s:message code="default.mail.reply"/>" style="float:right;">
				<img alt="<s:message code="default.mail.addr"/>" src="<c:url value="/resources/tag_crazyjk_gmail.png"/>">
			</a>
			
			<span style="float:right; font-size:12px; line-height:35px;">
				<s:message code="default.hello"/>&nbsp;	${auth.name}&nbsp;
			</span>
		</h1>
	</header>
 
	<nav id="deco_nav">
		<div>
		<%@ include file="/WEB-INF/views/menu.jspf" %>
		</div>
	</nav>

	<section id="deco_section">
		<div id="deco_section_wapper">
		<sitemesh:write property="body">Body goes here. Blah blah blah.</sitemesh:write>
		</div>
	</section>
	
	<footer id="deco_footer">
		<div>
			&copy; <time datetime="<%=dateString %>" title="Today"><%=dateString %></time> 
			<a href="<c:url value="/jkServlet"/>">kAmOrU.</a> All rights reserved.

			<section style="float:right">
				<form name="langChange">
					<select name="lang" onchange="document.forms['langChange'].submit();">
						<option value="ko" <%="ko".equals(lang) ? "selected" : "" %>><s:message code="default.korean"/></option>
						<option value="en" <%="en".equals(lang) ? "selected" : "" %>><s:message code="default.english"/></option>
						<option value="ja" <%="ja".equals(lang) ? "selected" : "" %>><s:message code="default.japanese"/></option>
					</select>
				</form>
			</section>
		</div>
	</footer>

</body>
</html>