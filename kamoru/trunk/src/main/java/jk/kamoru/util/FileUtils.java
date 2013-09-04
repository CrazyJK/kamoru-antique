package jk.kamoru.util;

import java.io.File;

import junit.framework.Assert;

/**
 * commons.io.FileUtils 상속하고 필요한 기능 추가
 * @author kamoru
 *
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
	
	
	/**
	 * 입력된 경로의 모든 파일명을 상위폴더명+일련번호로 변경
	 * @param basepath
	 */
	public static void renameToFoldername(File basepath) {
		Assert.assertTrue("It is not directory! ", basepath.isDirectory());
		File[] files = basepath.listFiles();
		int digit = String.valueOf(files.length).length();
		int count = 0;
		for(File f : files) {
			String parent = f.getParent();
			String folder = parent.substring(parent.lastIndexOf(System.getProperty("file.separator")) + 1, parent.length());
			String fullname = f.getName();
			int lastIndex = fullname.lastIndexOf(".");
			String ext = fullname.substring(lastIndex + 1);
			String newName = folder + StringUtils.addZero(++count, digit);
			
			f.renameTo(new File(parent, newName + "." + ext.toLowerCase()));
		}
	}
	
	public static String getNameExceptExtension(File file) {
		Assert.assertTrue("It is not file!", file.isFile());
		return StringUtils.substringBeforeLast(file.getName(), ".");
	}

}