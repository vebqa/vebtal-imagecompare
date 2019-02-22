package org.vebqa.vebtal.icomp;

import java.io.File;

import org.junit.rules.ExternalResource;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageDriver extends ExternalResource {

	private String pathToResource;
	
	private String opencv_path;
	
	private boolean isLoaded;
	
	private Mat current;

	private static final Logger logger = LoggerFactory.getLogger(ImageDriver.class);
	
	public ImageDriver() {
		this.isLoaded = false;
		this.opencv_path = null;
	}

	public void init() {
		
		opencv_path = opencv_path + "\\" + Core.NATIVE_LIBRARY_NAME + ".dll";
		try {
			System.load(opencv_path);
		} catch (Exception e) {
			String tError = "Native code library failed to load from: " + opencv_path;
			logger.error(tError, e);
		}
		this.current = new Mat();
	}
	
	public ImageDriver loadImage(String aPathToDoc) {
		this.pathToResource = aPathToDoc;
		load();
		return this;
	}

	public void load(File anImageFile) {
		this.pathToResource = anImageFile.getAbsolutePath();
		load();
	}
	
	public void load() {
		this.current = Imgcodecs.imread(this.pathToResource, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		this.isLoaded = true;
	}
	
	public void close() {
		this.current = new Mat();
		this.isLoaded = false;
	}
	
	public boolean isLoaded() {
		return this.isLoaded;
	}
	
	/**
	 * Usage via fluent api in external resource context
	 * @param aPath
	 */
	public ImageDriver setOpenCVPath(String aPath) {
		this.opencv_path = aPath;
		init();
		return this;
	}
	
	public Mat getCurrent() {
		return this.current;
	}
}