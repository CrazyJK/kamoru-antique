package kamoru.app.spring.video.service;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import kamoru.app.spring.video.dao.VideoDao;
import kamoru.app.spring.video.domain.Action;
import kamoru.app.spring.video.domain.Actress;
import kamoru.app.spring.video.domain.Studio;
import kamoru.app.spring.video.domain.Video;

@Service
public class VideoServiceImpl implements VideoService {
	protected static final Log logger = LogFactory.getLog(VideoServiceImpl.class);

	public final String FileEncoding = "UTF-8";

	@Autowired private VideoDao videoDao;
	
	@Value("#{videoProp['defaultCoverFilePath']}") private String defaultCoverFilePath;
	@Value("#{videoProp['editor']}") private String editor;
	@Value("#{videoProp['player']}") private String player;
	@Value("#{videoProp['mainBasePath']}") private String mainBasePath;
	
	@Override
	public List<Video> getVideoListByParams(Map<String, String> params) {
		if(params == null)
			params = new HashMap<String, String>();
		String studio 	= StringUtils.trim(params.get("studio")); 
		String opus 	= StringUtils.trim(params.get("opus"));
		String title 	= StringUtils.trim(params.get("title"));
		String actress 	= StringUtils.trim(params.get("actress"));
		boolean addCond 		= BooleanUtils.toBoolean(params.get("addCond"));
		boolean existVideo 		= BooleanUtils.toBoolean(params.get("existVideo")); 
		boolean existSubtitles 	= BooleanUtils.toBoolean(params.get("existSubtitles"));
		String listViewType 	= params.get("listViewType");
		if(listViewType == null) {
			listViewType = "box";
			params.put("listViewType", listViewType); 
		}
		String sortMethod 		= params.get("sortMethod");
		boolean sortReverse 	= BooleanUtils.toBoolean(params.get("sortReverse")); 
		
		return videoDao.getVideoList(studio, opus, title, actress, addCond, existVideo, existSubtitles, sortMethod, sortReverse);
	}

	@Override
	public List<Actress> getActressList() {
		return videoDao.getActressList();
	}

	@Override
	public List<Studio> getStudioList() {
		return videoDao.getStudioList();
	}

	@Override
	public Video getVideo(String opus) {
		return videoDao.getVideo(opus);
	}

	@Override
	public void saveVideoOverview(String opus, String overViewTxt) {
		Video video = getVideo(opus);
		if(!video.isExistOverviewFile()) video.setOverviewFile(new File(getVideoPathWithoutExtension(video) + ".txt"));
		try {
			FileUtils.writeStringToFile(video.getOverviewFile(), overViewTxt, FileEncoding);
		} catch (IOException e) {
			logger.error("save overview error", e);
			throw new RuntimeException(e);
		}
		logger.debug("saveOverViewTxt : " + opus + " [" + video.getOverviewFile() + "]");
	}

	private String getVideoPathWithoutExtension(Video video) {
		if(video.isExistVideoFileList()) {
			String videoPath = video.getVideoFileList().get(0).getAbsolutePath();
			return videoPath.substring(0, videoPath.lastIndexOf("."));
		} else if(video.isExistCoverFile()) {
			return video.getCoverFilePath().substring(0, video.getCoverFilePath().lastIndexOf("."));
		} else if(video.isExistOverviewFile()) {
			return video.getOverviewFilePath().substring(0, video.getOverviewFilePath().lastIndexOf("."));
		} else if(video.isExistSubtitlesFileList()) {
			String subtitlesPath = video.getSubtitlesFileList().get(0).getAbsolutePath();
			return subtitlesPath.substring(0, subtitlesPath.lastIndexOf("."));
		} else {
			throw new RuntimeException("No video file");
		}
	}

	@Override
	public void deleteVideo(String opus) {
		saveHistory(getVideo(opus), Action.DELETE);
		videoDao.deleteVideo(opus);
	}

	@Override
	public void playVideo(String opus) {
		saveHistory(getVideo(opus), Action.PLAY);
		executeCommand(Action.PLAY, getVideo(opus));
	}

	@Override
	public void editVideoSubtitles(String opus) {
		executeCommand(Action.SUBTITLES, getVideo(opus));
	}
	
	@Async
	private void executeCommand(Action action, Video video) {
		String command = null;
		String[] argumentsArray = null;
		switch(action) {
		case PLAY:
			command = player;
			argumentsArray = video.getVideoFileListPathArray();
			break;
		case SUBTITLES:
			command = editor;
			argumentsArray = video.getSubtitlesFileListPathArray();
			break;
		default:
			throw new RuntimeException("Unknown Action");
		}
		if(argumentsArray == null)
			throw new RuntimeException("No arguments");
		
		String[] cmdArray = ArrayUtils.addAll(new String[]{command}, argumentsArray);
		try {
			Runtime.getRuntime().exec(cmdArray);
		} catch (IOException e) {
			logger.error("Error : executes command", e);
			throw new RuntimeException(e);
		}

		
	}

	@Override
	public File getVideoCoverFile(String opus) {
		File coverFile = videoDao.getVideo(opus).getCoverFile();
		if(coverFile == null)
			coverFile = getDefaultCoverFile();
		return coverFile;
	}

	private File getDefaultCoverFile() {
		return new File(defaultCoverFilePath);
	}

	// action method
	private void saveHistory(Video video, Action action) {
		String msg = null; 
		String currDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	
		switch(action) {
		case PLAY :
			msg = "play video : " + video.getVideoFileListPath();
			break;
		case OVERVIEW :
			msg = "write overview : " + video.getOverviewFilePath();
			break;
		case COVER :
			msg = "view cover : " + video.getCoverFilePath();
			break;
		case SUBTITLES :
			msg = "edit subtitles : " + video.getSubtitlesFileListPath();
			break;
		case DELETE :
			msg = "delete all : " + video.toString();
			break;
		default:
			throw new IllegalStateException("Undefined Action : " + action.toString());
		}
		String historymsg = MessageFormat.format("{0}, {1}, {2},\"{3}\"{4}", 
				currDate, video.getOpus(), action, msg, System.getProperty("line.separator"));
		
		logger.debug("save history - " + historymsg);
		try {
			if(video.getHistoryFile() == null)
				video.setHistoryFile(new File(getVideoPathWithoutExtension(video) + ".log"));
			FileUtils.writeStringToFile(video.getHistoryFile(), historymsg, FileEncoding, true);
		} catch (IOException e) {
			logger.error(historymsg, e);
		}
		try {
			FileUtils.writeStringToFile(new File(mainBasePath, "history.log"), historymsg, FileEncoding, true);
		} catch (IOException e) {
			logger.error(historymsg, e);
		}
	}

	@Override
	public Actress getActress(String actressName) {
		return videoDao.getActress(actressName);
	}

	@Override
	public Studio getStudio(String studioName) {
		return videoDao.getStudio(studioName);
	}

	@Override
	public List<Video> findVideoList(String query) {
		if(query == null || query.trim().length() == 0)
			return new ArrayList<Video>();

		query = query.toLowerCase();
		List<Video> found = new ArrayList<Video>();
		for(Video video : videoDao.getVideoList()) {
			if(video.getOpus().toLowerCase().indexOf(query) > -1) {
				found.add(video);
			}
		}
		return found;
	}
}

