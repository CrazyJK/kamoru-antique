$(document).ready(function(){
	$(window).bind("resize", resizeDivHeight);
	$("*[onclick]").css("cursor", "pointer");
	$("label").bind("click", function(){
		var id = $(this).attr("for");
		$("#" + id).val("");
	});
	$('span[id^="checkbox"]').bind("click", function(){
		var idArr = $(this).attr("id").split("-");
		if($("#" + idArr[1]).val() == "on" || $("#" + idArr[1]).val() == "true") {
			$("#" + idArr[1]).val("off");
			$(this).removeClass("checkbox-on");
		} else {
			$("#" + idArr[1]).val("on");
			$(this).addClass("checkbox-on");
		}
	}).each(function(){
		var idArr = $(this).attr("id").split("-");
		if($("#" + idArr[1]).val() == "on" || $("#" + idArr[1]).val() == "true") {
			$(this).addClass("checkbox-on");
		} else {
			$(this).removeClass("checkbox-on");
		}
	});
	$('span[id^="radio"]').bind("click", function(){
		var idArr = $(this).attr("id").split("-");
		$("#" + idArr[1]).val(idArr[2]);
		$('span[id^="radio-' + idArr[1] + '"]').removeClass("radio-on");
		$(this).addClass("radio-on");
	}).each(function(){
		var idArr = $(this).attr("id").split("-");
		if($("#" + idArr[1]).val() == idArr[2]) {
			$(this).addClass("radio-on");
		} else {
			$(this).removeClass("radio-on");
		}
	});
	$("li").toggle(
		function() {
			$(this).animate({
				opacity: 0.75
				}, 1000, function(){
					$(this).css("background-color", "green");
					$("#DEL-"+$(this).attr("id")).css("display", "");
				});
		}, function(){
			$(this).animate({
				opacity: 1
				}, 500, function(){
					$(this).css("background-color", "");
					$("#DEL-"+$(this).attr("id")).css("display", "none");
				});
		});
 	$('span[id^="checkbox-exist"]').bind("click", function(){
 		if($("#addCond").val() == "off") {
 			$("#checkbox-addCond").click();
 			$("#debug").html("addCond click");
 		}
	});

 	if($("#viewStudioDiv").val() != "on") {
 		$("#studioDiv").css("display", "none");
 	}
 	if($("#viewActressDiv").val() != "on") {
 		$("#actressDiv").css("display", "none");
 	}
 	
	resizeDivHeight();
});
function resizeDivHeight() {
	var windowHeight = $(window).height();
	//var documentHeight = $(document).outerHeight();
	var searchDivHeight = $("#headerDiv").outerHeight();
	var resizeContentDivHeight = windowHeight - searchDivHeight - 16 - 20 - 20; 
	//alert(resizeContentDivHeight);
	$("#contentDiv").height(resizeContentDivHeight);
	resizeBackgroundImage();
}
function resizeBackgroundImage() {
	var url = $('#contentDiv').css('background-image').replace(/url\(|\)$/ig, "");
	var img = $("<img />");
	img.hide();
	img.bind('load', function(){
		var imgWidth  = $(this).width();
		var imgHeight = $(this).height();
		var divWidth  = $("#contentDiv").width();
		var divHeight = $("#contentDiv").height();
		var width  = 0;
		var height = 0;
		
		if(imgWidth < divWidth && imgHeight < divHeight) { // 1. x:- y:-
			width  = imgWidth;
			height = imgHeight;
		}else if(imgWidth < divWidth && imgHeight > divHeight) { // 2. x:- y:+
			width  = ratioSize(divHeight, imgWidth, imgHeight);
			height = divHeight;
		}else if(imgWidth > divWidth && imgHeight < divHeight) { // 3. x:+ y:-
			width  = divWidth;
			height = ratioSize(divWidth, imgHeight, imgWidth);
		}else if(imgWidth > divWidth && imgHeight > divHeight) { // 4. x:+ y:+
			width  = ratioSize(divHeight, imgWidth, imgHeight);
			height = divHeight;
		}
		//$("#debug").html("background-image resize :{"+imgWidth+","+imgHeight+"}->{"+width+","+height+"}");
		$("#contentDiv").css("background-size", width + "px " + height + "px");
	});
	$("body").append(img);
	img.attr("src", url);
}
function ratioSize(numerator1, numerator2, denominator) {
	return parseInt(numerator1 * numerator2 / denominator);
}
function fnStudioDivToggle() {
	$("#studioDiv").toggle();
	$("#debug").html("fnStudioDivToggle");
	resizeDivHeight();
}
function fnActressDivToggle() {
	$("#actressDiv").toggle();
	$("#debug").html("fnActressDivToggle");
	resizeDivHeight();
}
function fnStudioSearch(studio) {
	$("input:text").each(function(){
		$(this).val("");
	});
	$("#studio").val(studio);
	fnDetailSearch();
}
function fnActressSearch(actress) {
	$("input:text").each(function(){
		$(this).val("");
	});
	$("#actress").val(actress);
	fnDetailSearch();
}
function fnDetailSearch() {
	var frm = document.forms[0];
	frm.submit();
}
function fnDeleteOpus(selectedOpus) {
	if(confirm("Really? Are you sure to delete this opus?")) {
		if(confirm("Are you kidding? D.E.L.E.T.E [" + selectedOpus + "]?")) {
			$("#debug").html("delete " + selectedOpus);
			$("#hiddenHttpMethod").val("delete");
			var frm = document.forms["actionFrm"];
			frm.action = context + "video/" + selectedOpus;
			frm.submit();
		}
	}
}
function fnEditSubtitles(selectedOpus) {
	$("#debug").html("edit subtitles " + selectedOpus);
	var frm = document.forms["actionFrm"];
	frm.action = context + "video/" + selectedOpus + "/subtitles";
	frm.submit();
}
function fnPlay(selectedOpus) {
	$("#debug").html("Video play " + selectedOpus);
	var frm = document.forms["actionFrm"];
	frm.action = context + "video/" + selectedOpus + "/play";
	frm.submit();
}
function fnRandomPlay() {
	$("#debug").html("Random play start");
	var frm = document.forms["actionFrm"];
	frm.action = context + "video/randomplay";
	frm.submit();
}
function fnOpusFocus(opus) {
	//alert("fnOpusFocus " + opus);
	$("#" + opus).animate({
		opacity: 0.75,
	}, 1000, function(){
		$(this).css("background-color", "green");
	});
}
function fnBGImageView() {
	var vUrl    = context + "video/bgimage?curr=y";
    var vName   = "imageview-"+opus;
    var vWidth  = 800;
    var vHeight = 539;
    var vLeft   = (window.screen.width  - vWidth)/2;
    var vTop    = (window.screen.height - vHeight)/2;
    var vSpecs  = "width="+vWidth+", height="+vHeight+", top="+vTop+", left="+vLeft
    			 + "toolbar=0,location=0,directories=0,titlebar=0"+
          		   "status=0,menubar=0,scrollbars=0,resizable=1";
    window.open(vUrl, vName, vSpecs);	
}
function fnImageView(opus) {
	$("#debug").html("Cover image view : " + opus);
	var vUrl    = context + "video/" + opus + "/cover";
    var vName   = "imageview-"+opus;
    var vWidth  = 800;
    var vHeight = 539;
    var vLeft   = (window.screen.width  - vWidth)/2;
    var vTop    = (window.screen.height - vHeight)/2;
    var vSpecs  = "width="+vWidth+", height="+vHeight+", top="+vTop+", left="+vLeft
    			 + "toolbar=0,location=0,directories=0,titlebar=0"+
          		   "status=0,menubar=0,scrollbars=0,resizable=1";
    window.open(vUrl, vName, vSpecs);	
}
function fnEditOverview(opus) {
	$("#debug").html("Overview Popup : " + opus);
	var vUrl    = context + "video/" + opus + "/overview";
    var vName   = "overview-"+opus;
    var vWidth  = 400;
    var vHeight = 300;
    var vLeft   = window.event.x;
    var vTop    = window.event.y;
    var vSpecs  = "width="+vWidth+", height="+vHeight+", top="+vTop+", left="+vLeft
    			 + "toolbar=0,location=0,directories=0,titlebar=0"+
          		   "status=0,menubar=0,scrollbars=0,resizable=1";
    window.open(vUrl, vName, vSpecs);	
}
function fnVideoDetail(opus) {
	var vUrl    = context + "video/" + opus;
    var vName   = "overview-"+opus;
    var vWidth  = 850;
    var vHeight = 800;
    var vLeft   = (window.screen.width  - vWidth)/2;
    var vTop    = (window.screen.height - vHeight)/2;
    var vSpecs  = "width="+vWidth+", height="+vHeight+", top="+vTop+", left="+vLeft
    			 + "toolbar=0,location=0,directories=0,titlebar=0"+
          		   "status=0,menubar=0,scrollbars=0,resizable=1";
    window.open(vUrl, vName, vSpecs);	
}