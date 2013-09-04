package jk.kamoru.web;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import jk.kamoru.app.image.service.ImageService;
import jk.kamoru.app.video.VideoCore;
import jk.kamoru.app.video.VideoException;
import jk.kamoru.app.video.domain.Sort;
import jk.kamoru.app.video.domain.Video;
import jk.kamoru.app.video.domain.VideoSearch;
import jk.kamoru.app.video.domain.View;
import jk.kamoru.app.video.service.VideoService;
import jk.kamoru.app.video.util.VideoUtils;

/**
 * Video Collection controller<br>
 * data loading, searching, background-image setting
 * @author kamoru
 *
 */
@Controller
@RequestMapping("/video")
public class VideoController {

	protected static final Logger logger = LoggerFactory.getLogger(VideoController.class);
	
	@Autowired private ImageService imageService;
	@Autowired private VideoService videoService;

	@RequestMapping(value="/actress", method=RequestMethod.GET)
	public String actress(Model model) {
		logger.trace("actress");
		model.addAttribute(videoService.getActressList());
		return "video/actressList";
	}

	@RequestMapping(value="/actress/{actress}", method=RequestMethod.GET)
	public String actressName(Model model, @PathVariable String actress) {
		logger.trace(actress);
		model.addAttribute(videoService.getActress(actress));
		return "video/actressDetail";
	}

	@RequestMapping(value="/actress/{actress}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void putActressInfo(@PathVariable String actress, @RequestParam Map<String, String> params) {
		logger.trace("{}", params);
		videoService.saveActressInfo(actress, params);
	}
	
	@RequestMapping("/error")
	public void error() {
		throw new RuntimeException("error");
	}

	@RequestMapping("/videoError")
	public void errorVideo() {
		throw new VideoException("error");
	}

	@RequestMapping(value="/history", method=RequestMethod.GET)
	public String history(Model model, @RequestParam(value="q", required=false, defaultValue="") String query) {
		logger.trace("query={}", query);
		model.addAttribute("historyList", videoService.findHistory(query));
		return "video/history";
	}

	private HttpEntity<byte[]> httpEntity(byte[] imageBytes, String suffix) {
		long today = new Date().getTime();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("max-age=" + VideoCore.WebCacheTime_sec);
		headers.setContentLength(imageBytes.length);
		headers.setContentType(MediaType.parseMediaType("image/" + suffix));
		headers.setDate(		today + VideoCore.WebCacheTime_Mili);
		headers.setExpires(		today + VideoCore.WebCacheTime_Mili);
		headers.setLastModified(today - VideoCore.WebCacheTime_Mili);
		
		return new HttpEntity<byte[]>(imageBytes, headers);
	}

	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(Model model) {
		logger.trace("list");
		model.addAttribute("videoList", videoService.getVideoList());
		return "video/videoList";
	}

	@RequestMapping(value="/no/cover", method=RequestMethod.GET)
	public HttpEntity<byte[]> noCover() {
		logger.trace("noCover");
		return httpEntity(videoService.getDefaultCoverFileByteArray(), "jpg");
	}
	
	@RequestMapping(value="/{opus}", method=RequestMethod.GET)
	public String opus(Model model, @PathVariable String opus) {
		logger.trace(opus);
		model.addAttribute("video", videoService.getVideo(opus));
		return "video/videoDetail";
	}
	@RequestMapping(value="/{opus}/cover", method=RequestMethod.GET)
	public HttpEntity<byte[]> opusCover(@PathVariable String opus, HttpServletResponse response, @RequestHeader("User-Agent") String agent) throws IOException {
		logger.trace("{} - agent:{}", opus, agent);
		boolean isChrome = agent.indexOf("Chrome") > -1;
		File imageFile = videoService.getVideoCoverFile(opus, isChrome);
		if(imageFile == null) {
			response.sendRedirect("../no/cover");
			return null;
		}
		return httpEntity(videoService.getVideoCoverByteArray(opus, isChrome), VideoUtils.getFileExtension(imageFile));
	}
	@RequestMapping(value="/{opus}", method=RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusDelete(@PathVariable("opus") String opus) {
		logger.trace(opus);
		videoService.deleteVideo(opus);
	}

	@RequestMapping(value="/{opus}/overview", method=RequestMethod.GET)
	public String opusOverview(Model model, @PathVariable("opus") String opus) {
		logger.trace(opus);
		model.addAttribute("video", videoService.getVideo(opus));
		return "video/videoOverview";
	}
	
	@RequestMapping(value="/{opus}/overview", method=RequestMethod.POST) //	@ResponseStatus(HttpStatus.NO_CONTENT)
	public String opusOverviewPost(@PathVariable("opus") String opus, @RequestParam("overViewTxt") String overViewTxt) {
		logger.trace("{} - {}", opus, overViewTxt);
		videoService.saveVideoOverview(opus, overViewTxt);
		return "video/videoOverviewSave";
	}
	
	@RequestMapping(value="/{opus}/play", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusPlay(@PathVariable String opus) {
		logger.trace(opus);
		videoService.playVideo(opus);
	}
	
	@RequestMapping(value="/{opus}", method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusPost() {
		logger.warn("POST do not something yet");
	}
	
	@RequestMapping(value="/{opus}/rank/{rank}", method=RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusRank(@PathVariable String opus, @PathVariable int rank) {
		logger.trace("{} : {}", opus, rank);
		videoService.rankVideo(opus, rank);
	}
	
	@RequestMapping(value="/{opus}/subtitles", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void opusSubtitles(@PathVariable String opus) {
		logger.trace(opus);
		videoService.editVideoSubtitles(opus);
	}
	
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public String search(Model model, @RequestParam(value="q", required=false, defaultValue="") String query) {
		logger.trace("query={}", query);
		model.addAttribute("videoList", videoService.findVideoList(query));
        return "video/search";		
	}

	@RequestMapping(value="/studio", method=RequestMethod.GET)
	public String studio(Model model) {
		logger.trace("studio");
		model.addAttribute(videoService.getStudioList());
		return "video/studioList";
	}
	@RequestMapping(value="/studio/{studio}", method=RequestMethod.GET)
	public String studioName(Model model, @PathVariable String studio) {
		logger.trace(studio);
		model.addAttribute(videoService.getStudio(studio));
		return "video/studioDetail";
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public String video(Model model, @ModelAttribute VideoSearch videoSearch) {
		logger.trace("{}", videoSearch);
		List<Video> videoList =  videoService.searchVideo(videoSearch);

		model.addAttribute("views", View.values());
		model.addAttribute("sorts", Sort.values());
		model.addAttribute("videoList", videoList);
		model.addAttribute("opusArray", VideoUtils.getOpusArrayStyleStringWithVideofile(videoList));
		model.addAttribute("actressList", videoService.getActressListOfVideoes(videoList));
		model.addAttribute("studioList", videoService.getStudioListOfVideoes(videoList));
		model.addAttribute("bgImageCount", imageService.getImageSourceSize());
		return "video/videoMain";
	}
	
	@RequestMapping(value="/briefing", method=RequestMethod.GET)
	public String briefing(Model model) {
		logger.trace("briefing");
		model.addAttribute("pathMap", videoService.groupByPath());
		model.addAttribute("dateMap", videoService.groupByDate());
		model.addAttribute("rankMap", videoService.groupByRank());
		model.addAttribute("playMap", videoService.groupByPlay());
		model.addAttribute(videoService.getStudioList());
		model.addAttribute(videoService.getActressList());
		model.addAttribute(videoService.getVideoList());
		model.addAttribute("bgImageCount", imageService.getImageSourceSize());

		return "video/briefing";
	}
}