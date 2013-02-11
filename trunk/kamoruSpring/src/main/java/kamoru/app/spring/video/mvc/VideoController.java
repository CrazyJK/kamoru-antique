package kamoru.app.spring.video.mvc;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import kamoru.app.spring.video.domain.Video;
import kamoru.app.spring.video.service.VideoService;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class VideoController {

	private static final Logger logger = LoggerFactory.getLogger(VideoController.class);
	
	@Autowired
	private VideoService videoService;
	
	@RequestMapping(value="/video")
	public String av(Model model, @RequestParam Map<String, String> params ) {
		logger.info("start");
		List<Video> videoList = videoService.getVideoListByParams(params);
		Map<String, Integer> actressMap = videoService.getActressMap();
		Map<String, Integer> studioMap  = videoService.getStudioMap();
		model.addAttribute(videoList);
		model.addAttribute("actressMap", actressMap);
		model.addAttribute("studioMap", studioMap);
		model.addAttribute("params", params);
		return "video/video";
	}

	@RequestMapping(value="/video/{opus}", method=RequestMethod.GET)
	public String showAVOpus(Model model, @PathVariable String opus) {
		Video video = videoService.getVideo(opus);
		model.addAttribute(video);
		return "video/opus";
	}

/*	@RequestMapping(value="/video/{opus}/cover", method=RequestMethod.GET)
	public String showCover(Model model, @PathVariable String opus) {
		Video video = videoService.getVideo(opus);
		model.addAttribute(video);
		return "video/image";
	}
*/
/*	@RequestMapping(value="/video/{opus}/cover", method=RequestMethod.GET)
	public @ResponseBody byte[] image(@PathVariable String opus) throws IOException {
		Video video = videoService.getVideo(opus);
		File imageFile = video.getCoverImageFile();
		return FileUtils.readFileToByteArray(imageFile);
	}
*/
	@RequestMapping(value="/video/{opus}/cover", method=RequestMethod.GET)
	public void image(@PathVariable String opus, HttpServletResponse response) throws IOException {
		Video video = videoService.getVideo(opus);
		File imageFile = video.getCoverImageFile();
		Tika tika = new Tika();
	    String mimeType = tika.detect(imageFile);
		response.setContentType(mimeType);
		response.setDateHeader("Expires", new Date().getTime() + 86400*1000l);
		response.setHeader("Cache-Control", "max-age=" + 86400);
		response.getOutputStream().write(FileUtils.readFileToByteArray(imageFile));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	//	/video/{opus}/overview	- GET : 품평 보기, POST : 품평 수정
	@RequestMapping(value="/video/{opus}/overview", method=RequestMethod.GET)
	public String showOverview(Model model, @PathVariable("opus") String opus) {
		Video video = videoService.getVideo(opus);
		model.addAttribute("video", video);
		return "video/overview";
	}
	@RequestMapping(value="/video/{opus}/overview", method=RequestMethod.POST)
//	@ResponseStatus(HttpStatus.NO_CONTENT)
	public String doSaveOverview(@PathVariable("opus") String opus, @RequestParam("overViewTxt") String overViewTxt) {
		videoService.saveOverview(opus, overViewTxt);
		return "video/overviewSave";
	}
	@RequestMapping(value="/video/{opus}", method=RequestMethod.DELETE)
	public void doDeleteVideo(@PathVariable("opus") String opus) {
		videoService.deleteVideo(opus);
	}

	@RequestMapping(value="/video/{opus}/play", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void doPlayVideo(@PathVariable String opus) {
		videoService.playVideo(opus);
	}

	@RequestMapping(value="/video/{opus}/subtitles", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void doEditSubtitles(@PathVariable String opus) {
		videoService.editSubtitles(opus);
	}

	@RequestMapping(value="/video/randomplay", method=RequestMethod.GET)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void doPlayRandomVideo() {
		videoService.playRandomVideo();
	}
	
	@RequestMapping(value="/video/actress", method=RequestMethod.GET)
	public String showActress(Model model) {
		model.addAttribute(videoService.getActressMap());
		return "video/actress";
	}
	
	@RequestMapping(value="/video/actress/{actress}", method=RequestMethod.GET)
	public String showVideoListByActress(Model model, @PathVariable String actress) {
		model.addAttribute(videoService.getVideoListByActress(actress));
		return "video/list";
	}
	
	@RequestMapping(value="/video/studio", method=RequestMethod.GET)
	public String showStudio(Model model) {
		model.addAttribute(videoService.getStudioMap());
		return "video/studio";
	}
	
	@RequestMapping(value="/video/studio/{studio}", method=RequestMethod.GET)
	public String showVideoListByStudio(Model model, @PathVariable String studio) {
		model.addAttribute(videoService.getVideoListByStudio(studio));
		return "video/list";
	}
	
	@RequestMapping(value="/video/title/{title}", method=RequestMethod.GET)
	public String showVideoListByTitle(Model model, @PathVariable String title) {
		model.addAttribute(videoService.getVideoListByTitle(title));
		return "video/list";
	}

	@RequestMapping(value="/video/bgimage", method=RequestMethod.GET)
	public void showBGImage(@RequestParam(value="curr", required=false) String curr,
			HttpServletResponse response) throws IOException {
		File imageFile = videoService.getBGImageFile(curr);
		Tika tika = new Tika();
	    String mimeType = tika.detect(imageFile);
		response.setContentType(mimeType);
		response.getOutputStream().write(FileUtils.readFileToByteArray(imageFile));
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}
	
}
