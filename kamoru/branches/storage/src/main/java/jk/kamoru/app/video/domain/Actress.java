package jk.kamoru.app.video.domain;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import jk.kamoru.app.video.VideoCore;
import jk.kamoru.app.video.util.VideoUtils;
import jk.kamoru.util.FileUtils;
import jk.kamoru.util.StringUtils;
import lombok.Data;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
@XmlRootElement(name = "actress", namespace = "http://www.w3.org/2001/XMLSchema-instance")
@XmlAccessorType(XmlAccessType.FIELD)
public class Actress implements Serializable, Comparable<Actress> {

	private static final long serialVersionUID = VideoCore.SERIAL_VERSION_UID;

	@Value("#{prop['video.basePath']}") 		private String[] basePath;

	private String name;
	private String localName;
	private String birth;
	private String bodySize;
	private String debut;
	private String height;
	
	@XmlTransient
	@JsonIgnore
	private List<Studio> studioList;
	@XmlTransient
	@JsonIgnore
	private List<Video>   videoList;

	private boolean loaded;

	public Actress() {
		studioList = new ArrayList<Studio>();
		videoList = new ArrayList<Video>();
	}
	public Actress(String name) {
		this();
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s %s %s %s %s [%s]",
						name, StringUtils.trimToEmpty(birth), StringUtils.trimToEmpty(bodySize), StringUtils.trimToEmpty(debut), StringUtils.trimToEmpty(height), StringUtils.trimToEmpty(localName), videoList.size());
	}
	@Override
	public int compareTo(Actress comp) {
		return StringUtils.compareTo(this.getName(), comp.getName());
	}
	
	public boolean contains(String actressName) {
		return VideoUtils.equalsName(name, actressName);
	}
	
	public String getBirth() {
		loadInfo();
		return birth;
	}
	public String getBodySize() {
		loadInfo();
		return bodySize;
	}
	public String getDebut() {
		loadInfo();
		return debut;
	}
	public String getHeight() {
		loadInfo();
		return height;
	}
	public String getLocalName() {
		loadInfo();
		return localName;
	}
	
	@JsonIgnore
	public List<URL> getWebImage() {
		return VideoUtils.getGoogleImage(this.getName());
	}
	
	private void loadInfo() {
		if (!loaded) {
			Map<String, String> info = VideoUtils.readFileToMap(new File(basePath[0], name + FileUtils.EXTENSION_SEPARATOR + VideoCore.EXT_ACTRESS));
			this.localName = info.get("LOCALNAME");
			this.birth     = info.get("BIRTH");
			this.height    = info.get("HEIGHT");
			this.bodySize  = info.get("BODYSIZE");
			this.debut     = info.get("DEBUT");
			loaded = true;
		}
	}
	public void reloadInfo() {
		loaded = false;
		loadInfo();
	}
	public void addStudio(Studio studio) {
		if(!this.studioList.contains(studio))
			this.studioList.add(studio);
	}
	public void addVideo(Video video) {
		if(!this.videoList.contains(video))
			this.videoList.add(video);
	}
	
	public void emptyVideo() {
		videoList.clear();
	}
	
}
