package org.vebqa.vebtal.icomp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageStore {

	public static final Logger logger = LoggerFactory.getLogger(ImageStore.class);
	
	private static final ImageStore store = new ImageStore();
	
	private ImageDriver pdfDriver = new ImageDriver();
	
	public ImageStore() {
		logger.debug("Image Store created");
	}
	
	public static ImageStore getStore() {
		return store;
	}
	
	public ImageDriver getDriver() {
		return pdfDriver;
	}
}
