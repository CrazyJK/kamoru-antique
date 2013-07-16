package com.kamoru.app.video.source;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
 
import com.kamoru.app.video.VideoException;
import com.kamoru.app.video.domain.Actress;
import com.kamoru.app.video.domain.Studio;
import com.kamoru.app.video.domain.Video;
import com.kamoru.app.video.util.VideoUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;


public class FileBaseVideoSource implements VideoSource {
	protected static final Log logger = LogFactory.getLog(FileBaseVideoSource.class);

	private final String UNKNOWN = "_Unknown";
	private final String unclassifiedStudio = UNKNOWN;
	private final String unclassifiedOpus = UNKNOWN;
	private final String unclassifiedActress = UNKNOWN;

	// property
	private String[] paths;
	private String video_extensions;
	private String cover_extensions;
	private String subtitles_extensions;
	private boolean webp_mode;
	private String webp_exec;

	// data source
	private Map<String, Video> videoMap;
	private Map<String, Studio> studioMap;
	private Map<String, Actress> actressMap;
	
	@Inject Provider<Video> videoProvider;
	@Inject Provider<Studio> studioProvider;
	@Inject Provider<Actress> actressProvider;

	// setter
	public void setPaths(String[] paths) {
		Assert.notNull(paths, "base paths must not be null");
		this.paths = paths;
	}
	public void setVideo_extensions(String video_extensions) {
		Assert.notNull(video_extensions, "video ext must not be null");
		this.video_extensions = video_extensions;
	}
	public void setCover_extensions(String cover_extensions) {
		Assert.notNull(cover_extensions, "cover ext must not be null");
		this.cover_extensions = cover_extensions;
	}
	public void setSubtitles_extensions(String subtitles_extensions) {
		Assert.notNull(subtitles_extensions, "subtitles ext must not be null");
		this.subtitles_extensions = subtitles_extensions;
	}
	public void setWebp_mode(boolean webp_mode) {
		this.webp_mode = webp_mode;
	}
	public void setWebp_exec(String webp_exec) {
		this.webp_exec = webp_exec;
	}
	
	/**
	 * 기존에 만든적이 없으면, video source를 로드를 호출한다.
	 */
	public final void createVideoSource() {
		if(videoMap == null || studioMap == null || actressMap == null)
			load();
	}
	/**
	 * video데이터를 로드한다.
	 */
	private void load() {
		logger.info(new String());
		
		Collection<File> files = new ArrayList<File>();
		for(String path : paths) {
			File directory = new File(path);
			if(directory.isDirectory()) {
				logger.debug("directory scanning : " + directory);
				Collection<File> found = FileUtils.listFiles(directory, null, true);
				files.addAll(found);
			}
		}
		logger.debug("total found file size : " + files.size());

		videoMap = new HashMap<String, Video>();
		studioMap = new HashMap<String, Studio>();
		actressMap = new HashMap<String, Actress>();
		
		int unclassifiedNo = 1;
		for(File file : files) {
			String filename = file.getName();
			String name = VideoUtils.getFileName(file);
			String ext  = VideoUtils.getFileExtension(file).toLowerCase();
			
			//연속 스페이스 제거
			name = name.replaceAll("\\s{2,}", " ");
			
			if("history.log".equals(filename))
				continue;
			
			String[] names = StringUtils.split(name, "]");
			String studioName  = UNKNOWN;
			String opus    = UNKNOWN;
			String title   = filename;
			String actressName = UNKNOWN;
			String etcInfo = "";
			
			switch(names.length) {
			case 5:
				etcInfo 	= VideoUtils.removeUnnecessaryCharacter(names[4]);
			case 4:
				actressName = VideoUtils.removeUnnecessaryCharacter(names[3]);
			case 3:
				title 		= VideoUtils.removeUnnecessaryCharacter(names[2]);
			case 2:
				opus 		= VideoUtils.removeUnnecessaryCharacter(names[1]);
				studioName 	= VideoUtils.removeUnnecessaryCharacter(names[0]);
				break;
			case 1:
				studioName 	= unclassifiedStudio;
				opus 		= unclassifiedOpus + unclassifiedNo++;
				title 		= filename;
				actressName = unclassifiedActress;
				break;
			default: // if names length is over 6
				studioName 	= VideoUtils.removeUnnecessaryCharacter(names[0]);
				opus 		= VideoUtils.removeUnnecessaryCharacter(names[1]);
				title 		= VideoUtils.removeUnnecessaryCharacter(names[2]);
				actressName = VideoUtils.removeUnnecessaryCharacter(names[3]);
				for(int i=4, iEnd=names.length; i<iEnd; i++)
					etcInfo = VideoUtils.removeUnnecessaryCharacter(etcInfo + " " + names[i]);
			}
			
			Video video = null;
			if((video = videoMap.get(opus.toLowerCase())) == null) {
				video = this.videoProvider.get();
				video.setOpus(opus.toUpperCase());
				video.setTitle(title);
				video.setEtcInfo(etcInfo);
				videoMap.put(opus.toLowerCase(), video);
			}
			
			// set File
			if(video_extensions.toLowerCase().indexOf(ext) > -1) {
				video.setVideoFile(file);
			}
			else if(cover_extensions.toLowerCase().indexOf(ext) > -1) {
				if(webp_mode) {
					video.setCoverWebpFile(convertWebpFile(file));
				}
				video.setCoverFile(file);
			}
			else if(subtitles_extensions.toLowerCase().indexOf(ext) > -1) {
				video.setSubtitlesFile(file);
			}
			else if ("info".equalsIgnoreCase(ext)) {
				video.setInfoFile(file);
			}
			else if("webp".equalsIgnoreCase(ext)) {
			}
			else {
				video.setEtcFile(file);
			}
			
			Studio studio = null;
			String lowerCaseStudioName = studioName.toLowerCase();
			if((studio = studioMap.get(lowerCaseStudioName)) == null) {
				studio = this.studioProvider.get();
				studio.setName(studioName);
				studioMap.put(lowerCaseStudioName, studio);
			}

			List<Actress> actressList = getActressList(actressName);

			// inject reference
			studio.putVideo(video);
			
			video.setStudio(studio);
			
			for(Actress actress : actressList) { 
				Actress actressInMap = null;
				String forwardActressName = VideoUtils.forwardNameSort(actress.getName());
				if((actressInMap = actressMap.get(forwardActressName)) == null) {
					actressMap.put(forwardActressName, actress);
					actressInMap = actress;
				}
				actressInMap.putVideo(video);
				actressInMap.putStudio(studio);
				studio.putActress(actressInMap);
				video.putActress(actressInMap);
			}
			
		}
		logger.debug("total found video size : " + videoMap.size());
	}

