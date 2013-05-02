package com.kamoru.app.video.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.kamoru.app.video.domain.Actress;
import com.kamoru.app.video.domain.Studio;
import com.kamoru.app.video.domain.Video;
import com.kamoru.app.video.domain.VideoSearch;

public interface VideoService {

	List<Video> getVideoList();

	List<Studio> getStudioList();

	List<Actress> getActressList();

	Video getVideo(String opus);

	Studio getStudio(String studioName);

	Actress getActress(String actressName);

	void deleteVideo(String opus);

	void playVideo(String opus);

	File getVideoCoverFile(String opus);

	byte[] getDefaultCoverFileByteArray();
	
	byte[] getVideoCoverByteArray(String opus);
	
	void saveVideoOverview(String opus, String overViewTxt);

	void editVideoSubtitles(String opus);

	List<Video> findVideoList(String query);

	List<Video> searchVideo(VideoSearch videoSearch);

}
