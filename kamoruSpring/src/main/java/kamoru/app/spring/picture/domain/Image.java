package kamoru.app.spring.picture.domain;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

import com.mortennobel.imagescaling.ResampleOp;

import kamoru.app.spring.video.util.VideoUtils;

public class Image {

	private String name;
	private String suffix;
	private long size;
	private long lastModified;
	private File file;
	
	public Image(File file) {
		this.file = file;
		init();
	}

	private void init() {
		this.name = file.getName();
		this.suffix = VideoUtils.getFileExtension(file);
		this.size = file.length();
		this.lastModified = file.lastModified();
	}

	public String getName() {
		return name;
	}

	public String getSuffix() {
		return suffix;
	}

	public long getSize() {
		return size;
	}

	public long getLastModified() {
		return lastModified;
	}

	public File getFile() {
		return file;
	}

	public byte[] getImageBytes(PictureType type) {
		try {
			switch(type) {
			case MASTER:
				return FileUtils.readFileToByteArray(file);
			case WEB:
				return toByteArray(Scalr.resize(ImageIO.read(file), Scalr.Mode.FIT_TO_WIDTH, 500));
			case THUMBNAIL:
				return toByteArray(Scalr.pad(Scalr.resize(ImageIO.read(file), Method.SPEED, 150, Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER), 4));
			default:
				throw new RuntimeException("잘못된 타입");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private byte[] toByteArray(BufferedImage bi) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.setUseCache(false);
		try {
			ImageIO.write(bi, "gif", outputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return outputStream.toByteArray();
	}
	
}