	/**
	 * 이미지를 webp파일로 반환. 없으면 만드는 작업 호출
	 * @param file
	 * @return
	 */
	private File convertWebpFile(File file) {
		logger.debug(file.getAbsoluteFile());
		File webpfile = new File(file.getParent(), VideoUtils.getFileName(file) + ".webp");
		if(webpfile.exists()) {
			return webpfile;
		}
		String command = webp_exec + " \"" + file.getAbsolutePath() + "\" -q 80 -o \"" + webpfile.getAbsolutePath() + "\"";
//		String[] command = {webp_exec, file.getAbsolutePath(), "-q 80 -o", webpfile.getAbsolutePath()};
		logger.info(command);
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			logger.error(e);
		}
		return webpfile;
	}
	
	/**
	 * 컴마(,)로 구분된 이름을 <code>List&lt;Actress&gt;</code>로 반환
	 * @param actressNames
	 * @return
	 */
	private List<Actress> getActressList(String actressNames) {
		logger.debug(actressNames);
		List<Actress> actressList = new ArrayList<Actress>();
		
		String[] namesArr = StringUtils.split(actressNames, ",");
		for(String name : namesArr) {
			name = StringUtils.join(StringUtils.split(name), " ");
			boolean bFindSameName = false;
			for(Actress actress : actressList) {
				if(VideoUtils.equalsName(actress.getName(), name)) {
					bFindSameName = true;
					break;
				}
			}
			if(!bFindSameName) {
				Actress actress = this.actressProvider.get();
				actress.setName(name);
				actressList.add(actress);
			}
		}
		return actressList;
	}
	
	@Override
	public synchronized void reload() {
		logger.info(new String());
		load();
	}
	@Override
	public Map<String, Video> getVideoMap() {
		logger.info(new String());
		createVideoSource();
		return videoMap;
	}
	@Override
	public Map<String, Studio> getStudioMap() {
		logger.info(new String());
		createVideoSource();
		return studioMap;
	}
	@Override
	public Map<String, Actress> getActressMap() {
		logger.info(new String());
		createVideoSource();
		return actressMap;
	}
	@Override
	public void removeVideo(String opus) {
		logger.info(opus);
		createVideoSource();
		videoMap.get(opus.toLowerCase()).removeVideo();
		videoMap.remove(opus.toLowerCase());
	}
	@Override
	public Video getVideo(String opus) {
		logger.info(opus);
		createVideoSource();
		if (videoMap.containsKey(opus.toLowerCase())) {
			return videoMap.get(opus.toLowerCase());
		}
		else {
			throw new VideoException("Video not found : " + opus);
		}
	}
	@Override
	public Studio getStudio(String name) {
		logger.info(name);
		createVideoSource();
		if (studioMap.containsKey(name.toLowerCase())) {
			return studioMap.get(name.toLowerCase());
		}
		else {
			throw new VideoException("Studio not found : " + name);
		}
	}
	@Override
	public Actress getActress(String name) {
		logger.info(name);
		createVideoSource();
		if (actressMap.containsKey(VideoUtils.forwardNameSort(name))) {
			return actressMap.get(VideoUtils.forwardNameSort(name));
		}
		else {
			throw new VideoException("Actress not found : " + name);
		}
	}
	@Override
	public List<Video> getVideoList() {
		logger.info(new String());
		createVideoSource();
		return new ArrayList<Video>(videoMap.values());
	}
	@Override
	public List<Studio> getStudioList() {
		logger.info(new String());
		createVideoSource();
		return new ArrayList<Studio>(studioMap.values());
	}
	@Override
	public List<Actress> getActressList() {
		logger.info(new String());
		createVideoSource();
		return new ArrayList<Actress>(actressMap.values());
	}

}
