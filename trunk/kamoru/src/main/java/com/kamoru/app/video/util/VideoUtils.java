package com.kamoru.app.video.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.kamoru.app.video.VideoCore;
import com.kamoru.app.video.dao.VideoDaoFile;
import com.kamoru.app.video.domain.Actress;
import com.kamoru.app.video.domain.Video;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VideoUtils {

	protected static final Log logger = LogFactory.getLog(VideoUtils.class);

	/**
	 * <p>단독으로 파일명을 재조합한다.
	 * 상황과 조건에 따라 로직이 달라지므로 사용시 수정 필요</P>
	 * @param path
	 * @param unclassifiedPath
	 */
	public static void changeOldNameStyle(String path, String unclassifiedPath) {
		Collection<File> found = FileUtils.listFiles(new File(path), null, true);
		int classified = -1;
		for(File file : found) {
			String name		= file.getName();
			String filename = getFileName(file);
			String extname  = getFileExtension(file);
			
//			if(ctrl.listBGImageName.equals(name) || ctrl.historyName.equals(name))
//				continue;
			
			String[] filenamepart = StringUtils.split(filename, ']');
//			System.out.format("%s%n", ArrayUtils.toString(filenamepart));
			int partCount = filenamepart.length;
			String studio = "", opus = "", title = "NoTitle", actress = "UnKnown", date = "";
			switch(partCount) {
			case 5:
				date = removeUnnecessaryCharacter(filenamepart[4]);
			case 4:
				actress = removeUnnecessaryCharacter(filenamepart[3]);
			case 3:
				title = removeUnnecessaryCharacter(filenamepart[2]);
			case 2:
				opus = removeUnnecessaryCharacter(filenamepart[1]);
				String[] opuspart = StringUtils.splitByCharacterType(opus);
				if(opuspart != null && opuspart.length == 2) {
					opus = opuspart[0].toUpperCase() + "-" + opuspart[1];
				}
				studio = removeUnnecessaryCharacter(filenamepart[0]);
				classified = 0;
				break;
			case 1:
				classified = 1;
				break;
			default:
				classified = 2;
				break;
			}
			studio = "IDEAPOCKET".equals(studio) ? "IdeaPocket" : studio;
			if(title.startsWith("20")) {
				date = title.substring(0, 10);
				title = title.substring(10);
			}
			title = title.replaceAll(opus, "").trim();
			String uniqueName = "Unknown";
			if(title.startsWith(uniqueName)) {
				title = title.replaceAll(uniqueName, "").trim();
				actress = uniqueName;
			}
			if(classified == 0) {
				System.out.format("정리됨 : %s -> [%s][%s][%s][%s][%s].%s%n", name, studio, opus, title, actress, extname, date);
				String newName = MessageFormat.format("[{0}][{1}][{2}][{3}][{5}].{4}", studio, opus, title, actress, extname, date);
				try {
					if(!name.equals(newName)) {
						FileUtils.moveFile(file, new File(path, newName));
						System.out.format("    move : %s -> %s%n", name, newName);
					}
				} catch (IOException e) {
					System.out.format("Error : %s%n", e.getMessage());
				}
			}
			else if(classified == 1){
				System.out.format("정리안됨 : %s -> move to %s%n", file.getAbsoluteFile(), unclassifiedPath);
				try {
					FileUtils.moveFileToDirectory(file, new File(unclassifiedPath), true);
				} catch (IOException e) {
					System.out.format("Error : %s%n", e.getMessage());
				}
			}
			else if(classified == 2) {
				System.out.format("인자가 많음 : %s%n", file.getAbsolutePath());
			} 
			else {
				System.out.format("이건 뭥미 : %s%n", file.getAbsolutePath());
			}
		}
	}
	
	/**
	 * 파일 이름 반환
	 * @param file
	 * @return 확장자 뺀 이름만
	 */
	public static String getFileName(File file) {
		return getName(file.getName()); 
	}
	/**
	 * 확장자를 제거한 이름 반환
	 * @param name
	 * @return
	 */
	public static String getName(String name) {
		int index = name.lastIndexOf(".");
		return index < 0 ? name : name.substring(0, index); 
	}
	/**
	 * 확장자 반환
	 * @param file
	 * @return 확장자. 없으면 공백 반환
	 */
	public static String getFileExtension(File file) {
		String filename = file.getName();
		int index = filename.lastIndexOf(".");
		return index < 0 ? "" : filename.substring(index + 1); 
	}
	public static String removeUnnecessaryCharacter(String str) {
		// 공백, [ 제거  
		str = str.trim();
		str = str.startsWith("[") ? str.substring(1) : str;
		return str.trim();
	}

	/**
	 * 배열을 컴마(,)로 구분한 문자열로 반환. a, b<br>
	 * ArrayUtils.toString() 이용
	 * @param array
	 * @return
	 */
	public static String arrayToString(Object array) {
		String toString = ArrayUtils.toString(array);
		return toString.substring(1, toString.length() - 1);
	}
	
	/**
	 * video list을 opus값 배열 스타일로 반환. [abs-123, avs-34]
	 * @param videoList
	 * @return
	 */
	public static String getOpusArrayStyleString(List<Video> videoList) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(int i=0, iEnd=videoList.size(); i<iEnd; i++) {
			if(i > 0)
				sb.append(",");
			sb.append("\"" + videoList.get(i).getOpus() + "\"");
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * 이름을 공백으로 나나고, 소문자,역순 정렬하여 반환. " abc ewq " -> "ewq abc"
	 * @param name
	 * @return
	 */
	public static String reverseActressName(String name) {
		if(name == null) return null;
		String[] nameArr = StringUtils.split(name.toLowerCase());
		ArrayUtils.reverse(nameArr);
		String retName = "";
		for(int i=0; i<nameArr.length; i++)
			retName += nameArr[i] + " ";
		return retName.trim();
	}

	/**
	 * 주어진 이름을 구글 이미지 검색을 사용해 URL list로 반환.<br>
	 * google url : https://ajax.googleapis.com/ajax/services/search/images<br>
	 * 에러 발생시 빈 list 리턴.
	 * @param name
	 * @return
	 */
	public static List<URL> getGoogleImage(String name) {
		List<URL> list = new ArrayList<URL>();
		try {
			name = URLEncoder.encode(name);
			URL url = new URL("https://ajax.googleapis.com/ajax/services/search/images?" +
			        "v=1.0&q=" + name + "&userip=&safe=off");
			URLConnection connection = url.openConnection();
			connection.addRequestProperty("Referer", "http://www.kamoru.com");
	
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
	
			JSONObject json = JSONObject.fromObject(builder.toString());
			JSONObject responseData = json.getJSONObject("responseData");
			JSONArray results = responseData.getJSONArray("results");
			for(int i=0, e=results.size(); i<e; i++){
				String urlStr = results.getJSONObject(i).getString("url");
				list.add(new URL(urlStr));
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return list;
	}
	
	public static void main(String[] args) {
//		VideoUtils.changeOldNameStyle("E:\\AV_JAP", "E:\\AV_JAP\\unclassified");
		System.out.println(ArrayUtils.toString(VideoUtils.getGoogleImage("Abigaile")));
	}

	/**
	 * list를 컴마(,)로 구분한 string반환
	 * @param list
	 * @return
	 */
	public static <T> String toListToSimpleString(List<T> list) {
		StringBuilder sb = new StringBuilder();
		for(int i=0, e=list.size(); i<e; i++) {
			T name = list.get(i);
			sb.append(name);
			if(i < e-1)
				sb.append(", ");
		}
		return sb.toString();
	}

	/**
	 * video list중 video파일이 있는것만 골라 opus값 배열 스타일로 반환. [abs-123, avs-34]
	 * @param videoList
	 * @return
	 */
	public static String getOpusArrayStyleStringWithVideofile(List<Video> videoList) {
		List<Video> videoListWithVideofile = new ArrayList<Video>();
		for(Video video : videoList) {
			if(video.getVideoFileList() != null && video.getVideoFileList().size() > 0) {
				videoListWithVideofile.add(video);
			}
		}
		return getOpusArrayStyleString(videoListWithVideofile);
	}
	
	/**
	 * file의 내용을 읽어 반환. null이나 ioexception시 공백 반환
	 * @param file
	 * @return
	 */
	public static String readFileToString(File file) {
		if(file == null || !file.exists()) return "";
		try {
			return FileUtils.readFileToString(file, VideoCore.FileEncoding);
		}catch(IOException ioe){
			logger.error(ioe);
			return "";
		}
	}

	/**
	 * 파일을 byte배열로 읽어 반환. null이거나 에러시 null반환
	 * @param file
	 * @return
	 */
	public static byte[] readFileToByteArray(File file) {
		if(file == null || !file.exists())
			return null;
		try {
			return FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			logger.error(e);
			return null;
		}
	}


}
