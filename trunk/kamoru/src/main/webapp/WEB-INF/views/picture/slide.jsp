<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html> 
<head>
<meta charset="UTF-8">
<link rel="shortcut icon" type="image/x-icon" href="<c:url value="/resources/video/video-favicon.ico" />">
<title>Local Image Viewer</title>
<link rel="stylesheet" href="<c:url value="/resources/video/video.css" />" />
<style type="text/css">
* {margin:0px; padding:0px;}
body {background-size:contain; background-repeat:no-repeat; background-position:center center;}
li {display:inline-block;}
#navDiv {position:absolute; right:0px; top:0px; margin:10px 5px 0px 0px; cursor:pointer;}
#imageThumbnailDiv {position:absolute; bottom:0px; height:160px; width:100%; margin:10px 5px 0px 0px; text-align:center; overflow:hidden;}
#imageDiv {text-align:center};
.otherThumbnails  {opacity:0.5; border: solid 1px green;}
.currentThumbnail {opacity:1.0; border: solid 2px cyan;}
.thumbDiv {width:150px; height:160px;}
</style>
<!--[if lt IE 9]>
<script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
<script src="<c:url value="/resources/image-popup.js" />"></script>
<script type="text/javascript">
$(document).ready(function(){
	$(window).bind("mousewheel", function(e){
		var delta = 0;
		var event=window.event || e;
		if (event.wheelDelta) {
			delta = event.wheelDelta/120;
			if (window.opera) delta = -delta;
		} else if (event.detail) 
			delta = -event.detail/3;
		if (delta) {
			if (delta > 0) {
				//alert("마우스 휠 위로~");
				fnPrevImageView();
		    }
		    else {
				//alert("마우스 휠 아래로~");
				fnNextImageView();
		    }
		}
		//alert("detail=" + evt.detail + " wheelDelta=" + evt.wheelDelta);
	});
	$(window).bind("keyup", function(e){
		var event=window.event || e;
		//alert(event.keyCode); // 37:right, 38:up,  39:left, 40:down, 32:space
		switch(event.keyCode) {
		case 37:
			fnPrevImageView(); break;
		case 32:
		case 39:
			fnNextImageView(); break;
		case 13:
		case 38:
			fnRandomImageView(); break;
		}
		
	});
	/* 
	$("#imageDiv").bind("click", function(e){
		var event=window.event || e;
		//alert(event.type + " - " + event.button + ", keyValue=" + event.keyCode);
		
		//event.preventDefault();
		//event.stopPropagation();
		if(event.button == 0) {
			fnRandomImageView();
		} 
	}); */
	$(window).bind("resize", resizeImage);
	fnRandomImageView();
	resizeImage();
});
function resizeImage() {
	var windowHeight = $(window).height();
	$("#imageDiv").height(windowHeight);
}
var imagepath = '<s:url value="/image/" />';
var selectedNumber;
var selectedImgUrl;
var imageCount = <c:out value="${imageCount}" />;
function fnViewImage(current) {
	selectedNumber = current;
	selectedImgUrl = imagepath + selectedNumber;
	
	$("body").css("background-image", "url('" + selectedImgUrl + "')");
	$("#leftNo").html(getPrevNumber());
	$("#currNo").html(selectedNumber);
	$("#rightNo").html(getNextNumber());
	fnDisplayThumbnail();
}
function fnFullyImageView(){
	var img = $("<img />");
	img.hide();
	img.attr("src", selectedImgUrl);
	img.bind('load', function(){
		mw_image_window(this);
	});
	return img;
}
function popupImage(url) {
}

function getPrevNumber() {
	return selectedNumber == 0 ? imageCount - 1 : selectedNumber - 1;
}
function getNextNumber() {
	return selectedNumber == imageCount -1 ? 0 : selectedNumber + 1;
}
function fnPrevImageView() {
	fnViewImage(getPrevNumber());
}
function fnNextImageView() {
	fnViewImage(getNextNumber());
}
function fnRandomImageView() {
	fnViewImage(Math.floor(Math.random() * imageCount));
}
function fnDisplayThumbnail() {
	var thumbnailRange = parseInt(parseInt($(window).width() / 200) / 2);
	$("#imageThumbnailUL").empty();
	for(var current = selectedNumber - thumbnailRange; current <= selectedNumber + thumbnailRange; current++) {
		var thumbNo = current;
		if(thumbNo < 0 ) {
			thumbNo = imageCount + thumbNo;
		}
		if (thumbNo >= imageCount) {
			thumbNo = thumbNo - imageCount;
		}
		var cssClass = "otherThumbnails";
		if(thumbNo == selectedNumber)
			cssClass = "currentThumbnail";
		var img = $("<img id='thumbnail' onclick='fnViewImage("+thumbNo+")' class='"+cssClass+"'/>");
		img.attr("src", imagepath + thumbNo + "/thumbnail");
		var li = $("<li>");
		var div = $("<div class='thumbDiv'>");
		div.append(img);
		li.append(div);
		$("#imageThumbnailUL").append(li);
	}
}
</script>
</head>
<body>
<span id="debug" style="display:none;"></span>
<div id="imageDiv">
	<div id="navDiv">
		<span class="bgSpan" onclick="fnPrevImageView()">&lt;<span id="leftNo"></span></span>
		<span id="fullImageBtn" onclick="fnFullyImageView();" class="bgSpan"><span id="currNo"></span></span>
		<span class="bgSpan" onclick="fnNextImageView()"><span id="rightNo"></span>&gt;</span>
	</div>
	<div id="imageThumbnailDiv"><ul id="imageThumbnailUL"></ul></div>
</div>
</body>
</html>
