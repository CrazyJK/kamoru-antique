package jk.kamoru.app.image.source;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jk.kamoru.app.image.ImageException;
import jk.kamoru.app.image.domain.Image;
import jk.kamoru.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class LocalImageSource implements ImageSource {

	private List<Image> imageList = new ArrayList<Image>();

	@Value("#{prop['image.basePath']}")	private String[] backgroundImagePoolPath;

	private void listImages() {
		List<File> imageFileList = new ArrayList<File>();
		for (String path : this.backgroundImagePoolPath) {
			File dir = new File(path);
			if (dir.isDirectory()) {
				log.debug("directory scanning : {}", dir);
				Collection<File> found = FileUtils.listFiles(dir, new String[] {"jpg", "jpeg", "gif", "png" }, true);
				imageFileList.addAll(found);
			}
		}

		imageList.clear();
		for (File file : imageFileList) {
			imageList.add(new Image(file));
		}
		
		try {
			Collections.sort(imageList, new Comparator<Image>() {
				@Override
				public int compare(Image o1, Image o2) {
//					return NumberUtils.compare(o1.getLastModified(), o2.getLastModified());
					/* 아래처럼 비교할 경우 에러 발생
					   java.lang.IllegalArgumentException: Comparison method violates its general contract! 
					return (int) (o1.getLastModified() - o2.getLastModified()); */
					return o1.getLastModified() - o2.getLastModified() > 0 ? 1 : -1;
				}
			});
		}
		catch (Exception e) {
			log.warn("Error: {}", e.getMessage());
//			e.printStackTrace();
		}
		log.info("Total found image size : {}", imageList.size());
	}

	private List<Image> createImageSource() {
		if (imageList == null)
			reload();
		return imageList;
	}

	@Override
	public Image getImage(int idx) {
		try {
			return createImageSource().get(idx);
		}
		catch(IndexOutOfBoundsException  e) {
			throw new ImageException("Image not found", e);
		}
	}

	@Override
	public List<Image> getImageList() {
		return createImageSource();
	}

	@Override
	public int getImageSourceSize() {
		return createImageSource().size();
	}

	@Override
	@Scheduled(cron = "0 */7 * * * *")
	public void reload() {
		listImages();
	}

	@Override
	public void delete(int idx) {
		Image image = createImageSource().get(idx);
		image.delete();
		createImageSource().remove(idx);
		log.info("DELETE - {}", image.getName());
	}

}
