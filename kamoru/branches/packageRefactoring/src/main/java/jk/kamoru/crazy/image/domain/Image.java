package jk.kamoru.crazy.image.domain;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import jk.kamoru.crazy.image.ImageException;
import jk.kamoru.util.FileUtils;
import lombok.Cleanup;
import lombok.Data;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

@Data
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
		this.suffix = FileUtils.getExtension(file);
		this.size = file.length();
		this.lastModified = file.lastModified();
	}

	public byte[] getImageBytes(PictureType type) {
		try {
			switch (type) {
			case MASTER:
				return FileUtils.readFileToByteArray(file);
			case WEB:
				return readBufferedImageToByteArray(Scalr.resize(ImageIO.read(file), Scalr.Mode.FIT_TO_WIDTH, 500));
			case THUMBNAIL:
				return readBufferedImageToByteArray(Scalr.resize(ImageIO.read(file), Method.SPEED, 100, Scalr.OP_ANTIALIAS, Scalr.OP_BRIGHTER));
			default:
				throw new IllegalArgumentException("wrong type=" + type);
			}
		} catch (IOException e) {
			throw new ImageException(e);
		}
	}

	private byte[] readBufferedImageToByteArray(BufferedImage bi) throws IOException {
		@Cleanup
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.setUseCache(false);
		ImageIO.write(bi, "gif", outputStream);
		return outputStream.toByteArray();
	}

	public void delete() {
		FileUtils.deleteQuietly(file);
	}

}
